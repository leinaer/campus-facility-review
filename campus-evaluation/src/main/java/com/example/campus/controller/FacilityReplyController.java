package com.example.campus.controller;

import com.example.campus.annotation.OperationLog;
import com.example.campus.entity.FacilityReply;
import com.example.campus.service.FacilityReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设施官方回复控制器
 */
@RestController
@RequestMapping("/api/facility/reply")
public class FacilityReplyController {

    @Autowired
    private FacilityReplyService facilityReplyService;

    /**
     * 回复评价（管理员）
     */
    @PostMapping("/publish")
    @OperationLog(module = "评价管理", type = "回复", description = "回复评价")
    public Map<String, Object> replyEvaluation(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String role = (String) request.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                result.put("code", 403);
                result.put("msg", "权限不足");
                return result;
            }

            Long evaluationId = Long.parseLong(params.get("evaluationId").toString());
            String adminIdStr = (String) request.getAttribute("userId");
            Long adminId = Long.parseLong(adminIdStr);
            String content = (String) params.get("content");

            boolean success = facilityReplyService.replyEvaluation(evaluationId, adminId, content);

            result.put("code", success ? 200 : 500);
            result.put("msg", success ? "回复成功" : "回复失败");
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "回复失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取评价的回复列表
     */
    @GetMapping("/by-evaluation/{evaluationId}")
    public Map<String, Object> getReplies(@PathVariable Long evaluationId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<FacilityReply> replies = facilityReplyService.getRepliesByEvaluationId(evaluationId);
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", replies);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }
}