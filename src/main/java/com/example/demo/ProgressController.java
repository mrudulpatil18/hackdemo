package com.example.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Instant;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ProgressController {

    private final ProgressEmitter emitter;

    @GetMapping(value = "/sse/{jobId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ProgressUpdate>> getProgressUpdates(@PathVariable String jobId) {
        log.info("Getting Progress for job id: " + jobId);
        return emitter.getStream(jobId)
                .map(progressUpdate ->
                        ServerSentEvent.<ProgressUpdate>builder()
                                .id(String.valueOf(Instant.now().getEpochSecond()))
                                .event("update")
                                .data(progressUpdate)
                                .build());
    }

    @GetMapping("/start")
    public String startFakeJob() {
        String jobId = emitter.createJob();
        simulateFakeJob(jobId);
        log.info("Started A fake job for id: " + jobId);
        return jobId;
    }

    @PostMapping("/start")
    public String startFakeJobWs() {
        String jobId = emitter.createJob();
        simulateFakeJob(jobId);
        return jobId;
    }


    private void simulateFakeJob(String jobId) {
        new Thread(() -> {
            try {
                String[] steps = {
                        "Starting process",
                        "Importing files",
                        "Analyzing files",
                        "Analyzing portfolio",
                        "Generating research",
                        "Generating research document",
                        "Personalizing for the user",
                        "Done"
                };

                for (String step : steps) {
                    log.info("Emitting event: " + step);
                    emitter.emit(jobId, new ProgressUpdate(step));
                    Thread.sleep(5000); // Simulate delay between steps
                }
                emitter.complete(jobId);

            } catch (InterruptedException e) {
                emitter.emit(jobId, new ProgressUpdate("Error occurred: " + e.getMessage()));
            }
        }).start();
    }
}
