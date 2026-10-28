package com.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.common.BusinessException;
import com.mall.common.ErrorCode;
import com.mall.dto.AddressRequest;
import com.mall.entity.Address;
import com.mall.mapper.AddressMapper;
import com.mall.security.SecurityUtils;
import com.mall.vo.AddressVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressMapper addressMapper;

    public List<AddressVO> list() {
        Long userId = SecurityUtils.current().getId();
        return addressMapper.selectList(new LambdaQueryWrapper<Address>()
                        .eq(Address::getUserId, userId)
                        .orderByDesc(Address::getIsDefault)
                        .orderByDesc(Address::getId))
                .stream().map(AddressVO::from).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public AddressVO create(AddressRequest request) {
        Long userId = SecurityUtils.current().getId();
        Address address = new Address();
        address.setUserId(userId);
        fill(address, request);
        if (address.getIsDefault() == 1) {
            clearDefault(userId);
        } else if (count(userId) == 0) {
            address.setIsDefault(1);
        }
        addressMapper.insert(address);
        return AddressVO.from(address);
    }

    @Transactional(rollbackFor = Exception.class)
    public AddressVO update(Long id, AddressRequest request) {
        Address address = requireOwn(id);
        fill(address, request);
        if (address.getIsDefault() == 1) {
            clearDefault(address.getUserId());
        }
        addressMapper.updateById(address);
        return AddressVO.from(address);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Address address = requireOwn(id);
        addressMapper.deleteById(id);
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            Address next = addressMapper.selectOne(new LambdaQueryWrapper<Address>()
                    .eq(Address::getUserId, address.getUserId())
                    .orderByDesc(Address::getId)
                    .last("LIMIT 1"));
            if (next != null) {
                next.setIsDefault(1);
                addressMapper.updateById(next);
            }
        }
    }

    private void fill(Address address, AddressRequest request) {
        address.setReceiver(request.getReceiver().trim());
        address.setPhone(request.getPhone());
        address.setProvince(request.getProvince().trim());
        address.setCity(request.getCity().trim());
        address.setDistrict(request.getDistrict().trim());
        address.setDetail(request.getDetail().trim());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()) ? 1 : 0);
    }

    private void clearDefault(Long userId) {
        List<Address> addresses = addressMapper.selectList(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId).eq(Address::getIsDefault, 1));
        for (Address address : addresses) {
            address.setIsDefault(0);
            addressMapper.updateById(address);
        }
    }

    private long count(Long userId) {
        Long count = addressMapper.selectCount(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
        return count == null ? 0 : count;
    }

    private Address requireOwn(Long id) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(SecurityUtils.current().getId())) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "收货地址不存在");
        }
        return address;
    }
}
