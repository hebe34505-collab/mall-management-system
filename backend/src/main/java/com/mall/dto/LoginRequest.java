package com.mall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "请输入用户名或邮箱")
    @Size(max = 128, message = "账号过长")
    private String account;

    @NotBlank(message = "请输入密码")
    @Size(max = 32, message = "密码过长")
    private String password;
}
