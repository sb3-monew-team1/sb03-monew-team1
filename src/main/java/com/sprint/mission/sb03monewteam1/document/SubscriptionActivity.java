package com.sprint.mission.sb03monewteam1.document;

import com.sprint.mission.sb03monewteam1.dto.SubscriptionActivityDto;
import com.sprint.mission.sb03monewteam1.dto.SubscriptionDto;
import java.time.Instant;
import java.util.ArrayList;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document(collection = "subscription_activities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionActivity {

    @Id
    private UUID userId;

    @Builder.Default
    private List<SubscriptionActivityDto> subscriptions = new ArrayList<>();

    @Builder.Default
    private Instant createdAt = Instant.now();

    private Instant updatedAt;
}
