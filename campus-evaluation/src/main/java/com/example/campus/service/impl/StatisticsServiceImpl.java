package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campus.dto.StatisticsDTO;
import com.example.campus.entity.DailyPost;
import com.example.campus.entity.Evaluation;
import com.example.campus.entity.Facility;
import com.example.campus.entity.User;
import com.example.campus.repository.CategoryMapper;
import com.example.campus.repository.DailyPostMapper;
import com.example.campus.repository.EvaluationMapper;
import com.example.campus.repository.FacilityMapper;
import com.example.campus.repository.UserMapper;
import com.example.campus.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计业务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FacilityMapper facilityMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private DailyPostMapper dailyPostMapper;

    @Override
    public StatisticsDTO getAdminStatistics() {
        StatisticsDTO statistics = new StatisticsDTO();

        // 1. 今日新增评价数
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();

        QueryWrapper<Evaluation> todayEvalQuery = new QueryWrapper<>();
        todayEvalQuery.ge("create_time", startOfDay);
        Long todayEvals = evaluationMapper.selectCount(todayEvalQuery);
        statistics.setTodayEvaluations(todayEvals.intValue());

        // 计算今日评价增长率
        QueryWrapper<Evaluation> yesterdayEvalQuery = new QueryWrapper<>();
        yesterdayEvalQuery.ge("create_time", yesterdayStart).lt("create_time", startOfDay);
        Long yesterdayEvals = evaluationMapper.selectCount(yesterdayEvalQuery);
        if (yesterdayEvals > 0) {
            int growth = (int) Math.round(((todayEvals - yesterdayEvals) * 100.0) / yesterdayEvals);
            statistics.setTodayEvaluationsGrowth(growth);
        } else {
            statistics.setTodayEvaluationsGrowth(todayEvals > 0 ? 100 : 0);
        }

        // 2. 今日新增用户数
        QueryWrapper<User> todayUserQuery = new QueryWrapper<>();
        todayUserQuery.ge("create_time", startOfDay);
        Long todayUsers = userMapper.selectCount(todayUserQuery);
        statistics.setTodayUsers(todayUsers.intValue());

        // 计算今日用户增长率
        QueryWrapper<User> yesterdayUserQuery = new QueryWrapper<>();
        yesterdayUserQuery.ge("create_time", yesterdayStart).lt("create_time", startOfDay);
        Long yesterdayUsers = userMapper.selectCount(yesterdayUserQuery);
        if (yesterdayUsers > 0) {
            int growth = (int) Math.round(((todayUsers - yesterdayUsers) * 100.0) / yesterdayUsers);
            statistics.setTodayUsersGrowth(growth);
        } else {
            statistics.setTodayUsersGrowth(todayUsers > 0 ? 100 : 0);
        }

        // 3. 帖子总数
        QueryWrapper<DailyPost> postQuery = new QueryWrapper<>();
        postQuery.eq("status", 1);
        statistics.setTotalPosts(dailyPostMapper.selectCount(postQuery).intValue());

        // 4. 设施总数
        QueryWrapper<Facility> facilityQuery = new QueryWrapper<>();
        facilityQuery.eq("status", 1);
        statistics.setTotalFacilities(facilityMapper.selectCount(facilityQuery).intValue());

        // 在用设施数（同totalFacilities）
        statistics.setActiveFacilities(statistics.getTotalFacilities());

        // 5. 评价总数
        QueryWrapper<Evaluation> evalQuery = new QueryWrapper<>();
        evalQuery.eq("status", 1);
        statistics.setTotalEvaluations(evaluationMapper.selectCount(evalQuery).intValue());

        // 6. 用户总数
        statistics.setTotalUsers(userMapper.selectCount(null).intValue());

        // 7. 平均评分
        List<Evaluation> allEvaluations = evaluationMapper.selectList(evalQuery);
        if (!allEvaluations.isEmpty()) {
            double avgRating = allEvaluations.stream()
                    .mapToInt(Evaluation::getRating)
                    .average()
                    .orElse(0.0);
            statistics.setAvgRating(String.format("%.1f", avgRating));
        } else {
            statistics.setAvgRating("0.0");
        }

        // 8. 近7天评价趋势
        statistics.setEvaluationTrend(getEvaluationTrend(7));

        // 9. 分类统计
        statistics.setCategoryStats(getCategoryStatistics());

        // 10. 热门设施TOP10
        statistics.setTopFacilities(getTopFacilities(10));

        return statistics;
    }

    @Override
    public List<Map<String, Object>> getEvaluationTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 查询指定日期范围内的评价
        QueryWrapper<Evaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("create_time", startDate.atStartOfDay())
                .le("create_time", endDate.atTime(23, 59, 59))
                .eq("status", 1)
                .orderByAsc("create_time");

        List<Evaluation> evaluations = evaluationMapper.selectList(queryWrapper);

        // 按日期分组统计
        Map<String, Long> dateCountMap = evaluations.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCreateTime().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Collectors.counting()
                ));

        // 填充所有日期（包括没有评价的日期）
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            Map<String, Object> item = new HashMap<>();
            item.put("date", dateStr);
            item.put("count", dateCountMap.getOrDefault(dateStr, 0L));
            trend.add(item);
        }

        return trend;
    }

    @Override
    public List<Map<String, Object>> getCategoryStatistics() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查询所有设施
        List<Facility> facilities = facilityMapper.selectList(new QueryWrapper<>());

        // 按分类 ID 统计设施数量
        Map<Long, Long> categoryFacilityCountMap = facilities.stream()
                .filter(f -> f.getStatus() == 1)
                .collect(Collectors.groupingBy(
                        Facility::getCategoryId,
                        Collectors.counting()
                ));

        // 查询所有评价，并关联设施获取分类 ID
        QueryWrapper<Evaluation> evalQuery = new QueryWrapper<>();
        evalQuery.eq("status", 1);
        List<Evaluation> evaluations = evaluationMapper.selectList(evalQuery);

        // 构建设施 ID 到分类 ID 的映射
        Map<Long, Long> facilityCategoryMap = facilities.stream()
                .collect(Collectors.toMap(
                        Facility::getFacilityId,
                        Facility::getCategoryId,
                        (existing, replacement) -> existing
                ));

        // 按分类 ID 统计评价数量和平均评分
        Map<Long, List<Evaluation>> categoryEvaluationsMap = evaluations.stream()
                .filter(e -> e.getFacilityId() != null && facilityCategoryMap.containsKey(e.getFacilityId()))
                .collect(Collectors.groupingBy(
                        e -> facilityCategoryMap.get(e.getFacilityId())
                ));

        // 查询所有启用的分类
        QueryWrapper<com.example.campus.entity.Category> categoryQuery = new QueryWrapper<>();
        categoryQuery.eq("status", 1);
        List<com.example.campus.entity.Category> categories = categoryMapper.selectList(categoryQuery);

        // 组装分类统计数据
        for (com.example.campus.entity.Category category : categories) {
            Map<String, Object> item = new HashMap<>();
            Long categoryId = category.getCategoryId();

            // 设施数量
            item.put("categoryName", category.getName());
            item.put("facilityCount", categoryFacilityCountMap.getOrDefault(categoryId, 0L).intValue());

            // 评价数量和平均评分
            List<Evaluation> categoryEvals = categoryEvaluationsMap.getOrDefault(categoryId, new ArrayList<>());
            item.put("evaluationCount", categoryEvals.size());

            if (!categoryEvals.isEmpty()) {
                double avgRating = categoryEvals.stream()
                        .mapToInt(Evaluation::getRating)
                        .average()
                        .orElse(0.0);
                item.put("avgRating", Math.round(avgRating * 10.0) / 10.0);
            } else {
                item.put("avgRating", 0.0);
            }

            result.add(item);
        }

        // 按设施数量降序排序
        result.sort((a, b) -> Integer.compare(
                (Integer) b.get("facilityCount"),
                (Integer) a.get("facilityCount")
        ));

        return result;
    }



//    @Override
//    public List<Map<String, Object>> getTopFacilities(int limit) {
//        QueryWrapper<Facility> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("status", 1)
//                .gt("review_count", 0)
//                .orderByDesc("rating")
//                .last("LIMIT " + limit);
//
//        List<Facility> facilities = facilityMapper.selectList(queryWrapper);
//
//        List<Map<String, Object>> result = new ArrayList<>();
//        for (Facility facility : facilities) {
//            Map<String, Object> item = new HashMap<>();
//            item.put("facilityId", facility.getFacilityId());
//            item.put("name", facility.getName());
//            item.put("rating", facility.getRating());
//            item.put("reviewCount", facility.getReviewCount());
//            item.put("coverImage", facility.getCoverImage());
//            result.add(item);
//        }
//
//        return result;
//    }

//    @Override
//    public List<Map<String, Object>> getTopFacilities(int limit) {
//        QueryWrapper<Facility> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("status", 1)
//                .gt("review_count", 0)
//                .orderByDesc("rating")
//                .last("LIMIT " + limit);
//
//        List<Facility> facilities = facilityMapper.selectList(queryWrapper);
//
//        List<Map<String, Object>> result = new ArrayList<>();
//        int currentRank = 1;
//        for (Facility facility : facilities) {
//            Map<String, Object> item = new HashMap<>();
//            item.put("facilityId", facility.getFacilityId());
//            item.put("name", facility.getName());
//            item.put("rating", facility.getRating());
//            item.put("reviewCount", facility.getReviewCount());
//            item.put("coverImage", facility.getCoverImage());
//
//            Integer lastRank = facility.getLastRank();
//            Integer trend = (lastRank == null || lastRank == 0) ? 0 : lastRank - currentRank;
//            item.put("trend", trend);
//
//            facility.setLastRank(currentRank);
//            facilityMapper.updateById(facility);
//
//            result.add(item);
//            currentRank++;
//        }
//
//        return result;
//    }

    @Override
    public List<Map<String, Object>> getTopFacilities(int limit) {
        return getTopFacilities(limit, false);
    }

    /**
     * 获取热门设施TOP N
     * @param limit 数量
     * @param updateRank 是否更新排名（定时任务调用时为true）
     */
    public List<Map<String, Object>> getTopFacilities(int limit, boolean updateRank) {
        QueryWrapper<Facility> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                .gt("review_count", 0)
                .orderByDesc("rating")
                .last("LIMIT " + limit);

        List<Facility> facilities = facilityMapper.selectList(queryWrapper);

        // 查询所有分类，构建 ID 到名称的映射
        List<com.example.campus.entity.Category> categories = categoryMapper.selectList(null);
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(
                        com.example.campus.entity.Category::getCategoryId,
                        com.example.campus.entity.Category::getName,
                        (existing, replacement) -> existing
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        int currentRank = 1;
        for (Facility facility : facilities) {
            Map<String, Object> item = new HashMap<>();
            item.put("facilityId", facility.getFacilityId());
            item.put("name", facility.getName());
            item.put("rating", facility.getRating());
            item.put("reviewCount", facility.getReviewCount());
            item.put("coverImage", facility.getCoverImage());

            // 添加前端期望的字段
            item.put("avgRating", facility.getRating());
            item.put("evaluationCount", facility.getReviewCount());
            item.put("categoryName", categoryMap.getOrDefault(facility.getCategoryId(), "未分类"));

            Integer lastRank = facility.getLastRank();
            Integer trend = (lastRank == null || lastRank == 0) ? 0 : lastRank - currentRank;
            item.put("trend", trend);

            if (updateRank) {
                facility.setLastRank(currentRank);
                facility.setRankUpdateTime(LocalDateTime.now());
                facilityMapper.updateById(facility);
            }

            result.add(item);
            currentRank++;
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getUserTrend(int days) {
        List<Map<String, Object>> trend = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        // 查询指定日期范围内的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("create_time", startDate.atStartOfDay())
                .le("create_time", endDate.atTime(23, 59, 59))
                .orderByAsc("create_time");

        List<User> users = userMapper.selectList(queryWrapper);

        // 按日期分组统计
        Map<String, Long> dateCountMap = users.stream()
                .collect(Collectors.groupingBy(
                        u -> u.getCreateTime().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        Collectors.counting()
                ));

        // 填充所有日期（包括没有新增用户的日期）
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            String dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            Map<String, Object> item = new HashMap<>();
            item.put("date", dateStr);
            item.put("count", dateCountMap.getOrDefault(dateStr, 0L));
            trend.add(item);
        }

        return trend;
    }

    @Override
    public List<Map<String, Object>> getRatingDistribution() {
        List<Map<String, Object>> distribution = new ArrayList<>();

        // 查询所有启用的评价
        QueryWrapper<Evaluation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        List<Evaluation> evaluations = evaluationMapper.selectList(queryWrapper);

        // 统计每个评分的数量
        Map<Integer, Long> ratingCountMap = evaluations.stream()
                .collect(Collectors.groupingBy(
                        Evaluation::getRating,
                        Collectors.counting()
                ));

        // 从5星到1星构建分布数据
        String[] starNames = {"5星", "4星", "3星", "2星", "1星"};
        for (int i = 5; i >= 1; i--) {
            Map<String, Object> item = new HashMap<>();
            item.put("rating", starNames[5 - i]);
            item.put("star", i);
            item.put("count", ratingCountMap.getOrDefault(i, 0L));
            distribution.add(item);
        }

        return distribution;
    }


}
