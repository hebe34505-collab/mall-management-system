package com.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OverviewVO {
    private long userCount;
    private long productCount;
    private long effectiveOrderCount;
    private BigDecimal salesAmount;
    private String salesRule;
    private List<OrderVO> recentOrders;
    private List<ProductVO> hotProducts;
}
