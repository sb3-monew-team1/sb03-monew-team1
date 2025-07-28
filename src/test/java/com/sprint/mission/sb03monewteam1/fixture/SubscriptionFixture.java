package com.sprint.mission.sb03monewteam1.fixture;

import com.sprint.mission.sb03monewteam1.entity.Interest;
import com.sprint.mission.sb03monewteam1.entity.Subscription;
import com.sprint.mission.sb03monewteam1.entity.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubscriptionFixture {

    private static final UUID DEFAULT_SUBSCRIPTION_ID = UUID.fromString(
        "550e8400-e29b-41d4-a716-446655440001");


    public static Subscription createSubscription(User user, Interest interest) {
        return Subscription.builder()
            .user(user)
            .interest(interest)
            .build();
    }

    public static List<Subscription> createSubscriptions(User user, List<Interest> interests) {
        List<Subscription> subscriptions = new ArrayList<>();
        for (Interest interest : interests) {
            subscriptions.add(createSubscription(user, interest));
        }
        return subscriptions;
    }

    public static List<Subscription> createSubscriptions(User user) {
        List<Interest> interests = List.of(
            InterestFixture.createInterest("기술", 10L),
            InterestFixture.createInterest("스포츠", 20L),
            InterestFixture.createInterest("문화", 15L)
        );
        return createSubscriptions(user, interests);
    }

    public static UUID getDefaultSubscriptionId() {
        return DEFAULT_SUBSCRIPTION_ID;
    }
}