package com.sprint.mission.sb03monewteam1.controller;

import com.sprint.mission.sb03monewteam1.controller.api.NotificationApi;
import com.sprint.mission.sb03monewteam1.dto.NotificationDto;
import com.sprint.mission.sb03monewteam1.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    @PatchMapping(path = "/{notificationId}")
    public ResponseEntity<NotificationDto> confirm(
        @PathVariable UUID notificationId,
        @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {

        log.info("알림 개별 확인 요청: notificationId={}, userId={}", notificationId, userId);

        NotificationDto result = notificationService.confirm(notificationId, userId);

        log.info("알림 개별 확인 완료: notificationId={}, userId={}", notificationId, userId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(result);
    }
}
