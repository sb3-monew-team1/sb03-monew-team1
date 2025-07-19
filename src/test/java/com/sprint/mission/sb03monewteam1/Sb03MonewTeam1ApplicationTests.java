package com.sprint.mission.sb03monewteam1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.sprint.mission.sb03monewteam1.config.LoadTestEnv;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootTest(properties = "de.flapdoodle.mongodb.embedded.version=5.0.5")
@EnableAsync
@LoadTestEnv
class Sb03MonewTeam1ApplicationTests {

    @Test
    void contextLoads() {
    }
}
