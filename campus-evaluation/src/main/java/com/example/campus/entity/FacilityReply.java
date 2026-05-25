package com.example.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 设施官方回复实体类
 * 对应数据库表：facility_reply
 */
@Data
@TableName("facility_reply")
public class FacilityReply {

    /**
     * 主键ID
     */
    @TableId(value = "reply_id", type = IdType.AUTO)
    private Long replyId;

    /**
     * 评价ID
     */
    @TableField("evaluation_id")
    private Long evaluationId;

    /**
     * 管理员ID
     */
    @TableField("admin_id")
    private Long adminId;

    /**
     * 回复内容
     */
    @TableField("content")
    private String content;

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
