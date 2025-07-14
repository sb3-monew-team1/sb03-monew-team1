package com.sprint.mission.sb03monewteam1.interceptor;

import com.sprint.mission.sb03monewteam1.exception.user.MissMonewIdHeaderException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@Profile("!test")
public class LoginInterceptor implements HandlerInterceptor {

    public static final String REQUEST_USER_ID_HEADER = "Monew-Request-User-ID";

    @Override
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String userId = request.getHeader(REQUEST_USER_ID_HEADER);

        if (userId == null || userId.isBlank()) {
            throw new MissMonewIdHeaderException("Monew-Request-User-ID 헤더를 찾을 수 없습니다");
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
