package com.sprint.mission.sb03monewteam1.controller;

import com.sprint.mission.sb03monewteam1.controller.api.CommentApi;
import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.CommentUpdateRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.service.CommentService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController implements CommentApi {

    private final CommentService commentService;

    @Override
    @PostMapping
    public ResponseEntity<CommentDto> create(
            @Valid @RequestBody CommentRegisterRequest commentRegisterRequest
    ) {
        log.info("댓글 등록 요청: {}", commentRegisterRequest);
        CommentDto commentDto = commentService.create(commentRegisterRequest);
        log.info("댓글 등록 완료: {}", commentDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(commentDto);
    }

    @Override
    @GetMapping
    public ResponseEntity<CursorPageResponse<CommentDto>> getComments(
        @RequestHeader("Monew-Request-User-ID") UUID userId,
        @RequestParam(required = false) UUID articleId,
        @RequestParam String orderBy,
        @RequestParam String direction,
        @RequestParam(required = false) String cursor,
        @RequestParam(required = false) Instant after,
        @RequestParam int limit
    ) {
        log.info("댓글 목록 조회 요청: articleId = {}, cursor = {}, after = {}, limit = {}, orderBy = {}, direction = {}", articleId, cursor, after, limit, orderBy, direction);
        CursorPageResponse<CommentDto> result = commentService.getCommentsWithCursorBySort(
            articleId, cursor, after, limit, orderBy, direction
        );
        log.info("댓글 목록 조회 완료");
        log.info("조회된 댓글 수: {}", result.content().size());

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(result);
    }

    @Override
    @PatchMapping(path = "/{commentId}")
    public ResponseEntity<CommentDto> update(
        @PathVariable UUID commentId,
        @RequestHeader("Monew-Request-User-ID") UUID userId,
        @Valid @RequestBody CommentUpdateRequest commentUpdateRequest
    ) {
        String updateContent = commentUpdateRequest.content();
        log.info("댓글 수정 요청: commentId = {}, userId = {}, 수정 내용 = {}", commentId, userId, updateContent);

        CommentDto result = commentService.update(commentId, userId, commentUpdateRequest);

        log.info("댓글 수정 완료");

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(result);
    }
}
