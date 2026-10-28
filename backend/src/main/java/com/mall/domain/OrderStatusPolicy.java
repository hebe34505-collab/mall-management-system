package com.mall.domain;

/**
 * 订单状态机。用户只能取消待发货订单；管理员不能把已完成或已取消订单改回其他状态。
 */
public final class OrderStatusPolicy {

    private OrderStatusPolicy() {
    }

    public static boolean canUserCancel(OrderStatus status) {
        return status == OrderStatus.PENDING_SHIPMENT;
    }

    public static boolean canAdminTransit(OrderStatus from, OrderStatus to) {
        if (from == null || to == null || from == to) {
            return false;
        }
        return switch (from) {
            case PENDING_SHIPMENT -> to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED -> to == OrderStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
