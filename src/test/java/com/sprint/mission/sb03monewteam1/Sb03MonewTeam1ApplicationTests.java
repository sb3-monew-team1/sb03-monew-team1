package com.sprint.mission.sb03monewteam1;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class Sb03MonewTeam1ApplicationTests {

    @BeforeAll
    static void setUp() {
        Dotenv dotenv = Dotenv.configure()
            .directory("./")
            .load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
    }

    @Test
    void contextLoads() {
    }
}
