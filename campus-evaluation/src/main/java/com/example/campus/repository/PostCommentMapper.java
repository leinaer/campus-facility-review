package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.PostComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子评论数据访问接口（Mapper）
 */
@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {
}
