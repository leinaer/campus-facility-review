package com.example.campus.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.campus.dto.EvaluationVO;
import com.example.campus.entity.Evaluation;
import com.example.campus.entity.EvaluationLike;
import com.example.campus.entity.User;
import com.example.campus.repository.EvaluationLikeMapper;
import com.example.campus.repository.FacilityMapper;
import com.example.campus.repository.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 评价 VO 转换工具
 */
@Component
public class EvaluationVOConverter {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FacilityMapper facilityMapper;

    @Autowired
    private EvaluationLikeMapper evaluationLikeMapper;

    /**
     * 转换单个评价为 VO（包含点赞状态和用户信息）
     */
    public EvaluationVO toVO(Evaluation evaluation, Long currentUserId) {
        if (evaluation == null) {
            return null;
        }

        EvaluationVO vo = EvaluationVO.fromEntity(evaluation);

        // 设置用户信息
        if (evaluation.getUserId() != null) {
            User user = userMapper.selectById(evaluation.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
                vo.setUserAvatar(user.getAvatarUrl());
            }
        }

        // 设置设施名称
        if (evaluation.getFacilityId() != null) {
            com.example.campus.entity.Facility facility = facilityMapper.selectById(evaluation.getFacilityId());
            if (facility != null) {
                vo.setFacilityName(facility.getName());
            }
        }

        // 设置点赞状态
        if (currentUserId != null) {
            QueryWrapper<EvaluationLike> likeQuery = new QueryWrapper<>();
            likeQuery.eq("evaluation_id", evaluation.getEvaluationId())
                    .eq("user_id", currentUserId);
            boolean liked = evaluationLikeMapper.selectCount(likeQuery) > 0;
            vo.setIsLiked(liked);
        } else {
            vo.setIsLiked(false);
        }

        return vo;
    }

    /**
     * 批量转换评价列表为 VO
     */
    public List<EvaluationVO> toVOList(List<Evaluation> evaluations, Long currentUserId) {
        if (evaluations == null || evaluations.isEmpty()) {
            return new ArrayList<>();
        }

        List<EvaluationVO> voList = new ArrayList<>();
        for (Evaluation evaluation : evaluations) {
            voList.add(toVO(evaluation, currentUserId));
        }
        return voList;
    }
}
