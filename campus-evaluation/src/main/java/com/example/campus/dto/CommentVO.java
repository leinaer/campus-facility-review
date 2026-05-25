package com.example.campus.dto;

import com.example.campus.entity.EvaluationComment;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 评论视图对象
 */
@Data
public class CommentVO {

    private Long commentId;
    private Long evaluationId;
    private Long userId;
    private Long parentId;
    private Long replyToUserId;
    private String content;
    private String images;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联信息
    private String userNickname;
    private String userAvatar;
    private String replyToUserNickname;

    // 前端展示字段
    private String relativeTime;
    private Boolean isLiked;
    private Integer userVoteType;
    private Integer replyCount;
    private java.util.List<CommentVO> replies;

    /**
     * 计算相对时间
     */
    public void calculateRelativeTime() {
        if (this.createTime == null) {
            this.relativeTime = "";
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long days = java.time.Duration.between(this.createTime, now).toDays();
        long hours = java.time.Duration.between(this.createTime, now).toHours();
        long minutes = java.time.Duration.between(this.createTime, now).toMinutes();

        if (minutes < 1) {
            this.relativeTime = "刚刚";
        } else if (minutes < 60) {
            this.relativeTime = minutes + "分钟前";
        } else if (hours < 24 && days < 3) {
            this.relativeTime = hours + "小时前";
        } else if (days < 3) {
            this.relativeTime = days + "天前";
        } else {
            java.time.format.DateTimeFormatter formatter;
            if (this.createTime.getYear() == LocalDate.now().getYear()) {
                formatter = java.time.format.DateTimeFormatter.ofPattern("M月d日");
            } else {
                formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy年M月d日");
            }
            this.relativeTime = this.createTime.format(formatter);
        }
    }

    /**
     * 从实体转换
     */
    public static CommentVO fromEntity(EvaluationComment comment) {
        if (comment == null) {
            return null;
        }
        CommentVO vo = new CommentVO();
        vo.setCommentId(comment.getCommentId());
        vo.setEvaluationId(comment.getEvaluationId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setReplyToUserId(comment.getReplyToUserId());
        vo.setContent(comment.getContent());
        vo.setImages(comment.getImages());
        vo.setLikeCount(comment.getLikeCount() != null ? comment.getLikeCount() : 0);
        vo.setDislikeCount(comment.getDislikeCount() != null ? comment.getDislikeCount() : 0);
        vo.setStatus(comment.getStatus());
        vo.setCreateTime(comment.getCreateTime());
        vo.setUpdateTime(comment.getUpdateTime());
        vo.calculateRelativeTime();
        return vo;
    }
}
