package com.lottery.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public class Ticket extends BaseEntity {
    private Long id;
    private Integer userId;
    private String drawNo;
    private String numbers;
    private Integer betCount;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime buyTime;
}