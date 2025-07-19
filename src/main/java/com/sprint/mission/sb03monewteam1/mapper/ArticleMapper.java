package com.sprint.mission.sb03monewteam1.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.CollectedArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Article;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    @Mapping(target = "interests", ignore = true)
    @Mapping(target = "commentCount", source = "commentCount")
    ArticleDto toDto(Article article);

    Article toEntity(CollectedArticleDto dto);
}
