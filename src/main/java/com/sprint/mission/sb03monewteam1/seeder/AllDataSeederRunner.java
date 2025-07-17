package com.sprint.mission.sb03monewteam1.seeder;

import jakarta.annotation.PostConstruct;

import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class AllDataSeederRunner {

    private final List<DataSeeder> seeders;

    @PostConstruct
    public void runAllSeeders() {
        seeders.stream()
            .sorted(Comparator.comparingInt(this::getOrder)) // Optional: @Order 기반
            .forEach(DataSeeder::seed);
    }

    private int getOrder(DataSeeder seeder) {
        if (seeder instanceof UserDataSeeder) return 1;
        if (seeder instanceof InterestDataSeeder) return 2;
        if (seeder instanceof SubscriptionDataSeeder) return 3;
        if (seeder instanceof ArticleDataSeeder) return 4;
        if (seeder instanceof CommentDataSeeder) return 5;
        if (seeder instanceof CommentLikeDataSeeder) return 6;
        return 99;
    }
}
