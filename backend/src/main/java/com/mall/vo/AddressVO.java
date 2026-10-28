package com.mall.vo;

import com.mall.entity.Address;
import lombok.Data;

@Data
public class AddressVO {
    private Long id;
    private String receiver;
    private String phone;
    private String province;
    private String city;
    private String district;
    private String detail;
    private String fullAddress;
    private Boolean isDefault;

    public static AddressVO from(Address address) {
        AddressVO vo = new AddressVO();
        vo.setId(address.getId());
        vo.setReceiver(address.getReceiver());
        vo.setPhone(address.getPhone());
        vo.setProvince(address.getProvince());
        vo.setCity(address.getCity());
        vo.setDistrict(address.getDistrict());
        vo.setDetail(address.getDetail());
        vo.setFullAddress(address.getProvince() + address.getCity() + address.getDistrict() + address.getDetail());
        vo.setIsDefault(address.getIsDefault() != null && address.getIsDefault() == 1);
        return vo;
    }
}
