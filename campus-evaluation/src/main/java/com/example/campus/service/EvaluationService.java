package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.Evaluation;

import java.util.List;
import java.util.Map;

/**
 * 评价业务接口
 */
public interface EvaluationService extends IService<Evaluation> {
    
    /**
     * 根据设施ID获取评价列表（支持筛选排序）
     * @param facilityId 设施ID
     * @param sortBy 排序方式：latest-最新, high-高分, low-低分, withImage-有图
     * @param page 页码
     * @param size 每页数量
     */
    Map<String, Object> getByFacilityId(Long facilityId, String sortBy, int page, int size);
    
    /**
     * 发布评价
     */
    boolean publishEvaluation(Evaluation evaluation);
    
    /**
     * 获取用户的评价列表
     */
    List<Evaluation> getByUserId(Long userId);
    
    /**
     * 获取最新评价动态（类似朋友圈）
     * @param limit 数量限制
     * @param filterType 筛选类型：all-全部, featured-精选, withImage-有图, highRating-高分
     */
    List<Evaluation> getLatestEvaluations(int limit, String filterType);
    /**
     * 删除评价（只能删除自己的）
     * @param evaluationId 评价ID
     * @param userId 用户ID
     * @return true-删除成功，false-无权删除或不存在
     */
    boolean deleteEvaluation(Long evaluationId, Long userId);
}
