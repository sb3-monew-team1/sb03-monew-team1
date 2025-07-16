package com.sprint.mission.sb03monewteam1.mapper;

import com.sprint.mission.sb03monewteam1.dto.CommentLikeDto;
import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentLikeMapper {

    @Mapping(source = "user.id", target = "likedBy")
    @Mapping(source = "comment.id", target = "commentId")
    @Mapping(source = "comment.article.id", target = "articleId")
    @Mapping(source = "comment.author.id", target = "commentUserId")
    @Mapping(source = "comment.author.nickname", target = "commentUserNickname")
    @Mapping(source = "comment.content", target = "commentContent")
    @Mapping(source = "comment.likeCount", target = "commentLikeCount")
    @Mapping(source = "comment.createdAt", target = "commentCreatedAt")
    CommentLikeDto toDto(CommentLike commentLike);
}
