package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.PostTagRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日常帖子标签关联数据访问接口（Mapper）
 */
@Mapper
public interface PostTagRelationMapper extends BaseMapper<PostTagRelation> {
}
