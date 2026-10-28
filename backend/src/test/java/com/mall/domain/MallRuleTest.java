package com.mall.domain;

import com.mall.common.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MallRuleTest {

    @Test
    void orderTotalUsesDatabasePriceInsteadOfClientTotal() {
        BigDecimal first = OrderAmountCalculator.lineAmount(new BigDecimal("49.00"), 2);
        BigDecimal second = OrderAmountCalculator.lineAmount(new BigDecimal("42.00"), 1);
        assertEquals(new BigDecimal("140.00"), OrderAmountCalculator.total(List.of(first, second)));
    }

    @Test
    void insufficientStockAndOffShelfAreRejected() {
        assertThrows(BusinessException.class, () -> StockPolicy.assertPurchasable("ON_SHELF", 1, 2));
        assertThrows(BusinessException.class, () -> StockPolicy.assertPurchasable("OFF_SHELF", 10, 1));
        StockPolicy.assertPurchasable("ON_SHELF", 2, 2);
    }

    @Test
    void userCanOnlyCancelPendingOrderAndAdminCannotReopenClosedOrder() {
        assertTrue(OrderStatusPolicy.canUserCancel(OrderStatus.PENDING_SHIPMENT));
        assertFalse(OrderStatusPolicy.canUserCancel(OrderStatus.SHIPPED));
        assertTrue(OrderStatusPolicy.canAdminTransit(OrderStatus.PENDING_SHIPMENT, OrderStatus.SHIPPED));
        assertTrue(OrderStatusPolicy.canAdminTransit(OrderStatus.PENDING_SHIPMENT, OrderStatus.CANCELLED));
        assertTrue(OrderStatusPolicy.canAdminTransit(OrderStatus.SHIPPED, OrderStatus.COMPLETED));
        assertFalse(OrderStatusPolicy.canAdminTransit(OrderStatus.COMPLETED, OrderStatus.CANCELLED));
        assertFalse(OrderStatusPolicy.canAdminTransit(OrderStatus.CANCELLED, OrderStatus.PENDING_SHIPMENT));
        assertFalse(OrderStatusPolicy.canAdminTransit(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
    }

    @Test
    void userCannotReadAnotherUsersOrderAndRoleCannotBeEscalated() {
        assertFalse(AccessPolicy.canReadOrder(2L, "USER", 3L));
        assertTrue(AccessPolicy.canReadOrder(2L, "USER", 2L));
        assertTrue(AccessPolicy.canReadOrder(1L, "ADMIN", 3L));
        assertFalse(AccessPolicy.canReadOrder(null, "ADMIN", 3L));
        assertEquals("USER", AccessPolicy.retainRole("USER"));
        assertFalse(AccessPolicy.countsAsSales(OrderStatus.CANCELLED));
        assertTrue(AccessPolicy.countsAsSales(OrderStatus.PENDING_SHIPMENT));
        assertTrue(AccessPolicy.countsAsSales(OrderStatus.COMPLETED));
    }

    @Test
    void developmentSeedPasswordsMatchBcryptHashes() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertTrue(encoder.matches("Admin@123456", "$2b$10$oQIbKYYbTkHTOVOtgEcW6eBeHzY7GmiCYoRVXNMZ1V/uhvKAq3giq"));
        assertTrue(encoder.matches("User@123456", "$2b$10$K2BAELqrYi4Jnzzp3OPSCuRZlwHqjzcSbgEENnFWz7WZbPKwr4gmy"));
        assertFalse(encoder.matches("Admin@123456", "$2b$10$K2BAELqrYi4Jnzzp3OPSCuRZlwHqjzcSbgEENnFWz7WZbPKwr4gmy"));
    }
}
