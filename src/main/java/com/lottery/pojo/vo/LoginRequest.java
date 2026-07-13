package com.lottery.pojo.vo;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}