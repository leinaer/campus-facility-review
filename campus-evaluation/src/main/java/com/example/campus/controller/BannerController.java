package com.example.campus.controller;

import com.example.campus.common.Result;
import com.example.campus.entity.Banner;
import com.example.campus.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping("/list")
    public Result<List<Banner>> getBanners() {
        try {
            List<Banner> banners = bannerService.getActiveBanners();
            return Result.success(banners);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("获取Banner失败：" + e.getMessage());
        }
    }

    @GetMapping("/admin/list")
    public Map<String, Object> getAllBanners() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Banner> banners = bannerService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Banner>()
                            .orderByAsc("sort_order")
            );
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", banners);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/add")
    public Map<String, Object> addBanner(@RequestBody Banner banner) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (banner.getStatus() == null) {
                banner.setStatus(1);
            }
            boolean success = bannerService.save(banner);
            if (success) {
                result.put("code", 200);
                result.put("msg", "添加成功");
            } else {
                result.put("code", 500);
                result.put("msg", "添加失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "添加失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/update")
    public Map<String, Object> updateBanner(@RequestBody Banner banner) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = bannerService.updateById(banner);
            if (success) {
                result.put("code", 200);
                result.put("msg", "更新成功");
            } else {
                result.put("code", 500);
                result.put("msg", "更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "更新失败：" + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/delete/{bannerId}")
    public Map<String, Object> deleteBanner(@PathVariable Long bannerId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = bannerService.removeById(bannerId);
            if (success) {
                result.put("code", 200);
                result.put("msg", "删除成功");
            } else {
                result.put("code", 500);
                result.put("msg", "删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return result;
    }

    @PutMapping("/status/{bannerId}")
    public Map<String, Object> updateStatus(@PathVariable Long bannerId, @RequestBody Map<String, Integer> param) {
        Map<String, Object> result = new HashMap<>();
        try {
            Banner banner = bannerService.getById(bannerId);
            if (banner != null) {
                banner.setStatus(param.get("status"));
                boolean success = bannerService.updateById(banner);
                if (success) {
                    result.put("code", 200);
                    result.put("msg", "更新成功");
                } else {
                    result.put("code", 500);
                    result.put("msg", "更新失败");
                }
            } else {
                result.put("code", 404);
                result.put("msg", "Banner不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "更新失败：" + e.getMessage());
        }
        return result;
    }
}
