package com.sprint.mission.sb03monewteam1.repository.mongodb;

import com.sprint.mission.sb03monewteam1.document.CommentLikeActivity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentLikeActivityRepository extends MongoRepository<CommentLikeActivity, UUID> {

    @Aggregation(pipeline = {
        "{ $match: { _id: ?0 } }",
        "{ $project: { commentLikes: { $slice: [ { $reverseArray: \"$commentLikes\" }, 10 ] } } }"
    })
    Optional<CommentLikeActivity> findRecent10CommentLikesByUserId(UUID userId);
}
