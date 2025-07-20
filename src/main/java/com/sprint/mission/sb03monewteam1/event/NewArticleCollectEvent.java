package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.ArticleDto;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewArticleCollectEvent {

    private final UUID interestId;

    private final String interestName;

    private final List<ArticleDto> articles;
}
