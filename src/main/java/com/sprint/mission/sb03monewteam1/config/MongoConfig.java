package com.sprint.mission.sb03monewteam1.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(basePackages = "com.sprint.mission.sb03monewteam1.repository.mongodb")
public class MongoConfig {

}
