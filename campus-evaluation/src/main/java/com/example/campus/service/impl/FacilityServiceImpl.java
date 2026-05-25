package com.example.campus.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.constant.CacheConstant;
import com.example.campus.entity.Facility;
import com.example.campus.repository.FacilityMapper;
import com.example.campus.service.FacilityService;
import com.example.campus.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 设施业务实现类
 */
@Service
public class FacilityServiceImpl extends ServiceImpl<FacilityMapper, Facility> implements FacilityService {

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<Facility> getByCategoryId(Long categoryId) {
        QueryWrapper<Facility> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", categoryId)
                .eq("status", 1)
                .orderByDesc("rating");
        return this.list(queryWrapper);
    }

//    @Override
//    public List<Facility> searchFacilities(String keyword) {
//        QueryWrapper<Facility> queryWrapper = new QueryWrapper<>();
//        queryWrapper.like("name", keyword)
//                .eq("status", 1)
//                .orderByDesc("rating");
//        return this.list(queryWrapper);
//    }
@Override
public Map<String, Object> searchFacilities(String keyword, int page, int size) {
    Page<Facility> facilityPage = new Page<>(page, size);
    QueryWrapper<Facility> queryWrapper = new QueryWrapper<>();

    if (keyword != null && !keyword.trim().isEmpty()) {
        String trimKeyword = keyword.trim();
        queryWrapper.and(wrapper -> wrapper
                .like("name", trimKeyword)
                .or()
                .like("location", trimKeyword)
        );
    }

    queryWrapper.eq("status", 1)
            .orderByDesc("rating");

    Page<Facility> resultPage = this.page(facilityPage, queryWrapper);

    Map<String, Object> result = new HashMap<>();
    result.put("list", resultPage.getRecords());
    result.put("total", resultPage.getTotal());
    result.put("page", page);
    result.put("size", size);
    result.put("pages", resultPage.getPages());

    return result;
}

    @Override
    public List<Facility> getRecommendedFacilities(String type, int limit) {
        String cacheKey = String.format(CacheConstant.FACILITY_TOP, type, limit);

        if (redisUtil.hasKey(cacheKey)) {
            return (List<Facility>) redisUtil.get(cacheKey);
        }

        QueryWrapper<Facility> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);

        if ("high".equals(type)) {
            queryWrapper.orderByDesc("rating")
                    .last("LIMIT " + limit);
        } else if ("low".equals(type)) {
            queryWrapper.gt("review_count", 0)
                    .orderByAsc("rating")
                    .last("LIMIT " + limit);
        }

        List<Facility> facilities = this.list(queryWrapper);

        redisUtil.set(cacheKey, facilities, CacheConstant.FACILITY_TOP_EXPIRE, TimeUnit.SECONDS);

        return facilities;
    }

    @Override
    public Map<String, Object> getLatestFacilities(int page, int size) {
        Page<Facility> facilityPage = new Page<>(page, size);
        QueryWrapper<Facility> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                .orderByDesc("create_time");

        Page<Facility> resultPage = this.page(facilityPage, queryWrapper);

        Map<String, Object> result = new HashMap<>();
        result.put("list", resultPage.getRecords());
        result.put("total", resultPage.getTotal());
        result.put("page", page);
        result.put("size", size);
        result.put("pages", resultPage.getPages());

        return result;
    }

    @Override
    public Facility getById(java.io.Serializable id) {
        String cacheKey = String.format(CacheConstant.FACILITY_DETAIL, id);

        if (redisUtil.hasKey(cacheKey)) {
            return (Facility) redisUtil.get(cacheKey);
        }

        Facility facility = super.getById(id);

        if (facility != null && facility.getStatus() == 1) {
            redisUtil.set(cacheKey, facility, CacheConstant.FACILITY_DETAIL_EXPIRE, TimeUnit.SECONDS);
        }

        return facility;
    }

    @Override
    public boolean save(Facility entity) {
        boolean result = super.save(entity);
        if (result) {
            clearFacilityCache();
        }
        return result;
    }

    @Override
    public boolean updateById(Facility entity) {
        boolean result = super.updateById(entity);
        if (result) {
            clearFacilityCache();
            String cacheKey = String.format(CacheConstant.FACILITY_DETAIL, entity.getFacilityId());
            redisUtil.delete(cacheKey);
        }
        return result;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            clearFacilityCache();
            String cacheKey = String.format(CacheConstant.FACILITY_DETAIL, id);
            redisUtil.delete(cacheKey);
        }
        return result;
    }

//    private void clearFacilityCache() {
//        redisUtil.delete(CacheConstant.CATEGORY_LIST);
//    }
    private void clearFacilityCache() {
        redisUtil.delete(CacheConstant.CATEGORY_LIST);
        redisUtil.delete(String.format(CacheConstant.FACILITY_TOP, "high", 10));
        redisUtil.delete(String.format(CacheConstant.FACILITY_TOP, "low", 10));
        redisUtil.delete(String.format(CacheConstant.FACILITY_TOP, "high", 20));
        redisUtil.delete(String.format(CacheConstant.FACILITY_TOP, "low", 20));
        redisUtil.delete(String.format(CacheConstant.FACILITY_TOP, "high", 50));
        redisUtil.delete(String.format(CacheConstant.FACILITY_TOP, "low", 50));
    }

    @Override
    public List<String> getHotSearchKeywords(int limit) {
        String cacheKey = CacheConstant.HOT_SEARCH_KEYWORDS;

        if (redisUtil.hasKey(cacheKey)) {
            return (List<String>) redisUtil.get(cacheKey);
        }

        QueryWrapper<Facility> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1)
                .orderByDesc("review_count")
                .last("LIMIT " + limit);

        List<Facility> hotFacilities = this.list(queryWrapper);
        List<String> keywords = hotFacilities.stream()
                .map(Facility::getName)
                .collect(java.util.stream.Collectors.toList());

        redisUtil.set(cacheKey, keywords, CacheConstant.HOT_SEARCH_KEYWORDS_EXPIRE, TimeUnit.SECONDS);

        return keywords;
    }
}
