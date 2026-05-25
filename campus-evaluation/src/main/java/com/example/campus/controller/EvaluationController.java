package com.example.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campus.annotation.OperationLog;
import com.example.campus.common.Result;
import com.example.campus.dto.EvaluationVO;
import com.example.campus.entity.Category;
import com.example.campus.entity.Evaluation;
import com.example.campus.entity.Facility;
import com.example.campus.exception.BusinessException;
import com.example.campus.repository.CategoryMapper;
import com.example.campus.repository.FacilityMapper;
import com.example.campus.service.EvaluationService;
import com.example.campus.utils.EvaluationVOConverter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/evaluation")
public class EvaluationController {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private FacilityMapper facilityMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private EvaluationVOConverter evaluationVOConverter;

    @GetMapping("/admin/list")
    public Map<String, Object> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer rating) {
        Map<String, Object> result = new HashMap<>();
        try {
            Page<Evaluation> evaluationPage = new Page<>(page, size);
            LambdaQueryWrapper<Evaluation> wrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like(Evaluation::getContent, keyword);
            }

            if (status != null) {
                wrapper.eq(Evaluation::getStatus, status);
            }

            if (rating != null) {
                wrapper.eq(Evaluation::getRating, rating);
            }

            wrapper.orderByDesc(Evaluation::getCreateTime);
            Page<Evaluation> pageResult = evaluationService.page(evaluationPage, wrapper);

            // 获取分页数据（在转换之前保存总数）
            long total = pageResult.getTotal();
            List<Evaluation> evaluations = pageResult.getRecords();

            // 查询所有设施，构建 ID 到名称的映射
            List<Facility> facilities = facilityMapper.selectList(null);
            Map<Long, Facility> facilityMap = facilities.stream()
                    .collect(Collectors.toMap(
                            Facility::getFacilityId,
                            f -> f,
                            (existing, replacement) -> existing
                    ));

            // 查询所有分类，构建 ID 到名称的映射
            List<Category> categories = categoryMapper.selectList(null);
            Map<Long, String> categoryMap = categories.stream()
                    .collect(Collectors.toMap(
                            Category::getCategoryId,
                            Category::getName,
                            (existing, replacement) -> existing
                    ));

            // 转换数据，添加设施名称和分类名称
            List<Map<String, Object>> records = new ArrayList<>();
            for (Evaluation evaluation : evaluations) {
                Map<String, Object> map = new HashMap<>();
                map.put("evaluationId", evaluation.getEvaluationId());
                map.put("userId", evaluation.getUserId());
                map.put("facilityId", evaluation.getFacilityId());
                map.put("content", evaluation.getContent());
                map.put("rating", evaluation.getRating());
                map.put("images", evaluation.getImages());
                map.put("likeCount", evaluation.getLikeCount());
                map.put("commentCount", evaluation.getCommentCount());
                map.put("status", evaluation.getStatus());
                map.put("createTime", evaluation.getCreateTime());
                map.put("updateTime", evaluation.getUpdateTime());

                // 添加设施信息
                Facility facility = facilityMap.get(evaluation.getFacilityId());
                if (facility != null) {
                    map.put("facilityName", facility.getName());
                    map.put("categoryName", categoryMap.getOrDefault(facility.getCategoryId(), "未分类"));
                } else {
                    map.put("facilityName", "未知设施");
                    map.put("categoryName", "未分类");
                }

                records.add(map);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("records", records);
            data.put("total", total);
            data.put("page", pageResult.getCurrent());
            data.put("size", pageResult.getSize());

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            log.error("获取评价列表失败", e);
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }



    @DeleteMapping("/admin/delete/{evaluationId}")
    public Map<String, Object> adminDelete(@PathVariable Long evaluationId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = evaluationService.removeById(evaluationId);
            if (success) {
                result.put("code", 200);
                result.put("msg", "删除成功");
            } else {
                result.put("code", 500);
                result.put("msg", "删除失败");
            }
        } catch (Exception e) {
            log.error("删除评价失败", e);
            result.put("code", 500);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/admin/status/{evaluationId}")
    public Map<String, Object> adminUpdateStatus(
            @PathVariable Long evaluationId,
            @RequestBody Map<String, Integer> param) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer status = param.get("status");
            Evaluation evaluation = evaluationService.getById(evaluationId);
            if (evaluation == null) {
                result.put("code", 404);
                result.put("msg", "评价不存在");
                return result;
            }
            evaluation.setStatus(status);
            evaluationService.updateById(evaluation);
            result.put("code", 200);
            result.put("msg", "操作成功");
        } catch (Exception e) {
            log.error("更新评价状态失败", e);
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/publish")
    @OperationLog(module = "评价管理", type = "发布", description = "发布评价")
    public Map<String, Object> publishEvaluation(@RequestBody Evaluation evaluation, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            evaluation.setUserId(userId);

            boolean success = evaluationService.publishEvaluation(evaluation);

            if (success) {
                result.put("code", 200);
                result.put("msg", "发布成功");
            } else {
                result.put("code", 500);
                result.put("msg", "发布失败");
            }
        } catch (BusinessException e) {
            result.put("code", e.getCode());
            result.put("msg", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "发布失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/by-facility/{facilityId}")
    public Map<String, Object> getByFacility(@PathVariable Long facilityId,
                                             @RequestParam(defaultValue = "latest") String sortBy,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size,
                                             HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = null;
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr != null) {
                currentUserId = Long.parseLong(userIdStr);
            }

            Map<String, Object> data = evaluationService.getByFacilityId(facilityId, sortBy, page, size);

            @SuppressWarnings("unchecked")
            List<Evaluation> evaluations = (List<Evaluation>) data.get("list");
            List<EvaluationVO> voList = evaluationVOConverter.toVOList(evaluations, currentUserId);

            data.put("list", voList);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", data);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/my-evaluations")
    public Map<String, Object> getMyEvaluations(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            Long userId = Long.parseLong(userIdStr);

            List<Evaluation> evaluations = evaluationService.getByUserId(userId);

            List<EvaluationVO> voList = evaluationVOConverter.toVOList(evaluations, userId);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", voList);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/latest")
    public Map<String, Object> getLatestEvaluations(@RequestParam(defaultValue = "20") int limit,
                                                    @RequestParam(required = false) String filterType,
                                                    HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long currentUserId = null;
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr != null) {
                currentUserId = Long.parseLong(userIdStr);
            }

            List<Evaluation> evaluations = evaluationService.getLatestEvaluations(limit, filterType);

            List<EvaluationVO> voList = evaluationVOConverter.toVOList(evaluations, currentUserId);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", voList);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{evaluationId}")
    @OperationLog(module = "评价管理", type = "删除", description = "删除评价")
    public Map<String, Object> deleteEvaluation(@PathVariable Long evaluationId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            boolean success = evaluationService.deleteEvaluation(evaluationId, userId);

            if (success) {
                result.put("code", 200);
                result.put("msg", "删除成功");
            } else {
                result.put("code", 403);
                result.put("msg", "无权删除或评价不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return result;
    }
    @PostMapping("/admin/batch-delete")
    public Map<String, Object> adminBatchDelete(@RequestBody Map<String, List<Long>> param) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Long> ids = param.get("ids");
            if (ids == null || ids.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "请选择要删除的评价");
                return result;
            }
            evaluationService.removeByIds(ids);
            result.put("code", 200);
            result.put("msg", "批量删除成功");
        } catch (Exception e) {
            log.error("批量删除失败", e);
            result.put("code", 500);
            result.put("msg", "批量删除失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/admin/batch-status")
    public Map<String, Object> adminBatchStatus(@RequestBody Map<String, Object> param) {
        Map<String, Object> result = new HashMap<>();
        try {
            @SuppressWarnings("unchecked")
            List<Long> ids = (List<Long>) param.get("ids");
            Integer status = (Integer) param.get("status");

            if (ids == null || ids.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "请选择要操作的评价");
                return result;
            }

            List<Evaluation> evaluations = evaluationService.listByIds(ids);
            evaluations.forEach(evaluation -> evaluation.setStatus(status));
            evaluationService.updateBatchById(evaluations);

            result.put("code", 200);
            result.put("msg", "批量操作成功");
        } catch (Exception e) {
            log.error("批量更新状态失败", e);
            result.put("code", 500);
            result.put("msg", "批量操作失败：" + e.getMessage());
        }
        return result;
    }
}
