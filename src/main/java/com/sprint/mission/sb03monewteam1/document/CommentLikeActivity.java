package com.sprint.mission.sb03monewteam1.document;

import com.sprint.mission.sb03monewteam1.dto.CommentLikeDto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "comment_like_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentLikeActivity {

    @Id
    private UUID userId;

    @Builder.Default
    private List<CommentLikeDto> commentLikes = new ArrayList<>();

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}
