package com.lottery.controller;

import com.lottery.common.ResultCode;
import com.lottery.pojo.vo.*;
import com.lottery.service.SessionService;
import com.lottery.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final SessionService sessionService;

    public UserController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
    }

    @PostMapping("/register")
    public R<LoginVO> register(@RequestBody RegisterRequest req) {
        try {
            LoginVO vo = userService.register(req.getUsername(), req.getPassword(), req.getPhone());
            try {
                String token = sessionService.createSession(vo);
                vo.setToken(token);
            } catch (Exception e) {
                // Redis 不可用时，生成一个本地 token
                vo.setToken("local_" + java.util.UUID.randomUUID().toString().replace("-", ""));
            }
            return R.success(vo);
        } catch (RuntimeException e) {
            return R.error(ResultCode.USER_EXISTS, e.getMessage());
        }
    }

    @PostMapping("/login")
    public R<LoginVO> login(@RequestBody LoginRequest req) {
        try {
            LoginVO vo = userService.login(req.getUsername(), req.getPassword());
            try {
                String token = sessionService.createSession(vo);
                vo.setToken(token);
            } catch (Exception e) {
                // Redis 不可用时，生成一个本地 token
                vo.setToken("local_" + java.util.UUID.randomUUID().toString().replace("-", ""));
            }
            return R.success(vo);
        } catch (RuntimeException e) {
            if ("user not found".equals(e.getMessage())) {
                return R.error(ResultCode.USER_NOT_FOUND);
            }
            return R.error(ResultCode.PASSWORD_ERROR);
        }
    }

    @GetMapping("/session/info")
    public R<LoginVO> sessionInfo(@RequestParam String token) {
        try {
            LoginVO session = sessionService.getSession(token);
            if (session == null) {
                return R.error(ResultCode.USER_NOT_FOUND, "会话已过期，请重新登录");
            }
            sessionService.refreshSession(token);
            return R.success(session);
        } catch (Exception e) {
            return R.error(ResultCode.USER_NOT_FOUND, "Redis 不可用");
        }
    }

    @PostMapping("/logout")
    public R<Void> logout(@RequestBody LogoutRequest req) {
        try {
            sessionService.removeSession(req.getToken());
        } catch (Exception e) {
            // Redis 不可用时忽略
        }
        return R.success(null);
    }

    @PostMapping("/recharge")
    public R<UserVO> recharge(@RequestBody RechargeRequest req) {
        try {
            UserVO vo = userService.recharge(req.getUserId(), req.getAmount());
            // 更新 Redis 会话中的余额
            if (req.getToken() != null && !req.getToken().isEmpty()) {
                try {
                    LoginVO session = sessionService.getSession(req.getToken());
                    if (session != null) {
                        session.setBalance(vo.getBalance());
                        sessionService.updateSession(req.getToken(), session);
                    }
                } catch (Exception e) {
                    // Redis 不可用时忽略
                }
            }
            return R.success(vo);
        } catch (RuntimeException e) {
            return R.error(ResultCode.USER_NOT_FOUND, e.getMessage());
        }
    }

    @GetMapping("/user/info")
    public R<UserVO> getUserInfo(@RequestParam Integer userId) {
        try {
            UserVO vo = userService.getUserInfo(userId);
            return R.success(vo);
        } catch (RuntimeException e) {
            return R.error(ResultCode.USER_NOT_FOUND);
        }
    }
}