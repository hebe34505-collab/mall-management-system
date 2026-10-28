package com.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.BusinessException;
import com.mall.common.ErrorCode;
import com.mall.common.PageResult;
import com.mall.domain.AccessPolicy;
import com.mall.domain.ProductStatus;
import com.mall.dto.ProductSaveRequest;
import com.mall.dto.StockAdjustRequest;
import com.mall.entity.CartItem;
import com.mall.entity.Category;
import com.mall.entity.Product;
import com.mall.entity.StockLog;
import com.mall.mapper.CartItemMapper;
import com.mall.mapper.CategoryMapper;
import com.mall.mapper.ProductMapper;
import com.mall.mapper.StockLogMapper;
import com.mall.security.LoginUser;
import com.mall.security.SecurityUtils;
import com.mall.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final CategoryMapper categoryMapper;
    private final CartItemMapper cartItemMapper;
    private final StockLogMapper stockLogMapper;
    private final OperationLogService operationLogService;

    public PageResult<ProductVO> pagePublic(String keyword, Long categoryId, String sort, Boolean recommend, long page, long size) {
        return query(keyword, categoryId, sort, ProductStatus.ON_SHELF.name(), recommend, page, size);
    }

    public PageResult<ProductVO> pageAdmin(String keyword, Long categoryId, String sort, String status, long page, long size) {
        assertAdmin();
        return query(keyword, categoryId, sort, status, null, page, size);
    }

    public ProductVO detailPublic(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || !ProductStatus.ON_SHELF.name().equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        return toVo(product);
    }

    public ProductVO detailAdmin(Long id) {
        assertAdmin();
        Product product = requireProduct(id);
        return toVo(product);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProductVO create(ProductSaveRequest request) {
        LoginUser admin = assertAdmin();
        Category category = requireEnabledCategory(request.getCategoryId());
        ProductStatus status = requireStatus(request.getStatus());
        Product product = new Product();
        fill(product, request, status);
        product.setSales(0);
        productMapper.insert(product);
        writeStockLog(product.getId(), product.getStock(), product.getStock(), "ADMIN_ADJUST", admin.getId());
        operationLogService.record(admin.getId(), admin.getUsername(), "PRODUCT", "CREATE", "新增商品 " + product.getName());
        return ProductVO.from(product, category.getName());
    }

    @Transactional(rollbackFor = Exception.class)
    public ProductVO update(Long id, ProductSaveRequest request) {
        LoginUser admin = assertAdmin();
        Product product = requireProduct(id);
        Category category = requireEnabledCategory(request.getCategoryId());
        ProductStatus status = requireStatus(request.getStatus());
        int oldStock = product.getStock();
        fill(product, request, status);
        productMapper.updateById(product);
        if (oldStock != product.getStock()) {
            writeStockLog(product.getId(), product.getStock() - oldStock, product.getStock(), "ADMIN_ADJUST", admin.getId());
        }
        operationLogService.record(admin.getId(), admin.getUsername(), "PRODUCT", "UPDATE", "编辑商品 " + product.getName());
        return ProductVO.from(product, category.getName());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        LoginUser admin = assertAdmin();
        Product product = requireProduct(id);
        cartItemMapper.delete(new LambdaQueryWrapper<CartItem>().eq(CartItem::getProductId, id));
        productMapper.deleteById(id);
        operationLogService.record(admin.getId(), admin.getUsername(), "PRODUCT", "DELETE", "删除商品 " + product.getName());
    }

    @Transactional(rollbackFor = Exception.class)
    public ProductVO changeStatus(Long id, String statusValue) {
        LoginUser admin = assertAdmin();
        Product product = requireProduct(id);
        ProductStatus status = requireStatus(statusValue);
        product.setStatus(status.name());
        productMapper.updateById(product);
        operationLogService.record(admin.getId(), admin.getUsername(), "PRODUCT", "STATUS",
                product.getName() + " 调整为" + status.label());
        return toVo(product);
    }

    @Transactional(rollbackFor = Exception.class)
    public ProductVO adjustStock(Long id, StockAdjustRequest request) {
        LoginUser admin = assertAdmin();
        Product product = requireProduct(id);
        int oldStock = product.getStock();
        product.setStock(request.getStock());
        productMapper.updateById(product);
        writeStockLog(product.getId(), request.getStock() - oldStock, request.getStock(), "ADMIN_ADJUST", admin.getId());
        operationLogService.record(admin.getId(), admin.getUsername(), "STOCK", "ADJUST",
                product.getName() + " 库存 " + oldStock + " -> " + request.getStock());
        return toVo(product);
    }

    public List<ProductVO> hot(int limit) {
        int size = Math.min(Math.max(limit, 1), 20);
        List<Product> products = productMapper.selectList(new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, ProductStatus.ON_SHELF.name())
                .orderByDesc(Product::getSales)
                .last("LIMIT " + size));
        return toVoList(products);
    }

    private PageResult<ProductVO> query(String keyword, Long categoryId, String sort, String status, Boolean recommend, long page, long size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(status)) {
            if (ProductStatus.parse(status) == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "商品状态不正确");
            }
            wrapper.eq(Product::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }
        if (Boolean.TRUE.equals(recommend)) {
            wrapper.eq(Product::getIsRecommend, 1);
        }
        if (StringUtils.hasText(keyword)) {
            String text = keyword.trim();
            wrapper.and(item -> item.like(Product::getName, text).or().like(Product::getSubtitle, text));
        }
        applySort(wrapper, sort);
        Page<Product> result = productMapper.selectPage(new Page<>(normalizePage(page), normalizeSize(size, 12)), wrapper);
        return new PageResult<>(toVoList(result.getRecords()), result.getTotal(), result.getCurrent(), result.getSize());
    }

    private void applySort(LambdaQueryWrapper<Product> wrapper, String sort) {
        switch (sort == null ? "" : sort) {
            case "price_asc" -> wrapper.orderByAsc(Product::getPrice).orderByDesc(Product::getId);
            case "price_desc" -> wrapper.orderByDesc(Product::getPrice).orderByDesc(Product::getId);
            case "sales_desc" -> wrapper.orderByDesc(Product::getSales).orderByDesc(Product::getId);
            default -> wrapper.orderByDesc(Product::getCreatedAt).orderByDesc(Product::getId);
        }
    }

    private void fill(Product product, ProductSaveRequest request, ProductStatus status) {
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName().trim());
        product.setSubtitle(blank(request.getSubtitle()));
        product.setDescription(blank(request.getDescription()));
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCoverImage(blank(request.getCoverImage()));
        product.setStatus(status.name());
        product.setIsRecommend(Boolean.TRUE.equals(request.getRecommend()) ? 1 : 0);
    }

    private List<ProductVO> toVoList(List<Product> products) {
        Map<Long, String> names = categoryNames(products.stream().map(Product::getCategoryId).distinct().toList());
        return products.stream().map(product -> ProductVO.from(product, names.get(product.getCategoryId()))).toList();
    }

    private ProductVO toVo(Product product) {
        Category category = categoryMapper.selectById(product.getCategoryId());
        return ProductVO.from(product, category == null ? null : category.getName());
    }

    private Map<Long, String> categoryNames(List<Long> ids) {
        Map<Long, String> names = new HashMap<>();
        if (ids.isEmpty()) {
            return names;
        }
        categoryMapper.selectBatchIds(ids).forEach(category -> names.put(category.getId(), category.getName()));
        return names;
    }

    private Product requireProduct(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        return product;
    }

    private Category requireEnabledCategory(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || category.getStatus() == null || category.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "商品分类不存在或已停用");
        }
        return category;
    }

    private ProductStatus requireStatus(String status) {
        ProductStatus parsed = ProductStatus.parse(status);
        if (parsed == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "商品状态只能是 ON_SHELF 或 OFF_SHELF");
        }
        return parsed;
    }

    private void writeStockLog(Long productId, int change, int after, String type, Long operatorId) {
        StockLog log = new StockLog();
        log.setProductId(productId);
        log.setChangeQty(change);
        log.setStockAfter(after);
        log.setBizType(type);
        log.setOperatorId(operatorId);
        log.setCreatedAt(LocalDateTime.now());
        stockLogMapper.insert(log);
    }

    private LoginUser assertAdmin() {
        LoginUser user = SecurityUtils.current();
        if (!AccessPolicy.isAdmin(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有权限访问该资源");
        }
        return user;
    }

    private long normalizePage(long page) {
        return page < 1 ? 1 : page;
    }

    private long normalizeSize(long size, long defaultSize) {
        if (size < 1) {
            return defaultSize;
        }
        return Math.min(size, 100);
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
