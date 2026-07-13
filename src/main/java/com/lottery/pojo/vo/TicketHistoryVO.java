package com.lottery.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TicketHistoryVO {
    private Long ticketId;
    private String drawNo;
    private String numbers;
    private Integer betCount;
    private BigDecimal amount;
    private Integer status;
    private String statusDesc;
    private LocalDateTime buyTime;
    private String username;
    private String phone;
}