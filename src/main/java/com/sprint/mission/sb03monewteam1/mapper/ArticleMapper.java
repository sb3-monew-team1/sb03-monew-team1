package com.sprint.mission.sb03monewteam1.mapper;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import com.sprint.mission.sb03monewteam1.dto.CollectedArticleDto;
import com.sprint.mission.sb03monewteam1.entity.Article;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticleMapper {

    @Mapping(target = "interests", ignore = true)
    @Mapping(target = "commentCount", source = "commentCount")
    ArticleDto toDto(Article article);

    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "articleViews", ignore = true)
    Article toEntity(CollectedArticleDto dto);
}
