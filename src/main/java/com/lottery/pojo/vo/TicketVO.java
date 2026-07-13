package com.lottery.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TicketVO {
    private Long ticketId;
    private String drawNo;
    private String numbers;
    private Integer betCount;
    private BigDecimal amount;
}