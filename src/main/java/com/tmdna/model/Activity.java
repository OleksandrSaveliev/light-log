package com.tmdna.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"timestamp", "status"})
public class Activity {
    private String timestamp;
    private String status;

    public Activity() {
    }

    public Activity(String timestamp, String status) {
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}