package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ProgressEmitter {

    private final Map<String, Sinks.Many<ProgressUpdate>> sinks = new ConcurrentHashMap<>();

    public String createJob() {
        String jobId = UUID.randomUUID().toString();
        sinks.put(jobId, Sinks.many().multicast().onBackpressureBuffer());
        return jobId;
    }

    public void emit(String jobId, ProgressUpdate update) {
        Sinks.Many<ProgressUpdate> sink = sinks.get(jobId);
        if (sink != null) {
            sink.tryEmitNext(update);
        }
    }

    public Flux<ProgressUpdate> getStream(String jobId) {
        return sinks.containsKey(jobId)
                ? sinks.get(jobId).asFlux()
                : Flux.error(new IllegalArgumentException("Invalid job ID"));
    }

    public void complete(String jobId) {
        Sinks.Many<ProgressUpdate> sink = sinks.get(jobId);
        log.info("Stopping the stream for Job Id: " + jobId);
        if (sink != null) {
            sink.tryEmitComplete();
            sinks.remove(jobId);
        }
    }


}
