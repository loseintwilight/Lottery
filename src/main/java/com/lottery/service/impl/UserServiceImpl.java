package com.lottery.service.impl;

import com.lottery.common.PasswordUtil;
import com.lottery.mapper.NotificationMapper;
import com.lottery.mapper.UserMapper;
import com.lottery.pojo.entity.User;
import com.lottery.pojo.vo.LoginVO;
import com.lottery.pojo.vo.NotificationVO;
import com.lottery.pojo.vo.UserVO;
import com.lottery.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;

    public UserServiceImpl(UserMapper userMapper, NotificationMapper notificationMapper) {
        this.userMapper = userMapper;
        this.notificationMapper = notificationMapper;
    }

    @Override
    @Transactional
    public LoginVO register(String username, String password, String phone) {
        User exist = userMapper.selectByUsername(username);
        if (exist != null) {
            throw new RuntimeException("username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(PasswordUtil.encrypt(password));
        user.setPhone(phone);
        user.setBalance(BigDecimal.ZERO);
        userMapper.insert(user);

        return buildLoginVO(user);
    }

    @Override
    public LoginVO login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("user not found");
        }
        if (!PasswordUtil.matches(password, user.getPassword())) {
            throw new RuntimeException("password error");
        }
        return buildLoginVO(user);
    }

    @Override
    @Transactional
    public UserVO recharge(Integer userId, BigDecimal amount) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("user not found");
        }
        userMapper.addBalance(userId, amount);
        user = userMapper.selectById(userId);

        UserVO vo = new UserVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setBalance(user.getBalance());
        vo.setPhone(user.getPhone());
        return vo;
    }

    @Override
    public UserVO getUserInfo(Integer userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("user not found");
        }
        UserVO vo = new UserVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setBalance(user.getBalance());
        vo.setPhone(user.getPhone());
        return vo;
    }

    private LoginVO buildLoginVO(User user) {
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setBalance(user.getBalance());
        vo.setPhone(user.getPhone());

        List<NotificationVO> unread = notificationMapper.selectUnreadByUserId(user.getId());
        vo.setUnreadNotifications(unread);
        return vo;
    }
}