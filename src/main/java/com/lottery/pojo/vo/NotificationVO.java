package com.lottery.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class NotificationVO {
    private Long id;
    private String drawNo;
    private Long ticketId;
    private String prizeLevel;
    private BigDecimal prizeAmount;
    private Integer isRead;
    private String numbers;
}