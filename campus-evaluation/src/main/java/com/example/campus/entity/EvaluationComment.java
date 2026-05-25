package com.example.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价评论实体类
 * 对应数据库表：evaluation_comment
 */
@Data
@TableName("evaluation_comment")
public class EvaluationComment {

    /**
     * 主键ID
     */
    @TableId(value = "comment_id", type = IdType.AUTO)
    private Long commentId;

    /**
     * 评价ID
     */
    @TableField("evaluation_id")
    private Long evaluationId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 父评论ID（用于楼中楼回复，0表示一级评论）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 回复的用户ID（如果是回复评论）
     */
    @TableField("reply_to_user_id")
    private Long replyToUserId;

    /**
     * 评论内容
     */
    @TableField("content")
    private String content;

    /**
     * 图片URL列表（JSON格式存储）
     */
    @TableField("images")
    private String images;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 点踩数
     */
    @TableField("dislike_count")
    private Integer dislikeCount;

    /**
     * 状态：1-正常，0-已删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
