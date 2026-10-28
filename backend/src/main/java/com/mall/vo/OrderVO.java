package com.mall.vo;

import com.mall.domain.OrderStatus;
import com.mall.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private String username;
    private String receiver;
    private String phone;
    private String fullAddress;
    private BigDecimal totalAmount;
    private String status;
    private String statusLabel;
    private String remark;
    private Boolean cancellable;
    private LocalDateTime createdAt;
    private List<OrderItemVO> items;

    public static OrderVO from(Order order, String username, List<OrderItemVO> items) {
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setUserId(order.getUserId());
        vo.setUsername(username);
        vo.setReceiver(order.getReceiver());
        vo.setPhone(order.getPhone());
        vo.setFullAddress(order.getFullAddress());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        OrderStatus status = OrderStatus.parse(order.getStatus());
        vo.setStatusLabel(status == null ? order.getStatus() : status.label());
        vo.setRemark(order.getRemark());
        vo.setCancellable(status == OrderStatus.PENDING_SHIPMENT);
        vo.setCreatedAt(order.getCreatedAt());
        vo.setItems(items);
        return vo;
    }
}
