package com.lottery.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class LotteryDraw extends BaseEntity {
    private Integer id;
    private String drawNo;
    private String numbers;
    private LocalDateTime drawTime;
    private Integer totalBets;
    private BigDecimal totalAmount;
}