package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.dto.request.CommentRegisterRequest;
import com.sprint.mission.sb03monewteam1.entity.Article;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import com.sprint.mission.sb03monewteam1.entity.User;
import java.util.UUID;
import org.springframework.test.util.ReflectionTestUtils;

public class CommentFixture {

    public static Comment createComment(String content, User user, Article article) {
        Comment comment = Comment.builder()
                .content(content)
                .author(user)
                .article(article)
                .build();
        ReflectionTestUtils.setField(comment, "id", UUID.randomUUID());
        return comment;
    }

    public static CommentRegisterRequest createCommentRegisterRequest(String content, UUID userId, UUID articleId) {
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
}