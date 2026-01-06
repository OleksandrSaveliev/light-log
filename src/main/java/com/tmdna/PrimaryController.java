package com.tmdna;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.tmdna.model.Activity;
import com.tmdna.service.HistoryService;
import com.tmdna.service.TimerService;
import com.tmdna.utils.DurationFormatter;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PrimaryController {

    private boolean isAvailable = true;
    private long unavailableStartTime = 0;

    private final HistoryService historyService = new HistoryService();
    private TimerService timerService;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    private Button primaryButton;

    @FXML
    private Label timerLabel;

    @FXML
    private Label downtimeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea historyTextArea;

    @FXML
    private Button toggleHistoryButton;

    @FXML
    private VBox historyContent;

    @FXML
    private VBox historyDivider;

    @FXML
    public void initialize() {
        downtimeLabel.setText("");
        timerService = new TimerService(timerLabel);
        checkInitialStatus();
    }

    private void checkInitialStatus() {
        historyService.getLastActivity()
                .ifPresentOrElse(this::handleExistingActivity, this::updateUI);
    }

    private void handleExistingActivity(Activity activity) {
        if (isUnavailable(activity)) {
            applyUnavailableState(activity);
        }
        updateUI();
    }

    private boolean isUnavailable(Activity activity) {
        return "UNAVAILABLE".equals(activity.getStatus());
    }

    private void applyUnavailableState(Activity activity) {
        isAvailable = false;

        long durationMillis = calculateUnavailableDuration(activity);
        unavailableStartTime = System.currentTimeMillis() - durationMillis;

        timerService.startTimer(durationMillis);
    }

    private long calculateUnavailableDuration(Activity activity) {
        LocalDateTime lastUnavailableTime
                = LocalDateTime.parse(activity.getTimestamp(), formatter);

        return Duration.between(lastUnavailableTime, LocalDateTime.now()).toMillis();
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
        historyService.clearHistory();
        updateActivityDisplay();
    }

    @FXML
    private void toggleHistory() {
        boolean isHistoryVisible = historyContent.isVisible();
        historyContent.setVisible(!isHistoryVisible);
        historyContent.setManaged(!isHistoryVisible);
        historyDivider.setVisible(!isHistoryVisible);
        historyDivider.setManaged(!isHistoryVisible);

        Stage stage = (Stage) primaryButton.getScene().getWindow();
        if (!isHistoryVisible) {
            stage.setHeight(stage.getHeight() + 400);
            toggleHistoryButton.setText("▲");
            updateActivityDisplay();
        } else {
            stage.setHeight(stage.getHeight() - 400);
            toggleHistoryButton.setText("▼");
        }
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
        historyService.logStatusChange("UNAVAILABLE");
        downtimeLabel.setText("");
    }

    private void handleAvailableStatus() {
        long unavailableDuration = System.currentTimeMillis() - unavailableStartTime;
        String durationStr = DurationFormatter.format(unavailableDuration);

        historyService.logStatusChange("AVAILABLE");
        downtimeLabel.setText("Last Downtime: " + durationStr);
        timerLabel.setText("00:00:00");
    }

    private void toggleTimer() {
        if (timerService.isRunning()) {
            timerService.stopTimer();
            timerService.reset();
        } else {
            timerService.startTimer();
        }
    }

    private void updateActivityDisplay() {
        List<String> activities = historyService.getActivities();
        StringBuilder content = new StringBuilder();

        for (String activity : activities) {
            content.append(activity).append("\n");
        }

        historyTextArea.setText(content.toString());
    }
}
