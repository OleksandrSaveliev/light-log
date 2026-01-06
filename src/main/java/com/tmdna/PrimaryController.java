package com.tmdna;

import java.util.List;

import com.tmdna.utils.ActivityLogger;
import com.tmdna.utils.DurationFormatter;
import com.tmdna.utils.TimerManager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class PrimaryController {

    private boolean isAvailable = true;
    private long unavailableStartTime = 0;

    private final ActivityLogger activityLogger = new ActivityLogger();
    private TimerManager timerManager;

    @FXML
    private Button primaryButton;

    @FXML
    private Label timerLabel;

    @FXML
    private Label downtimeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea activityTextArea;

    @FXML
    public void initialize() {
        timerManager = new TimerManager(timerLabel);
        updateActivityDisplay();
        updateStatusLabel();
    }

    @FXML
    private void toggleElectricity() {
        isAvailable = !isAvailable;
        updateUI();
        logStatusChange();
        toggleTimer();
    }

    @FXML
    private void cleanHistory() {
        activityLogger.clearHistory();
        updateActivityDisplay();
    }

    private void updateUI() {
        String text = isAvailable ? "Switch to Unavailable" : "Switch to Available";
        primaryButton.setText(text);
        updateStatusLabel();
    }

    private void updateStatusLabel() {
        if (isAvailable) {
            statusLabel.setText("ONLINE");
            statusLabel.setStyle("-fx-text-fill: green;");
        } else {
            statusLabel.setText("OFFLINE");
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    private void logStatusChange() {

        if (!isAvailable) {
            handleUnavailableStatus();
        } else {
            handleAvailableStatus();
        }

        updateActivityDisplay();
    }

    private void handleUnavailableStatus() {
        unavailableStartTime = System.currentTimeMillis();
        activityLogger.logStatusChange("UNAVAILABLE");
        downtimeLabel.setText("");
    }

    private void handleAvailableStatus() {
        long unavailableDuration = System.currentTimeMillis() - unavailableStartTime;
        String durationStr = DurationFormatter.format(unavailableDuration);

        activityLogger.logStatusChange("AVAILABLE");
        activityLogger.logDowntime(durationStr);
        activityLogger.logSeparator();
        downtimeLabel.setText("Last Downtime: " + durationStr);
        timerLabel.setText("00:00:00");
    }

    private void toggleTimer() {
        if (timerManager.isRunning()) {
            timerManager.stopTimer();
        } else {
            timerManager.reset();
            timerManager.startTimer();
        }
    }

    private void updateActivityDisplay() {
        List<String> activities = activityLogger.getActivities();
        StringBuilder content = new StringBuilder();

        for (String activity : activities) {
            content.append(activity).append("\n");
        }

        activityTextArea.setText(content.toString());
    }
}
