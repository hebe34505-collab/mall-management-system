package com.mall.domain;

import com.mall.common.BusinessException;
import com.mall.common.ErrorCode;

/**
 * 下单前的库存与上架校验。真正扣库存仍由 SQL 条件更新完成，避免并发超卖。
 */
public final class StockPolicy {

    private StockPolicy() {
    }

    public static void assertPurchasable(String status, int stock, int quantity) {
        if (!ProductStatus.ON_SHELF.name().equals(status)) {
            throw new BusinessException(ErrorCode.BUSINESS, "商品已下架，不能购买");
        }
        if (quantity < 1 || quantity > 99) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "购买数量必须在 1 到 99 之间");
        }
        if (stock < quantity) {
            throw new BusinessException(ErrorCode.BUSINESS, "库存不足");
        }
    }
}
