package com.example.campus.config;

import com.example.campus.interceptor.AdminInterceptor;
import com.example.campus.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/evaluation/publish",
                        "/api/evaluation/my-evaluations",
                        "/api/evaluation/{evaluationId}",
                        "/api/evaluation/like/**",
                        "/api/evaluation/helpful/**",
                        "/api/like/**",
                        "/api/comment/publish",
                        "/api/comment/delete/**",
                        "/api/comment/vote/**",
                        "/api/post/publish",
                        "/api/post/like/**",
                        "/api/post/comment/**",
                        "/api/collect/**",
                        "/api/favorite/**",
                        "/api/post/my-posts",
                        "/api/post/{postId}",
                        "/api/auth/user/stats",
                        "/api/auth/user/level",
                        "/api/notification/**",
                        "/api/activity/add",
                        "/api/activity/update",
                        "/api/activity/status",
                        "/api/activity/delete/**",
                        "/api/activity/end/**",
                        "/api/activity/admin/**",
                        "/api/user/profile",
                        "/api/user/avatar",
                        "/api/tag/admin/**")
                .excludePathPatterns("/api/auth/login",
                        "/api/auth/logout",
                        "/api/category/list",
                        "/api/facility/**",
                        "/api/evaluation/by-facility/**",
                        "/api/evaluation/latest",
                        "/api/statistics/top-facilities",
                        "/api/banner/list",
                        "/api/facility/by-category/**",
                        "/api/post/discover",
                        "/api/post/by-topic/**",
                        "/api/evaluation/list/**",
                        "/api/activity/active",
                        "/api/activity/list",
                        "/api/tag/hot",
                        "/api/tag/activity",
                        "/api/tag/search");

        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/api/category/add",
                        "/api/category/update",
                        "/api/category/delete/**",
                        "/api/category/status/**",
                        "/api/category/admin/list",
                        "/api/facility/add",
                        "/api/facility/update",
                        "/api/facility/offline/**",
                        "/api/evaluation/admin/delete/**",
                        "/api/statistics/admin/**",
                        "/api/activity/add",
                        "/api/activity/update",
                        "/api/activity/status",
                        "/api/activity/delete/**",
                        "/api/activity/end/**",
                        "/api/activity/admin/**",
                        "/api/tag/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:D:/campus-uploads/");

        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
