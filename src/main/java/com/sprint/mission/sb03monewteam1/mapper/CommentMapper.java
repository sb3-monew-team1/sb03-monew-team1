package com.sprint.mission.sb03monewteam1.mapper;

import com.sprint.mission.sb03monewteam1.dto.CommentDto;
import com.sprint.mission.sb03monewteam1.entity.Comment;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "userId", source = "author.id")
    @Mapping(target = "userNickname", source = "author.nickname")
    @Mapping(target = "likedByMe", ignore = true)
    CommentDto toDto(Comment comment);
}
