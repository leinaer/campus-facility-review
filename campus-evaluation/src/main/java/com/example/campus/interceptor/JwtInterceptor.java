package com.example.campus.interceptor;

import com.example.campus.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT验证拦截器
 * 用于验证用户登录状态
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

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
            String role = claims.get("role", String.class);

            request.setAttribute("userId", userId);
            request.setAttribute("role", role != null ? role : "USER");

            return true;

        } catch (ExpiredJwtException e) {
            result.put("code", 401);
            result.put("msg", "登录已过期，请重新登录");
            writeResponse(response, result);
            return false;
        } catch (MalformedJwtException e) {
            result.put("code", 401);
            result.put("msg", "无效的Token");
            writeResponse(response, result);
            return false;
        } catch (Exception e) {
            result.put("code", 401);
            result.put("msg", "认证失败：" + e.getMessage());
            writeResponse(response, result);
            return false;
        }
    }

    private void writeResponse(HttpServletResponse response, Map<String, Object> result) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(result));
    }
}