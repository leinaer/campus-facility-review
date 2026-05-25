package com.example.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签实体类
 * 对应数据库表：tag
 */
@Data
@TableName("tag")
public class Tag {

    /**
     * 标签ID
     */
    @TableId(value = "tag_id", type = IdType.AUTO)
    private Long tagId;

    /**
     * 标签名称
     */
    @TableField("tag_name")
    private String tagName;

    /**
     * 标签类型：1-普通标签,2-活动标签
     */
    @TableField("tag_type")
    private Integer tagType;

    /**
     * 关联活动ID（活动标签时有效）
     */
    @TableField("activity_id")
    private Long activityId;

    /**
     * 标签图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 标签颜色
     */
    @TableField("color")
    private String color;

    /**
     * 使用次数
     */
    @TableField("use_count")
    private Integer useCount;

    /**
     * 状态：1-启用,0-禁用
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
