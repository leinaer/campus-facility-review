package com.example.campus.controller;

import com.example.campus.service.UserNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息通知控制器
 */
@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    @Autowired
    private UserNotificationService notificationService;

    /**
     * 获取用户通知列表
     */
    @GetMapping("/list")
    public Map<String, Object> getNotifications(@RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            Map<String, Object> data = notificationService.getUserNotifications(userId, page, size);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            long count = notificationService.getUnreadCount(userId);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", count);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/mark-read/{notificationId}")
    public Map<String, Object> markAsRead(@PathVariable Long notificationId,
                                          HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            boolean success = notificationService.markAsRead(notificationId, userId);

            if (success) {
                result.put("code", 200);
                result.put("msg", "操作成功");
            } else {
                result.put("code", 403);
                result.put("msg", "无权操作");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 全部标记为已读
     */
    @PostMapping("/mark-all-read")
    public Map<String, Object> markAllAsRead(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            int count = notificationService.markAllAsRead(userId);

            result.put("code", 200);
            result.put("msg", "操作成功");
            result.put("data", count);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    public Map<String, Object> deleteNotification(@PathVariable Long notificationId,
                                                  HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            boolean success = notificationService.deleteNotification(notificationId, userId);

            if (success) {
                result.put("code", 200);
                result.put("msg", "删除成功");
            } else {
                result.put("code", 403);
                result.put("msg", "无权操作");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return result;
    }
}