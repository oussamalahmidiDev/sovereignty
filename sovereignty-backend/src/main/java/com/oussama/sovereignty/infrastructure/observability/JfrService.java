package com.oussama.sovereignty.infrastructure.observability;

import jdk.jfr.Configuration;
import jdk.jfr.Recording;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class JfrService {

    private final AtomicReference<Recording> currentRecording = new AtomicReference<>();
    private final AtomicReference<Path> recordingFile = new AtomicReference<>();

    public String startRecording(long durationSeconds) throws Exception {
        Recording recording = new Recording(Configuration.getConfiguration("profile"));

        Path file = Files.createTempFile("sovereigntyai-", ".jfr");

        recording.setName("SovereigntyAI-JFR");
        recording.setToDisk(true);
        recording.setDestination(file);
        recording.setDuration(Duration.ofSeconds(durationSeconds));

        if (!currentRecording.compareAndSet(null, recording)) {
            recording.close();
            return "A recording is already running";
        }

        recordingFile.set(file);

        recording.start();

        return "JFR recording started";
    }

    public String stopRecording() {
        Recording recording = currentRecording.getAndSet(null);
        if (recording == null) {
            return "No active recording";
        }

        try {
            recording.stop();
            recording.close();
        } catch (Exception e) {
            return "Error while stopping recording: " + e.getMessage();
        }

        return "JFR recording stopped";
    }

    public synchronized Path getRecordingFile() {
        return recordingFile.get();
    }
}
