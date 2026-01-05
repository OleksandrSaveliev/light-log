package com.tmdna.utils;

import javafx.animation.AnimationTimer;
import javafx.scene.control.Label;

/**
 * Manages the timer functionality.
 */
public class TimerManager {

    private boolean timerRunning = false;
    private long timerStartTime = 0;
    private AnimationTimer animationTimer;
    private final Label timerLabel;

    public TimerManager(Label timerLabel) {
        this.timerLabel = timerLabel;
    }

    public void startTimer() {
        timerRunning = true;
        timerStartTime = System.currentTimeMillis();

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (timerRunning) {
                    updateTimerDisplay();
                }
            }
        };
        animationTimer.start();
    }

    public void stopTimer() {
        timerRunning = false;
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    public boolean isRunning() {
        return timerRunning;
    }

    public void reset() {
        stopTimer();
        timerLabel.setText("00:00:00");
    }

    private void updateTimerDisplay() {
        long elapsed = System.currentTimeMillis() - timerStartTime;
        timerLabel.setText(DurationFormatter.format(elapsed));
    }
}
