package com.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.BusinessException;
import com.mall.common.ErrorCode;
import com.mall.domain.StockPolicy;
import com.mall.dto.CartAddRequest;
import com.mall.dto.CartUpdateRequest;
import com.mall.entity.CartItem;
import com.mall.entity.Product;
import com.mall.mapper.CartItemMapper;
import com.mall.mapper.ProductMapper;
import com.mall.security.SecurityUtils;
import com.mall.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final ProductMapper productMapper;

    public List<CartItemVO> list() {
        Long userId = SecurityUtils.current().getId();
        List<CartItem> items = cartItemMapper.selectList(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .orderByDesc(CartItem::getId));
        return toVo(items);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<CartItemVO> add(CartAddRequest request) {
        Long userId = SecurityUtils.current().getId();
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        CartItem existing = cartItemMapper.selectOne(new LambdaQueryWrapper<CartItem>()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, product.getId())
                .last("LIMIT 1"));
        int nextQuantity = request.getQuantity() + (existing == null ? 0 : existing.getQuantity());
        StockPolicy.assertPurchasable(product.getStatus(), product.getStock(), nextQuantity);
        if (existing == null) {
            CartItem item = new CartItem();
            item.setUserId(userId);
            item.setProductId(product.getId());
            item.setQuantity(request.getQuantity());
            cartItemMapper.insert(item);
        } else {
            existing.setQuantity(nextQuantity);
            cartItemMapper.updateById(existing);
        }
        return list();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<CartItemVO> update(Long id, CartUpdateRequest request) {
        CartItem item = requireOwn(id);
        Product product = productMapper.selectById(item.getProductId());
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        StockPolicy.assertPurchasable(product.getStatus(), product.getStock(), request.getQuantity());
        item.setQuantity(request.getQuantity());
        cartItemMapper.updateById(item);
        return list();
    }

    @Transactional(rollbackFor = Exception.class)
    public List<CartItemVO> delete(Long id) {
        CartItem item = requireOwn(id);
        cartItemMapper.deleteById(item.getId());
        return list();
    }

    @Transactional(rollbackFor = Exception.class)
    public void clear() {
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, SecurityUtils.current().getId()));
    }

    private CartItem requireOwn(Long id) {
        CartItem item = cartItemMapper.selectById(id);
        if (item == null || !item.getUserId().equals(SecurityUtils.current().getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "购物车商品不存在");
        }
        return item;
    }

    private List<CartItemVO> toVo(List<CartItem> items) {
        if (items.isEmpty()) {
            return List.of();
        }
        List<Long> productIds = items.stream().map(CartItem::getProductId).distinct().toList();
        Map<Long, Product> products = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        return items.stream().map(item -> CartItemVO.from(item, products.get(item.getProductId()))).toList();
    }
}
