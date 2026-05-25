package com.example.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.Evaluation;
import com.example.campus.entity.EvaluationHelpful;
import com.example.campus.repository.EvaluationHelpfulMapper;
import com.example.campus.repository.EvaluationMapper;
import com.example.campus.service.EvaluationHelpfulService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 评价有用性投票业务实现类
 */
@Service
public class EvaluationHelpfulServiceImpl extends ServiceImpl<EvaluationHelpfulMapper, EvaluationHelpful> implements EvaluationHelpfulService {

    @Autowired
    private EvaluationMapper evaluationMapper;

    @Override
    @Transactional
    public boolean vote(Long evaluationId, Long userId, Integer voteType) {
        QueryWrapper<EvaluationHelpful> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("evaluation_id", evaluationId).eq("user_id", userId);
        EvaluationHelpful existingVote = this.getOne(queryWrapper);

        if (existingVote != null) {
            if (existingVote.getVoteType().equals(voteType)) {
                return false;
            }
            existingVote.setVoteType(voteType);
            existingVote.setCreateTime(LocalDateTime.now());
            this.updateById(existingVote);
        } else {
            EvaluationHelpful newVote = new EvaluationHelpful();
            newVote.setEvaluationId(evaluationId);
            newVote.setUserId(userId);
            newVote.setVoteType(voteType);
            newVote.setCreateTime(LocalDateTime.now());
            this.save(newVote);
        }

        updateHelpfulCount(evaluationId);
        return true;
    }

    @Override
    public boolean hasVoted(Long evaluationId, Long userId) {
        QueryWrapper<EvaluationHelpful> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("evaluation_id", evaluationId).eq("user_id", userId);
        return this.count(queryWrapper) > 0;
    }

    private void updateHelpfulCount(Long evaluationId) {
        QueryWrapper<EvaluationHelpful> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("evaluation_id", evaluationId).eq("vote_type", 1);
        long count = this.count(queryWrapper);

        Evaluation evaluation = evaluationMapper.selectById(evaluationId);
        if (evaluation != null) {
            evaluation.setHelpfulCount((int) count);
            evaluationMapper.updateById(evaluation);
        }
    }
}