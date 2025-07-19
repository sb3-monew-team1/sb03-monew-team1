package com.sprint.mission.sb03monewteam1.repository.mongodb;

import com.sprint.mission.sb03monewteam1.document.CommentLikeActivity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

@Document(collection = "comment_like_activities")
public interface CommentLikeActivityRepository extends MongoRepository<CommentLikeActivity, UUID> {

    @Aggregation(pipeline = {
        "{ $match: { _id: ?0 } }",
        "{ $project: { commentLikes: { $slice: [ { $reverseArray: \"$commentLikes\" }, 10 ] } } }" // 최신 10개 댓글 좋아요만 가져오기
    })
    Optional<CommentLikeActivity> findRecent10CommentLikesByUserId(UUID userId);
}
