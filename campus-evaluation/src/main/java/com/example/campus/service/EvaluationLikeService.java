package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.Evaluation;
import com.example.campus.entity.EvaluationLike;

/**
 * 评价点赞业务接口
 */
public interface EvaluationLikeService extends IService<EvaluationLike> {

    /**
     * 切换点赞状态
     */
    boolean toggleLike(Long evaluationId, Long userId);

    /**
     * 检查是否已点赞
     */
    boolean isLiked(Long evaluationId, Long userId);

    /**
     * 获取评价信息
     */
    Evaluation getEvaluationById(Long evaluationId);
}
