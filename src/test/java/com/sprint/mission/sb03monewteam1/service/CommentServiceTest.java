package com.sprint.mission.sb03monewteam1.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;

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
import com.sprint.mission.sb03monewteam1.exception.comment.CommentAlreadyLikedException;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentException;
import com.sprint.mission.sb03monewteam1.exception.comment.CommentNotFoundException;
import com.sprint.mission.sb03monewteam1.exception.comment.UnauthorizedCommentAccessException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidCursorException;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidSortOptionException;
import com.sprint.mission.sb03monewteam1.exception.user.UserNotFoundException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentLikeFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.mapper.CommentLikeMapper;
import com.sprint.mission.sb03monewteam1.mapper.CommentMapper;
import com.sprint.mission.sb03monewteam1.repository.ArticleRepository;
import com.sprint.mission.sb03monewteam1.repository.CommentLikeRepository;
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
import org.assertj.core.api.Assertions;
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
    private CommentLikeRepository commentLikeRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private CommentLikeMapper commentLikeMapper;

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

            given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));
            given(articleRepository.findByIdAndIsDeletedFalse(articleId)).willReturn(Optional.of(article));
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

            given(userRepository.findByIdAndIsDeletedFalse(invalidUserId)).willReturn(Optional.empty());

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

            given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));
            given(articleRepository.findByIdAndIsDeletedFalse(invalidArticleId)).willReturn(Optional.empty());

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
            UUID articleId = UUID.randomUUID();
            Article article = ArticleFixture.createArticleWithId(articleId);
            User user = UserFixture.createUser();

            int pageSize = 5;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            List<Comment> commentList = createCommentsWithCreatedAt(10, article, user);

            List<Comment> sorted = commentList.stream()
                    .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                    .collect(Collectors.toList());

            List<Comment> firstPage = sorted.subList(0, pageSize + 1);

            given(articleRepository.findByIdAndIsDeletedFalse(articleId)).willReturn(Optional.of(article));
            given(commentRepository.findCommentsWithCursorBySort(
                eq(articleId), eq(null), eq(null), eq(pageSize + 1), eq(sortBy), eq(sortDirection)))
                .willReturn(firstPage);
            given(commentRepository.countByArticleId(articleId)).willReturn(10L);
            given(commentMapper.toDto(any(Comment.class))).willAnswer(invocation -> {
                Comment comment = invocation.getArgument(0);
                return CommentFixture.createCommentDto(comment);
                }
            );

            // when
            CursorPageResponse<CommentDto> result = commentService.getCommentsWithCursorBySort(
                articleId, null, null, pageSize, sortBy, sortDirection
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
            assertThat(result.nextCursor()).isEqualTo(sorted.get(pageSize).getCreatedAt().toString());
            assertThat(result.nextAfter()).isEqualTo(sorted.get(pageSize).getCreatedAt());
            assertThat(result.size()).isEqualTo(pageSize);
            assertThat(result.totalElements()).isEqualTo(10L);
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        void 커서없이_조회시_좋아요수기준_내림차순으로_반환한다() {

            // given
            UUID articleId = UUID.randomUUID();
            Article article = ArticleFixture.createArticleWithId(articleId);
            User user = UserFixture.createUser();
            int pageSize = 5;
            String sortBy = "likeCount";
            String sortDirection = "DESC";

            List<Comment> commentList = createCommentsWithLikeCount(10, article, user);

            List<Comment> sorted = commentList.stream()
                    .sorted(Comparator.comparing(Comment::getLikeCount).reversed())
                    .collect(Collectors.toList());

            List<Comment> firstPage = sorted.subList(0, pageSize + 1);

            given(articleRepository.findByIdAndIsDeletedFalse(articleId)).willReturn(Optional.of(article));
            given(commentRepository.findCommentsWithCursorBySort(
                eq(articleId), eq(null), eq(null), eq(pageSize + 1), eq(sortBy), eq(sortDirection)))
                .willReturn(firstPage);
            given(commentRepository.countByArticleId(articleId)).willReturn(10L);
            given(commentMapper.toDto(any(Comment.class))).willAnswer(invocation -> {
                    Comment comment = invocation.getArgument(0);
                    return CommentFixture.createCommentDto(comment);
                }
            );

            // when
            CursorPageResponse<CommentDto> result = commentService.getCommentsWithCursorBySort(
                articleId, null, null, pageSize, sortBy, sortDirection
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(pageSize);
            assertThat(result.nextCursor()).isEqualTo(sorted.get(pageSize).getLikeCount().toString());
            assertThat(result.nextAfter()).isEqualTo(sorted.get(pageSize).getCreatedAt());
            assertThat(result.size()).isEqualTo(pageSize);
            assertThat(result.totalElements()).isEqualTo(10L);
            assertThat(result.hasNext()).isTrue();
        }

        @Test
        void 커서기반으로_다음페이지를_정상조회한다() {

            // given
            UUID articleId = UUID.randomUUID();
            Article article = ArticleFixture.createArticleWithId(articleId);
            User user = UserFixture.createUser();
            int pageSize = 5;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            List<Comment> commentList = createCommentsWithCreatedAt(10, article, user);

            List<Comment> sorted = commentList.stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                .collect(Collectors.toList());

            List<Comment> firstPage = sorted.subList(0, pageSize + 1);
            List<Comment> secondPage = sorted.subList(pageSize, sorted.size());

            Comment lastOfFirstPage = firstPage.get(pageSize - 1);
            Instant cursor = lastOfFirstPage.getCreatedAt();
            Instant after = lastOfFirstPage.getCreatedAt();

            given(articleRepository.findByIdAndIsDeletedFalse(articleId)).willReturn(Optional.of(article));
            given(commentRepository.findCommentsWithCursorBySort(
                eq(articleId), eq(cursor.toString()), eq(after), eq(pageSize + 1), eq(sortBy), eq(sortDirection)))
                .willReturn(secondPage);
            given(commentRepository.countByArticleId(article.getId())).willReturn(10L);
            given(commentMapper.toDto(any(Comment.class))).willAnswer(invocation -> {
                    Comment comment = invocation.getArgument(0);
                    return CommentFixture.createCommentDto(comment);
                }
            );

            // when
            CursorPageResponse<CommentDto> result = commentService.getCommentsWithCursorBySort(
                articleId, cursor.toString(), after, pageSize, sortBy, sortDirection
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
            UUID articleId = UUID.randomUUID();
            Article article = ArticleFixture.createArticleWithId(articleId);
            User user = UserFixture.createUser();
            int pageSize = 5;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            List<Comment> commentList = createCommentsWithCreatedAt(10, article, user);

            List<Comment> sorted = commentList.stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt).reversed())
                .collect(Collectors.toList());

            Instant nextCursor = sorted.get(pageSize-1).getCreatedAt();
            Instant nextAfter = sorted.get(pageSize-1).getCreatedAt();

            List<Comment> lastPage = sorted.subList(pageSize, sorted.size());

            given(articleRepository.findByIdAndIsDeletedFalse(articleId)).willReturn(Optional.of(article));
            given(commentRepository.findCommentsWithCursorBySort(
                eq(articleId), eq(nextCursor.toString()), eq(nextAfter), eq(pageSize + 1), eq(sortBy), eq(sortDirection)))
                .willReturn(lastPage);
            given(commentRepository.countByArticleId(articleId)).willReturn(10L);
            given(commentMapper.toDto(any(Comment.class))).willAnswer(invocation -> {
                    Comment comment = invocation.getArgument(0);
                    return CommentFixture.createCommentDto(comment);
                }
            );

            // when
            CursorPageResponse<CommentDto> result = commentService.getCommentsWithCursorBySort(
                articleId, nextCursor.toString(), nextAfter, pageSize, sortBy, sortDirection
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

            assertThat(result.nextCursor()).isNull();
            assertThat(result.nextAfter()).isNull();
            assertThat(result.size()).isEqualTo(pageSize);
            assertThat(result.totalElements()).isEqualTo(10L);
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        void 댓글이_없는_기사를_조회하면_빈_리스트를_반환한다() {

            // given
            UUID articleId = UUID.randomUUID();
            Article article = ArticleFixture.createArticleWithId(articleId);
            int pageSize = 5;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            given(articleRepository.findByIdAndIsDeletedFalse(articleId)).willReturn(Optional.of(article));
            given(commentRepository.findCommentsWithCursorBySort(
                eq(articleId), eq(null), eq(null), eq(pageSize + 1), eq(sortBy), eq(sortDirection)))
                .willReturn(Collections.emptyList());
            given(commentRepository.countByArticleId(article.getId())).willReturn(0L);

            // when
            CursorPageResponse<CommentDto> result = commentService.getCommentsWithCursorBySort(
                articleId, null, null, pageSize, sortBy, sortDirection
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEmpty();
            assertThat(result.nextCursor()).isNull();
            assertThat(result.nextAfter()).isNull();
            assertThat(result.size()).isEqualTo(pageSize);
            assertThat(result.totalElements()).isEqualTo(0L);
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        void 댓글_수가_페이지_크기와_일치할_때_hasNext는_false를_반환한다() {

            // given
            UUID articleId = UUID.randomUUID();
            Article article = ArticleFixture.createArticleWithId(articleId);
            User user = UserFixture.createUser();
            int pageSize = 5;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            List<Comment> commentList = createCommentsWithCreatedAt(pageSize, article, user);

            given(articleRepository.findByIdAndIsDeletedFalse(articleId)).willReturn(Optional.of(article));
            given(commentRepository.findCommentsWithCursorBySort(
                eq(articleId), eq(null), eq(null), eq(pageSize + 1), eq(sortBy), eq(sortDirection)))
                .willReturn(commentList.subList(0, pageSize));
            given(commentRepository.countByArticleId(articleId)).willReturn(5L);

            // when
            CursorPageResponse<CommentDto> result = commentService.getCommentsWithCursorBySort(
                articleId, null, null, pageSize, sortBy, sortDirection
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
            UUID articleId = UUID.randomUUID();
            Article article = ArticleFixture.createArticleWithId(articleId);
            User user = UserFixture.createUser();
            String invalidCursor = "invalid-value";
            int pageSize = 5;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            List<Comment> comments = createCommentsWithCreatedAt(pageSize, article, user);

            given(articleRepository.findByIdAndIsDeletedFalse(articleId)).willReturn(Optional.of(article));

            // when & then
            assertThatThrownBy(() ->
                commentService.getCommentsWithCursorBySort(articleId, invalidCursor, null, pageSize, sortBy, sortDirection)
            ).isInstanceOf(InvalidCursorException.class)
                .hasMessageContaining(ErrorCode.INVALID_CURSOR_DATE.getMessage());
        }

        @Test
        void 유효하지_않은_페이지_크기로_조회시_예외가_발생한다() {

            // given
            UUID articleId = UUID.randomUUID();
            Article article = ArticleFixture.createArticleWithId(articleId);
            int invalidPageSize = -1;
            String sortBy = "createdAt";
            String sortDirection = "DESC";

            given(articleRepository.findByIdAndIsDeletedFalse(articleId)).willReturn(Optional.of(article));

            // when & then
            assertThatThrownBy(() ->
                commentService.getCommentsWithCursorBySort(articleId, null, null, invalidPageSize, sortBy, sortDirection)
            ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("페이지 크기는 1 이상이어야 합니다");
        }

        @Test
        void 잘못된_정렬기준_입력시_예외가_발생한다() {

            // given
            UUID articleId = UUID.randomUUID();
            Article article = ArticleFixture.createArticleWithId(articleId);
            String invalidSort = "unknownField"; // 허용되지 않은 정렬 기준
            int pageSize = 5;
            String sortDirection = "DESC";

            given(articleRepository.findByIdAndIsDeletedFalse(articleId)).willReturn(Optional.of(article));

            // when & then
            assertThatThrownBy(() ->
                commentService.getCommentsWithCursorBySort(articleId, null, null, pageSize, invalidSort, sortDirection)
            )
                .isInstanceOf(InvalidSortOptionException.class)
                .hasMessageContaining(ErrorCode.INVALID_SORT_FIELD.getMessage());
        }

        @Test
        void 잘못된_정렬방향_입력시_예외가_발생한다() {

            // given
            UUID articleId = UUID.randomUUID();
            Article article = ArticleFixture.createArticleWithId(articleId);
            int pageSize = 5;
            String sortBy = "createdAt";
            String invalidSortDirection = "unknownDirection";  // 허용되지 않은 정렬 방향

            given(articleRepository.findByIdAndIsDeletedFalse(articleId)).willReturn(Optional.of(article));

            // when & then
            assertThatThrownBy(() ->
                commentService.getCommentsWithCursorBySort(articleId, null, null, pageSize, sortBy, invalidSortDirection)
            )
                .isInstanceOf(InvalidSortOptionException.class)
                .hasMessageContaining(ErrorCode.INVALID_SORT_DIRECTION.getMessage());
        }
    }

    @Nested
    @DisplayName("댓글 수정 테스트")
    class CommentUpdateTest {

        @Test
        void 댓글을_수정하면_수정된_CommentDTO를_반환해야_한다() {

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

            given(commentRepository.findByIdAndIsDeletedFalse(commentId)).willReturn(Optional.of(comment));
            given(commentMapper.toDto(any(Comment.class))).willReturn(expectedCommentDto);

            // when
            CommentDto result = commentService.update(commentId, userId, commentUpdateRequest);

            // then
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(commentId);
            assertThat(result.content()).isEqualTo(updateContent);
        }

        @Test
        void 댓글을_수정할_때_존재하지_않는_댓글이라면_예외가_발생한다() {

            // given
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
            UUID userId = user.getId();
            String updateContent = "댓글 수정 테스트";
            UUID invalidCommentId = UUID.randomUUID();

            CommentUpdateRequest commentUpdateRequest = CommentUpdateRequest.builder()
                .content(updateContent)
                .build();

            given(commentRepository.findByIdAndIsDeletedFalse(invalidCommentId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() ->
                commentService.update(invalidCommentId, userId, commentUpdateRequest
                )).isInstanceOf(CommentNotFoundException.class)
                .hasMessageContaining(ErrorCode.COMMENT_NOT_FOUND.getMessage());
        }

        @Test
        void 댓글을_수정할_때_작성자가_아니라면_예외가_발생한다() {

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

            given(commentRepository.findByIdAndIsDeletedFalse(commentId)).willReturn(Optional.of(comment));

            // when & then
            assertThatThrownBy(() ->
                commentService.update(commentId, otherUserId, commentUpdateRequest
                )).isInstanceOf(UnauthorizedCommentAccessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN_ACCESS.getMessage());
        }
    }

    @Nested
    @DisplayName("댓글 논리 삭제 테스트")
    class CommentDeleteTest {

        @Test
        void 댓글을_논리삭제하면_isDeleted가_true가_된다() {

            // given
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

            Article article = ArticleFixture.createArticleWithCommentCount(1L);
            ReflectionTestUtils.setField(article, "id", UUID.randomUUID());

            String content = "댓글 논리 삭제 테스트";
            Comment comment = CommentFixture.createComment(content, user, article);
            ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
            UUID commentId = comment.getId();

            given(commentRepository.findByIdAndIsDeletedFalse(commentId)).willReturn(Optional.of(comment));

            // when
            Comment result = commentService.delete(commentId, user.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getIsDeleted()).isTrue();
            assertThat(article.getCommentCount()).isEqualTo(0L);
        }

        @Test
        void 존재하지_않는_댓글_삭제_요청시_예외를_던진다() {

            // given
            UUID commentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(commentRepository.findByIdAndIsDeletedFalse(commentId)).willReturn(Optional.empty());

            // when & then
            Assertions.assertThatThrownBy(() -> {
                commentService.delete(commentId, userId);
                }).isInstanceOf(CommentNotFoundException.class);

            then(commentRepository).should().findByIdAndIsDeletedFalse(commentId);
        }

        @Test
        void 이미_삭제된_댓글_삭제_요청시_예외를_던진다() {

            // given
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

            Article article = ArticleFixture.createArticleWithCommentCount(1L);
            ReflectionTestUtils.setField(article, "id", UUID.randomUUID());

            Comment deletedComment = CommentFixture.createCommentWithIsDeleted("삭제 테스트", user, article);
            ReflectionTestUtils.setField(deletedComment, "id", UUID.randomUUID());
            UUID deletedCommentId = deletedComment.getId();

            given(commentRepository.findByIdAndIsDeletedFalse(deletedCommentId)).willReturn(Optional.empty());

            // when & then
            Assertions.assertThatThrownBy(() -> {
                commentService.delete(deletedCommentId, user.getId());
            }).isInstanceOf(CommentNotFoundException.class);

            then(commentRepository).should().findByIdAndIsDeletedFalse(deletedCommentId);
        }

        @Test
        void 다른_사용자가_댓글_논리삭제_요청시_권한_예외를_던진다() {

            // given
            UUID authorId = UUID.randomUUID();
            UUID otherUserId = UUID.randomUUID();
            UUID commentId = UUID.randomUUID();

            User author = UserFixture.createUser();
            ReflectionTestUtils.setField(author, "id", authorId);

            Article article = ArticleFixture.createArticleWithCommentCount(1L);
            ReflectionTestUtils.setField(article, "id", UUID.randomUUID());

            Comment comment = CommentFixture.createComment("댓글", author, article);
            ReflectionTestUtils.setField(comment, "id", commentId);

            given(commentRepository.findByIdAndIsDeletedFalse(commentId)).willReturn(Optional.of(comment));

            // when & then
            Assertions.assertThatThrownBy(() -> {
                    commentService.delete(commentId, otherUserId);
                }).isInstanceOf(UnauthorizedCommentAccessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN_ACCESS.getMessage());

            then(commentRepository).should().findByIdAndIsDeletedFalse(commentId);
        }
    }

    @Nested
    @DisplayName("댓글 물리 삭제 테스트")
    class CommentHardDeleteTest {

        @Test
        void 댓글을_물리삭제하면_좋아요와_댓글이_함께_삭제되고_기사_댓글수가_1감소한다() {

            // given
            UUID commentId = UUID.randomUUID();
            User user = UserFixture.createUser();
            Article article = ArticleFixture.createArticleWithCommentCount(1L);
            ReflectionTestUtils.setField(article, "id", UUID.randomUUID());
            Comment comment = CommentFixture.createComment("댓글 물리 삭제 테스트", user, article);
            ReflectionTestUtils.setField(comment, "id", commentId);

            given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
            willDoNothing().given(commentLikeRepository).deleteByCommentId(commentId);
            willDoNothing().given(commentRepository).deleteById(commentId);

            // when
            commentService.deleteHard(commentId);

            // then
            assertThat(article.getCommentCount()).isEqualTo(0L);
            then(commentRepository).should().findById(commentId);
            then(commentLikeRepository).should().deleteByCommentId(commentId);
            then(commentRepository).should().deleteById(commentId);
        }

        @Test
        void 존재하지_않는_댓글_물리삭제_요청시_예외를_던진다() {

            // given
            UUID invalidCommentId = UUID.randomUUID();

            given(commentRepository.findById(invalidCommentId)).willReturn(Optional.empty());

            // when & then
            Assertions.assertThatThrownBy(() -> {
                commentService.deleteHard(invalidCommentId);
            }).isInstanceOf(CommentNotFoundException.class);

            then(commentRepository).should().findById(invalidCommentId);
        }

        @Test
        void 논리삭제된_댓글은_물리삭제시_기사_댓글수가_감소하지_않는다() {

            // given
            UUID commentId = UUID.randomUUID();
            User user = UserFixture.createUser();
            Article article = ArticleFixture.createArticleWithCommentCount(1L);

            Comment deletedComment = CommentFixture.createComment("삭제된 댓글", user, article);
            deletedComment.delete();
            ReflectionTestUtils.setField(deletedComment, "id", commentId);

            given(commentRepository.findById(commentId)).willReturn(Optional.of(deletedComment));
            willDoNothing().given(commentLikeRepository).deleteByCommentId(commentId);
            willDoNothing().given(commentRepository).deleteById(commentId);

            // when & then
            commentService.deleteHard(commentId);

            // then
            assertThat(article.getCommentCount()).isEqualTo(1L);
            then(commentRepository).should().findById(commentId);
            then(commentLikeRepository).should().deleteByCommentId(commentId);
            then(commentRepository).should().deleteById(commentId);
        }
    }

    @Nested
    @DisplayName("댓글 좋아요 테스트")
    class CommentLikeTest {

        @Test
        void 댓글에_좋아요시_좋아요_객체와_댓글의_좋아요수가_1증가한다() {

            // given
            UUID commentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            UUID commentLikeId = UUID.randomUUID();

            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", userId);
            Article article = ArticleFixture.createArticleWithId(UUID.randomUUID());
            Comment comment = CommentFixture.createComment("좋아요 테스트", user, article);
            ReflectionTestUtils.setField(comment, "id", commentId);

            CommentLike savedCommentLike = CommentLikeFixture.createCommentLike(user, comment);
            ReflectionTestUtils.setField(savedCommentLike, "id", commentLikeId);
            CommentLikeDto expectedCommentLikeDto = CommentLikeFixture.createCommentLikeDto(savedCommentLike);

            given(commentRepository.findByIdAndIsDeletedFalse(commentId)).willReturn(Optional.of(comment));
            given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));
            given(commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)).willReturn(false);
            given(commentLikeRepository.save(any(CommentLike.class))).willReturn(savedCommentLike);
            given(commentLikeMapper.toDto(any(CommentLike.class))).willReturn(expectedCommentLikeDto);

            // when
            CommentLikeDto result = commentService.like(commentId, userId);

            // then
            assertThat(result).isEqualTo(expectedCommentLikeDto);
            assertThat(comment.getLikeCount()).isEqualTo(1L);
        }

        @Test
        void 존재하지_않는_댓글에_좋아요시_예외를_던진다() {

            // given
            UUID invalidCommentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();

            given(commentRepository.findByIdAndIsDeletedFalse(invalidCommentId)).willReturn(Optional.empty());

            // when & then
            Assertions.assertThatThrownBy(() -> {
                commentService.like(invalidCommentId, userId);
            }).isInstanceOf(CommentNotFoundException.class);

            then(commentRepository).should().findByIdAndIsDeletedFalse(invalidCommentId);
        }

        @Test
        void 존재하지_않는_사용자가_댓글_좋아요시_예외를_던진다() {

            // given
            UUID commentId = UUID.randomUUID();
            UUID invalidUserId = UUID.randomUUID();

            User user = UserFixture.createUser();
            Article article = ArticleFixture.createArticleWithId(UUID.randomUUID());
            Comment comment = CommentFixture.createComment("좋아요 테스트", user, article);
            ReflectionTestUtils.setField(comment, "id", commentId);

            given(commentRepository.findByIdAndIsDeletedFalse(commentId)).willReturn(Optional.of(comment));
            given(userRepository.findByIdAndIsDeletedFalse(invalidUserId)).willReturn(Optional.empty());

            // when & then
            Assertions.assertThatThrownBy(() -> {
                commentService.like(commentId, invalidUserId);
            }).isInstanceOf(UserNotFoundException.class);

            then(commentRepository).should().findByIdAndIsDeletedFalse(commentId);
            then(userRepository).should().findByIdAndIsDeletedFalse(invalidUserId);
        }

        @Test
        void 이미_좋아요를_누른_댓글이면_예외를_던진다() {

            // given
            UUID commentId = UUID.randomUUID();
            UUID userId = UUID.randomUUID();
            User user = UserFixture.createUser();
            ReflectionTestUtils.setField(user, "id", userId);
            Article article = ArticleFixture.createArticleWithId(UUID.randomUUID());
            Comment comment = CommentFixture.createComment("좋아요 테스트", user, article);
            ReflectionTestUtils.setField(comment, "id", commentId);

            given(commentRepository.findByIdAndIsDeletedFalse(commentId)).willReturn(Optional.of(comment));
            given(userRepository.findByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));
            given(commentLikeRepository.existsByCommentIdAndUserId(commentId, userId)).willReturn(true);

            // when & then
            Assertions.assertThatThrownBy(() -> {
                commentService.like(commentId, userId);
            }).isInstanceOf(CommentAlreadyLikedException.class);

            then(commentRepository).should().findByIdAndIsDeletedFalse(commentId);
            then(userRepository).should().findByIdAndIsDeletedFalse(userId);
            then(commentLikeRepository).should().existsByCommentIdAndUserId(commentId, userId);
        }
    }

    private List<Comment> createCommentsWithCreatedAt(int count, Article article, User user) {
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Instant createdAt = Instant.now().plusMillis(i);
            Comment comment = CommentFixture.createCommentWithCreatedAt("test" + i, user, article, createdAt);
            ReflectionTestUtils.setField(comment, "createdAt", createdAt);
            ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
            comments.add(comment);
        }
        return comments;
    }

    private List<Comment> createCommentsWithLikeCount(int count, Article article, User user) {
        List<Comment> comments = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Comment comment = CommentFixture.createCommentWithLikeCount("test" + i, user, article, (long) i);
            ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
            comments.add(comment);
        }
        return comments;
    }
}
