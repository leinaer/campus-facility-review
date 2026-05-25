package com.example.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价有用性投票实体类
 * 对应数据库表：evaluation_helpful
 */
@Data
@TableName("evaluation_helpful")
public class EvaluationHelpful {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 投票类型：1-有用，0-无用
     */
    @TableField("vote_type")
    private Integer voteType;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
