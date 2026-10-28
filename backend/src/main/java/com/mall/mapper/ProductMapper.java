package com.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.entity.Product;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT * FROM products WHERE id = #{id} FOR UPDATE")
    Product selectForUpdate(@Param("id") Long id);

    /**
     * 条件更新保证并发下不会把库存扣成负数，也保证下架商品不能被买走。
     */
    @Update("""
            UPDATE products
               SET stock = stock - #{quantity},
                   sales = sales + #{quantity},
                   updated_at = NOW()
             WHERE id = #{id}
               AND status = 'ON_SHELF'
               AND stock >= #{quantity}
            """)
    int deductStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Update("""
            UPDATE products
               SET stock = stock + #{quantity},
                   sales = GREATEST(sales - #{quantity}, 0),
                   updated_at = NOW()
             WHERE id = #{id}
            """)
    int restoreStock(@Param("id") Long id, @Param("quantity") int quantity);
}
