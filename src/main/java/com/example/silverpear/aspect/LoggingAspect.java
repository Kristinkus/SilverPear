package com.example.silverpear.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {


    private static final String LOG_NAME = "com.example.silverpear.service.performance";
    private final Logger logger = LoggerFactory.getLogger(LOG_NAME);

    @Around("execution(* com.example.silverpear.service..*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            logger.info("время выполнения: {} мс | {}", executionTime, joinPoint.getSignature().toShortString());
        }
    }
}
