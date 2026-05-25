package com.example.campus.controller;

import com.example.campus.service.PostLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 帖子点赞控制器
 */
@RestController
@RequestMapping("/api/post/like")
public class PostLikeController {

    @Autowired
    private PostLikeService postLikeService;

    /**
     * 切换点赞状态
     */
    @PostMapping("/toggle/{postId}")
    public Map<String, Object> toggleLike(@PathVariable Long postId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            boolean isLiked = postLikeService.toggleLike(postId, userId);

            result.put("code", 200);
            result.put("msg", "操作成功");
            result.put("data", isLiked);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 检查点赞状态
     */
    @GetMapping("/status/{postId}")
    public Map<String, Object> checkLikeStatus(@PathVariable Long postId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            boolean isLiked = postLikeService.isLiked(postId, userId);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", isLiked);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }
}