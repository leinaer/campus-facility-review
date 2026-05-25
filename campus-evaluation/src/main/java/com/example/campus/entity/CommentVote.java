package com.example.campus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论投票实体类
 */
@Data
@TableName("comment_vote")
public class CommentVote {

    @TableId(value = "vote_id", type = IdType.AUTO)
    private Long voteId;

    @TableField("comment_id")
    private Long commentId;

    @TableField("user_id")
    private Long userId;

    @TableField("vote_type")
    private Integer voteType;

    @TableField("create_time")
    private LocalDateTime createTime;
}