package com.sprint.mission.sb03monewteam1.config;

import com.sprint.mission.sb03monewteam1.interceptor.IpLoggingInterceptor;
import com.sprint.mission.sb03monewteam1.interceptor.LoginInterceptor;
import com.sprint.mission.sb03monewteam1.interceptor.MDCLoggingInterceptor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class WebMvcConfig implements WebMvcConfigurer {

    private final IpLoggingInterceptor ipLoggingInterceptor;
    private final LoginInterceptor loginInterceptor;
    private final MDCLoggingInterceptor mdcLoggingInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(ipLoggingInterceptor)
            .order(1)
            .addPathPatterns("/api/**");

        registry.addInterceptor(loginInterceptor)
            .order(2)
            .addPathPatterns("/api/**")
            .excludePathPatterns("/api/users/login", "/api/users");

        registry.addInterceptor(mdcLoggingInterceptor)
            .order(3)
            .addPathPatterns("/api/**");
    }
}
