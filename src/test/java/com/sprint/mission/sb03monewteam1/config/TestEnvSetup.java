package com.sprint.mission.sb03monewteam1.config;

import io.github.cdimascio.dotenv.Dotenv;

public class TestEnvSetup {

    private static boolean isInitialized = false;

    public static void loadEnvVariables() {
        if (!isInitialized) {
            Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();

            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });

            isInitialized = true;
        }
    }
}