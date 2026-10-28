package com.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class UserSummaryVO {
    private long orderCount;
    private long pendingCount;
    private long shippedCount;
    private long completedCount;
    private long cancelledCount;
    private BigDecimal totalSpent;
    private String salesRule;
    private List<OrderVO> recentOrders;
}
