package com.sprint.mission.sb03monewteam1.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewsCollectJobCompletedEvent {

    private final String jobName;
}
