package com.example.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 设施实体类
 * 对应数据库表：facility
 */
@Data
@TableName("facility")
public class Facility {

    /**
     * 主键ID
     */
    @TableId(value = "facility_id", type = IdType.AUTO)
    private Long facilityId;

    /**
     * 设施名称（如：一食堂、三教）
     */
    @TableField("name")
    private String name;

    /**
     * 分类ID
     */
    @TableField("category_id")
    private Long categoryId;

    /**
     * 封面图URL
     */
    @TableField("cover_image")
    private String coverImage;

    /**
     * 位置描述（如：南校区东门旁）
     */
    @TableField("location")
    private String location;

    /**
     * 综合评分（1-5分）
     */
    @TableField("rating")
    private BigDecimal rating;

    /**
     * 评价总数
     */
    @TableField("review_count")
    private Integer reviewCount;

    /**
     * 所属校区（如：南校区、北校区）
     */
    @TableField("campus")
    private String campus;

    /**
     * 状态：1-正常，0-下架
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

    /**
     * 上次排名（用于计算趋势）
     */
    @TableField("last_rank")
    private Integer lastRank;

    /**
     * 排名更新时间
     */
    @TableField("rank_update_time")
    private LocalDateTime rankUpdateTime;
}
