package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import com.sprint.mission.sb03monewteam1.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentLikeFixture {

    private static final UUID DEFAULT_COMMENT_LIKE_ID = UUID.fromString(
        "550e8400-e29b-41d4-a716-446655440003");
    private static final String DEFAULT_COMMENT_CONTENT = "테스트용 댓글입니다";

    public static CommentLike createCommentLike() {

        User user = UserFixture.createUser();

        return createCommentLike(user,
            CommentFixture.createComment(
                DEFAULT_COMMENT_CONTENT,
                user,
                ArticleFixture.createArticle()
            ));
    }

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

    public static UUID getDefaultCommentLikeId() {
        return DEFAULT_COMMENT_LIKE_ID;
    }
}
