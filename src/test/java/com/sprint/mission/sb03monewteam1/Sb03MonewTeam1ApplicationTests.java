package com.sprint.mission.sb03monewteam1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@EnableAsync
@LoadTestEnv
@ActiveProfiles("test")
class Sb03MonewTeam1ApplicationTests {

    @Test
    void contextLoads() {
    }
}
