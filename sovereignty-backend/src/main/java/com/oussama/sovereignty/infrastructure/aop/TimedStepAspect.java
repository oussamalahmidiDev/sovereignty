package com.oussama.sovereignty.infrastructure.aop;

import com.oussama.sovereignty.application.aop.TimedStep;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
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
    private final Tracer tracer;

    @Around("@annotation(timedStep)")
    public Object measureTime(ProceedingJoinPoint joinPoint, TimedStep timedStep) throws Throwable {
        Timer.Sample timer = Timer.start(meterRegistry);

        Span span = tracer.nextSpan().name(timedStep.value()).start();

        try {
            return joinPoint.proceed();
        } finally {
            timer.stop(
                    Timer.builder(timedStep.value())
                            .tag("class", joinPoint.getSignature().getDeclaringType().getSimpleName())
                            .tag("method", joinPoint.getSignature().getName())
                            .register(meterRegistry)

            );

            span.end();
        }
    }
}
