package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.EvaluationHelpful;

/**
 * 评价有用性投票业务接口
 */
public interface EvaluationHelpfulService extends IService<EvaluationHelpful> {

    /**
     * 投票（有用/无用）
     * @param evaluationId 评价ID
     * @param userId 用户ID
     * @param voteType 投票类型：1-有用，0-无用
     * @return true-投票成功
     */
    boolean vote(Long evaluationId, Long userId, Integer voteType);

    /**
     * 检查用户是否已投票
     */
    boolean hasVoted(Long evaluationId, Long userId);
}