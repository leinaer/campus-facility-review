package com.example.campus.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.Evaluation;
import com.example.campus.entity.EvaluationLike;
import com.example.campus.repository.EvaluationLikeMapper;
import com.example.campus.repository.EvaluationMapper;
import com.example.campus.service.EvaluationLikeService;
import com.example.campus.service.UserNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 评价点赞业务实现类
 */
@Service
public class EvaluationLikeServiceImpl extends ServiceImpl<EvaluationLikeMapper, EvaluationLike> implements EvaluationLikeService {

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Autowired
    private UserNotificationService notificationService;

    @Override
    @Transactional
    public boolean toggleLike(Long evaluationId, Long userId) {
        QueryWrapper<EvaluationLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("evaluation_id", evaluationId)
                   .eq("user_id", userId);
        EvaluationLike existingLike = this.getOne(queryWrapper);

        Evaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation == null) {
            return false;
        }

        if (existingLike != null) {
            this.removeById(existingLike.getLikeId());

            evaluation.setLikeCount(Math.max(0, evaluation.getLikeCount() - 1));
            evaluationMapper.updateById(evaluation);

            return false;
        } else {
            EvaluationLike newLike = new EvaluationLike();
            newLike.setEvaluationId(evaluationId);
            newLike.setUserId(userId);
            newLike.setCreateTime(LocalDateTime.now());
            this.save(newLike);

            evaluation.setLikeCount(evaluation.getLikeCount() + 1);
            evaluationMapper.updateById(evaluation);

            sendLikeNotification(evaluationId, userId);

            return true;
        }
    }

    @Override
    public boolean isLiked(Long evaluationId, Long userId) {
        QueryWrapper<EvaluationLike> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("evaluation_id", evaluationId)
                   .eq("user_id", userId);
        return this.count(queryWrapper) > 0;
    }

    private void sendLikeNotification(Long evaluationId, Long fromUserId) {
        try {
            Evaluation evaluation = evaluationMapper.selectById(evaluationId);
            if (evaluation == null || evaluation.getUserId().equals(fromUserId)) {
                return;
            }

            notificationService.createNotification(
                    evaluation.getUserId(),
                    "LIKE",
                    "有人赞了你的评价",
                    "用户赞了你在设施的评价",
                    fromUserId,
                    evaluationId,
                    "EVALUATION"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Evaluation getEvaluationById(Long evaluationId) {
        return evaluationMapper.selectById(evaluationId);
    }


}
