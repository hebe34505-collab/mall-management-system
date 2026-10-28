package com.mall.domain;

public enum ProductStatus {
    ON_SHELF,
    OFF_SHELF;

    public String label() {
        return this == ON_SHELF ? "上架" : "下架";
    }

    public static ProductStatus parse(String value) {
        try {
            return ProductStatus.valueOf(value);
        } catch (Exception exception) {
            return null;
        }
    }
}
