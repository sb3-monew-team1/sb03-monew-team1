package com.sprint.mission.sb03monewteam1.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.springframework.test.context.ContextConfiguration;

@Retention(RetentionPolicy.RUNTIME)
@ContextConfiguration(initializers = EnvInitializer.class)
public @interface LoadTestEnv {
}
