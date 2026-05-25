package com.example.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 话题实体类
 * 对应数据库表：post_topic
 */
@Data
@TableName("post_topic")
public class PostTopic {

    /**
     * 话题ID
     */
    @TableId(value = "topic_id", type = IdType.AUTO)
    private Long topicId;

    /**
     * 话题名称
     */
    @TableField("name")
    private String name;

    /**
     * 话题图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 话题描述
     */
    @TableField("description")
    private String description;

    /**
     * 帖子数量
     */
    @TableField("post_count")
    private Integer postCount;

    /**
     * 关注人数
     */
    @TableField("follow_count")
    private Integer followCount;

    /**
     * 状态：1-启用，0-禁用
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
