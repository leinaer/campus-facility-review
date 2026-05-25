package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子点赞数据访问接口（Mapper）
 */
@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {
}
