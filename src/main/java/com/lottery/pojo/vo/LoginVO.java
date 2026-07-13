package com.lottery.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class LoginVO {
    private Integer userId;
    private String username;
    private BigDecimal balance;
    private String phone;
    private String token;
    private List<NotificationVO> unreadNotifications;
}