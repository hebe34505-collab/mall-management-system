package com.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.BusinessException;
import com.mall.common.ErrorCode;
import com.mall.common.PageResult;
import com.mall.domain.AccessPolicy;
import com.mall.domain.OrderStatus;
import com.mall.dto.UserStatusRequest;
import com.mall.entity.Order;
import com.mall.entity.User;
import com.mall.mapper.OrderMapper;
import com.mall.mapper.StatsMapper;
import com.mall.mapper.UserMapper;
import com.mall.security.LoginUser;
import com.mall.security.SecurityUtils;
import com.mall.vo.OrderVO;
import com.mall.vo.UserSummaryVO;
import com.mall.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final StatsMapper statsMapper;
    private final OrderService orderService;
    private final OperationLogService operationLogService;

    public UserSummaryVO summary() {
        LoginUser loginUser = SecurityUtils.current();
        UserSummaryVO summary = new UserSummaryVO();
        summary.setOrderCount(count(loginUser.getId(), null));
        summary.setPendingCount(count(loginUser.getId(), OrderStatus.PENDING_SHIPMENT));
        summary.setShippedCount(count(loginUser.getId(), OrderStatus.SHIPPED));
        summary.setCompletedCount(count(loginUser.getId(), OrderStatus.COMPLETED));
        summary.setCancelledCount(count(loginUser.getId(), OrderStatus.CANCELLED));
        BigDecimal spent = statsMapper.sumUserSpent(loginUser.getId());
        summary.setTotalSpent(spent == null ? BigDecimal.ZERO : spent);
        summary.setSalesRule("个人消费金额只统计待发货、已发货和已完成订单，已取消订单不计入。");
        List<Order> recent = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, loginUser.getId())
                .orderByDesc(Order::getCreatedAt)
                .last("LIMIT 5"));
        summary.setRecentOrders(recent.stream().map(order -> orderService.myDetail(order.getId())).toList());
        return summary;
    }

    public PageResult<UserVO> adminPage(String keyword, Integer status, long page, long size) {
        assertAdmin();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>().orderByDesc(User::getId);
        if (status != null) {
            if (status != 0 && status != 1) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "用户状态只能是 0 或 1");
            }
            wrapper.eq(User::getStatus, status);
        }
        if (StringUtils.hasText(keyword)) {
            String text = keyword.trim();
            wrapper.and(item -> item.like(User::getUsername, text).or().like(User::getEmail, text).or().like(User::getNickname, text));
        }
        long pageNo = page < 1 ? 1 : page;
        long pageSize = size < 1 ? 10 : Math.min(size, 100);
        Page<User> result = userMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        return new PageResult<>(result.getRecords().stream().map(UserVO::from).toList(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Transactional(rollbackFor = Exception.class)
    public UserVO changeStatus(Long userId, UserStatusRequest request) {
        LoginUser admin = assertAdmin();
        if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "用户状态只能是 0 或 1");
        }
        if (admin.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.BUSINESS, "不能禁用当前登录的管理员");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        user.setStatus(request.getStatus());
        user.setRole(AccessPolicy.retainRole(user.getRole()));
        if (request.getStatus() == 0) {
            user.setTokenVersion(user.getTokenVersion() + 1);
        }
        userMapper.updateById(user);
        operationLogService.record(admin.getId(), admin.getUsername(), "USER", "STATUS",
                user.getUsername() + (request.getStatus() == 1 ? " 已启用" : " 已禁用"));
        return UserVO.from(user);
    }

    private long count(Long userId, OrderStatus status) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId);
        if (status != null) {
            wrapper.eq(Order::getStatus, status.name());
        }
        Long count = orderMapper.selectCount(wrapper);
        return count == null ? 0 : count;
    }

    private LoginUser assertAdmin() {
        LoginUser user = SecurityUtils.current();
        if (!AccessPolicy.isAdmin(user.getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有权限访问该资源");
        }
        return user;
    }
}
