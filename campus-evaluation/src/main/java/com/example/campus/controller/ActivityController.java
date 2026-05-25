package com.example.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campus.annotation.OperationLog;
import com.example.campus.entity.Activity;
import com.example.campus.service.ActivityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/activity")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @GetMapping("/list")
    public Map<String, Object> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            Page<Activity> activityPage = new Page<>(page, size);
            LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like(Activity::getTitle, keyword);
            }

            wrapper.orderByDesc(Activity::getCreateTime);
            Page<Activity> pageResult = activityService.page(activityPage, wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("list", pageResult.getRecords());
            data.put("total", pageResult.getTotal());
            data.put("page", pageResult.getCurrent());
            data.put("size", pageResult.getSize());

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            log.error("获取活动列表失败", e);
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/active")
    public Map<String, Object> getActiveActivities() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Activity> activities = activityService.getActiveActivities();

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", activities);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/add")
    @OperationLog(module = "活动管理", type = "创建", description = "创建活动")
    public Map<String, Object> addActivity(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("=== 创建活动 === 用户ID: {}, 角色: {}",
                    request.getAttribute("userId"),
                    request.getAttribute("role"));

            Activity activity = new Activity();
            activity.setTitle((String) params.get("title"));
            activity.setDescription((String) params.get("description"));
            activity.setCoverImage((String) params.get("coverImage"));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            activity.setStartTime(LocalDateTime.parse((String) params.get("startTime"), formatter));
            activity.setEndTime(LocalDateTime.parse((String) params.get("endTime"), formatter));

            if (params.get("status") != null) {
                activity.setStatus((Integer) params.get("status"));
            }

            if (params.get("tagId") != null) {
                activity.setTagId(Long.valueOf(params.get("tagId").toString()));
            }

            boolean success = activityService.save(activity);

            if (success) {
                result.put("code", 200);
                result.put("msg", "创建成功");
            } else {
                result.put("code", 500);
                result.put("msg", "创建失败");
            }
        } catch (Exception e) {
            log.error("创建活动失败", e);
            result.put("code", 500);
            result.put("msg", "创建失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/update")
    @OperationLog(module = "活动管理", type = "更新", description = "更新活动")
    public Map<String, Object> updateActivity(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long activityId = Long.valueOf(params.get("activityId").toString());
            Activity activity = activityService.getById(activityId);
            if (activity == null) {
                result.put("code", 404);
                result.put("msg", "活动不存在");
                return result;
            }

            activity.setTitle((String) params.get("title"));
            activity.setDescription((String) params.get("description"));
            activity.setCoverImage((String) params.get("coverImage"));

            if (params.get("startTime") != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                activity.setStartTime(LocalDateTime.parse((String) params.get("startTime"), formatter));
            }
            if (params.get("endTime") != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                activity.setEndTime(LocalDateTime.parse((String) params.get("endTime"), formatter));
            }
            if (params.get("status") != null) {
                activity.setStatus((Integer) params.get("status"));
            }
            if (params.get("tagId") != null) {
                activity.setTagId(Long.valueOf(params.get("tagId").toString()));
            } else {
                activity.setTagId(null);
            }

            boolean success = activityService.updateById(activity);

            if (success) {
                result.put("code", 200);
                result.put("msg", "更新成功");
            } else {
                result.put("code", 500);
                result.put("msg", "更新失败");
            }
        } catch (Exception e) {
            log.error("更新活动失败", e);
            result.put("code", 500);
            result.put("msg", "更新失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/status")
    @OperationLog(module = "活动管理", type = "状态更新", description = "更新活动状态")
    public Map<String, Object> updateStatus(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String role = (String) request.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                result.put("code", 403);
                result.put("msg", "权限不足");
                return result;
            }

            Long activityId = Long.valueOf(params.get("activityId").toString());
            Integer status = (Integer) params.get("status");

            Activity activity = activityService.getById(activityId);
            if (activity == null) {
                result.put("code", 404);
                result.put("msg", "活动不存在");
                return result;
            }

            activity.setStatus(status);
            boolean success = activityService.updateById(activity);

            if (success) {
                result.put("code", 200);
                result.put("msg", "操作成功");
            } else {
                result.put("code", 500);
                result.put("msg", "操作失败");
            }
        } catch (Exception e) {
            log.error("更新活动状态失败", e);
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/delete/{activityId}")
    @OperationLog(module = "活动管理", type = "删除", description = "删除活动")
    public Map<String, Object> deleteActivity(@PathVariable Long activityId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String role = (String) request.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                result.put("code", 403);
                result.put("msg", "权限不足");
                return result;
            }

            boolean success = activityService.removeById(activityId);

            if (success) {
                result.put("code", 200);
                result.put("msg", "删除成功");
            } else {
                result.put("code", 500);
                result.put("msg", "删除失败");
            }
        } catch (Exception e) {
            log.error("删除活动失败", e);
            result.put("code", 500);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/end/{activityId}")
    @OperationLog(module = "活动管理", type = "结束", description = "结束活动")
    public Map<String, Object> endActivity(@PathVariable Long activityId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String role = (String) request.getAttribute("role");
            if (!"ADMIN".equals(role)) {
                result.put("code", 403);
                result.put("msg", "权限不足");
                return result;
            }

            boolean success = activityService.endActivity(activityId);

            if (success) {
                result.put("code", 200);
                result.put("msg", "操作成功");
            } else {
                result.put("code", 500);
                result.put("msg", "操作失败");
            }
        } catch (Exception e) {
            log.error("结束活动失败", e);
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }
}
