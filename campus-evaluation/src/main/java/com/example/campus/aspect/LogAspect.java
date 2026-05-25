package com.example.campus.aspect;

import com.example.campus.annotation.OperationLog;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 日志切面
 * 自动记录带有@OperationLog注解的方法调用
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 环绕通知：记录方法执行前后的信息
     */
    @Around("@annotation(com.example.campus.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 获取方法信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // 获取注解信息
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        // 构建日志信息
        Map<String, Object> logInfo = new HashMap<>();
        logInfo.put("module", operationLog.module());
        logInfo.put("type", operationLog.type());
        logInfo.put("description", operationLog.description());
        logInfo.put("method", method.getName());
        logInfo.put("class", joinPoint.getTarget().getClass().getName());
        logInfo.put("args", Arrays.toString(joinPoint.getArgs()));

        if (request != null) {
            logInfo.put("uri", request.getRequestURI());
            logInfo.put("ip", getClientIp(request));

            // 从request中获取userId（如果已登录）
            Object userId = request.getAttribute("userId");
            if (userId != null) {
                logInfo.put("userId", userId);
            }
        }

        try {
            // 执行目标方法
            Object result = joinPoint.proceed();

            // 计算耗时
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            logInfo.put("costTime", costTime + "ms");
            logInfo.put("status", "SUCCESS");

            // 记录成功日志
            log.info("操作日志: {}", objectMapper.writeValueAsString(logInfo));

            return result;

        } catch (Throwable e) {
            // 记录异常日志
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;
            logInfo.put("costTime", costTime + "ms");
            logInfo.put("status", "FAILED");
            logInfo.put("error", e.getMessage());

            log.error("操作异常: {}", objectMapper.writeValueAsString(logInfo), e);

            throw e;
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
