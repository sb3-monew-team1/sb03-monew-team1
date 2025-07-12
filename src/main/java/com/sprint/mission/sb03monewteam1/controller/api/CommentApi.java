package com.sprint.mission.sb03monewteam1.controller.api;

import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "댓글 관리", description = "댓글 관련 API")
public interface CommentApi {

    @Operation(summary = "댓글 등록")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "등록 성공",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(implementation = CommentDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (입력값 검증 실패)",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "*/*",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    ResponseEntity<CommentDto> create(CommentRegisterRequest commentRegisterRequest);
}
