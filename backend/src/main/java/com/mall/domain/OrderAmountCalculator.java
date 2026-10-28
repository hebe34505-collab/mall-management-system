package com.mall.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 订单金额只由数据库中的单价和数量计算，不接收前端传入的总价。
 */
public final class OrderAmountCalculator {

    private OrderAmountCalculator() {
    }

    public static BigDecimal lineAmount(BigDecimal unitPrice, int quantity) {
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("商品价格无效");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("购买数量无效");
        }
        return unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal total(List<BigDecimal> lineAmounts) {
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal lineAmount : lineAmounts) {
            sum = sum.add(lineAmount);
        }
        return sum.setScale(2, RoundingMode.HALF_UP);
    }
}
