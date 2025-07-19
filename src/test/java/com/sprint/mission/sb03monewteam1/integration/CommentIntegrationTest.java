package com.sprint.mission.sb03monewteam1.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.request.CommentUpdateRequest;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentLikeFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.mapper.CommentMapper;
import com.sprint.mission.sb03monewteam1.repository.jpa.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.CommentLikeRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.jpa.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@LoadTestEnv
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("CommentIntegration 테스트")
public class CommentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private CommentMapper commentMapper;

    @Nested
    @DisplayName("댓글 등록 테스트")
    class CommentCreateTest {

        @Test
        void 댓글을_등록하면_Repository까지_반영되어야_한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();
            String content = "댓글 등록 테스트";

            CommentRegisterRequest commentRegisterRequest = CommentRegisterRequest.builder()
                .userId(savedUser.getId())
                .articleId(savedArticle.getId())
                .content(content)
                .build();

            // when & then
            MvcResult result = mockMvc.perform(post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(commentRegisterRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value(content))
                .andExpect(jsonPath("$.userId").value(String.valueOf(savedUser.getId())))
                .andExpect(jsonPath("$.articleId").value(String.valueOf(savedArticle.getId())))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.likedByMe").value(false))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            UUID commentId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

            Comment registeredComment = commentRepository.findById(commentId).orElseThrow();
            assertThat(registeredComment.getContent()).isEqualTo(content);
            assertThat(registeredComment.getAuthor().getId()).isEqualTo(savedUser.getId());
            assertThat(registeredComment.getArticle().getId()).isEqualTo(savedArticle.getId());
        }

        @Test
        void 댓글을_등록할_때_내용이_비어있으면_400을_반환해야_한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();
            String content = "";

            CommentRegisterRequest commentRegisterRequest = CommentRegisterRequest.builder()
                .userId(savedUser.getId())
                .articleId(savedArticle.getId())
                .content(content)
                .build();

            // when & then
            MvcResult result = mockMvc.perform(post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(commentRegisterRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists())
                .andReturn();
        }

        @Test
        void 댓글을_등록할_때_내용이_500자를_초과하면_400을_반환해야_한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();
            String content = "1".repeat(501);

            CommentRegisterRequest commentRegisterRequest = CommentRegisterRequest.builder()
                .userId(savedUser.getId())
                .articleId(savedArticle.getId())
                .content(content)
                .build();

            // when & then
            MvcResult result = mockMvc.perform(post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(commentRegisterRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists())
                .andReturn();
        }

        @Test
        void 댓글을_등록할_때_논리삭제된_유저라면_404를_반환해야_한다() throws Exception {

            // given
            User savedUser = userRepository.save(
                User.builder()
                    .email("registTest@codeit.com")
                    .nickname("registTest")
                    .password("regist1234!")
                    .isDeleted(true)
                    .build()
            );
            Article savedArticle = createArticle();
            String content = "댓글 등록 테스트";

            CommentRegisterRequest commentRegisterRequest = CommentRegisterRequest.builder()
                .userId(savedUser.getId())
                .articleId(savedArticle.getId())
                .content(content)
                .build();

            // when & then
            MvcResult result = mockMvc.perform(post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(commentRegisterRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists())
                .andReturn();
        }

        @Test
        void 댓글을_등록할_때_논리삭제된_뉴스기사라면_404를_반환해야_한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = articleRepository.save(
                Article.builder()
                    .source("NAVER")
                    .sourceUrl("https://news.naver.com/article/sample")
                    .title("샘플 기사")
                    .summary("샘플 기사 요약")
                    .publishDate(Instant.now())
                    .isDeleted(true)
                    .build()
            );
            String content = "댓글 등록 테스트";

            CommentRegisterRequest commentRegisterRequest = CommentRegisterRequest.builder()
                .userId(savedUser.getId())
                .articleId(savedArticle.getId())
                .content(content)
                .build();

            // when & then
            MvcResult result = mockMvc.perform(post("/api/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsBytes(commentRegisterRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ARTICLE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists())
                .andReturn();
        }
    }

    @Nested
    @DisplayName("댓글 목록 조회 테스트")
    class CommentListReadTest {

        @Test
        void 댓글_목록을_조회하면_댓글목록이_반환된다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();

            List<CommentDto> commentDtos = new ArrayList<>();

            for (int i=0; i<10; i++) {
                Comment comment = commentRepository.save(CommentFixture.createComment(i+"번째 댓글", savedUser, savedArticle));
                commentDtos.add(commentMapper.toDto(comment));
            }

            // when & then
            mockMvc.perform(get("/api/comments")
                    .param("articleId", savedArticle.getId().toString())
                    .param("orderBy", "createdAt")
                    .param("direction", "DESC")
                    .param("limit", "5")
                    .header("Monew-Request-User-ID", savedUser.getId().toString())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.nextCursor").exists())
                .andExpect(jsonPath("$.nextAfter").exists())
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value((long) 10))
                .andExpect(jsonPath("$.hasNext").value(true));
        }

        @Test
        void 정렬_기준이_유효하지_않다면_400을_반환해야_한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();

            List<CommentDto> commentDtos = new ArrayList<>();

            for (int i=0; i<10; i++) {
                Comment comment = commentRepository.save(CommentFixture.createComment(i+"번째 댓글", savedUser, savedArticle));
                commentDtos.add(commentMapper.toDto(comment));
            }

            String invalidSortBy = "invalidSortBy";

            // when & then
            mockMvc.perform(get("/api/comments")
                .param("articleId", savedArticle.getId().toString())
                .param("orderBy", invalidSortBy)
                .param("direction", "DESC")
                .param("limit", "5")
                .header("Monew-Request-User-ID", savedUser.getId().toString())
            )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_SORT_FIELD.name()))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void 정렬방향이_유효하지_않다면_400을_반환해야한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();

            List<CommentDto> commentDtos = new ArrayList<>();

            for (int i=0; i<10; i++) {
                Comment comment = commentRepository.save(CommentFixture.createComment(i+"번째 댓글", savedUser, savedArticle));
                commentDtos.add(commentMapper.toDto(comment));
            }

            String invalidSortDirection = "invalidSortDirection";

            // when & then
            mockMvc.perform(get("/api/comments")
                    .param("articleId", savedArticle.getId().toString())
                    .param("orderBy", "createdAt")
                    .param("direction", invalidSortDirection)
                    .param("limit", "5")
                    .header("Monew-Request-User-ID", savedUser.getId().toString())
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_SORT_DIRECTION.name()))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void 존재하지_않는_게시글_ID로_요청하면_404를_반환해야한다() throws Exception {

            // given
            User savedUser = createUser();
            UUID invalidArticleId = UUID.randomUUID();

            // when & then
            mockMvc.perform(get("/api/comments")
                    .param("articleId", invalidArticleId.toString())
                    .param("orderBy", "createdAt")
                    .param("direction", "DESC")
                    .param("limit", "5")
                    .header("Monew-Request-User-ID", savedUser.getId().toString())
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.ARTICLE_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("댓글 수정 테스트")
    class CommentUpdateTest {

        @Test
        void 댓글을_수정하면_Repository까지_반영되어야_한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();
            String originalContent = "기존 댓글";
            String updateContent = "수정 댓글";
            Comment savedComment = commentRepository.save(CommentFixture.createComment(originalContent, savedUser, savedArticle));

            CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
                .content(updateContent)
                .build();

            // when & then
            MvcResult result = mockMvc.perform(patch("/api/comments/" + savedComment.getId().toString())
                    .content(objectMapper.writeValueAsBytes(commentUpdateRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Monew-Request-User-ID", savedUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedComment.getId().toString()))
                .andExpect(jsonPath("$.articleId").value(savedArticle.getId().toString()))
                .andExpect(jsonPath("$.userId").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.userNickname").value(savedUser.getNickname()))
                .andExpect(jsonPath("$.content").value(updateContent))
                .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            UUID commentId = UUID.fromString(objectMapper.readTree(responseBody).get("id").asText());

            Comment registeredComment = commentRepository.findById(commentId).orElseThrow();
            assertThat(registeredComment.getContent()).isEqualTo(updateContent);
        }

        @Test
        void 댓글을_수정할_때_내용이_비어있으면_400을_반환해야_한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();
            String originalContent = "기존 댓글";
            String updateContent = "";
            Comment savedComment = commentRepository.save(CommentFixture.createComment(originalContent, savedUser, savedArticle));

            CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
                .content(updateContent)
                .build();

            // when & then
            MvcResult result = mockMvc.perform(patch("/api/comments/" + savedComment.getId().toString())
                    .content(objectMapper.writeValueAsBytes(commentUpdateRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Monew-Request-User-ID", savedUser.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists())
                .andReturn();
        }

        @Test
        void 댓글을_수정할_때_내용이_500자를_초과하면_400을_반환해야_한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();
            String originalContent = "기존 댓글";
            String updateContent = "1".repeat(501);
            Comment savedComment = commentRepository.save(CommentFixture.createComment(originalContent, savedUser, savedArticle));

            CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
                .content(updateContent)
                .build();

            // when & then
            MvcResult result = mockMvc.perform(patch("/api/comments/" + savedComment.getId().toString())
                    .content(objectMapper.writeValueAsBytes(commentUpdateRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Monew-Request-User-ID", savedUser.getId().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists())
                .andReturn();
        }

        @Test
        void 댓글을_수정할_때_존재하지_않는_댓글이라면_404가_반환되어야_한다() throws Exception {

            // given
            User savedUser = createUser();
            UUID invalidCommentId = UUID.randomUUID();
            String updateContent = "수정 댓글";

            CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
                .content(updateContent)
                .build();

            // when & then
            MvcResult result = mockMvc.perform(patch("/api/comments/" + invalidCommentId.toString())
                    .content(objectMapper.writeValueAsBytes(commentUpdateRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Monew-Request-User-ID", savedUser.getId().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists())
                .andReturn();
        }

        @Test
        void 댓글을_수정할_때_작성자가_아니라면_403을_반환해야_한다() throws Exception {

            // given
            User savedAuthor = createUser();
            Article savedArticle = createArticle();
            String originalContent = "기존 댓글";
            String updateContent = "수정 댓글";
            Comment savedComment = commentRepository.save(CommentFixture.createComment(originalContent, savedAuthor, savedArticle));
            UUID invalidAuthorId = UUID.randomUUID();

            CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
                .content(updateContent)
                .build();

            // when & then
            MvcResult result = mockMvc.perform(patch("/api/comments/" + savedComment.getId().toString())
                    .content(objectMapper.writeValueAsBytes(commentUpdateRequest))
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .header("Monew-Request-User-ID", invalidAuthorId.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.name()))
                .andExpect(jsonPath("$.message").exists())
                .andReturn();
        }
    }

    @Nested
    @DisplayName("댓글 논리 삭제 테스트")
    class CommentDeleteTest {

        @Test
        void 댓글을_논리삭제하면_Repository까지_반영되어야_한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();
            String content = "댓글 논리삭제 테스트";
            Comment savedComment = commentRepository.save(CommentFixture.createComment(content, savedUser, savedArticle));
            UUID commentId = savedComment.getId();

            // when & then
            mockMvc.perform(delete("/api/comments/" + commentId.toString())
                    .header("Monew-Request-User-ID", savedUser.getId()))
                .andExpect(status().isNoContent());

            Comment deletedComment = commentRepository.findById(commentId).orElseThrow();
            assertThat(deletedComment.getIsDeleted()).isTrue();
        }

        @Test
        void 댓글을_논리_삭제할_때_이미_삭제된_댓글이면_404가_반환되어야_한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();
            String content = "이미 삭제된 댓글";
            Comment savedComment = commentRepository.save(CommentFixture.createCommentWithIsDeleted(content, savedUser, savedArticle));
            UUID commentId = savedComment.getId();

            // when & then
            mockMvc.perform(delete("/api/comments/" + commentId.toString())
                    .header("Monew-Request-User-ID", savedUser.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists());
        }

        @Test
        void 댓글을_논리_삭제할_때_작성자가_아니라면_403을_반환해야_한다() throws Exception {

            // given
            User savedAuthor = createUser();
            Article savedArticle = createArticle();
            String content = "댓글 삭제 테스트";
            Comment savedComment = commentRepository.save(CommentFixture.createComment(content, savedAuthor, savedArticle));
            UUID commentId = savedComment.getId();
            UUID invalidAuthorId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/api/comments/" + commentId.toString())
                    .header("Monew-Request-User-ID", invalidAuthorId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN_ACCESS.name()))
                .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("댓글 물리 삭제 테스트")
    class CommentDeleteHardTest {

        @Test
        void 댓글을_물리삭제하면_연관된_좋아요도_삭제되어야_한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();
            String content = "댓글 물리삭제 테스트";
            Comment savedComment = commentRepository.save(
                CommentFixture.createComment(content, savedUser, savedArticle));
            UUID commentId = savedComment.getId();
            CommentLike savedCommentLike = commentLikeRepository.save(
                CommentLike.builder()
                    .comment(savedComment)
                    .user(savedUser)
                    .build()
            );
            UUID commentLikeId = savedCommentLike.getId();

            // when & then
            mockMvc.perform(delete("/api/comments/" + commentId.toString() + "/hard"))
                .andExpect(status().isNoContent());

            boolean existsComment = commentRepository.existsById(commentId);
            boolean existsLike = commentLikeRepository.existsById(commentLikeId);
            assertThat(existsComment).isFalse();
            assertThat(existsLike).isFalse();
        }

        @Test
        void 댓글을_물리삭제할_때_존재하지_않는_댓글이면_404가_반환되어야_한다() throws Exception {

            // given
            UUID invalidCommentId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/api/comments/" + invalidCommentId + "/hard"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("댓글 좋아요 테스트")
    class CommentLikeTest {

        @Test
        void 댓글에_좋아요를_누르면_Repository까지_반영되어야_한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();
            String content = "댓글 좋아요 테스트";
            Comment savedComment = commentRepository.save(CommentFixture.createComment(content, savedUser, savedArticle));
            UUID commentId = savedComment.getId();
            UUID userId = savedUser.getId();
            UUID articleId = savedArticle.getId();


            // when & then
            MvcResult result = mockMvc.perform(post("/api/comments/{commentId}/comment-likes", commentId)
                    .header("Monew-Request-User-ID", savedUser.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likedBy").value(userId.toString()))
                .andExpect(jsonPath("$.commentId").value(commentId.toString()))
                .andExpect(jsonPath("$.commentLikeCount").value(1L))
                .andExpect(jsonPath("$.articleId").value(articleId.toString()))
                .andExpect(jsonPath("$.commentUserId").value(userId.toString()))
                .andExpect(jsonPath("$.commentUserNickname").value(savedUser.getNickname()))
                .andExpect(jsonPath("$.commentContent").value(savedComment.getContent()))
                .andExpect(jsonPath("$.commentCreatedAt").exists())
                .andReturn();

            CommentLike commentLike = commentLikeRepository.findByUserIdAndCommentId(userId, commentId).orElseThrow();
            assertThat(commentLike).isNotNull();
        }

        @Test
        void 존재하지_않는_댓글에_좋아요를_요청시_404가_반환되어야_한다() throws Exception {

            // given
            UUID invalidCommentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            // when & then
            mockMvc.perform(post("/api/comments/{commentId}/comment-likes", invalidCommentId)
                    .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("댓글 좋아요 취소 테스트")
    class CommentLikeCancelTest {

        @Test
        void 댓글_좋아요_취소시_Repository까지_반영되어야_한다() throws Exception {

            // given
            User savedUser = createUser();
            Article savedArticle = createArticle();
            String content = "댓글 좋아요 테스트";
            Comment savedComment = commentRepository.save(CommentFixture.createComment(content, savedUser, savedArticle));
            CommentLike savedCommentLike = commentLikeRepository.save(CommentLikeFixture.createCommentLike(savedUser, savedComment));
            UUID commentId = savedComment.getId();
            UUID userId = savedUser.getId();

            // when & then
            mockMvc.perform(delete("/api/comments/{commentId}/comment-likes", commentId)
                    .header("Monew-Request-User-ID", userId.toString()))
                .andExpect(status().isNoContent());

            boolean existsCommentLike = commentLikeRepository.existsById(savedCommentLike.getId());
            assertThat(existsCommentLike).isFalse();
        }

        @Test
        void 존재하지_않는_댓글에_좋아요_취소_요청시_404가_반환되어야_한다() throws Exception {

            // given
            User savedUser = createUser();
            UUID invalidCommentId = UUID.randomUUID();

            // when & then
            mockMvc.perform(delete("/api/comments/{commentId}/comment-likes", invalidCommentId.toString())
                    .header("Monew-Request-User-ID", savedUser.getId().toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.COMMENT_NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").exists());
        }
    }

    private User createUser() {
        return userRepository.save(UserFixture.createUser());
    }

    private Article createArticle() {
        return articleRepository.save(ArticleFixture.createArticle());
    }
}
