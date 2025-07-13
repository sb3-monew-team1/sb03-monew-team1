package com.sprint.mission.sb03monewteam1.seeder;

import jakarta.annotation.PostConstruct;
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
        seeders.forEach(DataSeeder::seed);
    }

}
