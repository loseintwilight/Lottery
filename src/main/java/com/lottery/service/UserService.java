package com.lottery.service;

import com.lottery.pojo.vo.LoginVO;
import com.lottery.pojo.vo.UserVO;

import java.math.BigDecimal;

public interface UserService {
    LoginVO register(String username, String password, String phone);

    LoginVO login(String username, String password);

    UserVO recharge(Integer userId, BigDecimal amount);

    UserVO getUserInfo(Integer userId);
}