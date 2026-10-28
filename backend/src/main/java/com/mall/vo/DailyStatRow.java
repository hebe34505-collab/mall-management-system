package com.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * MyBatis 映射用的趋势行。日期使用字符串，避免驱动把 DATE 转成非预期类型。
 */
@Data
public class DailyStatRow {
    private String statDate;
    private Long orderCount;
    private BigDecimal salesAmount;
}
