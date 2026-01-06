package com.tmdna.service;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.tmdna.model.Activity;
import com.tmdna.utils.DurationFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class HistoryService {

    private static final String LOG_FILE = "data.csv";
    private static final String SEPARATOR = "─────────────────────────────────────────";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CsvMapper csvMapper;
    private final CsvSchema schema;

    public HistoryService() {
        csvMapper = new CsvMapper();
        schema = csvMapper.schemaFor(Activity.class).withHeader();
    }

    public void logStatusChange(String status) {
        String timestamp = LocalDateTime.now().format(formatter);
        writeToFile(new Activity(timestamp, status));
    }

    public List<String> getActivities() {
        if (!Files.exists(Paths.get(LOG_FILE))) {
            return Collections.emptyList();
        }

        try {
            MappingIterator<Activity> it = csvMapper.readerFor(Activity.class).with(schema).readValues(new File(LOG_FILE));
            List<Activity> allActivities = it.readAll();
            return buildFormattedHistory(allActivities);
        } catch (IOException e) {
            System.err.println("Error reading activity log: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<String> buildFormattedHistory(List<Activity> allActivities) {
        List<String> formattedHistory = new ArrayList<>();
        Optional<LocalDateTime> unavailableStartTime = Optional.empty();

        for (Activity currentActivity : allActivities) {
            String status = currentActivity.getStatus();
            LocalDateTime timestamp = LocalDateTime.parse(currentActivity.getTimestamp(), formatter);

            if ("UNAVAILABLE".equals(status)) {
                unavailableStartTime = Optional.of(timestamp);
                formattedHistory.add(formatLogLine(currentActivity));
            } else if ("AVAILABLE".equals(status)) {
                if (unavailableStartTime.isPresent()) {
                    long durationSeconds = java.time.Duration.between(unavailableStartTime.get(), timestamp).getSeconds();
                    String durationStr = DurationFormatter.format(durationSeconds * 1000);
                    formattedHistory.add(currentActivity.getTimestamp() + " - Status: AVAILABLE\n\t>>> DOWNTIME: " + durationStr + " <<<");
                    formattedHistory.add(SEPARATOR);
                    unavailableStartTime = Optional.empty();
                } else {
                    formattedHistory.add(formatLogLine(currentActivity));
                }
            } else {
                formattedHistory.add(formatLogLine(currentActivity));
            }
        }

        Collections.reverse(formattedHistory);
        return formattedHistory;
    }

    public Optional<Activity> getLastActivity() {
        if (!Files.exists(Paths.get(LOG_FILE))) {
            return Optional.empty();
        }

        try {
            MappingIterator<Activity> it = csvMapper.readerFor(Activity.class).with(schema).readValues(new File(LOG_FILE));
            List<Activity> allActivities = it.readAll();
            if (allActivities.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(allActivities.get(allActivities.size() - 1));
        } catch (IOException e) {
            System.err.println("Error reading activity log for last activity: " + e.getMessage());
            return Optional.empty();
        }
    }

    public void clearHistory() {
        try {
            Files.deleteIfExists(Paths.get(LOG_FILE));
        } catch (IOException e) {
            System.err.println("Error cleaning history: " + e.getMessage());
        }
    }

    private String formatLogLine(Activity activity) {
        if (activity.getTimestamp() != null && activity.getStatus() != null) {
            return activity.getTimestamp() + " - Status: " + activity.getStatus();
        }
        return "";
    }

    private void writeToFile(Activity newActivity) {
        try {
            File file = new File(LOG_FILE);
            boolean isNewFile = !file.exists() || file.length() == 0;

            try (FileWriter writer = new FileWriter(file, true)) {
                if (isNewFile) {
                    csvMapper.writer(schema).writeValue(writer, newActivity);
                } else {
                    csvMapper.writer(schema.withoutHeader()).writeValue(writer, newActivity);
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}
