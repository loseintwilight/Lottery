package com.lottery.pojo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity {
    private Integer id;
    private String username;
    private String password;
    private String phone;
    private BigDecimal balance;
}