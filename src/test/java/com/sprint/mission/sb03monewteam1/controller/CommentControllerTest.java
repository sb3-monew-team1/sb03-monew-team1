package com.sprint.mission.sb03monewteam1.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.service.CommentService;
import java.util.UUID;
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

    @Nested
    @DisplayName("댓글 등록 태스트")
    class CommentCreateTest {

        @Test
        void 댓글을_등록하면_201과_DTO가_반환되어야_한다() throws Exception {

            // given
            String content = "댓글 생성 테스트";
            User user = UserFixture.createUser();
            Article article = ArticleFixture.createArticle();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

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
        void 댓글을_등록할_때_존재하지_않는_사용자라면_400가_반환되어야_한다() throws Exception {

            // given
            String content = "댓글 생성 테스트";
            Article article = ArticleFixture.createArticle();
            UUID articleId = article.getId();
            UUID invalidUserId = UUID.randomUUID();

            CommentRegisterRequest commentRegisterRequest = CommentFixture.createCommentRegisterRequest(content, invalidUserId, articleId);

            given(commentService.create(commentRegisterRequest))
                    .willThrow(new CommentException(ErrorCode.USER_NOT_FOUND));

            // when & then
            mockMvc.perform(post("/api/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(commentRegisterRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("사용자를 찾을 수 없습니다."));
        }

        @Test
        void 댓글을_등록할_때_존재하지_않는_뉴스기사라면_400가_반환되어야_한다() throws Exception {

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
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("ARTICLE_NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("기사를 찾을 수 없습니다."));
        }
    }
}
