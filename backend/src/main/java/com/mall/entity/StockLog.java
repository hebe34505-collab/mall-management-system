package com.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("stock_logs")
public class StockLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Integer changeQty;
    private Integer stockAfter;
    private String bizType;
    private Long refId;
    private Long operatorId;
    private LocalDateTime createdAt;
}
