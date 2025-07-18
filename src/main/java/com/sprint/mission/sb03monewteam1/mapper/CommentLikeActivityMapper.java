package com.sprint.mission.sb03monewteam1.mapper;

import com.sprint.mission.sb03monewteam1.dto.CommentLikeActivityDto;
import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentLikeActivityMapper {

    @Mapping(target = "commentUserId", source = "comment.author.id")
    @Mapping(target = "commentUserNickname", source = "comment.author.nickname")
    @Mapping(target = "commentId", source = "comment.id")
    @Mapping(target = "articleId", source = "comment.article.id")
    @Mapping(target = "articleTitle", source = "comment.article.title")
    @Mapping(target = "commentContent", source = "comment.content")
    @Mapping(target = "commentLikeCount", source = "comment.likeCount")
    @Mapping(target = "commentCreatedAt", source = "comment.createdAt")
    CommentLikeActivityDto toDto(CommentLike entity);
}
