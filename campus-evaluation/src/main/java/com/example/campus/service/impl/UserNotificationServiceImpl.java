package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.UserNotification;
import com.example.campus.repository.UserNotificationMapper;
import com.example.campus.service.UserNotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息通知服务实现
 */
@Service
public class UserNotificationServiceImpl
        extends ServiceImpl<UserNotificationMapper, UserNotification>
        implements UserNotificationService {

    @Override
    public void createNotification(Long userId, String type, String title, String content,
                                   Long fromUserId, Long relatedId, String relatedType) {
        UserNotification notification = new UserNotification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setFromUserId(fromUserId);
        notification.setRelatedId(relatedId);
        notification.setRelatedType(relatedType);
        notification.setIsRead(0);
        notification.setCreateTime(LocalDateTime.now());

        this.save(notification);
    }

    @Override
    public Map<String, Object> getUserNotifications(Long userId, int page, int size) {
        Page<UserNotification> notificationPage = new Page<>(page, size);
        QueryWrapper<UserNotification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .orderByDesc("create_time");

        Page<UserNotification> resultPage = this.page(notificationPage, queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        result.put("page", page);
        result.put("size", size);
        result.put("pages", resultPage.getPages());

        return result;
    }

    @Override
    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        UserNotification notification = this.getById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return false;
        }

        notification.setIsRead(1);
        return this.updateById(notification);
    }

    @Override
    @Transactional
    public int markAllAsRead(Long userId) {
        return getBaseMapper().markAllAsRead(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        QueryWrapper<UserNotification> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("is_read", 0);
        return this.count(queryWrapper);
    }

    @Override
    @Transactional
    public boolean deleteNotification(Long notificationId, Long userId) {
        UserNotification notification = this.getById(notificationId);
        if (notification == null || !notification.getUserId().equals(userId)) {
            return false;
        }

        return this.removeById(notificationId);
    }
}