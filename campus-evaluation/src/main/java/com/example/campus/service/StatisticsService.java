package com.example.campus.service;

import com.example.campus.dto.StatisticsDTO;

/**
 * 统计业务接口
 */
public interface StatisticsService {

    /**
     * 获取管理员统计数据
     */
    StatisticsDTO getAdminStatistics();

    /**
     * 获取近N天的评价趋势
     * @param days 天数
     */
    java.util.List<java.util.Map<String, Object>> getEvaluationTrend(int days);

    /**
     * 获取分类统计
     */
    java.util.List<java.util.Map<String, Object>> getCategoryStatistics();

    /**
     * 获取热门设施TOP N
     * @param limit 数量
     */
    java.util.List<java.util.Map<String, Object>> getTopFacilities(int limit);

    /**
     * 获取近N天的用户增长趋势
     * @param days 天数
     */
    java.util.List<java.util.Map<String, Object>> getUserTrend(int days);

    /**
     * 获取评分分布（5星到1星）
     */
    java.util.List<java.util.Map<String, Object>> getRatingDistribution();

}
