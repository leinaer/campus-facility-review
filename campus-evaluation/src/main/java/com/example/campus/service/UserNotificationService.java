package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.UserNotification;

import java.util.List;
import java.util.Map;

/**
 * 消息通知服务接口
 */
public interface UserNotificationService extends IService<UserNotification> {

    /**
     * 创建通知
     */
    void createNotification(Long userId, String type, String title, String content,
                           Long fromUserId, Long relatedId, String relatedType);

    /**
     * 获取用户的通知列表
     */
    Map<String, Object> getUserNotifications(Long userId, int page, int size);

    /**
     * 标记通知为已读
     */
    boolean markAsRead(Long notificationId, Long userId);

    /**
     * 全部标记为已读
     */
    int markAllAsRead(Long userId);

    /**
     * 获取未读消息数量
     */
    long getUnreadCount(Long userId);

    /**
     * 删除通知
     */
    boolean deleteNotification(Long notificationId, Long userId);
}