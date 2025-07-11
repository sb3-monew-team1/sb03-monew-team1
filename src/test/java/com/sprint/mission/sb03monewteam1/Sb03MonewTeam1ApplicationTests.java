package com.sprint.mission.sb03monewteam1;

import com.sprint.mission.sb03monewteam1.config.TestEnvSetup;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Sb03MonewTeam1ApplicationTests {

    @BeforeAll
    static void setUp() {
        TestEnvSetup.loadEnvVariables();
    }

    @Test
    void contextLoads() {
    }
}
