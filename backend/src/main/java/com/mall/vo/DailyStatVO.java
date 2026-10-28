package com.mall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DailyStatVO {
    private String statDate;
    private long orderCount;
    private BigDecimal salesAmount;
}
