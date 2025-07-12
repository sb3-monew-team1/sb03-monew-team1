package com.sprint.mission.sb03monewteam1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import com.sprint.mission.sb03monewteam1.entity.ArticleView;

@Mapper(componentModel = "spring")
public interface ArticleViewMapper {

    @Mapping(source = "article.id", target = "articleId")
    ArticleViewDto toDto(ArticleView articleView);
}
