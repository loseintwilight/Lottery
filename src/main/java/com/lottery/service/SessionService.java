package com.lottery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.pojo.vo.LoginVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SessionService {

    private static final String PREFIX = "session:";
    private static final long TTL_SECONDS = 7200; // 2小时

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public SessionService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    /** 创建会话，返回 token */
    public String createSession(LoginVO loginVO) {
        String token = UUID.randomUUID().toString().replace("-", "");
        try {
            String json = objectMapper.writeValueAsString(loginVO);
            redis.opsForValue().set(PREFIX + token, json, TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Redis写入失败", e);
        }
        return token;
    }

    /** 根据 token 获取会话，返回 null 表示过期/不存在 */
    public LoginVO getSession(String token) {
        if (token == null || token.isEmpty()) return null;
        String json = redis.opsForValue().get(PREFIX + token);
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, LoginVO.class);
        } catch (Exception e) {
            return null;
        }
    }

    /** 移除会话（退出登录） */
    public void removeSession(String token) {
        if (token != null && !token.isEmpty()) {
            redis.delete(PREFIX + token);
        }
    }

    /** 刷新 TTL */
    public void refreshSession(String token) {
        if (token != null && !token.isEmpty()) {
            redis.expire(PREFIX + token, TTL_SECONDS, TimeUnit.SECONDS);
        }
    }

    /** 更新会话数据（如余额变更后调用），使用原有 token */
    public void updateSession(String token, LoginVO loginVO) {
        if (token == null || token.isEmpty()) return;
        try {
            String json = objectMapper.writeValueAsString(loginVO);
            redis.opsForValue().set(PREFIX + token, json, TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Redis更新失败", e);
        }
    }
}