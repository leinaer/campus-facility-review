package com.example.campus.dto;

import com.example.campus.entity.Evaluation;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价视图对象（包含前端需要的额外字段）
 */
@Data
public class EvaluationVO {

    private Long evaluationId;
    private Long facilityId;
    private Long userId;
    private Integer rating;
    private String mood;
    private String content;
    private String images;
    private String tags;
    private Integer likeCount;
    private Integer commentCount;
    private Integer helpfulCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;

    /**
     * 当前用户是否已投票（有用）
     */
    private Boolean isHelpful;

    /**
     * 用户昵称（关联查询）
     */
    private String userNickname;

    /**
     * 用户头像（关联查询）
     */
    private String userAvatar;

    /**
     * 设施名称（关联查询）
     */
    private String facilityName;

    /**
     * 从 Evaluation 实体转换
     */
    public static EvaluationVO fromEntity(Evaluation evaluation) {
        if (evaluation == null) {
            return null;
        }
        EvaluationVO vo = new EvaluationVO();
        vo.setEvaluationId(evaluation.getEvaluationId());
        vo.setFacilityId(evaluation.getFacilityId());
        vo.setUserId(evaluation.getUserId());
        vo.setRating(evaluation.getRating());
        vo.setMood(evaluation.getMood());
        vo.setContent(evaluation.getContent());
        vo.setImages(evaluation.getImages());
        vo.setTags(evaluation.getTags());
        vo.setLikeCount(evaluation.getLikeCount());
        vo.setCommentCount(evaluation.getCommentCount());
        vo.setHelpfulCount(evaluation.getHelpfulCount());
        vo.setStatus(evaluation.getStatus());
        vo.setCreateTime(evaluation.getCreateTime());
        vo.setUpdateTime(evaluation.getUpdateTime());
        return vo;
    }
}
