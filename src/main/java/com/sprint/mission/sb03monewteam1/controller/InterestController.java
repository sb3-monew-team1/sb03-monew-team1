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

    /**
     * 사용자의 관심사 목록을 페이징 및 정렬 조건에 따라 조회합니다.
     *
     * @param keyword   관심사 이름 또는 설명에 대한 검색 키워드
     * @param cursor    다음 페이지 조회를 위한 커서 값
     * @param limit     한 페이지에 조회할 관심사 개수
     * @param orderBy   정렬 기준 필드명 (예: subscriberCount)
     * @param direction 정렬 방향 ("ASC" 또는 "DESC")
     * @return          조회된 관심사 목록과 페이징 정보를 담은 응답
     */
    @GetMapping
    public ResponseEntity<CursorPageResponse<InterestDto>> getInterests(
        @RequestHeader("Monew-Request-User-ID") UUID userId,
        @RequestParam(defaultValue = "") String keyword,
        @RequestParam(defaultValue = "") String cursor,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam(defaultValue = "subscriberCount") String orderBy,
        @RequestParam(defaultValue = "DESC") String direction)
    {

        log.info("관심사 조회 요청: userId: {}. keyword: {}, cursor: {}, limit: {}, orderBy: {}, direction: {}",
            userId, keyword, cursor, limit, orderBy, direction);

        CursorPageResponse<InterestDto> response = interestService.getInterests(
            userId, keyword, cursor, limit, orderBy, direction);

        log.info("관심사 조회 완료: {}", response);

        return ResponseEntity.ok(response);
    }

    /**
     * 사용자가 특정 관심사를 구독하도록 구독 정보를 생성합니다.
     *
     * @param interestId 구독할 관심사의 UUID
     * @param userId 구독을 요청하는 사용자의 UUID
     * @return 생성된 구독 정보를 포함한 ResponseEntity (HTTP 201 Created)
     */
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

    /**
     * 지정된 관심사를 삭제합니다.
     *
     * @param interestId 삭제할 관심사의 UUID
     * @return 삭제가 성공적으로 완료되면 204 No Content 응답을 반환합니다.
     */
    @DeleteMapping("/{interestId}")
    public ResponseEntity<Void> deleteInterest(@PathVariable UUID interestId) {

        log.info("관심사 삭제 요청: interestId={}", interestId);

        interestService.deleteInterest(interestId);

        log.info("관심사 삭제 완료: interestId={}", interestId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
