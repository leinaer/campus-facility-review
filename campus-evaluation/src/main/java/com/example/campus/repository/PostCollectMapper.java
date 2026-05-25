package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.PostCollect;
import org.apache.ibatis.annotations.Mapper;

/**
 * 帖子收藏数据访问接口（Mapper）
 */
@Mapper
public interface PostCollectMapper extends BaseMapper<PostCollect> {
}
