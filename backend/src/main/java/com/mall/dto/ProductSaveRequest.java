package com.mall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSaveRequest {
    @NotNull(message = "请选择分类")
    private Long categoryId;

    @NotBlank(message = "请输入商品名称")
    @Size(max = 80, message = "商品名称不能超过 80 个字符")
    private String name;

    @Size(max = 120, message = "副标题不能超过 120 个字符")
    private String subtitle;

    @Size(max = 2000, message = "描述不能超过 2000 个字符")
    private String description;

    @NotNull(message = "请输入价格")
    @DecimalMin(value = "0.01", message = "价格必须大于 0")
    @Digits(integer = 8, fraction = 2, message = "价格最多保留两位小数")
    private BigDecimal price;

    @NotNull(message = "请输入库存")
    @Min(value = 0, message = "库存不能为负数")
    @Max(value = 1000000, message = "库存过大")
    private Integer stock;

    @Size(max = 255, message = "图片地址过长")
    private String coverImage;

    @NotBlank(message = "请选择上下架状态")
    private String status;

    private Boolean recommend;
}
