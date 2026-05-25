package com.example.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campus.entity.PostTopic;
import com.example.campus.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 话题控制器
 */
@RestController
@RequestMapping("/api/topic")
public class TopicController {

    @Autowired
    private TopicService topicService;

    /**
     * 获取话题列表（支持筛选和搜索）
     */
    @GetMapping("/list")
    public Map<String, Object> getTopicList(@RequestParam(defaultValue = "hot") String sortBy,
                                             @RequestParam(required = false) String keyword,
                                             HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;

            List<PostTopic> topics = topicService.getTopicList(sortBy, keyword, userId);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", topics);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取话题详情
     */
    @GetMapping("/{topicId}")
    public Map<String, Object> getTopicDetail(@PathVariable Long topicId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            Long userId = userIdStr != null ? Long.parseLong(userIdStr) : null;

            PostTopic topic = topicService.getTopicDetail(topicId, userId);

            if (topic != null) {
                result.put("code", 200);
                result.put("msg", "获取成功");
                result.put("data", topic);
            } else {
                result.put("code", 404);
                result.put("msg", "话题不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 切换话题关注状态
     */
    @PostMapping("/follow/toggle/{topicId}")
    public Map<String, Object> toggleFollow(@PathVariable Long topicId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            Map<String, Object> data = topicService.toggleFollow(topicId, userId);

            result.put("code", 200);
            result.put("msg", "操作成功");
            result.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取热门话题
     */
    @GetMapping("/hot")
    public Map<String, Object> getHotTopics(@RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<PostTopic> topics = topicService.getHotTopics(limit);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", topics);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }
}