package com.mall.controller;

import com.mall.common.ApiResponse;
import com.mall.dto.AddressRequest;
import com.mall.service.AddressService;
import com.mall.vo.AddressVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "收货地址")
@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ApiResponse<List<AddressVO>> list() {
        return ApiResponse.ok(addressService.list());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AddressVO> create(@Valid @RequestBody AddressRequest request) {
        return ApiResponse.ok(addressService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AddressVO> update(@PathVariable Long id, @Valid @RequestBody AddressRequest request) {
        return ApiResponse.ok(addressService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ApiResponse.ok();
    }
}
