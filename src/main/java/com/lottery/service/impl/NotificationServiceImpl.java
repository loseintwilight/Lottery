package com.lottery.service.impl;

import com.lottery.mapper.NotificationMapper;
import com.lottery.pojo.vo.NotificationVO;
import com.lottery.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    public NotificationServiceImpl(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @Override
    public List<NotificationVO> getUnreadNotifications(Integer userId) {
        return notificationMapper.selectUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationMapper.markAsRead(notificationId);
    }

    @Override
    public int countUnread(Integer userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }
}