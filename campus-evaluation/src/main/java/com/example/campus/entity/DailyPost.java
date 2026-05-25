package com.example.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 日常分享实体类
 * 对应数据库表：daily_post
 */
@Data
@TableName("daily_post")
public class DailyPost {

    /**
     * 帖子ID
     */
    @TableId(value = "post_id", type = IdType.AUTO)
    private Long postId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 心情表情（如：happy、normal、sad、angry）
     */
    @TableField("mood")
    private String mood;

    /**
     * 帖子标题
     */
    @TableField("title")
    private String title;

    /**
     * 封面图片URL
     */
    @TableField("cover_image")
    private String coverImage;


    /**
     * 分享内容
     */
    @TableField("content")
    private String content;

    /**
     * 图片URL列表（JSON格式存储）
     */
    @TableField("images")
    private String images;

    /**
     * 标签列表（JSON格式存储，如：["美食推荐","探店"]）
     */
    @TableField("tags")
    private String tags;

    /**
     * 关联话题ID列表（JSON格式存储）
     */
    @TableField("topic_ids")
    private String topicIds;

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
     * 收藏数
     */
    @TableField("collect_count")
    private Integer collectCount;

    /**
     * 浏览数
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 热度分
     */
    @TableField("hot_score")
    private BigDecimal hotScore;

    /**
     * 状态：1-正常，0-已删除，2-待审核
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
