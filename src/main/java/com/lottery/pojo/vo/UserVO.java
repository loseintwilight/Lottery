package com.lottery.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserVO {
    private Integer userId;
    private String username;
    private BigDecimal balance;
    private String phone;
}