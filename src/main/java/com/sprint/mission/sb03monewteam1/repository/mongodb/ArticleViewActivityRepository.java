package com.sprint.mission.sb03monewteam1.repository.mongodb;

import com.sprint.mission.sb03monewteam1.document.ArticleViewActivity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ArticleViewActivityRepository extends MongoRepository<ArticleViewActivity, UUID> {

    @Aggregation(pipeline = {
        "{ $match: { _id: ?0 } }",
        "{ $project: { articleViews: { $slice: [ { $sortArray: { input: \"$articleViews\", sortBy: { createdAt: -1 } } }, 10 ] } } }"
    })
    Optional<ArticleViewActivity> findRecent10ArticleViewsByUserId(UUID userId);
}
