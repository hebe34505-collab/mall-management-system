package com.mall.domain;

/**
 * 订单状态。
 * 销售额只统计待发货、已发货、已完成，已取消不计入。
 */
public enum OrderStatus {
    PENDING_SHIPMENT,
    SHIPPED,
    COMPLETED,
    CANCELLED;

    public String label() {
        return switch (this) {
            case PENDING_SHIPMENT -> "待发货";
            case SHIPPED -> "已发货";
            case COMPLETED -> "已完成";
            case CANCELLED -> "已取消";
        };
    }

    public static OrderStatus parse(String value) {
        try {
            return OrderStatus.valueOf(value);
        } catch (Exception exception) {
            return null;
        }
    }
}
