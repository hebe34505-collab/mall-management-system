package com.mall.vo;

import com.mall.domain.ProductStatus;
import com.mall.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String subtitle;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Integer sales;
    private String coverImage;
    private String status;
    private String statusLabel;
    private Boolean recommend;
    private Boolean purchasable;
    private LocalDateTime createdAt;

    public static ProductVO from(Product product, String categoryName) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setCategoryId(product.getCategoryId());
        vo.setCategoryName(categoryName);
        vo.setName(product.getName());
        vo.setSubtitle(product.getSubtitle());
        vo.setDescription(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        vo.setSales(product.getSales());
        vo.setCoverImage(product.getCoverImage());
        vo.setStatus(product.getStatus());
        ProductStatus status = ProductStatus.parse(product.getStatus());
        vo.setStatusLabel(status == null ? product.getStatus() : status.label());
        vo.setRecommend(product.getIsRecommend() != null && product.getIsRecommend() == 1);
        vo.setPurchasable(ProductStatus.ON_SHELF.name().equals(product.getStatus()) && product.getStock() != null && product.getStock() > 0);
        vo.setCreatedAt(product.getCreatedAt());
        return vo;
    }
}
