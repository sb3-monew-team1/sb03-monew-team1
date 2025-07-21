package com.sprint.mission.sb03monewteam1.document;

import com.sprint.mission.sb03monewteam1.dto.CommentActivityDto;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "comment_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentActivity {

    @Id
    private UUID userId;

    @Builder.Default
    private List<CommentActivityDto> comments = new ArrayList<>();

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}
