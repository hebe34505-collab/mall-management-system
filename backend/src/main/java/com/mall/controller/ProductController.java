package com.mall.controller;

import com.mall.common.ApiResponse;
import com.mall.common.PageResult;
import com.mall.dto.ProductSaveRequest;
import com.mall.service.ProductService;
import com.mall.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "商品")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "公开商品列表，只返回上架商品")
    @GetMapping
    public ApiResponse<PageResult<ProductVO>> page(@RequestParam(required = false) String keyword,
                                                    @RequestParam(required = false) Long categoryId,
                                                    @RequestParam(required = false) String sort,
                                                    @RequestParam(required = false) Boolean recommend,
                                                    @RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "12") long size) {
        return ApiResponse.ok(productService.pagePublic(keyword, categoryId, sort, recommend, page, size));
    }

    @Operation(summary = "公开商品详情")
    @GetMapping("/{id}")
    public ApiResponse<ProductVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(productService.detailPublic(id));
    }

    @Operation(summary = "管理员新增商品")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ProductVO> create(@Valid @RequestBody ProductSaveRequest request) {
        return ApiResponse.ok(productService.create(request));
    }

    @Operation(summary = "管理员编辑商品")
    @PutMapping("/{id}")
    public ApiResponse<ProductVO> update(@PathVariable Long id, @Valid @RequestBody ProductSaveRequest request) {
        return ApiResponse.ok(productService.update(id, request));
    }

    @Operation(summary = "管理员删除商品")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ApiResponse.ok();
    }

    @Operation(summary = "管理员上下架")
    @PatchMapping("/{id}/status")
    public ApiResponse<ProductVO> status(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ApiResponse.ok(productService.changeStatus(id, body.get("status")));
    }
}
