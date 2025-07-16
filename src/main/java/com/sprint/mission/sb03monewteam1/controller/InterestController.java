package com.sprint.mission.sb03monewteam1.controller;

import com.sprint.mission.sb03monewteam1.controller.api.InterestApi;
import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.SubscriptionDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.service.InterestService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
@Slf4j
public class InterestController implements InterestApi {

    private final InterestService interestService;

    @Override
    @PostMapping
    public ResponseEntity<InterestDto> create(
        @Valid @RequestBody InterestRegisterRequest request
    ) {
        log.info("관심사 등록 요청: {}", request);

        InterestDto response = interestService.create(request);
        log.info("관심사 등록 완료: {}", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CursorPageResponse<InterestDto>> getInterests(
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "") String cursor,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "subscriberCount") String orderBy,
        @RequestParam(defaultValue = "DESC") String direction)
    {

        log.info("관심사 조회 요청: keyword: {}, cursor: {}, limit: {}, orderBy: {}, direction: {}",
            keyword, cursor, limit, orderBy, direction);

        CursorPageResponse<InterestDto> response = interestService.getInterests(
            keyword, cursor, limit, orderBy, direction);

        log.info("관심사 조회 완료: {}", response);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{interestId}/subscriptions")

    public ResponseEntity<SubscriptionDto> createSubscription(
        @PathVariable UUID interestId,
        @RequestHeader("Monew-Request-User-ID") UUID userId) {

        log.info("구독 요청: userId={}, interestId={}", userId, interestId);

        SubscriptionDto subscriptionDto = interestService.createSubscription(userId, interestId);

        log.info("구독 생성 완료: userId={}, interestId={}, 구독된 관심사 이름={}",
            userId, interestId, subscriptionDto.interestName());

        return ResponseEntity.status(HttpStatus.CREATED).body(subscriptionDto);
    }
}
