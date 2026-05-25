package com.example.campus.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 统计数据DTO
 */
@Data
public class StatisticsDTO implements Serializable {

    /**
     * 帖子总数
     */
    private Integer totalPosts;

    /**
     * 今日新增评价数
     */
    private Integer todayEvaluations;

    /**
     * 今日新增用户数
     */
    private Integer todayUsers;

    /**
     * 设施总数
     */
    private Integer totalFacilities;

    /**
     * 在用设施数
     */
    private Integer activeFacilities;

    /**
     * 评价总数
     */
    private Integer totalEvaluations;

    /**
     * 用户总数
     */
    private Integer totalUsers;

    /**
     * 平均评分
     */
    private String avgRating;

    /**
     * 今日评价增长率（百分比）
     */
    private Integer todayEvaluationsGrowth;

    /**
     * 今日用户增长率（百分比）
     */
    private Integer todayUsersGrowth;

    /**
     * 近7天评价趋势（日期 -> 数量）
     */
    private List<Map<String, Object>> evaluationTrend;

    /**
     * 分类统计（分类名 -> 设施数量）
     */
    private List<Map<String, Object>> categoryStats;

    /**
     * 热门设施TOP10
     */
    private List<Map<String, Object>> topFacilities;
}
