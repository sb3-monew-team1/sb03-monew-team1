package com.sprint.mission.sb03monewteam1.seeder;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class AllDataSeederRunner {

    private final UserDataSeeder userDataSeeder;
    private final InterestDataSeeder interestDataSeeder;
    private final SubscriptionDataSeeder subscriptionDataSeeder;

    @PostConstruct
    public void runAllSeeders() {
        userDataSeeder.seed();

        interestDataSeeder.seed();

        subscriptionDataSeeder.seed();
    }
}
