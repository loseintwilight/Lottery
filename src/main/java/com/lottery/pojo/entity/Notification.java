package com.lottery.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class Notification extends BaseEntity {
    private Long id;
    private Integer userId;
    private String drawNo;
    private Long ticketId;
    private String prizeLevel;
    private BigDecimal prizeAmount;
    private Integer isRead;
}