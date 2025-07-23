package com.sprint.mission.sb03monewteam1.repository.mongodb;

import com.sprint.mission.sb03monewteam1.document.CommentActivity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentActivityRepository extends MongoRepository<CommentActivity, UUID> {

    @Aggregation(pipeline = {
        "{ $match: { _id: ?0 } }",
        "{ $project: { comments: { $slice: [ { $reverseArray: \"$comments\" }, 10 ] } } }" // 최신 10개 댓글 가져오기
    })
    Optional<CommentActivity> findRecent10CommentsByUserId(UUID userId);
}
