package com.lottery.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeRequest {
    private Integer userId;
    private BigDecimal amount;
    private String token; // 可选，用于更新 Redis 会话中的余额
}