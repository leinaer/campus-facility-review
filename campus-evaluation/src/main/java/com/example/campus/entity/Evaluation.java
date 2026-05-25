package com.example.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价实体类
 * 对应数据库表：evaluation
 */
@Data
@TableName("evaluation")
public class Evaluation {

    /**
     * 主键ID
     */
    @TableId(value = "evaluation_id", type = IdType.AUTO)
    private Long evaluationId;

    /**
     * 设施ID
     */
    @TableField("facility_id")
    private Long facilityId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 评分（1-5星）
     */
    @TableField("rating")
    private Integer rating;

    /**
     * 多维度评分JSON: {"taste":4,"environment":5,"service":3}
     */
    @TableField("dimension_scores")
    private String dimensionScores;

    /**
     * 心情表情（如：happy、normal、sad、angry）
     */
    @TableField("mood")
    private String mood;

    /**
     * 评价内容
     */
    @TableField("content")
    private String content;

    /**
     * 图片URL列表（JSON格式存储）
     */
    @TableField("images")
    private String images;

    /**
     * 标签列表（JSON格式存储，如：["空调足","排队久"]）
     */
    @TableField("tags")
    private String tags;

    /**
     * 到访时段：breakfast/lunch/dinner
     */
    @TableField("visit_time")
    private String visitTime;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 评论数
     */
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 有用数
     */
    @TableField("helpful_count")
    private Integer helpfulCount;

    /**
     * 状态：1-正常，0-已删除（软删除）
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
