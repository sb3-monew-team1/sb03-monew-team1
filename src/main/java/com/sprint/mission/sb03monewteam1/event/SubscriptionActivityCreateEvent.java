package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.SubscriptionActivityDto;
import lombok.Builder;

@Builder
public record SubscriptionActivityCreateEvent(
    SubscriptionActivityDto subscriptionActivityDto
) {

}