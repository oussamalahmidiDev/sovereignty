package com.oussama.sovereignty.infrastructure.aop;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class TimedStepAspect {

    private final MeterRegistry meterRegistry;

    @Around("@annotation(timedStep)")
    public Object measureTime(ProceedingJoinPoint joinPoint, TimedStep timedStep) throws Throwable {
        Timer.Sample timer = Timer.start(meterRegistry);

        try {
            return joinPoint.proceed();
        } finally {
            timer.stop(
                    Timer.builder(timedStep.value())
                            .tag("class", joinPoint.getSignature().getDeclaringType().getSimpleName())
                            .tag("method", joinPoint.getSignature().getName())
                            .register(meterRegistry)

            );
        }
    }
}
