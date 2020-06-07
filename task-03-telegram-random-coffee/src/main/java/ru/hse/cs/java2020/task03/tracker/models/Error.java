package ru.hse.cs.java2020.task03.tracker.models;

import ru.hse.cs.java2020.task03.tracker.ITrackerClient;

public class Error implements ITrackerClient.IError {
    private String[] errorMessages;

    public String[] getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(String[] errorMessages) {
        this.errorMessages = errorMessages;
    }
}
