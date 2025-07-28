package com.sprint.mission.sb03monewteam1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.sprint.mission.sb03monewteam1.repository.jpa")
public class JpaConfig {

}