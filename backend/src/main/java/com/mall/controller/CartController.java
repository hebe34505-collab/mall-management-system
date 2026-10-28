package com.mall.controller;

import com.mall.common.ApiResponse;
import com.mall.dto.CartAddRequest;
import com.mall.dto.CartUpdateRequest;
import com.mall.service.CartService;
import com.mall.vo.CartItemVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "购物车")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<List<CartItemVO>> list() {
        return ApiResponse.ok(cartService.list());
    }

    @PostMapping
    public ApiResponse<List<CartItemVO>> add(@Valid @RequestBody CartAddRequest request) {
        return ApiResponse.ok(cartService.add(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<List<CartItemVO>> update(@PathVariable Long id, @Valid @RequestBody CartUpdateRequest request) {
        return ApiResponse.ok(cartService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<List<CartItemVO>> delete(@PathVariable Long id) {
        return ApiResponse.ok(cartService.delete(id));
    }

    @DeleteMapping
    public ApiResponse<Void> clear() {
        cartService.clear();
        return ApiResponse.ok();
    }
}
