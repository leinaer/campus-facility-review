package com.example.campus.controller;

import com.alibaba.excel.EasyExcel;
import com.example.campus.dto.CategoryStatsExcelDTO;
import com.example.campus.dto.StatisticsDTO;
import com.example.campus.service.StatisticsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计数据控制器
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取管理员统计数据（需要管理员权限）
     */
    @GetMapping("/admin/overview")
    public Map<String, Object> getAdminOverview() {
        Map<String, Object> result = new HashMap<>();
        try {
            StatisticsDTO statistics = statisticsService.getAdminStatistics();
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", statistics);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取评价趋势（需要管理员权限）
     * @param days 天数，默认7天
     */
    @GetMapping("/admin/evaluation-trend")
    public Map<String, Object> getEvaluationTrend(@RequestParam(defaultValue = "7") int days) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (days > 30) {
                days = 30; // 最多查询30天
            }
            List<Map<String, Object>> trend = statisticsService.getEvaluationTrend(days);
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", trend);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取分类统计（需要管理员权限）
     */
    @GetMapping("/admin/category-stats")
    public Map<String, Object> getCategoryStats() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> stats = statisticsService.getCategoryStatistics();
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", stats);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取热门设施排行
     * @param limit 数量，默认10
     */
    @GetMapping("/top-facilities")
    public Map<String, Object> getTopFacilities(@RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (limit > 50) {
                limit = 50; // 最多50个
            }
            List<Map<String, Object>> facilities = statisticsService.getTopFacilities(limit);
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", facilities);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取用户增长趋势（需要管理员权限）
     * @param days 天数，默认7天
     */
    @GetMapping("/admin/user-trend")
    public Map<String, Object> getUserTrend(@RequestParam(defaultValue = "7") int days) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (days > 30) {
                days = 30;
            }
            List<Map<String, Object>> trend = statisticsService.getUserTrend(days);
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", trend);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 获取评分分布（需要管理员权限）
     */
    @GetMapping("/admin/rating-distribution")
    public Map<String, Object> getRatingDistribution() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> distribution = statisticsService.getRatingDistribution();
            result.put("code", 200);
            result.put("msg", "获取成功");
            result.put("data", distribution);
        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 500);
            result.put("msg", "获取失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 导出分类统计报表为 Excel
     */
    @GetMapping("/admin/export-category-stats")
    public void exportCategoryStats(HttpServletResponse response) {
        try {
            List<Map<String, Object>> stats = statisticsService.getCategoryStatistics();
            
            List<CategoryStatsExcelDTO> excelData = stats.stream().map(item -> {
                CategoryStatsExcelDTO dto = new CategoryStatsExcelDTO();
                dto.setCategoryName((String) item.get("categoryName"));
                dto.setFacilityCount(convertToInteger(item.get("facilityCount")));
                dto.setEvaluationCount(convertToInteger(item.get("evaluationCount")));
                dto.setAvgRating(convertToDouble(item.get("avgRating")));
                
                // 计算占比（如果需要的话，这里暂时设为0，因为需要总数才能计算）
                dto.setPercent(0.0);
                
                return dto;
            }).collect(Collectors.toList());
            
            // 计算占比
            int totalFacilities = excelData.stream()
                    .mapToInt(CategoryStatsExcelDTO::getFacilityCount)
                    .sum();
            
            if (totalFacilities > 0) {
                for (CategoryStatsExcelDTO dto : excelData) {
                    double percent = (dto.getFacilityCount() * 100.0) / totalFacilities;
                    dto.setPercent(Math.round(percent * 10.0) / 10.0); // 保留一位小数
                }
            }
            
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            
            String fileName = URLEncoder.encode("分类统计报表", StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", 
                "attachment;filename*=utf-8''" + fileName + ".xlsx");
            
            EasyExcel.write(response.getOutputStream(), CategoryStatsExcelDTO.class)
                .sheet("分类统计")
                .doWrite(excelData);
                
        } catch (IOException e) {
            e.printStackTrace();
            try {
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.reset();
                response.setContentType("application/json");
                response.setCharacterEncoding("utf-8");
                response.getWriter().write("{\"code\":500,\"msg\":\"导出失败：" + e.getMessage() + "\"}");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 安全转换为 Integer
     */
    private Integer convertToInteger(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.parseInt((String) obj);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * 安全转换为 Double
     */
    private Double convertToDouble(Object obj) {
        if (obj == null) {
            return 0.0;
        }
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}
