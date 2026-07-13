package com.lottery.controller;

import com.lottery.common.ResultCode;
import com.lottery.pojo.vo.IdRequest;
import com.lottery.pojo.vo.NotificationVO;
import com.lottery.pojo.vo.R;
import com.lottery.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/notifications")
    public R<List<NotificationVO>> getUnreadNotifications(@RequestParam Integer userId) {
        List<NotificationVO> list = notificationService.getUnreadNotifications(userId);
        return R.success(list);
    }

    @PostMapping("/notifications/read")
    public R<Void> markAsRead(@RequestBody IdRequest req) {
        try {
            notificationService.markAsRead(req.getId());
            return R.success(null);
        } catch (RuntimeException e) {
            return R.error(ResultCode.ERROR, e.getMessage());
        }
    }

    @GetMapping("/notifications/count")
    public R<Integer> countUnread(@RequestParam Integer userId) {
        int count = notificationService.countUnread(userId);
        return R.success(count);
    }
}