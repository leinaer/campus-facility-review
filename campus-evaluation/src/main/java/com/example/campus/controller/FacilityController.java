//package com.example.campus.controller;
//
//import com.example.campus.annotation.OperationLog;
//import com.example.campus.entity.Facility;
//import com.example.campus.exception.BusinessException;
//import com.example.campus.service.FacilityService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 设施控制器
// */
//@RestController
//@RequestMapping("/api/facility")
//public class FacilityController {
//
//    @Autowired
//    private FacilityService facilityService;
//
//    @GetMapping("/{id}")
//    public Map<String, Object> getFacilityDetail(@PathVariable Long id) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            Facility facility = facilityService.getById(id);
//            if (facility != null && facility.getStatus() == 1) {
//                result.put("code", 200);
//                result.put("msg", "获取成功");
//                result.put("data", facility);
//            } else {
//                result.put("code", 404);
//                result.put("msg", "设施不存在或已下架");
//            }
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "获取失败：" + e.getMessage());
//        }
//        return result;
//    }
//
////    @GetMapping("/by-category/{categoryId}")
////    public Map<String, Object> getByCategory(@PathVariable Long categoryId) {
////        Map<String, Object> result = new HashMap<>();
////        try {
////            List<Facility> facilities = facilityService.getByCategoryId(categoryId);
////            result.put("code", 200);
////            result.put("msg", "获取成功");
////            result.put("data", facilities);
////        } catch (BusinessException e) {
////            throw e;
////        } catch (Exception e) {
////            e.printStackTrace();
////            result.put("code", 500);
////            result.put("msg", "获取失败：" + e.getMessage());
////        }
////        return result;
////    }
//
////    @GetMapping("/search")
////    public Map<String, Object> searchFacilities(@RequestParam String keyword) {
////        Map<String, Object> result = new HashMap<>();
////        try {
////            List<Facility> facilities = facilityService.searchFacilities(keyword);
////            result.put("code", 200);
////            result.put("msg", "搜索成功");
////            result.put("data", facilities);
////        } catch (BusinessException e) {
////            throw e;
////        } catch (Exception e) {
////            e.printStackTrace();
////            result.put("code", 500);
////            result.put("msg", "搜索失败：" + e.getMessage());
////        }
////        return result;
////    }
//    @GetMapping("/search")
//    public Map<String, Object> searchFacilities(@RequestParam String keyword,
//                                                @RequestParam(defaultValue = "1") int page,
//                                                @RequestParam(defaultValue = "10") int size) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            Map<String, Object> data = facilityService.searchFacilities(keyword, page, size);
//            result.put("code", 200);
//            result.put("msg", "搜索成功");
//            result.put("data", data);
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "搜索失败：" + e.getMessage());
//        }
//        return result;
//    }
//
//    @GetMapping("/recommended")
//    public Map<String, Object> getRecommended(@RequestParam(defaultValue = "high") String type,
//                                              @RequestParam(defaultValue = "10") int limit) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            List<Facility> facilities = facilityService.getRecommendedFacilities(type, limit);
//            result.put("code", 200);
//            result.put("msg", "获取成功");
//            result.put("data", facilities);
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "获取失败：" + e.getMessage());
//        }
//        return result;
//    }
//
//    @GetMapping("/latest")
//    public Map<String, Object> getLatest(@RequestParam(defaultValue = "1") int page,
//                                         @RequestParam(defaultValue = "10") int size) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            Map<String, Object> data = facilityService.getLatestFacilities(page, size);
//            result.put("code", 200);
//            result.put("msg", "获取成功");
//            result.put("data", data);
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "获取失败：" + e.getMessage());
//        }
//        return result;
//    }
//
//    @GetMapping("/hot-search")
//    public Map<String, Object> getHotSearchKeywords(@RequestParam(defaultValue = "10") int limit) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            List<String> keywords = facilityService.getHotSearchKeywords(limit);
//            result.put("code", 200);
//            result.put("msg", "获取成功");
//            result.put("data", keywords);
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "获取失败：" + e.getMessage());
//        }
//        return result;
//    }
//
//    @OperationLog(module = "设施管理", type = "ADD", description = "新增设施")
//    @PostMapping("/add")
//    public Map<String, Object> addFacility(@RequestBody Facility facility) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            facility.setStatus(1);
//            boolean success = facilityService.save(facility);
//            if (success) {
//                result.put("code", 200);
//                result.put("msg", "添加成功");
//            } else {
//                result.put("code", 500);
//                result.put("msg", "添加失败");
//            }
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "添加失败：" + e.getMessage());
//        }
//        return result;
//    }
//
//    @OperationLog(module = "设施管理", type = "UPDATE", description = "编辑设施")
//    @PutMapping("/update")
//    public Map<String, Object> updateFacility(@RequestBody Facility facility) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            boolean success = facilityService.updateById(facility);
//            if (success) {
//                result.put("code", 200);
//                result.put("msg", "更新成功");
//            } else {
//                result.put("code", 500);
//                result.put("msg", "更新失败");
//            }
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "更新失败：" + e.getMessage());
//        }
//        return result;
//    }
//
//    @OperationLog(module = "设施管理", type = "OFFLINE", description = "下架设施")
//    @PutMapping("/offline/{id}")
//    public Map<String, Object> offlineFacility(@PathVariable Long id) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            Facility facility = facilityService.getById(id);
//            if (facility != null) {
//                facility.setStatus(0);
//                boolean success = facilityService.updateById(facility);
//                if (success) {
//                    result.put("code", 200);
//                    result.put("msg", "下架成功");
//                } else {
//                    result.put("code", 500);
//                    result.put("msg", "下架失败");
//                }
//            } else {
//                result.put("code", 404);
//                result.put("msg", "设施不存在");
//            }
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "下架失败：" + e.getMessage());
//        }
//        return result;
//    }
//
//    /**
//     * 获取所有设施列表（管理端使用）
//     */
//    @GetMapping("/admin/list")
//    public Map<String, Object> getAllFacilities() {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            List<Facility> facilities = facilityService.list();
//            result.put("code", 200);
//            result.put("msg", "获取成功");
//            result.put("data", facilities);
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "获取失败：" + e.getMessage());
//        }
//        return result;
//    }
//
//    /**
//     * 根据分类ID获取设施列表
//     */
//    @GetMapping("/by-category/{categoryId}")
//    public Map<String, Object> getFacilitiesByCategory(@PathVariable Long categoryId) {
//        Map<String, Object> result = new HashMap<>();
//        try {
//            List<Facility> facilities = facilityService.getByCategoryId(categoryId);
//            result.put("code", 200);
//            result.put("msg", "获取成功");
//            result.put("data", facilities);
//        } catch (BusinessException e) {
//            throw e;
//        } catch (Exception e) {
//            e.printStackTrace();
//            result.put("code", 500);
//            result.put("msg", "获取失败：" + e.getMessage());
//        }
//        return result;
//    }
//}


package com.example.campus.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campus.annotation.OperationLog;
import com.example.campus.entity.Category;
import com.example.campus.entity.Facility;
import com.example.campus.exception.BusinessException;
import com.example.campus.service.CategoryService;
import com.example.campus.service.FacilityService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/facility")
public class FacilityController {

    @Autowired
    private FacilityService facilityService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/{id}")
    public Map<String, Object> getFacilityDetail(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Facility facility = facilityService.getById(id);
            if (facility != null && facility.getStatus() == 1) {
                result.put("code", 200);
                result.put("msg", "获取成功");
                result.put("data", facility);
            } else {
                result.put("code", 404);
                result.put("msg", "设施不存在或已下架");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/search")
    public Map<String, Object> searchFacilities(@RequestParam String keyword,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = facilityService.searchFacilities(keyword, page, size);
            result.put("code", 200);
            result.put("msg", "搜索成功");
            result.put("data", data);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "搜索失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/recommended")
    public Map<String, Object> getRecommended(@RequestParam(defaultValue = "high") String type,
                                              @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Facility> facilities = facilityService.getRecommendedFacilities(type, limit);
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", facilities);
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
    public Map<String, Object> getLatest(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size) {
        Map<String, Object> result = new HashMap<>();
        try {
            Map<String, Object> data = facilityService.getLatestFacilities(page, size);
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

    @GetMapping("/hot-search")
    public Map<String, Object> getHotSearchKeywords(@RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<String> keywords = facilityService.getHotSearchKeywords(limit);
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", keywords);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    @OperationLog(module = "设施管理", type = "ADD", description = "新增设施")
    @PostMapping("/add")
    public Map<String, Object> addFacility(@RequestBody Facility facility) {
        Map<String, Object> result = new HashMap<>();
        try {
            facility.setStatus(1);
            boolean success = facilityService.save(facility);
            if (success) {
                result.put("code", 200);
                result.put("msg", "添加成功");
            } else {
                result.put("code", 500);
                result.put("msg", "添加失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "添加失败：" + e.getMessage());
        }
        return result;
    }

    @OperationLog(module = "设施管理", type = "UPDATE", description = "编辑设施")
    @PutMapping("/update")
    public Map<String, Object> updateFacility(@RequestBody Facility facility) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = facilityService.updateById(facility);
            if (success) {
                result.put("code", 200);
                result.put("msg", "更新成功");
            } else {
                result.put("code", 500);
                result.put("msg", "更新失败");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "更新失败：" + e.getMessage());
        }
        return result;
    }

    @OperationLog(module = "设施管理", type = "OFFLINE", description = "下架设施")
    @PutMapping("/offline/{id}")
    public Map<String, Object> offlineFacility(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Facility facility = facilityService.getById(id);
            if (facility != null) {
                facility.setStatus(0);
                boolean success = facilityService.updateById(facility);
                if (success) {
                    result.put("code", 200);
                    result.put("msg", "下架成功");
                } else {
                    result.put("code", 500);
                    result.put("msg", "下架失败");
                }
            } else {
                result.put("code", 404);
                result.put("msg", "设施不存在");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "下架失败：" + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteFacility(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = facilityService.removeById(id);
            if (success) {
                result.put("code", 200);
                result.put("msg", "删除成功");
            } else {
                result.put("code", 500);
                result.put("msg", "删除失败");
            }
        } catch (Exception e) {
            log.error("删除设施失败", e);
            result.put("code", 500);
            result.put("msg", "删除失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/admin/list")
    public Map<String, Object> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String campus,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("=== 获取设施列表 === 用户ID: {}, 角色: {}",
                     request.getAttribute("userId"),
                     request.getAttribute("role"));

            Page<Facility> facilityPage = new Page<>(page, size);
            LambdaQueryWrapper<Facility> wrapper = new LambdaQueryWrapper<>();

            if (keyword != null && !keyword.trim().isEmpty()) {
                wrapper.like(Facility::getName, keyword)
                       .or()
                       .like(Facility::getLocation, keyword);
            }

            if (categoryId != null) {
                wrapper.eq(Facility::getCategoryId, categoryId);
            }

            if (campus != null && !campus.trim().isEmpty()) {
                wrapper.eq(Facility::getCampus, campus);
            }

            if (status != null) {
                wrapper.eq(Facility::getStatus, status);
            }

            wrapper.orderByDesc(Facility::getCreateTime);
            Page<Facility> pageResult = facilityService.page(facilityPage, wrapper);

            List<Category> categories = categoryService.list();
            List<Map<String, Object>> records = pageResult.getRecords().stream().map(facility -> {
                Map<String, Object> map = new HashMap<>();
                map.put("facilityId", facility.getFacilityId());
                map.put("name", facility.getName());
                map.put("categoryId", facility.getCategoryId());
                map.put("coverImage", facility.getCoverImage());
                map.put("location", facility.getLocation());
                map.put("rating", facility.getRating());
                map.put("reviewCount", facility.getReviewCount());
                map.put("campus", facility.getCampus());
                map.put("status", facility.getStatus());
                map.put("createTime", facility.getCreateTime());

                Category category = categories.stream()
                        .filter(c -> c.getCategoryId().equals(facility.getCategoryId()))
                        .findFirst().orElse(null);
                map.put("categoryName", category != null ? category.getName() : "未分类");

                return map;
            }).toList();

            Map<String, Object> data = new HashMap<>();
            data.put("list", records);
            data.put("total", pageResult.getTotal());
            data.put("page", pageResult.getCurrent());
            data.put("size", pageResult.getSize());

            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", data);
        } catch (Exception e) {
            log.error("获取设施列表失败", e);
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
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
                result.put("msg", "请选择要删除的设施");
                return result;
            }
            facilityService.removeByIds(ids);
            result.put("code", 200);
            result.put("msg", "批量删除成功");
        } catch (Exception e) {
            log.error("批量删除失败", e);
            result.put("code", 500);
            result.put("msg", "批量删除失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/admin/batch-offline")
    public Map<String, Object> adminBatchOffline(@RequestBody Map<String, List<Long>> param) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Long> ids = param.get("ids");
            if (ids == null || ids.isEmpty()) {
                result.put("code", 400);
                result.put("msg", "请选择要下架的设施");
                return result;
            }

            List<Facility> facilities = facilityService.listByIds(ids);
            facilities.forEach(facility -> facility.setStatus(0));
            facilityService.updateBatchById(facilities);

            result.put("code", 200);
            result.put("msg", "批量下架成功");
        } catch (Exception e) {
            log.error("批量下架失败", e);
            result.put("code", 500);
            result.put("msg", "批量下架失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/by-category/{categoryId}")
    public Map<String, Object> getFacilitiesByCategory(@PathVariable Long categoryId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Facility> facilities = facilityService.getByCategoryId(categoryId);
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", facilities);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }
}
