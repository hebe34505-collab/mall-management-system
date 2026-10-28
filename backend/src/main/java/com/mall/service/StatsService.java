package com.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mall.common.PageResult;
import com.mall.domain.AccessPolicy;
import com.mall.entity.OperationLog;
import com.mall.mapper.OperationLogMapper;
import com.mall.mapper.StatsMapper;
import com.mall.security.SecurityUtils;
import com.mall.vo.DailyStatRow;
import com.mall.vo.DailyStatVO;
import com.mall.vo.OperationLogVO;
import com.mall.vo.OverviewVO;
import com.mall.common.BusinessException;
import com.mall.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private static final String SALES_RULE = "销售额只统计状态为待发货、已发货、已完成的订单金额，已取消订单不计入。下单成功即视为已支付，本系统未接入第三方支付。";

    private final StatsMapper statsMapper;
    private final OrderService orderService;
    private final ProductService productService;
    private final OperationLogMapper operationLogMapper;

    public OverviewVO overview() {
        assertAdmin();
        OverviewVO overview = new OverviewVO();
        overview.setUserCount(statsMapper.countUsers());
        overview.setProductCount(statsMapper.countProducts());
        overview.setEffectiveOrderCount(statsMapper.countEffectiveOrders());
        BigDecimal sales = statsMapper.sumEffectiveSales();
        overview.setSalesAmount(sales == null ? BigDecimal.ZERO : sales);
        overview.setSalesRule(SALES_RULE);
        overview.setRecentOrders(orderService.recent(8));
        overview.setHotProducts(productService.hot(5));
        return overview;
    }

    public List<DailyStatVO> trend(int days) {
        assertAdmin();
        int window = Math.min(Math.max(days, 1), 30);
        LocalDate startDate = LocalDate.now().minusDays(window - 1L);
        List<DailyStatRow> rows = statsMapper.trend(startDate.atStartOfDay());
        Map<String, DailyStatRow> byDate = rows.stream().collect(Collectors.toMap(DailyStatRow::getStatDate, Function.identity(), (left, right) -> left));
        List<DailyStatVO> result = new ArrayList<>();
        for (int i = 0; i < window; i++) {
            String day = startDate.plusDays(i).toString();
            DailyStatRow row = byDate.get(day);
            if (row == null) {
                result.add(new DailyStatVO(day, 0, BigDecimal.ZERO.setScale(2)));
            } else {
                result.add(new DailyStatVO(day, row.getOrderCount() == null ? 0 : row.getOrderCount(),
                        row.getSalesAmount() == null ? BigDecimal.ZERO : row.getSalesAmount()));
            }
        }
        return result;
    }

    public PageResult<OperationLogVO> logs(long page, long size) {
        assertAdmin();
        long pageNo = page < 1 ? 1 : page;
        long pageSize = size < 1 ? 10 : Math.min(size, 100);
        Page<OperationLog> result = operationLogMapper.selectPage(new Page<>(pageNo, pageSize),
                new LambdaQueryWrapper<OperationLog>().orderByDesc(OperationLog::getCreatedAt).orderByDesc(OperationLog::getId));
        return new PageResult<>(result.getRecords().stream().map(OperationLogVO::from).toList(),
                result.getTotal(), result.getCurrent(), result.getSize());
    }

    private void assertAdmin() {
        if (!AccessPolicy.isAdmin(SecurityUtils.current().getRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "没有权限访问该资源");
        }
    }
}
