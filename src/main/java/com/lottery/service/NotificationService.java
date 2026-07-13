package com.lottery.service;

import com.lottery.pojo.vo.NotificationVO;

import java.util.List;

public interface NotificationService {
    List<NotificationVO> getUnreadNotifications(Integer userId);

    void markAsRead(Long notificationId);

    int countUnread(Integer userId);
}