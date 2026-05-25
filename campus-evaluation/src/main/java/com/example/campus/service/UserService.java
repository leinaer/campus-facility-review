package com.example.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campus.entity.User; // 确保你的 User 实体类路径正确

/**
 * 用户业务接口
 */
public interface UserService extends IService<User> {
    // 这里可以定义额外的业务方法，比如根据 openid 获取用户
    User findByOpenid(String openid);
}