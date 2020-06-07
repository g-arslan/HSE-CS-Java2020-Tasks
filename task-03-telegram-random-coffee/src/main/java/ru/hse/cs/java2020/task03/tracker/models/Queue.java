package ru.hse.cs.java2020.task03.tracker.models;

import ru.hse.cs.java2020.task03.tracker.ITrackerClient;

public class Queue implements ITrackerClient.IQueue {
    private String id;
    private String display;
    private String key;

    public Queue(String key) {
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
