package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.EvaluationComment;

import java.util.List;

/**
 * 评价评论业务接口
 */
public interface EvaluationCommentService extends IService<EvaluationComment> {

    /**
     * 根据评价ID获取评论列表
     */
    List<EvaluationComment> getByEvaluationId(Long evaluationId, String sortBy);

    /**
     * 发布评论
     */
    boolean publishComment(EvaluationComment comment);

    /**
     * 删除评论（同时更新评价的评论数）
     */
    boolean deleteCommentById(Long commentId);
}
