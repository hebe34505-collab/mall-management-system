package com.mall.domain;

/**
 * 数据归属校验。普通用户不能读取他人订单，任何接口都不能修改角色。
 */
public final class AccessPolicy {

    private AccessPolicy() {
    }

    public static boolean isAdmin(String role) {
        return "ADMIN".equals(role);
    }

    public static boolean canReadOrder(Long currentUserId, String role, Long ownerUserId) {
        if (currentUserId == null || ownerUserId == null) {
            return false;
        }
        if (isAdmin(role)) {
            return true;
        }
        return currentUserId.equals(ownerUserId);
    }

    /**
     * 系统不提供修改角色的入口。即使请求里带了角色字段，也必须保持原角色。
     */
    public static String retainRole(String currentRole) {
        return currentRole;
    }

    public static boolean countsAsSales(OrderStatus status) {
        return status == OrderStatus.PENDING_SHIPMENT
                || status == OrderStatus.SHIPPED
                || status == OrderStatus.COMPLETED;
    }
}
