package com.lottery.mapper;

import com.lottery.pojo.entity.Notification;
import com.lottery.pojo.vo.NotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    int insert(Notification notification);

    int batchInsert(@Param("list") List<Notification> notifications);

    List<NotificationVO> selectUnreadByUserId(Integer userId);

    int markAsRead(Long id);

    int countUnreadByUserId(Integer userId);

    int deleteAll();
}
