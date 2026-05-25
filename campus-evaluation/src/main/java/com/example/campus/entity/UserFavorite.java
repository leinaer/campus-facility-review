package com.example.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户收藏实体类
 * 对应数据库表：user_favorite
 */
@Data
@TableName("user_favorite")
public class UserFavorite {

    @TableId(value = "favorite_id", type = IdType.AUTO)
    private Long favoriteId;

    @TableField("user_id")
    private Long userId;

    @TableField("facility_id")
    private Long facilityId;

    @TableField("create_time")
    private LocalDateTime createTime;
}