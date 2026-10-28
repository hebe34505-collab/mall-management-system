package com.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.BusinessException;
import com.mall.common.ErrorCode;
import com.mall.common.PageResult;
import com.mall.domain.AccessPolicy;
import com.mall.domain.OrderAmountCalculator;
import com.mall.domain.OrderStatus;
import com.mall.domain.OrderStatusPolicy;
import com.mall.domain.StockPolicy;
import com.mall.dto.CreateOrderRequest;
import com.mall.dto.OrderLineRequest;
import com.mall.entity.Address;
import com.mall.entity.CartItem;
import com.mall.entity.Order;
import com.mall.entity.OrderItem;
import com.mall.entity.Product;
import com.mall.entity.StockLog;
import com.mall.entity.User;
import com.mall.mapper.AddressMapper;
import com.mall.mapper.CartItemMapper;
import com.mall.mapper.OrderItemMapper;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.ProductMapper;
import com.mall.mapper.StockLogMapper;
import com.mall.mapper.UserMapper;
import com.mall.security.LoginUser;
import com.mall.security.SecurityUtils;
import com.mall.vo.OrderItemVO;
import com.mall.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final DateTimeFormatter ORDER_NO_TIME = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final AddressMapper addressMapper;
    private final CartItemMapper cartItemMapper;
    private final StockLogMapper stockLogMapper;
    private final UserMapper userMapper;
    private final OperationLogService operationLogService;

    @Transactional(rollbackFor = Exception.class)
    public OrderVO create(CreateOrderRequest request) {
        LoginUser loginUser = SecurityUtils.current();
        String idempotencyKey = blank(request.getIdempotencyKey());
        if (idempotencyKey != null) {
            Order existing = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                    .eq(Order::getUserId, loginUser.getId())
                    .eq(Order::getIdempotencyKey, idempotencyKey)
                    .last("LIMIT 1"));
            if (existing != null) {
                return toVo(existing, true);
            }
        }
        Address address = requireOwnAddress(loginUser.getId(), request.getAddressId());
        List<Line> lines = resolveLines(loginUser.getId(), request);
        lines.sort(Comparator.comparing(Line::productId));

        List<PricedLine> priced = new ArrayList<>();
        for (Line line : lines) {
            Product product = productMapper.selectForUpdate(line.productId());
            if (product == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
            }
            StockPolicy.assertPurchasable(product.getStatus(), product.getStock(), line.quantity());
            int updated = productMapper.deductStock(product.getId(), line.quantity());
            if (updated != 1) {
                throw new BusinessException(ErrorCode.CONFLICT, "商品「" + product.getName() + "」库存不足或已下架");
            }
            BigDecimal subtotal = OrderAmountCalculator.lineAmount(product.getPrice(), line.quantity());
            priced.add(new PricedLine(product, line.quantity(), subtotal, product.getStock() - line.quantity()));
        }
        BigDecimal total = OrderAmountCalculator.total(priced.stream().map(PricedLine::subtotal).toList());

        Order order = new Order();
        order.setOrderNo(nextOrderNo());
        order.setUserId(loginUser.getId());
        order.setAddressId(address.getId());
        order.setReceiver(address.getReceiver());
        order.setPhone(address.getPhone());
        order.setFullAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PENDING_SHIPMENT.name());
        order.setRemark(blank(request.getRemark()));
        order.setIdempotencyKey(idempotencyKey);
        orderMapper.insert(order);

        for (PricedLine line : priced) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(line.product().getId());
            item.setProductName(line.product().getName());
            item.setProductImage(line.product().getCoverImage());
            item.setUnitPrice(line.product().getPrice());
            item.setQuantity(line.quantity());
            item.setSubtotal(line.subtotal());
            orderItemMapper.insert(item);
            writeStockLog(line.product().getId(), -line.quantity(), line.stockAfter(), "ORDER_DEDUCT", order.getId(), loginUser.getId());
        }
        if (request.getCartItemIds() != null && !request.getCartItemIds().isEmpty()) {
            cartItemMapper.delete(new LambdaQueryWrapper<CartItem>()
                    .eq(CartItem::getUserId, loginUser.getId())
                    .in(CartItem::getId, request.getCartItemIds()));
        }
        operationLogService.record(loginUser.getId(), loginUser.getUsername(), "ORDER", "CREATE", "创建订单 " + order.getOrderNo());
        return toVo(order, true);
    }

    public PageResult<OrderVO> myOrders(String status, long page, long size) {
        LoginUser loginUser = SecurityUtils.current();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, loginUser.getId())
                .orderByDesc(Order::getCreatedAt)
                .orderByDesc(Order::getId);
        if (StringUtils.hasText(status)) {
            requireStatus(status);
            wrapper.eq(Order::getStatus, status);
        }
        Page<Order> result = orderMapper.selectPage(new Page<>(page < 1 ? 1 : page, normalizeSize(size)), wrapper);
        return new PageResult<>(result.getRecords().stream().map(order -> toVo(order, false)).toList(),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    public OrderVO myDetail(Long id) {
        return requireReadable(id, false);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderVO cancel(Long id) {
        LoginUser loginUser = SecurityUtils.current();
        Order order = orderMapper.selectById(id);
        if (order == null || !order.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        OrderStatus current = requireStatus(order.getStatus());
        if (!OrderStatusPolicy.canUserCancel(current)) {
            throw new BusinessException(ErrorCode.BUSINESS, "只有待发货订单可以取消");
        }
        int rows = orderMapper.compareAndSetStatus(order.getId(), OrderStatus.PENDING_SHIPMENT.name(), OrderStatus.CANCELLED.name());
        if (rows != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态已变化，请刷新后重试");
        }
        restoreStock(order.getId(), loginUser.getId());
        operationLogService.record(loginUser.getId(), loginUser.getUsername(), "ORDER", "CANCEL", "取消订单 " + order.getOrderNo());
        return myDetail(id);
    }

    public PageResult<OrderVO> adminPage(String status, String orderNo, String username, long page, long size) {
        assertAdmin();
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreatedAt).orderByDesc(Order::getId);
        if (StringUtils.hasText(status)) {
            requireStatus(status);
            wrapper.eq(Order::getStatus, status);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(Order::getOrderNo, orderNo.trim());
        }
        if (StringUtils.hasText(username)) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getUsername, username.trim()));
            if (users.isEmpty()) {
                return new PageResult<>(List.of(), 0, page < 1 ? 1 : page, normalizeSize(size));
            }
            wrapper.in(Order::getUserId, users.stream().map(User::getId).toList());
        }
        Page<Order> result = orderMapper.selectPage(new Page<>(page < 1 ? 1 : page, normalizeSize(size)), wrapper);
        return new PageResult<>(result.getRecords().stream().map(order -> toVo(order, false)).toList(),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    public OrderVO adminDetail(Long id) {
        assertAdmin();
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return toVo(order, true);
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderVO adminUpdateStatus(Long id, String targetValue) {
        LoginUser admin = assertAdmin();
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        OrderStatus current = requireStatus(order.getStatus());
        OrderStatus target = requireStatus(targetValue);
        if (!OrderStatusPolicy.canAdminTransit(current, target)) {
            throw new BusinessException(ErrorCode.BUSINESS, "不允许从「" + current.label() + "」变更为「" + target.label() + "」");
        }
        int rows = orderMapper.compareAndSetStatus(order.getId(), current.name(), target.name());
        if (rows != 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "订单状态已变化，请刷新后重试");
        }
        if (target == OrderStatus.CANCELLED) {
            restoreStock(order.getId(), admin.getId());
        }
        operationLogService.record(admin.getId(), admin.getUsername(), "ORDER", "STATUS",
                "订单 " + order.getOrderNo() + " 更新为" + target.label());
        return adminDetail(id);
    }

    public List<OrderVO> recent(int limit) {
        assertAdmin();
        int size = Math.min(Math.max(limit, 1), 20);
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreatedAt)
                .last("LIMIT " + size));
        return orders.stream().map(order -> toVo(order, false)).toList();
    }

    private void restoreStock(Long orderId, Long operatorId) {
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        items.stream().filter(item -> item.getProductId() != null).sorted(Comparator.comparing(OrderItem::getProductId)).forEach(item -> {
            Product product = productMapper.selectForUpdate(item.getProductId());
            if (product == null) {
                return;
            }
            productMapper.restoreStock(product.getId(), item.getQuantity());
            writeStockLog(product.getId(), item.getQuantity(), product.getStock() + item.getQuantity(), "ORDER_ROLLBACK", orderId, operatorId);
        });
    }

    private List<Line> resolveLines(Long userId, CreateOrderRequest request) {
        boolean fromCart = request.getCartItemIds() != null && !request.getCartItemIds().isEmpty();
        boolean fromItems = request.getItems() != null && !request.getItems().isEmpty();
        if (fromCart == fromItems) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择购物车商品或直接购买的商品");
        }
        Map<Long, Integer> merged = new LinkedHashMap<>();
        if (fromCart) {
            List<Long> ids = request.getCartItemIds().stream().distinct().toList();
            List<CartItem> cartItems = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                    .eq(CartItem::getUserId, userId)
                    .in(CartItem::getId, ids));
            if (cartItems.size() != ids.size()) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "购物车中有商品不存在");
            }
            for (CartItem cartItem : cartItems) {
                merged.merge(cartItem.getProductId(), cartItem.getQuantity(), Integer::sum);
            }
        } else {
            for (OrderLineRequest line : request.getItems()) {
                merged.merge(line.getProductId(), line.getQuantity(), Integer::sum);
            }
        }
        return merged.entrySet().stream().map(entry -> new Line(entry.getKey(), entry.getValue())).toList();
    }

    private OrderVO requireReadable(Long id, boolean withItems) {
        Order order = orderMapper.selectById(id);
        LoginUser loginUser = SecurityUtils.current();
        if (order == null || !AccessPolicy.canReadOrder(loginUser.getId(), loginUser.getRole(), order.getUserId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return toVo(order, withItems || true);
    }

    private OrderVO toVo(Order order, boolean withItems) {
        User user = userMapper.selectById(order.getUserId());
        List<OrderItemVO> items = List.of();
        if (withItems) {
            items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()))
                    .stream().map(OrderItemVO::from).toList();
        }
        return OrderVO.from(order, user == null ? null : user.getUsername(), items);
    }

    private Address requireOwnAddress(Long userId, Long addressId) {
        Address address = addressMapper.selectById(addressId);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "收货地址不存在");
        }
        return address;
    }

    private String nextOrderNo() {
        for (int i = 0; i < 5; i++) {
            String orderNo = "M" + LocalDateTime.now().format(ORDER_NO_TIME) + ThreadLocalRandom.current().nextInt(1000, 10000);
            Long count = orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
            if (count == null || count == 0) {
                return orderNo;
            }
        }
        throw new BusinessException(ErrorCode.CONFLICT, "订单号生成失败，请重试");
    }

    private void writeStockLog(Long productId, int change, int after, String type, Long refId, Long operatorId) {
        StockLog log = new StockLog();
        log.setProductId(productId);
        log.setChangeQty(change);
        log.setStockAfter(after);
        log.setBizType(type);
        log.setRefId(refId);
        log.setOperatorId(operatorId);
        log.setCreatedAt(LocalDateTime.now());
        stockLogMapper.insert(log);
    }

    private OrderStatus requireStatus(String value) {
        OrderStatus status = OrderStatus.parse(value);
        if (status == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "订单状态不正确");
        }
        return status;
    }

    private LoginUser assertAdmin() {
        LoginUser user = SecurityUtils.current();
        if (!AccessPolicy.isAdmin(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有权限访问该资源");
        }
        return user;
    }

    private long normalizeSize(long size) {
        if (size < 1) {
            return 10;
        }
        return Math.min(size, 100);
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private record Line(Long productId, int quantity) {
    }

    private record PricedLine(Product product, int quantity, BigDecimal subtotal, int stockAfter) {
    }
}
