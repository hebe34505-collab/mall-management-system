package com.mall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginVO {
    private String token;
    private String tokenType;
    private long expiresInMinutes;
    private UserVO user;
}
