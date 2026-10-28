package com.mall.mapper;

import com.mall.vo.DailyStatRow;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 统计查询。有效销售额只包含待发货、已发货、已完成，与 AccessPolicy.countsAsSales 保持一致。
 */
public interface StatsMapper {

    @Select("SELECT COUNT(*) FROM users")
    long countUsers();

    @Select("SELECT COUNT(*) FROM products")
    long countProducts();

    @Select("""
            SELECT COUNT(*) FROM orders
             WHERE status IN ('PENDING_SHIPMENT', 'SHIPPED', 'COMPLETED')
            """)
    long countEffectiveOrders();

    @Select("""
            SELECT COALESCE(SUM(total_amount), 0) FROM orders
             WHERE status IN ('PENDING_SHIPMENT', 'SHIPPED', 'COMPLETED')
            """)
    BigDecimal sumEffectiveSales();

    @Select("""
            SELECT DATE_FORMAT(created_at, '%Y-%m-%d') AS statDate,
                   COUNT(*) AS orderCount,
                   COALESCE(SUM(total_amount), 0) AS salesAmount
              FROM orders
             WHERE status IN ('PENDING_SHIPMENT', 'SHIPPED', 'COMPLETED')
               AND created_at >= #{start}
             GROUP BY DATE_FORMAT(created_at, '%Y-%m-%d')
             ORDER BY statDate
            """)
    List<DailyStatRow> trend(@Param("start") LocalDateTime start);

    @Select("""
            SELECT COALESCE(SUM(total_amount), 0) FROM orders
             WHERE user_id = #{userId}
               AND status IN ('PENDING_SHIPMENT', 'SHIPPED', 'COMPLETED')
            """)
    BigDecimal sumUserSpent(@Param("userId") Long userId);
}
