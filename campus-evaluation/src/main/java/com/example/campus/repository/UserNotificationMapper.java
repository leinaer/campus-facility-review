package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.UserNotification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserNotificationMapper extends BaseMapper<UserNotification> {

    /**
     * 标记通知为已读
     */
    @Update("UPDATE user_notification SET is_read = 1 WHERE notification_id = #{notificationId}")
    int markAsRead(@Param("notificationId") Long notificationId);

    /**
     * 批量标记为已读
     */
    @Update("UPDATE user_notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
}