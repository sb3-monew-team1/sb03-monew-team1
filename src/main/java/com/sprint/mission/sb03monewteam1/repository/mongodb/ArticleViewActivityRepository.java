package com.sprint.mission.sb03monewteam1.repository.mongodb;

import com.sprint.mission.sb03monewteam1.document.ArticleViewActivity;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

@Document(collection = "article_view_activities")
public interface ArticleViewActivityRepository extends MongoRepository<ArticleViewActivity, UUID> {
}
