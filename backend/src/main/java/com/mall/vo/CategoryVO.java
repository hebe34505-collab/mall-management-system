package com.mall.vo;

import com.mall.entity.Category;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private Integer sortOrder;
    private Integer status;
    private Long productCount;
    private LocalDateTime createdAt;

    public static CategoryVO from(Category category, long productCount) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSortOrder(category.getSortOrder());
        vo.setStatus(category.getStatus());
        vo.setProductCount(productCount);
        vo.setCreatedAt(category.getCreatedAt());
        return vo;
    }
}
