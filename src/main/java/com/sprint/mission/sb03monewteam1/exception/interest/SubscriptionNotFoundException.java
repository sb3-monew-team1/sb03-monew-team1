package com.sprint.mission.sb03monewteam1.exception.interest;

import com.sprint.mission.sb03monewteam1.exception.CustomException;
import com.sprint.mission.sb03monewteam1.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class SubscriptionNotFoundException extends CustomException {

  public SubscriptionNotFoundException(UUID interestId, UUID userId) {
    super(ErrorCode.SUBSCRIPTION_NOT_FOUND, Map.of("interestID", String.valueOf(interestId), "userId", String.valueOf(userId)));
  }
}
