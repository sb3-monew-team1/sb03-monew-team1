package com.sprint.mission.sb03monewteam1.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.CommentLikeDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.CommentUpdateRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentException;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.comment.UnauthorizedCommentAccessException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidSortOptionException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentLikeFixture;
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
            UUID userId = UUID.randomUUID();
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", userId);

            UUID articleId = UUID.randomUUID();
            Article article = ArticleFixture.createArticleWithId(articleId);
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

            given(commentService.getCommentsWithCursorBySort(eq(articleId), eq(null), eq(null), eq(pageSize), eq(sortBy), eq(sortDirection), eq(userId)))
                .willReturn(result);

            // when & then
            mockMvc.perform(get("/api/comments")
                    .param("articleId", articleId.toString())
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
            UUID userId = UUID.randomUUID();
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", userId);

            UUID articleId = UUID.randomUUID();
            Article article = ArticleFixture.createArticleWithId(articleId);
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

            given(commentService.getCommentsWithCursorBySort(eq(articleId), eq(cursor), eq(null), eq(pageSize), eq(sortBy), eq(sortDirection), eq(userId)))
                .willReturn(result);

            // when & then
            mockMvc.perform(get("/api/comments")
                    .param("articleId", articleId.toString())
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
                eq(invalidArticleId), any(), any(), eq(pageSize), eq(sortBy), eq(sortDirection), eq(userId))
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
                eq(null), eq(null), eq(null), eq(pageSize), eq(invalidSortBy), eq(sortDirection), eq(userId)
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
                eq(null), eq(null), eq(null), eq(pageSize), eq(sortBy), eq(invalidDirection), eq(userId)
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

    @Nested
    @DisplayName("댓글 수정 테스트")
    class CommentUpdateTest {

        @Test
        void 댓글을_수정하면_200과_수정된_댓글이_반환된다() throws Exception {

            // given
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            UUID userId = user.getId();
            Article article = ArticleFixture.createArticleWithId(UUID.randomUUID());
            String originalContent = "기존 댓글";
            String updateContent = "댓글 수정 테스트";
            Comment comment = CommentFixture.createComment(originalContent, user, article);
            ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
            UUID commentId = comment.getId();

            CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
                .content(updateContent)
                .build();
            CommentDto expectedCommentDto = CommentFixture.createCommentDtoWithContent(comment, updateContent);

            given(commentService.update(commentId, userId, commentUpdateRequest)).willReturn(expectedCommentDto);

            // When & Then
            mockMvc.perform(patch("/api/comments/" + commentId.toString())
                    .content(objectMapper.writeValueAsBytes(commentUpdateRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Monew-Request-User-ID", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(commentId.toString()))
                .andExpect(jsonPath("$.articleId").value(article.getId().toString()))
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.userNickname").value(user.getNickname()))
                .andExpect(jsonPath("$.content").value(updateContent));
        }

        @Test
        void 댓글을_수정할_때_존재하지_않는_댓글이라면_404가_반환되어야_한다() throws Exception {

            // given
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            UUID userId = user.getId();
            String updateContent = "댓글 수정 테스트";
            UUID invalidCommentId = UUID.randomUUID();

            CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
                .content(updateContent)
                .build();

            given(commentService.update(
                eq(invalidCommentId), eq(userId), eq(commentUpdateRequest))
            ).willThrow(new CommentNotFoundException(invalidCommentId));

            // When & Then
            mockMvc.perform(patch("/api/comments/" + invalidCommentId.toString())
                    .content(objectMapper.writeValueAsBytes(commentUpdateRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Monew-Request-User-ID", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void 댓글을_수정할_때_작성자가_아니라면_403가_반환되어야_한다() throws Exception {

            // given
            User author = UserFixture.createUser();
            ReflectionTestUtils.setField(author, "id", UUID.randomUUID());
            User otherUser = UserFixture.createUser();
            ReflectionTestUtils.setField(otherUser, "id", UUID.randomUUID());
            UUID otherUserId = otherUser.getId();

            Article article = ArticleFixture.createArticleWithId(UUID.randomUUID());
            String originalContent = "기존 댓글";
            String updateContent = "댓글 수정 테스트";
            Comment comment = CommentFixture.createComment(originalContent, author, article);
            ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
            UUID commentId = comment.getId();

            CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
                .content(updateContent)
                .build();

            given(commentService.update(
                eq(commentId), eq(otherUserId), eq(commentUpdateRequest))
            ).willThrow(new UnauthorizedCommentAccessException());

            // When & Then
            mockMvc.perform(patch("/api/comments/" + commentId.toString())
                    .content(objectMapper.writeValueAsBytes(commentUpdateRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Monew-Request-User-ID", otherUserId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.name()))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void 댓글을_수정할_때_댓글내용이_빈값이면_400가_반환되어야_한다() throws Exception {

            // given
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            UUID userId = user.getId();
            Article article = ArticleFixture.createArticleWithId(UUID.randomUUID());
            String originalContent = "기존 댓글";
            String updateContent = "";
            Comment comment = CommentFixture.createComment(originalContent, user, article);
            ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
            UUID commentId = comment.getId();

            CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
                .content(updateContent)
                .build();

            // When & Then
            mockMvc.perform(patch("/api/comments/" + commentId.toString())
                    .content(objectMapper.writeValueAsBytes(commentUpdateRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Monew-Request-User-ID", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_ERROR.name()));
        }
    }

    @Nested
    @DisplayName("댓글 논리 삭제 테스트")
    class CommentDeleteTest {

        @Test
        void 댓글을_논리삭제하면_204가_반환되어야_한다() throws Exception {

            // given
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            Article article = ArticleFixture.createArticleWithId(UUID.randomUUID());
            String content = "논리 삭제 테스트";
            Comment comment = CommentFixture.createComment(content, user, article);
            ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
            UUID commentId = comment.getId();

            // 컨트롤러 테스트이므로, 반환되는 comment 객체의 삭제 상태는 신경쓰지 않음
            given(commentService.delete(commentId, user.getId())).willReturn(comment);

            // when & then
            mockMvc.perform(delete("/api/comments/" + commentId.toString())
                    .header("Monew-Request-User-ID", user.getId()))
                .andExpect(status().isNoContent());
        }

        @Test
        void 댓글을_삭제할_때_존재하지_않는_댓글이면_404가_반환되어야_한다() throws Exception {

            // given
            UUID commentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(commentService.delete(commentId, userId))
                .willThrow(new CommentNotFoundException(commentId));

            // when & then
            mockMvc.perform(delete("/api/comments/" + commentId.toString())
                    .header("Monew-Request-User-ID", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void 댓글을_삭제할_때_이미_삭제된_댓글이면_404가_반환되어야_한다() throws Exception {

            // given
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            Article article = ArticleFixture.createArticleWithId(UUID.randomUUID());
            String content = "이미 삭제된 댓글";
            Comment deletedComment = CommentFixture.createCommentWithIsDeleted(content, user, article);
            ReflectionTestUtils.setField(deletedComment, "id", UUID.randomUUID());
            UUID deletedCommentId = deletedComment.getId();

            given(commentService.delete(deletedCommentId, user.getId()))
                .willThrow(new CommentNotFoundException(deletedCommentId));

            // when & then
            mockMvc.perform(delete("/api/comments/" + deletedCommentId.toString())
                    .header("Monew-Request-User-ID", user.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void 댓글을_삭제할_때_작성자가_아니라면_403가_반환되어야_한다() throws Exception {

            // given
            User user = UserFixture.createUser();
            Article article = ArticleFixture.createArticleWithId(UUID.randomUUID());
            String content = "논리 삭제 테스트";
            Comment comment = CommentFixture.createComment(content, user, article);
            ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
            UUID commentId = comment.getId();
            UUID otherUserId = UUID.randomUUID();

            given(commentService.delete(
                eq(commentId), eq(otherUserId))
            ).willThrow(new UnauthorizedCommentAccessException());

            // when & then
            mockMvc.perform(delete("/api/comments/" + commentId.toString())
                    .header("Monew-Request-User-ID", otherUserId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.name()))
                .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("댓글 물리 삭제 테스트")
    class CommentDeleteHardTest {

        @Test
        void 댓글을_물리삭제하면_204가_반환되어야_한다() throws Exception {

            // given
            UUID commentId = UUID.randomUUID();

            willDoNothing().given(commentService).deleteHard(commentId);

            // when & then
            mockMvc.perform(delete("/api/comments/" + commentId + "/hard"))
                .andExpect(status().isNoContent());

            then(commentService).should().deleteHard(commentId);
        }

        @Test
        void 댓글을_물리삭제할_때_존재하지_않는_댓글이면_404가_반환되어야_한다() throws Exception {

            // given
            UUID commentId = UUID.randomUUID();

            willThrow(new CommentNotFoundException(commentId))
                .given(commentService).deleteHard(commentId);

            // when & then
            mockMvc.perform(delete("/api/comments/" + commentId + "/hard"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("댓글 좋아요 테스트")
    class CommentLikeTest {

        @Test
        void 댓글에_좋아요를_누르면_200과_좋아요DTO_좋아요수가_1증가되어야_한다() throws Exception {

            // given
            UUID commentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            Article article = ArticleFixture.createArticleWithId(UUID.randomUUID());
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", userId);

            Comment comment = CommentFixture.createComment("좋아요 테스트", user, article);
            ReflectionTestUtils.setField(comment, "id", commentId);
            CommentLike commentLike = CommentLikeFixture.createCommentLike(user, comment);
            CommentLikeDto responseDto = CommentLikeFixture.createCommentLikeDto(commentLike);

            given(commentService.like(eq(commentId), eq(userId))).willReturn(responseDto);

            // when & then
            mockMvc.perform(post("/api/comments/{commentId}/like", commentId)
                    .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likedBy").value(userId.toString()))
                .andExpect(jsonPath("$.commentId").value(commentId.toString()))
                .andExpect(jsonPath("$.commentLikeCount").value(1L))
                .andExpect(jsonPath("$.articleId").value(article.getId().toString()))
                .andExpect(jsonPath("$.commentUserId").value(userId.toString()))
                .andExpect(jsonPath("$.commentUserNickname").value(user.getNickname()))
                .andExpect(jsonPath("$.commentContent").value(comment.getContent()))
                .andExpect(jsonPath("$.commentCreatedAt").exists());
        }

        @Test
        void 존재하지_않는_댓글에_좋아요를_요청시_404가_반환되어야_한다() throws Exception {

            // given
            UUID commentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(commentService.like(eq(commentId), eq(userId)))
                .willThrow(new CommentNotFoundException(commentId));

            // when & then
            mockMvc.perform(post("/api/comments/{commentId}/like", commentId)
                    .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isNotFound());
        }
    }
}
