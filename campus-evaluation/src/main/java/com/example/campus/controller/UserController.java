package com.example.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campus.entity.DailyPost;
import com.example.campus.entity.Evaluation;
import com.example.campus.entity.User;
import com.example.campus.service.DailyPostService;
import com.example.campus.service.EvaluationService;
import com.example.campus.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private DailyPostService dailyPostService;

    @Autowired
    private EvaluationService evaluationService;

    /**
     * 更新用户资料
     */
    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody Map<String, Object> param, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从request attribute中获取用户ID（由拦截器设置）
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }
            
            Long userId = Long.parseLong(userIdStr);

            User user = userService.getById(userId);
            if (user == null) {
                result.put("code", 404);
                result.put("msg", "用户不存在");
                return result;
            }

            // 更新用户信息
            if (param.containsKey("nickname")) {
                String nickname = (String) param.get("nickname");
                if (nickname != null && !nickname.trim().isEmpty()) {
                    user.setNickname(nickname.trim());
                }
            }
            
            if (param.containsKey("avatarUrl")) {
                user.setAvatarUrl((String) param.get("avatarUrl"));
            }
            
            if (param.containsKey("signature")) {
                user.setSignature((String) param.get("signature"));
            }
            
            if (param.containsKey("gender")) {
                Object genderObj = param.get("gender");
                if (genderObj instanceof Number) {
                    user.setGender(((Number) genderObj).intValue());
                }
            }

            user.setUpdateTime(LocalDateTime.now());
            userService.updateById(user);

            result.put("code", 200);
            result.put("msg", "更新成功");
            result.put("data", user);
        } catch (Exception e) {
            log.error("更新用户资料失败", e);
            result.put("code", 500);
            result.put("msg", "更新失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar")
    public Map<String, Object> uploadAvatar(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String token) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (file.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "文件不能为空");
                return result;
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;

            // 保存路径
            String uploadDir = "D:/campus-uploads/avatar/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filePath = uploadDir + fileName;
            file.transferTo(new File(filePath));

            // 返回访问URL
            String avatarUrl = "http://localhost:8080/uploads/avatar/" + fileName;

            result.put("code", 200);
            result.put("msg", "上传成功");
            result.put("data", Map.of("avatarUrl", avatarUrl));
        } catch (IOException e) {
            log.error("上传头像失败", e);
            result.put("code", 500);
            result.put("msg", "上传失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/admin/list")
    public Map<String, Object> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            Page<User> userPage = new Page<>(page, size);
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like(User::getNickname, keyword)
                       .or()
                       .like(User::getPhone, keyword);
            }

            wrapper.orderByDesc(User::getCreateTime);
            Page<User> pageResult = userService.page(userPage, wrapper);

            Map<String, Object> data = new HashMap<>();
            data.put("list", pageResult.getRecords());
            data.put("total", pageResult.getTotal());
            data.put("page", pageResult.getCurrent());
            data.put("size", pageResult.getSize());

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/admin/detail/{userId}")
    public Map<String, Object> adminGetUserDetail(@PathVariable Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.getById(userId);
            if (user == null) {
                result.put("code", 404);
                result.put("msg", "用户不存在");
                return result;
            }

            long postCount = dailyPostService.count(new LambdaQueryWrapper<DailyPost>()
                    .eq(DailyPost::getUserId, userId));
            long evaluationCount = evaluationService.count(new LambdaQueryWrapper<Evaluation>()
                    .eq(Evaluation::getUserId, userId));

            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getUserId());
            data.put("nickname", user.getNickname());
            data.put("avatarUrl", user.getAvatarUrl());
            data.put("phone", user.getPhone());
            data.put("role", user.getRole());
            data.put("status", user.getStatus());
            data.put("createTime", user.getCreateTime());
            data.put("postCount", postCount);
            data.put("evaluationCount", evaluationCount);

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            log.error("获取用户详情失败", e);
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/admin/status/{userId}")
    public Map<String, Object> adminUpdateUserStatus(
            @PathVariable Long userId,
            @RequestBody Map<String, Integer> param) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer status = param.get("status");
            User user = userService.getById(userId);
            if (user == null) {
                result.put("code", 404);
                result.put("msg", "用户不存在");
                return result;
            }
            user.setStatus(status);
            userService.updateById(user);
            result.put("code", 200);
            result.put("msg", "操作成功");
        } catch (Exception e) {
            log.error("更新用户状态失败", e);
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }
}
