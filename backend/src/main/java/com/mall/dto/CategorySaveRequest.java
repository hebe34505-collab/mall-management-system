package com.mall.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategorySaveRequest {
    @NotBlank(message = "请输入分类名称")
    @Size(max = 40, message = "分类名称不能超过 40 个字符")
    private String name;

    @Min(value = 0, message = "排序不能为负数")
    @Max(value = 9999, message = "排序值过大")
    private Integer sortOrder;

    private Integer status;
}
