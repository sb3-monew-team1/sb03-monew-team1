package com.sprint.mission.sb03monewteam1.dto;

import com.sprint.mission.sb03monewteam1.entity.Article;
import java.util.List;

public record ArticleWithKeyword(List<Article> articles, String keyword) {
    
}
