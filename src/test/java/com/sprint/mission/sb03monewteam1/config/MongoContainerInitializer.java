package com.sprint.mission.sb03monewteam1.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MongoDBContainer;

public class MongoContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final MongoDBContainer MONGO_CONTAINER = new MongoDBContainer("mongo:7.0").withReuse(false);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        if (!MONGO_CONTAINER.isRunning()) {
            MONGO_CONTAINER.start();
        }

        String mongoUri = MONGO_CONTAINER.getReplicaSetUrl();
        System.out.println("MongoDB URI: " + mongoUri);

        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
            applicationContext,
            "spring.data.mongodb.uri=" + mongoUri
        );
    }
}
