package com.example.campus.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 标记这是一个控制器，且返回的是字符串而不是页面
public class HomeController {

    // 当用户访问根路径 "/" 时，执行这个方法
    @GetMapping("/Home")
    public String home() {
        return "你好！校园评价系统正在运行中...";
    }
}