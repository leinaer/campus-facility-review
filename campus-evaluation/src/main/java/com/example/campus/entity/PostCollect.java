package com.example.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子收藏实体类
 * 对应数据库表：post_collect
 */
@Data
@TableName("post_collect")
public class PostCollect {

    /**
     * 主键ID
     */
    @TableId(value = "collect_id", type = IdType.AUTO)
    private Long collectId;

    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 收藏夹名称
     */
    @TableField("folder")
    private String folder;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private LocalDateTime createTime;
}
