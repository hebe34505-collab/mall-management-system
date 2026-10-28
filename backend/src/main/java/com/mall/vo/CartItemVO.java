package com.mall.vo;

import com.mall.domain.OrderAmountCalculator;
import com.mall.entity.CartItem;
import com.mall.entity.Product;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {
    private Long id;
    private Long productId;
    private String productName;
    private String coverImage;
    private BigDecimal price;
    private Integer stock;
    private String status;
    private Integer quantity;
    private BigDecimal subtotal;
    private Boolean purchasable;

    public static CartItemVO from(CartItem item, Product product) {
        CartItemVO vo = new CartItemVO();
        vo.setId(item.getId());
        vo.setProductId(item.getProductId());
        vo.setQuantity(item.getQuantity());
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setCoverImage(product.getCoverImage());
            vo.setPrice(product.getPrice());
            vo.setStock(product.getStock());
            vo.setStatus(product.getStatus());
            boolean purchasable = "ON_SHELF".equals(product.getStatus()) && product.getStock() >= item.getQuantity();
            vo.setPurchasable(purchasable);
            vo.setSubtotal(OrderAmountCalculator.lineAmount(product.getPrice(), item.getQuantity()));
        } else {
            vo.setPurchasable(false);
            vo.setSubtotal(BigDecimal.ZERO.setScale(2));
        }
        return vo;
    }
}
