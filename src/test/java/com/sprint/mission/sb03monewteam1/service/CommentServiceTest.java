package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponse;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.dto.response.CursorPageResponseCommentDto;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.article.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentException;
import com.sprint.mission.sb03monewteam1.exception.comment.InvalidSortOptionException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.mapper.CommentMapper;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.CommentRepository;
import com.sprint.mission.sb03monewteam1.repository.UserRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
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
    @DisplayName("댓글 등록 테스트")
    class CommentCreateTest {

        @Test
        void 댓글을_등록하면_CommentDto를_반환해야한다() {

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

        @Test
        void 댓글을_등록할_때_존재하지_않는_사용자라면_예외가_발생한다() {

            // given
            String content = "댓글 생성 테스트";
            Article article = ArticleFixture.createArticle();
            UUID articleId = article.getId();
            UUID invalidUserId = UUID.randomUUID();

            CommentRegisterRequest commentRegisterRequest = CommentFixture.createCommentRegisterRequest(content, invalidUserId, articleId);

            given(userRepository.findById(invalidUserId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.create(commentRegisterRequest))
                    .isInstanceOf(CommentException.class)
                    .extracting(e -> ((CommentException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        void 댓글을_등록할_때_존재하지_않는_뉴스기사라면_예외가_발생한다() {

            // given
            String content = "댓글 생성 테스트";
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

            UUID userId = user.getId();
            UUID invalidArticleId = UUID.randomUUID();

            CommentRegisterRequest commentRegisterRequest = CommentFixture.createCommentRegisterRequest(content, userId, invalidArticleId);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(articleRepository.findById(invalidArticleId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> commentService.create(commentRegisterRequest))
                    .isInstanceOf(CommentException.class)
                    .extracting(e -> ((CommentException) e).getErrorCode())
                    .isEqualTo(ErrorCode.ARTICLE_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("댓글 목록 조회 테스트")
    class CommentListReadTest {

        @Test
        void 커서없이_조회시_날짜기준_최신순으로_반환한다() throws InterruptedException {

            // given
            Article article = ArticleFixture.createArticle();
            User user = UserFixture.createUser();
            int pageSize = 5;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            List<Comment> commentList = new ArrayList<>();

            for (int i=0;i<10;i++) {
                Comment comment = CommentFixture.createCommentWithCreatedAt("test" + i, user, article, Instant.now().plusMillis(i));
                commentList.add(comment);
            }

            List<Comment> sorted = commentList.stream()
                    .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                    .collect(Collectors.toList());

            List<Comment> firstPage = sorted.subList(0, pageSize);

            given(commentRepository.findCommentsWithCursorBySort(
                eq(article.getId()), eq(null), eq(null), eq(pageSize), eq(sortBy), eq(sortDirection)))
                .willReturn(firstPage);
            given(commentRepository.countByArticleId(article.getId())).willReturn(10L);

            // when
            CursorPageResponseCommentDto result = commentService.getCommentsWithCursorBySort(
                article.getId(), null, null, pageSize, sortBy, sortDirection
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(pageSize);

            List<String> expectedContents = sorted.subList(0, pageSize).stream()
                .map(Comment::getContent)
                .toList();

            List<String> actualContents = result.content().stream()
                .map(CommentDto::content)
                .toList();

            assertThat(actualContents).isEqualTo(expectedContents);
            assertThat(result.nextCursor()).isEqualTo(sorted.get(pageSize-1).getId().toString());
            assertThat(result.nextAfter()).isEqualTo(sorted.get(pageSize-1).getCreatedAt());
            assertThat(result.size()).isEqualTo(pageSize);
            assertThat(result.totalElements()).isEqualTo(10L);
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        void 커서없이_조회시_좋아요수기준_내림차순으로_반환한다() {

            // given
            Article article = ArticleFixture.createArticle();
            User user = UserFixture.createUser();
            int pageSize = 5;
            String sortBy = "likeCount";
            String sortDirection = "DESC";

            List<Comment> commentList = new ArrayList<>();

            for (int i=0;i<10;i++) {
                long likeCount = i;
                Comment comment = CommentFixture.createCommentWithLikeCount("test" + i, user, article, likeCount);
                commentList.add(comment);
            }

            List<Comment> sorted = commentList.stream()
                    .sorted(Comparator.comparing(Comment::getLikeCount).reversed())
                    .collect(Collectors.toList());

            List<Comment> firstPage = sorted.subList(0, pageSize);

            given(commentRepository.findCommentsWithCursorBySort(
                eq(article.getId()), eq(null), eq(null), eq(pageSize), eq(sortBy), eq(sortDirection)))
                .willReturn(firstPage);
            given(commentRepository.countByArticleId(article.getId())).willReturn(10L);

            // when
            CursorPageResponseCommentDto result = commentService.getCommentsWithCursorBySort(
                article.getId(), null, null, pageSize, sortBy, sortDirection
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(pageSize);
            assertThat(result.nextCursor()).isEqualTo(sorted.get(pageSize-1).getId().toString());
            assertThat(result.nextAfter()).isEqualTo(sorted.get(pageSize-1).getCreatedAt());
            assertThat(result.size()).isEqualTo(pageSize);
            assertThat(result.totalElements()).isEqualTo(10L);
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        void 커서기반으로_다음페이지를_정상조회한다() {

            // given
            Article article = ArticleFixture.createArticle();
            User user = UserFixture.createUser();
            int pageSize = 5;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            List<Comment> commentList = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                Comment comment = CommentFixture.createCommentWithCreatedAt("test" + i, user, article, Instant.now().plusMillis(i));
                commentList.add(comment);
            }

            List<Comment> sorted = commentList.stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                .collect(Collectors.toList());

            List<Comment> firstPage = sorted.subList(0, pageSize);
            List<Comment> secondPage = sorted.subList(pageSize, 10);

            UUID cursor = firstPage.get(pageSize - 1).getId();
            Instant after = firstPage.get(pageSize - 1).getCreatedAt();

            given(commentRepository.findCommentsWithCursorBySort(
                eq(article.getId()), eq(cursor.toString()), eq(after), eq(pageSize), eq(sortBy), eq(sortDirection)))
                .willReturn(secondPage);
            given(commentRepository.countByArticleId(article.getId())).willReturn(10L);

            // when
            CursorPageResponseCommentDto result = commentService.getCommentsWithCursorBySort(
                article.getId(), cursor.toString(), after, pageSize, sortBy, sortDirection
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(5);

            List<String> expectedContents = secondPage.stream()
                .map(Comment::getContent)
                .toList();

            List<String> actualContents = result.content().stream()
                .map(CommentDto::content)
                .toList();

            assertThat(actualContents).isEqualTo(expectedContents);
            assertThat(result.totalElements()).isEqualTo(10L);
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        void 커서기반으로_마지막페이지를_정상조회한다() {

            // given
            Article article = ArticleFixture.createArticle();
            User user = UserFixture.createUser();
            int pageSize = 5;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            List<Comment> commentList = new ArrayList<>();

            for (int i = 0; i < 10; i++) {
                Comment comment = CommentFixture.createCommentWithCreatedAt("test" + i, user, article, Instant.now().plusMillis(i));
                commentList.add(comment);
            }

            List<Comment> sorted = commentList.stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                .collect(Collectors.toList());

            UUID nextCursor = sorted.get(pageSize-1).getId();
            Instant nextAfter = sorted.get(pageSize-1).getCreatedAt();

            List<Comment> lastPage = sorted.subList(pageSize, 10);

            given(commentRepository.findCommentsWithCursorBySort(
                eq(article.getId()), eq(nextCursor.toString()), eq(nextAfter), eq(pageSize), eq(sortBy), eq(sortDirection)))
                .willReturn(lastPage);
            given(commentRepository.countByArticleId(article.getId())).willReturn(10L);

            // when
            CursorPageResponseCommentDto result = commentService.getCommentsWithCursorBySort(
                article.getId(), nextCursor.toString(), nextAfter, pageSize, sortBy, sortDirection
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(pageSize);

            List<String> expectedContents = lastPage.stream()
                .map(Comment::getContent)
                .toList();
            List<String> actualContents = result.content().stream()
                .map(CommentDto::content)
                .toList();
            assertThat(actualContents).isEqualTo(expectedContents);

            assertThat(result.nextCursor()).isEqualTo(lastPage.get(pageSize - 1).getId().toString());
            assertThat(result.nextAfter()).isEqualTo(lastPage.get(pageSize - 1).getCreatedAt());
            assertThat(result.size()).isEqualTo(pageSize);
            assertThat(result.totalElements()).isEqualTo(10L);
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        void 댓글이_없는_기사를_조회하면_빈_리스트를_반환한다() {

            // given
            Article article = ArticleFixture.createArticle();
            int pageSize = 5;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            given(commentRepository.findCommentsWithCursorBySort(
                eq(article.getId()), eq(null), eq(null), eq(pageSize), eq(sortBy), eq(sortDirection)))
                .willReturn(Collections.emptyList());
            given(commentRepository.countByArticleId(article.getId())).willReturn(0L);

            // when
            CursorPageResponseCommentDto result = commentService.getCommentsWithCursorBySort(
                article.getId(), null, null, pageSize, sortBy, sortDirection
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEmpty();
            assertThat(result.nextCursor()).isNull();
            assertThat(result.nextAfter()).isNull();
            assertThat(result.size()).isEqualTo(0);
            assertThat(result.totalElements()).isEqualTo(0L);
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        void 댓글_수가_페이지_크기와_일치할_때_hasNext는_false를_반환한다() {

            // given
            Article article = ArticleFixture.createArticle();
            User user = UserFixture.createUser();
            int pageSize = 5;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            List<Comment> commentList = new ArrayList<>();

            for (int i = 0; i < pageSize; i++) {
                Comment comment = CommentFixture.createCommentWithCreatedAt("test" + i, user, article, Instant.now().plusMillis(i));
                commentList.add(comment);
            }

            given(commentRepository.findCommentsWithCursorBySort(
                eq(article.getId()), eq(null), eq(null), eq(pageSize), eq(sortBy), eq(sortDirection)))
                .willReturn(commentList.subList(0, pageSize));
            given(commentRepository.countByArticleId(article.getId())).willReturn(5L);

            // when
            CursorPageResponseCommentDto result = commentService.getCommentsWithCursorBySort(
                article.getId(), null, null, pageSize, sortBy, sortDirection
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(5);
            assertThat(result.totalElements()).isEqualTo(5L);
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        void 유효하지_않은_커서로_조회시_예외가_발생한다() {

            // given
            Article article = ArticleFixture.createArticle();
            String invalidCursor = "invalid-uuid"; // UUID 형식이 아닌 커서
            int pageSize = 5;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            // when & then
            assertThatThrownBy(() ->
                commentService.getCommentsWithCursorBySort(article.getId(), invalidCursor, null, pageSize, sortBy, sortDirection)
            ).isInstanceOf(InvalidCursorException.class)
                .hasMessageContaining("유효하지 않은 커서 값입니다.");
        }

        @Test
        void 유효하지_않은_페이지_크기로_조회시_예외가_발생한다() {

            // given
            Article article = ArticleFixture.createArticle();
            int invalidPageSize = -1;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            // when & then
            assertThatThrownBy(() ->
                commentService.getCommentsWithCursorBySort(article.getId(), null, null, invalidPageSize, sortBy, sortDirection)
            ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("페이지 크기는 1 이상이어야 합니다");
        }

        @Test
        void 잘못된_정렬기준_입력시_예외가_발생한다() {

            // given
            Article article = ArticleFixture.createArticle();
            String invalidSort = "unknownField"; // 허용되지 않은 정렬 기준
            int pageSize = 5;
            String sortDirection = "DESC";

            // when & then
            assertThatThrownBy(() ->
                commentService.getCommentsWithCursorBySort(article.getId(), null, null, pageSize, invalidSort, sortDirection)
            ).isInstanceOf(InvalidSortOptionException.class)
                .hasMessageContaining("허용되지 않은 정렬 기준입니다");
        }
    }
}
