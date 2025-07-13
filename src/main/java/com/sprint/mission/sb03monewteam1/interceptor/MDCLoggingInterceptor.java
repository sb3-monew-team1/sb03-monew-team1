package com.sprint.mission.sb03monewteam1.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MDCLoggingInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID = "requestId";
    private static final String REQUEST_METHOD = "requestMethod";
    private static final String REQUEST_URL = "requestUrl";
    private static final String IP_ADDRESS = "ip";
    private static final String HEADER_NAME = "Monew-Request-ID";
    private static final String HEADER_IP = "Monew-Client-IP";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler) {
        String requestId = UUID.randomUUID().toString();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        MDC.put(REQUEST_ID, requestId);
        MDC.put(REQUEST_METHOD, request.getMethod());
        MDC.put(REQUEST_URL, request.getRequestURI());
        MDC.put(IP_ADDRESS, ip);
        response.setHeader(HEADER_NAME, requestId);
        response.setHeader(HEADER_IP, ip);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler, @Nullable Exception ex) {
        MDC.clear();
    }
}
