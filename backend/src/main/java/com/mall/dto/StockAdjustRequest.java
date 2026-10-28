package com.mall.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustRequest {
    @NotNull(message = "请输入库存")
    @Min(value = 0, message = "库存不能为负数")
    @Max(value = 1000000, message = "库存过大")
    private Integer stock;
}
