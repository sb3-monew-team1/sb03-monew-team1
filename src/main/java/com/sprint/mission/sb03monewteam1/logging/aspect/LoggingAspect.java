package com.sprint.mission.sb03monewteam1.logging.aspect;

import java.util.Arrays;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("execution(* com.sprint.mission.sb03monewteam1.service..*(..))")
    public void serviceLayer() {
    }

    @Pointcut("execution(* com.sprint.mission.sb03monewteam1.controller..*(..))")
    public void controllerLayer() {
    }

    @Before("serviceLayer() || controllerLayer()")
    public void logBefore(JoinPoint joinPoint) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("==> {}#{} 실행 시작 - 매개변수: {}",
            className, methodName, Arrays.toString(args));
    }

    @AfterReturning(pointcut = "serviceLayer() || controllerLayer()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("<== {}#{} 실행 완료 - 반환값: {}",
            className, methodName, result);
    }

    @AfterThrowing(pointcut = "serviceLayer() || controllerLayer()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Exception exception) {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.error("!!! {}#{} 실행 중 예외 발생 - 예외: {}, 메시지: {}",
            className, methodName, exception.getClass().getSimpleName(), exception.getMessage());
    }

    @Around("serviceLayer()")
    public Object logExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        String className = proceedingJoinPoint.getTarget().getClass().getSimpleName();
        String methodName = proceedingJoinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();

        try {
            Object result = proceedingJoinPoint.proceed();
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            log.info("{}#{} 실행 시간: {}ms", className, methodName, executionTime);

            if (executionTime > 1000) {
                log.warn("[ExecutionTime] {}#{} 실행 시간이 {}ms로 느립니다. 성능 최적화가 필요합니다.",
                    className, methodName, executionTime);
            }

            return result;

        } catch (Throwable throwable) {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            log.error("{}#{} 실행 실패 - 실행 시간: {}ms, 예외: {}",
                className, methodName, executionTime, throwable.getMessage());

            throw throwable;
        }
    }
} 