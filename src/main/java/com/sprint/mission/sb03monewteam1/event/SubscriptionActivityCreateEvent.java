package com.sprint.mission.sb03monewteam1.event;

import com.sprint.mission.sb03monewteam1.dto.SubscriptionDto;
import lombok.Builder;

@Builder
public record SubscriptionActivityCreateEvent(
    SubscriptionDto subscriptionDto
) {

}