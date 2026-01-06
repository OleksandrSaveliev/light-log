package com.tmdna.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryLogger {

    private static final String LOG_FILE = "logs/activity.log";
    private static final String SEPARATOR = "─────────────────────────────────────────";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void logStatusChange(String status) {
        String timestamp = LocalDateTime.now().format(formatter);
        writeToFile(timestamp + " - Status: " + status + "\n");
    }

    public void logSeparator() {
        writeToFile(SEPARATOR + "\n");
    }

    public void logDowntime(String downtime) {
        writeToFile("DOWNTIME: " + downtime + "\n");
    }

    public List<String> getActivities() {
        if (!Files.exists(Paths.get(LOG_FILE))) {
            return Collections.emptyList();
        }

        try {
            List<String> allLines = Files.readAllLines(Paths.get(LOG_FILE));
            int lineCount = allLines.size();

            List<String> activities = new ArrayList<>();
            for (int i = lineCount - 1; i >= 0; i--) {
                activities.add(formatLogLine(allLines.get(i)));
            }
            return activities;
        } catch (IOException e) {
            System.err.println("Error reading activity log: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public void clearHistory() {
        try {
            Files.deleteIfExists(Paths.get(LOG_FILE));
        } catch (IOException e) {
            System.err.println("Error cleaning history: " + e.getMessage());
        }
    }

    private String formatLogLine(String line) {
        if (line.contains(SEPARATOR)) {
            return SEPARATOR;
        } else if (line.startsWith("DOWNTIME:")) {
            return "\t\t>>> " + line + " <<<";
        }
        return line;
    }

    private void writeToFile(String content) {
        try {
            Files.createDirectories(Paths.get("logs"));
            try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
                writer.write(content);
            }
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}
