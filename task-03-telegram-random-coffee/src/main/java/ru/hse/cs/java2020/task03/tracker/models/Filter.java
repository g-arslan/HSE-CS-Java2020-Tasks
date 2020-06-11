package ru.hse.cs.java2020.task03.tracker.models;

import ru.hse.cs.java2020.task03.tracker.ITrackerClient;

public class Filter implements ITrackerClient.IFilter {
    private Person assignee;
    private Queue queue;

    public Filter(Person assignee) {
        this.assignee = assignee;
    }

    public ITrackerClient.IPerson getAssignee() {
        return assignee;
    }

    public void setAssignee(ITrackerClient.IPerson assignee) {
        this.assignee = (Person) assignee;
    }

    public ITrackerClient.IQueue getQueue() {
        return queue;
    }

    public void setQueue(ITrackerClient.IQueue queue) {
        this.queue = (Queue) queue;
    }
}
