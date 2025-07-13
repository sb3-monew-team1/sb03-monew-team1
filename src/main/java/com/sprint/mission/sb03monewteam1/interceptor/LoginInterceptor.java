package com.sprint.mission.sb03monewteam1.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Controller
@Profile("!test")
public class LoginInterceptor implements HandlerInterceptor {

    public static final String REQUEST_USER_ID_HEADER = "MoNew-Request-User-ID";

    @Override
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader(REQUEST_USER_ID_HEADER);

        if (userId == null || userId.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"code\":\"UNAUTHORIZED\", \"message\":\"로그인이 필요합니다.\"}");
            return false;
        }

        try {
            UUID userUUID = UUID.fromString(userId);
            request.setAttribute("userId", userUUID);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format: {}", userId);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter()
                .write("{\"code\":\"UNAUTHORIZED\", \"message\":\"유효하지 않은 사용자 ID 입니다.\"}");
            return false;
        }

        return true;
    }

}
