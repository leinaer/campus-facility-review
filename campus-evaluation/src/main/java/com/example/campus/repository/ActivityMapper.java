package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.Activity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 活动数据访问接口（Mapper）
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {
}
