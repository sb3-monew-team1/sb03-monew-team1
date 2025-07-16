package com.sprint.mission.sb03monewteam1.mapper;

import com.sprint.mission.sb03monewteam1.dto.CommentLikeDto;
import com.sprint.mission.sb03monewteam1.entity.CommentLike;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentLikeMapper {

    CommentLikeDto toDto(CommentLike commentLike);
}
