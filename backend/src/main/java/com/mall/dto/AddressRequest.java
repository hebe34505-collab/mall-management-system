package com.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {
    @NotBlank(message = "请输入收货人")
    @Size(max = 32, message = "收货人过长")
    private String receiver;

    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "请输入省份")
    @Size(max = 32)
    private String province;

    @NotBlank(message = "请输入城市")
    @Size(max = 32)
    private String city;

    @NotBlank(message = "请输入区县")
    @Size(max = 32)
    private String district;

    @NotBlank(message = "请输入详细地址")
    @Size(max = 200, message = "详细地址过长")
    private String detail;

    private Boolean isDefault;
}
