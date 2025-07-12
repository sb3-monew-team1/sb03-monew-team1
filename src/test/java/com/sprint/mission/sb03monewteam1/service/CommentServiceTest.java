package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.mapper.CommentMapper;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("CommentService 슬라이스 테스트")
@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @BeforeEach
    void setUp() {

    }

    @Nested
    @DisplayName("댓글 생성 테스트")
    class CommentCreateTest {

        @Test
        void 댓글을_생성하면_CommentDto를_반환해야한다() {

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

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(articleRepository.findById(articleId)).willReturn(Optional.of(article));
            given(commentRepository.save(any(Comment.class))).willReturn(savedComment);
            given(commentMapper.toDto(any(Comment.class))).willReturn(expectedCommentDto);

            // when
            CommentDto result = commentService.create(commentRegisterRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo(expectedCommentDto.content());
            assertThat(result.articleId()).isEqualTo(expectedCommentDto.articleId());
            assertThat(result.userId()).isEqualTo(expectedCommentDto.userId());
            assertThat(result.likeCount()).isEqualTo(expectedCommentDto.likeCount());
            assertThat(result.likedByMe()).isEqualTo(expectedCommentDto.likedByMe());
            assertThat(result.createdAt()).isEqualTo(expectedCommentDto.createdAt());
        }
    }
}
