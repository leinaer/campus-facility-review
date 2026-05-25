package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.FacilityReply;

import java.util.List;

/**
 * 设施官方回复业务接口
 */
public interface FacilityReplyService extends IService<FacilityReply> {

    /**
     * 回复评价
     */
    boolean replyEvaluation(Long evaluationId, Long adminId, String content);

    /**
     * 获取评价的回复列表
     */
    List<FacilityReply> getRepliesByEvaluationId(Long evaluationId);
}