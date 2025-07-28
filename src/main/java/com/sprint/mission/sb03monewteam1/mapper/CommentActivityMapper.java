package com.sprint.mission.sb03monewteam1.mapper;

import com.sprint.mission.sb03monewteam1.dto.CommentActivityDto;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentActivityMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "articleTitle", source = "article.title")
    @Mapping(target = "userId", source = "author.id")
    @Mapping(target = "userNickname", source = "author.nickname")
    CommentActivityDto toDto(Comment comment);
}


