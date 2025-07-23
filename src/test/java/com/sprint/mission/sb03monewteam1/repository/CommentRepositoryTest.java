package com.sprint.mission.sb03monewteam1.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.sprint.mission.sb03monewteam1.config.QueryDslConfig;
import com.sprint.mission.sb03monewteam1.config.TestJpaAuditConfig;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.User;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import com.sprint.mission.sb03monewteam1.exception.common.InvalidSortOptionException;
import com.sprint.mission.sb03monewteam1.fixture.ArticleFixture;
import com.sprint.mission.sb03monewteam1.fixture.CommentFixture;
import com.sprint.mission.sb03monewteam1.fixture.UserFixture;
import com.sprint.mission.sb03monewteam1.repository.jpa.comment.CommentRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ComponentScan(basePackages = "com.sprint.mission.sb03monewteam1.mapper")
@Import({TestJpaAuditConfig.class, QueryDslConfig.class})
@ActiveProfiles("test")
@DisplayName("CommentRepository 슬라이스 테스트")
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private TestEntityManager em;

    Article article;
    User user;
    Instant baseTime = Instant.now().minusSeconds(3000);

    @BeforeEach
    void setUp() {
        article = ArticleFixture.createArticle();
        user = UserFixture.createUser();

        em.persist(article);
        em.persist(user);

        for (int i = 0; i < 5; i++) {
            Comment comment = CommentFixture.createCommentWithCreatedAt(
                "test" + i,
                user,
                article,
                baseTime.plusSeconds(i * 1000)
            );
            em.persist(comment);
        }

        for (int i= 0 ; i < 5; i++) {
            Comment comment = CommentFixture.createCommentWithLikeCount(
                "test" + i,
                user,
                article,
                (long) i
            );
            em.persist(comment);
        }

        em.flush();
        em.clear();
    }

    @Test
    void createdAt_내림차순으로_댓글을_조회한다() {

        // given
        UUID savedArticleId = article.getId();

        int pageSize = 5;
        String sortBy = "createdAt";
        String sortDirection = "DESC";

        // when
        List<Comment> result = commentRepository.findCommentsWithCursorBySort(
            savedArticleId,
            null,
            null,
            pageSize,
            sortBy,
            sortDirection
        );

        // then
        assertThat(result).hasSize(pageSize);
        assertThat(result).isSortedAccordingTo(
            Comparator.comparing(Comment::getCreatedAt).reversed()
        );
    }

    @Test
    void createdAt_오름차순으로_댓글을_조회한다() {
        // given
        UUID savedArticleId = article.getId();

        int pageSize = 5;
        String sortBy = "createdAt";
        String sortDirection = "ASC";

        // when
        List<Comment> result = commentRepository.findCommentsWithCursorBySort(
            savedArticleId,
            null,
            null,
            pageSize,
            sortBy,
            sortDirection
        );

        // then
        assertThat(result).hasSize(pageSize);
        assertThat(result).isSortedAccordingTo(
            Comparator.comparing(Comment::getCreatedAt)
        );
    }

    @Test
    void likecount_내림차순으로_댓글을_조회한다() {

        // given
        UUID savedArticleId = article.getId();
        int pageSize = 5;
        String cursor = "2";
        String sortBy = "likeCount";
        String sortDirection = "DESC";

        // when
        List<Comment> result = commentRepository.findCommentsWithCursorBySort(
            savedArticleId,
            cursor,
            null,
            pageSize,
            sortBy,
            sortDirection
        );

        // then
        assertThat(result).hasSize(pageSize);
        assertThat(result).extracting(Comment::getLikeCount).allMatch(count -> count < 2L);
        assertThat(result).isSortedAccordingTo(
            Comparator.comparing(Comment::getLikeCount).reversed()
        );
    }


    @Test
    void likeCount기준_내림차순_정렬에서_nextAfter가_있을때_정확히_조회된다() {

        // given
        UUID savedArticleId = article.getId();
        int pageSize = 5;
        String cursor = "2";
        Instant nextAfter = baseTime.plusSeconds(4000);
        String sortBy = "likeCount";
        String sortDirection = "DESC";

        // when
        List<Comment> result = commentRepository.findCommentsWithCursorBySort(
            savedArticleId,
            cursor,
            nextAfter,
            pageSize,
            sortBy,
            sortDirection
        );

        // then
        assertThat(result).allSatisfy(comment -> {
            boolean valid =
                comment.getLikeCount() < 2 ||
                    (comment.getLikeCount() == 2 && comment.getCreatedAt().isBefore(nextAfter));
            assertThat(valid).isTrue();
        });
        assertThat(result)
            .isSortedAccordingTo(Comparator
                .comparing(Comment::getLikeCount).reversed()
            );
    }

    @Test
    void likecount_오름차순으로_댓글을_조회한다() {

        // given
        UUID savedArticleId = article.getId();
        int pageSize = 2;
        String cursor = "2";
        String sortBy = "likeCount";
        String sortDirection = "ASC";

        // when
        List<Comment> result = commentRepository.findCommentsWithCursorBySort(
            savedArticleId,
            cursor,
            null,
            pageSize,
            sortBy,
            sortDirection
        );

        // then
        assertThat(result).hasSize(pageSize);
        assertThat(result).extracting(Comment::getLikeCount).allMatch(count -> count > 2L);
        assertThat(result).isSortedAccordingTo(
            Comparator.comparing(Comment::getLikeCount)
        );
    }

    @Test
    void likeCount기준_오름차순_정렬에서_nextAfter가_있을때_정확히_조회된다() {

        // given
        UUID savedArticleId = article.getId();
        int pageSize = 5;
        String cursor = "2";
        Instant nextAfter = baseTime.plusSeconds(4000);
        String sortBy = "likeCount";
        String sortDirection = "ASC";

        // when
        List<Comment> result = commentRepository.findCommentsWithCursorBySort(
            savedArticleId,
            cursor,
            nextAfter,
            pageSize,
            sortBy,
            sortDirection
        );

        // then
        result.forEach(c ->
            System.out.printf("likeCount: %d, createdAt: %s%n", c.getLikeCount(), c.getCreatedAt()));

        assertThat(result).allSatisfy(comment -> {
            boolean valid =
                comment.getLikeCount() > 2 ||
                    (comment.getLikeCount() == 2 && comment.getCreatedAt().isAfter(nextAfter));
            assertThat(valid).isTrue();
        });
        assertThat(result)
            .isSortedAccordingTo(Comparator
                .comparing(Comment::getLikeCount)
            );
    }


    @Test
    void 커서가_주어졌을_때_이후_데이터만_조회된다_DESC_정렬() {

        // given
        UUID savedArticleId = article.getId();
        String cursor = baseTime.plusSeconds(5000).toString();
        int pageSize = 5;
        String sortBy = "createdAt";
        String sortDirection = "DESC";

        // when
        List<Comment> result = commentRepository.findCommentsWithCursorBySort(
            savedArticleId,
            cursor,
            null,
            pageSize,
            sortBy,
            sortDirection
        );

        // then
        Instant cursorInstant = Instant.parse(cursor);
        assertThat(result)
            .allMatch(c -> c.getCreatedAt().isBefore(cursorInstant));
        assertThat(result)
            .isSortedAccordingTo(Comparator.comparing(Comment::getCreatedAt).reversed());
    }

    @Test
    void 커서가_주어졌을_때_이후_데이터만_조회된다_ASC_정렬() {

        // given
        UUID savedArticleId = article.getId();
        String cursor = baseTime.plusSeconds(2000).toString();
        int pageSize = 5;
        String sortBy = "createdAt";
        String sortDirection = "ASC";

        // when
        List<Comment> result = commentRepository.findCommentsWithCursorBySort(
            savedArticleId,
            cursor,
            null,
            pageSize,
            sortBy,
            sortDirection
        );

        // then
        Instant cursorInstant = Instant.parse(cursor);
        assertThat(result)
            .allMatch(c -> c.getCreatedAt().isAfter(cursorInstant));
        assertThat(result)
            .isSortedAccordingTo(Comparator.comparing(Comment::getCreatedAt));
    }

    @Test
    void 잘못된_정렬기준_입력시_예외_발생한다_커서_없음() {

        // given
        UUID savedArticleId = article.getId();
        String cursor = null;
        int pageSize = 5;
        String invalidSortBy = "invalid_sortBy";
        String sortDirection = "DESC";

        // when & then
        assertThatThrownBy(() ->
            commentRepository.findCommentsWithCursorBySort(savedArticleId, cursor, null, pageSize, invalidSortBy, sortDirection)
        )
            .isInstanceOf(InvalidSortOptionException.class)
            .hasMessageContaining(ErrorCode.INVALID_SORT_FIELD.getMessage());
    }

    @Test
    void 잘못된_정렬기준_입력시_예외_발생한다_커서_있음() {

        // given
        UUID savedArticleId = article.getId();
        String cursor = baseTime.plusSeconds(2000).toString();
        int pageSize = 5;
        String invalidSortBy = "invalid_sortBy";
        String sortDirection = "DESC";

        // when & then
        assertThatThrownBy(() ->
            commentRepository.findCommentsWithCursorBySort(savedArticleId, cursor, null, pageSize, invalidSortBy, sortDirection)
        )
            .isInstanceOf(InvalidSortOptionException.class)
            .hasMessageContaining(ErrorCode.INVALID_SORT_FIELD.getMessage());
    }

    @Test
    void isDeleted_가_true인_댓글은_findById로_조회되지_않는다() {

        // given
        Comment deletedComment = CommentFixture.createComment("삭제 테스트", user, article);
        deletedComment.delete(); // isDeleted = true
        em.persist(deletedComment);
        em.flush();
        em.clear();
        UUID id = deletedComment.getId();

        // when
        Optional<Comment> result = commentRepository.findByIdAndIsDeletedFalse(id);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void isDeleted_가_false인_댓글은_findById로_조회된다() {

        // given
        Comment comment = CommentFixture.createComment("삭제 테스트", user, article);
        em.persist(comment);
        em.flush();
        em.clear();
        UUID id = comment.getId();

        // when
        Optional<Comment> result = commentRepository.findByIdAndIsDeletedFalse(id);

        // then
        assertThat(result).isPresent();
    }
}
