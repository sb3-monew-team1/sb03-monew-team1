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
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<CursorPageResponse<NotificationDto>> getNotifications(
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Instant after,
        @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit,
        @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {

        log.info("미확인 알림 목록 조회 요청 - userId: {}, cursor: {}, limit: {}",
            userId, cursor, limit);

        CursorPageResponse<NotificationDto> result = notificationService
            .getUncheckedNotifications(userId, cursor, after, limit);

        log.info("미확인 알림 목록 조회 요청 완료 - userId: {}, cursor: {}, limit: {}",
            userId, cursor, limit);

        return ResponseEntity.ok(result);
    }

    @Override
    @PatchMapping(path = "/{notificationId}")
    public ResponseEntity<Void> confirm(
        @PathVariable UUID notificationId,
        @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {

        log.info("알림 개별 확인 요청: notificationId={}, userId={}", notificationId, userId);

        notificationService.confirm(notificationId, userId);

        log.info("알림 개별 확인 완료: notificationId={}, userId={}", notificationId, userId);

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    @Override
    @PatchMapping
    public ResponseEntity<Void> confirmAll(
        @RequestHeader("Monew-Request-User-ID") UUID userId
    ) {
        log.info("알림 전체 확인 요청: userId={}", userId);

        notificationService.confirmAll(userId);

        log.info("알림 전체 확인 완료: userId={}", userId);

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }
}
