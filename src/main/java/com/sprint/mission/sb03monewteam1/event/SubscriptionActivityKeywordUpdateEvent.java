package com.sprint.mission.sb03monewteam1.event;

import java.util.List;
import java.util.UUID;

public record SubscriptionActivityKeywordUpdateEvent(
    UUID interestId,
    List<String> newKeywords
) {

}
