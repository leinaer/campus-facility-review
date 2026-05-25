package com.example.campus.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.Facility;

import java.util.List;
import java.util.Map;

/**
 * 设施业务接口
 */
public interface FacilityService extends IService<Facility> {
    
    /**
     * 根据分类ID获取设施列表
     */
    List<Facility> getByCategoryId(Long categoryId);
    
    /**
     * 搜索设施（支持名称、标签模糊搜索）
     */
//    List<Facility> searchFacilities(String keyword);
    Map<String, Object> searchFacilities(String keyword, int page, int size);

    /**
     * 获取推荐设施（好评榜/吐槽榜）
     * @param type: "high" - 好评榜, "low" - 吐槽榜
     */
    List<Facility> getRecommendedFacilities(String type, int limit);
    
    /**
     * 获取最新设施列表（分页）
     */
    Map<String, Object> getLatestFacilities(int page, int size);

    /**
     * 获取热门搜索词（基于设施热度）
     */
    List<String> getHotSearchKeywords(int limit);

}
