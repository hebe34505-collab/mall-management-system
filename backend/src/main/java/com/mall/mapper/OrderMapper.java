package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface OrderMapper extends BaseMapper<Order> {

    @Update("""
            UPDATE orders
               SET status = #{toStatus},
                   updated_at = NOW()
             WHERE id = #{id}
               AND status = #{fromStatus}
            """)
    int compareAndSetStatus(@Param("id") Long id, @Param("fromStatus") String fromStatus, @Param("toStatus") String toStatus);
}
