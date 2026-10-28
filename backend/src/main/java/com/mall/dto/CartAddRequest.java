package com.mall.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartAddRequest {
    @NotNull(message = "请选择商品")
    private Long productId;

    @NotNull(message = "请输入数量")
    @Min(value = 1, message = "数量至少为 1")
    @Max(value = 99, message = "数量不能超过 99")
    private Integer quantity;
}
