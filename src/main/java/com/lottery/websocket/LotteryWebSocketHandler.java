package com.lottery.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lottery.pojo.vo.NotificationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LotteryWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(LotteryWebSocketHandler.class);

    // userId -> Set<WebSocketSession> (一个用户可能多个连接)
    private final ConcurrentHashMap<Integer, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    // sessionId -> userId
    private final ConcurrentHashMap<String, Integer> sessionUserMap = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("WebSocket connected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.debug("Received message: {}", payload);

            // 客户端发送 { "type": "auth", "userId": 1 }
            @SuppressWarnings("unchecked")
            Map<String, Object> msg = objectMapper.readValue(payload, Map.class);
            String type = (String) msg.get("type");

            if ("auth".equals(type)) {
                Integer userId = (Integer) msg.get("userId");
                sessionUserMap.put(session.getId(), userId);
                userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(session);
                log.info("User {} authenticated via WebSocket", userId);
                session.sendMessage(new TextMessage("{\"type\":\"auth_ok\"}"));
            }
        } catch (Exception e) {
            log.error("Error handling WebSocket message", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Integer userId = sessionUserMap.remove(session.getId());
        if (userId != null) {
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }
        log.info("WebSocket disconnected: {}, userId={}", session.getId(), userId);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error, session: {}", session.getId(), exception);
    }

    /**
     * 推送中奖通知给指定用户
     */
    public void pushNotification(Integer userId, List<NotificationVO> notifications) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            log.info("User {} not online, notification will be fetched on login", userId);
            return;
        }

        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "type", "winning",
                    "notifications", notifications
            ));
            TextMessage textMessage = new TextMessage(payload);

            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    try {
                        session.sendMessage(textMessage);
                    } catch (IOException e) {
                        log.error("Failed to send notification to session {}", session.getId(), e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error serializing notification", e);
        }
    }

    /**
     * 推送开奖结果给所有在线用户
     */
    public void broadcastDrawResult(Map<String, Object> drawResult) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "type", "draw_result",
                    "data", drawResult
            ));
            TextMessage textMessage = new TextMessage(payload);

            for (Set<WebSocketSession> sessions : userSessions.values()) {
                for (WebSocketSession session : sessions) {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(textMessage);
                        } catch (IOException e) {
                            log.error("Failed to broadcast to session {}", session.getId(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error broadcasting draw result", e);
        }
    }

    public boolean isUserOnline(Integer userId) {
        Set<WebSocketSession> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }
}