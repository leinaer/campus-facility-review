package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签数据访问接口（Mapper）
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}
