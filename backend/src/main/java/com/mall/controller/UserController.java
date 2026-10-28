package com.mall.controller;

import com.mall.common.ApiResponse;
import com.mall.service.UserService;
import com.mall.vo.UserSummaryVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "个人中心")
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/summary")
    public ApiResponse<UserSummaryVO> summary() {
        return ApiResponse.ok(userService.summary());
    }
}
