package com.example.campus.controller;

import com.example.campus.entity.Evaluation;
import com.example.campus.service.EvaluationLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 评价点赞控制器
 */
@RestController
@RequestMapping("/api/like")
public class EvaluationLikeController {

    @Autowired
    private EvaluationLikeService evaluationLikeService;

    @PostMapping("/toggle/{evaluationId}")
    public Map<String, Object> toggleLike(@PathVariable Long evaluationId,
                                          HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            Long userId = Long.parseLong(userIdStr);

            boolean isLiked = evaluationLikeService.toggleLike(evaluationId, userId);

            // 获取最新的点赞数
            Evaluation evaluation = evaluationLikeService.getEvaluationById(evaluationId);
            int likeCount = evaluation != null ? evaluation.getLikeCount() : 0;

            Map<String, Object> data = new HashMap<>();
            data.put("isLiked", isLiked);
            data.put("likeCount", likeCount);

            result.put("code", 200);
            result.put("msg", isLiked ? "点赞成功" : "取消点赞");
            result.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }


    @GetMapping("/check/{evaluationId}")
    public Map<String, Object> checkLike(@PathVariable Long evaluationId,
                                         HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            Long userId = Long.parseLong(userIdStr);

            boolean isLiked = evaluationLikeService.isLiked(evaluationId, userId);
            result.put("code", 200);
            result.put("msg", "查询成功");
            result.put("data", isLiked);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "查询失败：" + e.getMessage());
        }
        return result;
    }
}
