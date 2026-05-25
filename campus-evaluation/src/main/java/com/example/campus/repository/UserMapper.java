package com.example.campus.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.campus.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问接口（Mapper）
 * 继承 BaseMapper 后，自动拥有增删改查方法
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 这里暂时不需要写额外代码，MyBatis-Plus 会自动实现基础的 CRUD
}