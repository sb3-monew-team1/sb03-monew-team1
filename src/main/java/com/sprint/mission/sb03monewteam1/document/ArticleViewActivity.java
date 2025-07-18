package com.sprint.mission.sb03monewteam1.document;

import com.sprint.mission.sb03monewteam1.dto.ArticleViewActivityDto;
import com.sprint.mission.sb03monewteam1.dto.ArticleViewDto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "article_view_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleViewActivity {

    @Id
    private UUID userId;

    @Builder.Default
    private List<ArticleViewActivityDto> articleViews = new ArrayList<>();;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}

