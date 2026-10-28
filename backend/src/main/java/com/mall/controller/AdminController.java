package com.mall.controller;

import com.mall.common.ApiResponse;
import com.mall.common.PageResult;
import com.mall.dto.OrderStatusRequest;
import com.mall.dto.StockAdjustRequest;
import com.mall.dto.UserStatusRequest;
import com.mall.service.CategoryService;
import com.mall.service.OrderService;
import com.mall.service.ProductService;
import com.mall.service.StatsService;
import com.mall.service.UserService;
import com.mall.vo.CategoryVO;
import com.mall.vo.DailyStatVO;
import com.mall.vo.OperationLogVO;
import com.mall.vo.OrderVO;
import com.mall.vo.OverviewVO;
import com.mall.vo.ProductVO;
import com.mall.vo.UserVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理后台")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final OrderService orderService;
    private final UserService userService;
    private final StatsService statsService;

    @GetMapping("/products")
    public ApiResponse<PageResult<ProductVO>> products(@RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Long categoryId,
                                                        @RequestParam(required = false) String sort,
                                                        @RequestParam(required = false) String status,
                                                        @RequestParam(defaultValue = "1") long page,
                                                        @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(productService.pageAdmin(keyword, categoryId, sort, status, page, size));
    }

    @GetMapping("/products/{id}")
    public ApiResponse<ProductVO> product(@PathVariable Long id) {
        return ApiResponse.ok(productService.detailAdmin(id));
    }

    @PatchMapping("/products/{id}/stock")
    public ApiResponse<ProductVO> stock(@PathVariable Long id, @Valid @RequestBody StockAdjustRequest request) {
        return ApiResponse.ok(productService.adjustStock(id, request));
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryVO>> categories() {
        return ApiResponse.ok(categoryService.listAdmin());
    }

    @GetMapping("/orders")
    public ApiResponse<PageResult<OrderVO>> orders(@RequestParam(required = false) String status,
                                                    @RequestParam(required = false) String orderNo,
                                                    @RequestParam(required = false) String username,
                                                    @RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(orderService.adminPage(status, orderNo, username, page, size));
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<OrderVO> order(@PathVariable Long id) {
        return ApiResponse.ok(orderService.adminDetail(id));
    }

    @PatchMapping("/orders/{id}/status")
    public ApiResponse<OrderVO> orderStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusRequest request) {
        return ApiResponse.ok(orderService.adminUpdateStatus(id, request.getStatus()));
    }

    @GetMapping("/users")
    public ApiResponse<PageResult<UserVO>> users(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Integer status,
                                                  @RequestParam(defaultValue = "1") long page,
                                                  @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(userService.adminPage(keyword, status, page, size));
    }

    @PatchMapping("/users/{id}/status")
    public ApiResponse<UserVO> userStatus(@PathVariable Long id, @Valid @RequestBody UserStatusRequest request) {
        return ApiResponse.ok(userService.changeStatus(id, request));
    }

    @GetMapping("/stats/overview")
    public ApiResponse<OverviewVO> overview() {
        return ApiResponse.ok(statsService.overview());
    }

    @GetMapping("/stats/trend")
    public ApiResponse<List<DailyStatVO>> trend(@RequestParam(defaultValue = "7") int days) {
        return ApiResponse.ok(statsService.trend(days));
    }

    @GetMapping("/logs")
    public ApiResponse<PageResult<OperationLogVO>> logs(@RequestParam(defaultValue = "1") long page,
                                                         @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(statsService.logs(page, size));
    }
}
