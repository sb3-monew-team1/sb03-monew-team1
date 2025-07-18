package com.sprint.mission.sb03monewteam1.mapper;

import com.sprint.mission.sb03monewteam1.dto.ArticleViewActivityDto;
import com.sprint.mission.sb03monewteam1.entity.ArticleView;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticleViewActivityMapper {

    @Mapping(target = "viewedBy", source = "userId")
    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "source", source = "article.source")
    @Mapping(target = "sourceUrl", source = "article.sourceUrl")
    @Mapping(target = "articleTitle", source = "article.title")
    @Mapping(target = "articlePublishedDate", source = "article.createdAt")
    @Mapping(target = "articleSummary", source = "article.summary")
    @Mapping(target = "articleCommentCount", source = "article.commentCount")
    @Mapping(target = "articleViewCount", source = "article.viewCount")
    ArticleViewActivityDto toDto(ArticleView entity);
}
