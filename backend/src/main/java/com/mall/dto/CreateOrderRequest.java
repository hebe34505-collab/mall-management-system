package com.mall.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull(message = "请选择收货地址")
    private Long addressId;

    private List<Long> cartItemIds;

    @Valid
    private List<OrderLineRequest> items;

    @Size(max = 200, message = "备注不能超过 200 字")
    private String remark;

    @Size(max = 64, message = "幂等键过长")
    private String idempotencyKey;
}
