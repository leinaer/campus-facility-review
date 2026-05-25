package com.example.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campus.entity.DailyPost;
import com.example.campus.service.PostCollectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 帖子收藏控制器
 */
@RestController
@RequestMapping("/api/collect")
public class PostCollectController {

    @Autowired
    private PostCollectService postCollectService;

    /**
     * 切换收藏状态
     */
    @PostMapping("/toggle/{postId}")
    public Map<String, Object> toggleCollect(@PathVariable Long postId,
                                              @RequestParam(defaultValue = "default") String folder,
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
            boolean isCollected = postCollectService.toggleCollect(postId, userId, folder);

            result.put("code", 200);
            result.put("msg", "操作成功");
            result.put("data", isCollected);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 检查收藏状态
     */
    @GetMapping("/status/{postId}")
    public Map<String, Object> checkCollectStatus(@PathVariable Long postId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            boolean isCollected = postCollectService.isCollected(postId, userId);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", isCollected);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取我的收藏列表
     */
    @GetMapping("/my-collects")
    public Map<String, Object> getMyCollects(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
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
            IPage<DailyPost> postsPage = postCollectService.getMyCollects(userId, page, size);

            Map<String, Object> data = new HashMap<>();
            data.put("list", postsPage.getRecords());
            data.put("total", postsPage.getTotal());
            data.put("page", page);
            data.put("size", size);
            data.put("pages", postsPage.getPages());

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
}
