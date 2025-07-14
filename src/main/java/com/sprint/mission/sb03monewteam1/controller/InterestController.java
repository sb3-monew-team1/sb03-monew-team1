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
        log.info("받은 요청: {}", request);

        InterestDto response = interestService.create(request);
        log.info("응답: {}", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CursorPageResponse> getInterests(
        @RequestParam(defaultValue = "") String searchKeyword,
        @RequestParam(defaultValue = "") String cursor,
        @RequestParam(defaultValue = "10") int limit,
        @RequestParam String sortBy,
        @RequestParam String sortDirection) {

        log.info("요청 받은 파라미터 - searchKeyword: {}, cursor: {}, limit: {}, sortBy: {}, sortDirection: {}",
            searchKeyword, cursor, limit, sortBy, sortDirection);

        CursorPageResponse response = interestService.getInterests(
            searchKeyword, cursor, limit, sortBy, sortDirection);

        log.info("응답: {}", response);

        return ResponseEntity.ok(response);
    }
}
