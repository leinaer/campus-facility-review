package com.example.campus.controller;

import com.example.campus.service.EvaluationHelpfulService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 评价有用性投票控制器
 */
@RestController
@RequestMapping("/api/evaluation/helpful")
public class EvaluationHelpfulController {

    @Autowired
    private EvaluationHelpfulService evaluationHelpfulService;

    /**
     * 投票（有用/无用）
     */
    @PostMapping("/vote/{evaluationId}")
    public Map<String, Object> vote(@PathVariable Long evaluationId,
                                     @RequestParam Integer voteType,
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
            boolean success = evaluationHelpfulService.vote(evaluationId, userId, voteType);

            result.put("code", 200);
            result.put("msg", success ? "投票成功" : "已投过相同票");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "投票失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 检查投票状态
     */
    @GetMapping("/status/{evaluationId}")
    public Map<String, Object> checkVoteStatus(@PathVariable Long evaluationId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            boolean hasVoted = evaluationHelpfulService.hasVoted(evaluationId, userId);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", hasVoted);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }
}