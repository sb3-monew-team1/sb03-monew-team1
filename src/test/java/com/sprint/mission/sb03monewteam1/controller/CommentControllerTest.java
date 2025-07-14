package com.sprint.mission.sb03monewteam1.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidSortOptionException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.service.CommentService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CommentController.class)
@ActiveProfiles("test")
@DisplayName("CommentController 테스트")
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    Instant baseTime;

    @BeforeEach
    void setUp() {
        baseTime = Instant.now().minusSeconds(3000);
    }

    @Nested
    @DisplayName("댓글 등록 테스트")
    class CommentCreateTest {

        @Test
        void 댓글을_등록하면_201과_DTO가_반환되어야_한다() throws Exception {

            // given
            String content = "댓글 생성 테스트";
            User user = UserFixture.createUser();
            Article article = ArticleFixture.createArticle();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            ReflectionTestUtils.setField(article, "id", UUID.randomUUID());

            UUID userId = user.getId();
            UUID articleId = article.getId();

            CommentRegisterRequest commentRegisterRequest = CommentFixture.createCommentRegisterRequest(content, userId, articleId);
            Comment savedComment = CommentFixture.createComment(content,user, article);
            CommentDto expectedCommentDto = CommentFixture.createCommentDto(savedComment);

            given(commentService.create(commentRegisterRequest)).willReturn(expectedCommentDto);

            // When & Then
            mockMvc.perform(post("/api/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(commentRegisterRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.content").value(content))
                    .andExpect(jsonPath("$.userId").value(userId.toString()))
                    .andExpect(jsonPath("$.articleId").value(articleId.toString()))
                    .andExpect(jsonPath("$.likeCount").value(0))
                    .andExpect(jsonPath("$.likedByMe").value(false));
        }

        @Test
        void 댓글을_등록할_때_존재하지_않는_사용자라면_404가_반환되어야_한다() throws Exception {

            // given
            String content = "댓글 생성 테스트";
            Article article = ArticleFixture.createArticle();
            ReflectionTestUtils.setField(article, "id", UUID.randomUUID());
            UUID articleId = article.getId();
            UUID invalidUserId = UUID.randomUUID();

            CommentRegisterRequest commentRegisterRequest = CommentFixture.createCommentRegisterRequest(content, invalidUserId, articleId);

            given(commentService.create(commentRegisterRequest))
                    .willThrow(new CommentException(ErrorCode.USER_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/api/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(commentRegisterRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
        }

        @Test
        void 댓글을_등록할_때_존재하지_않는_뉴스기사라면_404가_반환되어야_한다() throws Exception {

            // given
            String content = "댓글 생성 테스트";
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            UUID userId = user.getId();
            UUID invalidArticleId = UUID.randomUUID();

            CommentRegisterRequest commentRegisterRequest = CommentFixture.createCommentRegisterRequest(content, userId, invalidArticleId);

            given(commentService.create(commentRegisterRequest))
                    .willThrow(new CommentException(ErrorCode.ARTICLE_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/api/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(commentRegisterRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("ARTICLE_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("기사를 찾을 수 없습니다."));
        }
    }

    @Nested
    @DisplayName("댓글 목록 조회 테스트")
    class CommentListReadTest {

        @Test
        void 댓글을_조회하면_200과_댓글목록이_반환된다() throws Exception {

            // given
            User user = UserFixture.createUser();
            Article article = ArticleFixture.createArticle();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            String sortBy = "createdAt";
            String sortDirection = "DESC";
            int totalCount = 10;
            int pageSize = 5;

            List<CommentDto> commentDtos = new ArrayList<>();

            for (int i = 0; i < totalCount; i++) {
                Comment comment = CommentFixture.createCommentWithCreatedAt("test" + i, user, article, baseTime.plusSeconds(i * 1000));
                ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
                ReflectionTestUtils.setField(comment, "createdAt", baseTime.plusSeconds(i * 1000));
                CommentDto commentDto = CommentFixture.createCommentDto(comment);
                commentDtos.add(commentDto);
            }

            List<CommentDto> pageContent = commentDtos.subList(0, pageSize);

            CursorPageResponse<CommentDto> result = CursorPageResponse.<CommentDto>builder()
                .content(pageContent)
                .nextCursor(pageContent.get(pageSize - 1).id().toString())
                .nextAfter(pageContent.get(pageSize - 1).createdAt())
                .size(pageSize)
                .totalElements((long) totalCount)
                .hasNext(true)
                .build();

            given(commentService.getCommentsWithCursorBySort(eq(null), eq(null), eq(null), eq(pageSize), eq(sortBy), eq(sortDirection)))
                .willReturn(result);

            // when & then
            mockMvc.perform(get("/api/comments")
                    .param("orderBy", sortBy)
                    .param("direction", sortDirection)
                    .param("limit", String.valueOf(pageSize))
                    .header("Monew-Request-User-ID", user.getId().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.nextCursor").exists())
                .andExpect(jsonPath("$.nextAfter").exists())
                .andExpect(jsonPath("$.size").value(pageSize))
                .andExpect(jsonPath("$.totalElements").value(totalCount))
                .andExpect(jsonPath("$.hasNext").value(true));
        }

        @Test
        void 커서가_있으면_커서_이후의_댓글만_조회된다() throws Exception {
            // given
            User user = UserFixture.createUser();
            Article article = ArticleFixture.createArticle();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            String sortBy = "createdAt";
            String sortDirection = "DESC";
            int totalCount = 10;
            int pageSize = 5;

            List<CommentDto> commentDtos = new ArrayList<>();

            for (int i = 0; i < totalCount; i++) {
                Comment comment = CommentFixture.createCommentWithCreatedAt("test" + i, user, article, baseTime.plusSeconds(i * 1000));
                ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
                ReflectionTestUtils.setField(comment, "createdAt", baseTime.plusSeconds(i * 1000));
                CommentDto commentDto = CommentFixture.createCommentDto(comment);
                commentDtos.add(commentDto);
            }

            String cursor = baseTime.toString();
            List<CommentDto> pageContent = commentDtos.stream()
                .filter(c -> c.createdAt().isAfter(baseTime))
                .limit(pageSize)
                .collect(Collectors.toList());

            CursorPageResponse<CommentDto> result = CursorPageResponse.<CommentDto>builder()
                .content(pageContent)
                .nextCursor(pageContent.get(pageSize - 1).id().toString())
                .nextAfter(pageContent.get(pageSize - 1).createdAt())
                .size(pageSize)
                .totalElements((long) totalCount)
                .hasNext(true)
                .build();

            given(commentService.getCommentsWithCursorBySort(eq(null), eq(cursor), eq(null), eq(pageSize), eq(sortBy), eq(sortDirection)))
                .willReturn(result);

            // when & then
            mockMvc.perform(get("/api/comments")
                    .param("cursor", cursor)
                    .param("orderBy", sortBy)
                    .param("direction", sortDirection)
                    .param("limit", String.valueOf(pageSize))
                    .header("Monew-Request-User-ID", user.getId().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.nextCursor").exists())
                .andExpect(jsonPath("$.nextAfter").exists())
                .andExpect(jsonPath("$.size").value(pageSize))
                .andExpect(jsonPath("$.totalElements").value(totalCount))
                .andExpect(jsonPath("$.hasNext").value(true));
        }

        @Test
        void 존재하지_않는_게시글_ID로_요청하면_404와_에러코드가_반환된다() throws Exception {
            // given
            UUID invalidArticleId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            String sortBy = "createdAt";
            String sortDirection = "DESC";
            int pageSize = 5;

            given(commentService.getCommentsWithCursorBySort(
                eq(invalidArticleId), any(), any(), eq(pageSize), eq(sortBy), eq(sortDirection))
            ).willThrow(new CommentException(ErrorCode.ARTICLE_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/comments")
                    .param("articleId", invalidArticleId.toString())
                    .param("orderBy", sortBy)
                    .param("direction", sortDirection)
                    .param("limit", String.valueOf(pageSize))
                    .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ARTICLE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void 잘못된_정렬기준이면_400과_에러코드가_반환된다() throws Exception {

            // given
            UUID userId = UUID.randomUUID();
            String invalidSortBy = "invalid_sortBy";
            String sortDirection = "DESC";
            int pageSize = 5;

            given(commentService.getCommentsWithCursorBySort(
                eq(null), eq(null), eq(null), eq(pageSize), eq(invalidSortBy), eq(sortDirection)
            )).willThrow(new InvalidSortOptionException(ErrorCode.INVALID_SORT_FIELD));

            // when & then
            mockMvc.perform(get("/api/comments")
                    .param("orderBy", invalidSortBy)
                    .param("direction", sortDirection)
                    .param("limit", String.valueOf(pageSize))
                    .header("Monew-Request-User-ID", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_SORT_FIELD.name()));
        }

        @Test
        void 잘못된_정렬방향이면_400과_에러코드가_반환된다() throws Exception {

            // given
            UUID userId = UUID.randomUUID();
            int pageSize = 5;
            String sortBy = "createdAt";
            String invalidDirection = "INVALID";

            given(commentService.getCommentsWithCursorBySort(
                eq(null), eq(null), eq(null), eq(pageSize), eq(sortBy), eq(invalidDirection)
            )).willThrow(new InvalidSortOptionException(ErrorCode.INVALID_SORT_DIRECTION));

            mockMvc.perform(get("/api/comments")
                    .param("orderBy", sortBy)
                    .param("direction", invalidDirection)
                    .param("limit", "5")
                    .header("Monew-Request-User-ID", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_SORT_DIRECTION.name()));
        }
    }
}
