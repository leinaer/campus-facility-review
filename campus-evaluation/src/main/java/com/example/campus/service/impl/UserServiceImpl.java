package com.example.campus.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campus.entity.User;
import com.example.campus.repository.UserMapper; // 注意这里引用的是 Mapper
import com.example.campus.service.UserService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Service // 关键：加上这个注解，Spring 才能扫描到它
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    // 如果需要自定义逻辑，写在这里
    @Override
    public User findByOpenid(String openid) {
        return this.getOne(new QueryWrapper<User>().eq("openid", openid));
    }
}