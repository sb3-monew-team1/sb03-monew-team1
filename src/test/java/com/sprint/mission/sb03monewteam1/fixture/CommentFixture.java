package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.User;
import java.time.Instant;
import java.util.UUID;

public class CommentFixture {

    private static final UUID DEFAULT_COMMENT_ID = UUID.randomUUID();
    private static final Long DEFAULT_LIKE_COUNT = 5L;
    private static final String DEFAULT_COMMENT = "테스트 댓글";

    public static Comment createComment(User user, Article article) {
        return Comment.builder()
            .content(DEFAULT_COMMENT)
            .author(user)
            .article(article)
            .likeCount(DEFAULT_LIKE_COUNT)
            .build();
    }

    public static Comment createComment(String content, User user, Article article) {
        return Comment.builder()
            .content(content)
            .author(user)
            .article(article)
            .build();
    }

    public static Comment createCommentWithLikeCount(String content, User user, Article article,
        Long count) {
        return Comment.builder()
            .content(content)
            .author(user)
            .article(article)
            .likeCount(count)
            .build();
    }

    public static Comment createCommentWithCreatedAt(String content, User user, Article article,
        Instant createdAt) {
        return Comment.builder()
            .content(content)
            .author(user)
            .article(article)
            .build();
    }

    public static Comment createCommentWithIsDeleted(String content, User user, Article article) {
        return Comment.builder()
            .content(content)
            .author(user)
            .article(article)
            .isDeleted(true)
            .build();
    }

    public static CommentRegisterRequest createCommentRegisterRequest(String content, UUID userId,
        UUID articleId) {
        return CommentRegisterRequest.builder()
            .content(content)
            .articleId(articleId)
            .userId(userId)
            .build();
    }

    public static CommentDto createCommentDto(Comment comment) {
        return CommentDto.builder()
            .id(comment.getId())
            .createdAt(comment.getCreatedAt())
            .articleId(comment.getArticle().getId())
            .userId(comment.getAuthor().getId())
            .content(comment.getContent())
            .userNickname(comment.getAuthor().getNickname())
            .likeCount(0L)
            .likedByMe(false)
            .build();
    }

    public static CommentDto createCommentDtoWithContent(Comment comment, String newContent) {
        return CommentDto.builder()
            .id(comment.getId())
            .createdAt(comment.getCreatedAt())
            .articleId(comment.getArticle().getId())
            .userId(comment.getAuthor().getId())
            .content(newContent)
            .userNickname(comment.getAuthor().getNickname())
            .likeCount(0L)
            .likedByMe(false)
            .build();
    }
}