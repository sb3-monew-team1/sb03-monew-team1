package com.sprint.mission.sb03monewteam1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication
public class Sb03MonewTeam1Application {

    public static void main(String[] args) {
        SpringApplication.run(Sb03MonewTeam1Application.class, args);
    }

}
