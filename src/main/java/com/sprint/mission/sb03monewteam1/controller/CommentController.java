package com.sprint.mission.sb03monewteam1.controller;

import com.sprint.mission.sb03monewteam1.controller.api.CommentApi;
import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
