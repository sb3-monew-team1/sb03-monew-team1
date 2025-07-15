package com.sprint.mission.sb03monewteam1.controller;

import com.sprint.mission.sb03monewteam1.controller.api.InterestApi;
import com.sprint.mission.sb03monewteam1.dto.InterestDto;
import com.sprint.mission.sb03monewteam1.dto.request.InterestRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.service.InterestService;
import jakarta.validation.Valid;
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
    public ResponseEntity<CursorPageResponse> getInterests(
        @RequestParam(defaultValue = "") String searchKeyword,
        @RequestParam(defaultValue = "") String cursor,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam String orderBy,
        @RequestParam String direction) {

        log.info("관심사 조회 요청: searchKeyword: {}, cursor: {}, limit: {}, orderBy: {}, direction: {}",
            searchKeyword, cursor, limit, orderBy, direction);

        CursorPageResponse response = interestService.getInterests(
            searchKeyword, cursor, limit, orderBy, direction);

        log.info("관심사 조회 완료: {}", response);

        return ResponseEntity.ok(response);
    }
}
