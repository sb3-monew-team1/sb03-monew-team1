package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.SubscriptionDto;
import java.util.UUID;

public record SubscriptionActivityCreateEvent(
    UUID userId,
    SubscriptionDto subscriptionDto
) {

}