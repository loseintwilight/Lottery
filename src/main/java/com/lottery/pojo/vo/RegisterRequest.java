package com.lottery.pojo.vo;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String phone;
}