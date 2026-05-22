package com.oussama.sovereignty.infrastructure.adapters.out.tracing;

import com.oussama.sovereignty.application.ports.out.TraceContextPort;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MicrometerTraceContextAdapter implements TraceContextPort {

    private final Tracer tracer;

    @Override
    public String getCurrentTraceId() {
        if (tracer != null && tracer.currentSpan() != null && tracer.currentSpan().context() != null) {
            return tracer.currentSpan().context().traceId();
        }
        return "N/A";
    }
}
