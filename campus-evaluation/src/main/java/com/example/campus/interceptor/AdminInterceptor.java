package com.example.campus.interceptor;

import com.example.campus.entity.User;
import com.example.campus.repository.UserMapper;
import com.example.campus.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员权限拦截器
 * 用于验证用户是否具有管理员权限
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getHeader("Authorization");

        Map<String, Object> result = new HashMap<>();

        if (token == null || token.isEmpty()) {
            result.put("code", 401);
            result.put("msg", "未登录，请先登录");
            writeResponse(response, result);
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        try {
            Claims claims = JwtUtil.parseToken(token);
            String userId = claims.getSubject();

            User user = userMapper.selectById(Long.parseLong(userId));

            if (user == null) {
                result.put("code", 403);
                result.put("msg", "用户不存在");
                writeResponse(response, result);
                return false;
            }

            if (!"ADMIN".equals(user.getRole())) {
                result.put("code", 403);
                result.put("msg", "无权限访问，需要管理员权限");
                writeResponse(response, result);
                return false;
            }

            request.setAttribute("userId", userId);
            request.setAttribute("user", user);

            return true;

        } catch (Exception e) {
            result.put("code", 401);
            result.put("msg", "认证失败：" + e.getMessage());
            writeResponse(response, result);
            return false;
        }
    }

    private void writeResponse(HttpServletResponse response, Map<String, Object> result) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(result));
    }
}
