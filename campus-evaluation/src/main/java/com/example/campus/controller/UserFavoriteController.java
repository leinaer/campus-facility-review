package com.example.campus.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campus.entity.Facility;
import com.example.campus.service.UserFavoriteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户收藏控制器
 */
@RestController
@RequestMapping("/api/favorite")
public class UserFavoriteController {

    @Autowired
    private UserFavoriteService userFavoriteService;

    /**
     * 切换收藏状态
     */
    @PostMapping("/toggle/{facilityId}")
    public Map<String, Object> toggleFavorite(@PathVariable Long facilityId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            boolean isFavorite = userFavoriteService.toggleFavorite(facilityId, userId);

            result.put("code", 200);
            result.put("msg", isFavorite ? "收藏成功" : "取消收藏");
            result.put("data", isFavorite);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "操作失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/is-favorite/{facilityId}")
    public Map<String, Object> isFavorite(@PathVariable Long facilityId, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            String userIdStr = (String) request.getAttribute("userId");
            if (userIdStr == null) {
                result.put("code", 401);
                result.put("msg", "请先登录");
                return result;
            }

            Long userId = Long.parseLong(userIdStr);
            boolean isFavorite = userFavoriteService.isFavorite(facilityId, userId);

            result.put("code", 200);
            result.put("data", isFavorite);
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
    @GetMapping("/my-favorites")
    public Map<String, Object> getMyFavorites(@RequestParam(defaultValue = "1") int page,
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
            IPage<Facility> favoritesPage = userFavoriteService.getMyFavorites(userId, page, size);

            Map<String, Object> data = new HashMap<>();
            data.put("list", favoritesPage.getRecords());
            data.put("total", favoritesPage.getTotal());
            data.put("page", page);
            data.put("size", size);
            data.put("pages", favoritesPage.getPages());

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