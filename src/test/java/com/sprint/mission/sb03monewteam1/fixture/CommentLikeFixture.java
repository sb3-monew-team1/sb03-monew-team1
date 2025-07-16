package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.dto.CommentLikeDto;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import com.sprint.mission.sb03monewteam1.entity.User;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentLikeFixture {

    private static final UUID DEFAULT_COMMENT_LIKE_ID = UUID.fromString(
        "550e8400-e29b-41d4-a716-446655440003");

    public static CommentLike createCommentLike(User user, Comment comment) {
        return CommentLike.builder()
            .user(user)
            .comment(comment)
            .build();
    }

    public static List<CommentLike> createCommentLikes(User user) {
        List<Comment> comments = List.of(
            CommentFixture.createComment("첫 번째 댓글", user, ArticleFixture.createArticle()),
            CommentFixture.createComment("두 번째 댓글", user, ArticleFixture.createArticle()),
            CommentFixture.createComment("세 번째 댓글", user, ArticleFixture.createArticle())
        );

        List<CommentLike> commentLikes = new ArrayList<>();
        for (Comment comment : comments) {
            commentLikes.add(createCommentLike(user, comment));
        }
        return commentLikes;
    }

    public static List<CommentLike> createCommentLikes(User user, List<Comment> comments) {
        List<CommentLike> commentLikes = new ArrayList<>();
        for (Comment comment : comments) {
            commentLikes.add(createCommentLike(user, comment));
        }
        return commentLikes;
    }

    public static CommentLikeDto createCommentLikeDto(CommentLike commentLike) {
        return CommentLikeDto.builder()
            .id(commentLike.getId())
            .likedBy(commentLike.getUser().getId())
            .createdAt(commentLike.getCreatedAt())
            .commentId(commentLike.getComment().getId())
            .articleId(commentLike.getComment().getArticle().getId())
            .commentUserId(commentLike.getComment().getAuthor().getId())
            .commentUserNickname(commentLike.getUser().getNickname())
            .commentContent(commentLike.getComment().getContent())
            .commentLikeCount(commentLike.getComment().getLikeCount())
            .commentCreatedAt(commentLike.getComment().getCreatedAt())
            .build();
    }

    public static CommentLikeDto createCommentLikeDtoWithLikeCount(CommentLike commentLike, Long likeCount) {
        return CommentLikeDto.builder()
            .id(commentLike.getId())
            .likedBy(commentLike.getUser().getId())
            .createdAt(commentLike.getCreatedAt())
            .commentId(commentLike.getComment().getId())
            .articleId(commentLike.getComment().getArticle().getId())
            .commentUserId(commentLike.getComment().getAuthor().getId())
            .commentUserNickname(commentLike.getUser().getNickname())
            .commentContent(commentLike.getComment().getContent())
            .commentLikeCount(likeCount)
            .commentCreatedAt(commentLike.getComment().getCreatedAt())
            .build();
    }

    public static UUID getDefaultCommentLikeId() {
        return DEFAULT_COMMENT_LIKE_ID;
    }
}
