package ru.hse.cs.java2020.task03.models;

import ru.hse.cs.java2020.task03.tracker.models.Issue;
import ru.hse.cs.java2020.task03.tracker.models.Person;
import ru.hse.cs.java2020.task03.tracker.models.Queue;

import javax.persistence.*;

@Entity
@Table(name = "tracker_issues")
public class TrackerIssue extends Issue {
    @Id
    private int telegramUserId;

    public TrackerIssue(int telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    public TrackerIssue() {
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "summary")
    public String getSummary() {
        return super.getSummary();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "queue")
    public String getQueueKey() {
        if (super.getQueue() == null) {
            return null;
        }
        return super.getQueue().getKey();
    }

    public void setQueueKey(String key) {
        if (super.getQueue() == null) {
            super.setQueue(new Queue(key));
        } else {
            super.getQueue().setKey(key);
        }
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "description")
    public String getDescription() {
        return super.getDescription();
    }

    @Access(AccessType.PROPERTY)
    @Column(name = "assignee")
    public String getAssigneeId() {
        if (super.getAssignee() == null) {
            return null;
        }
        return super.getAssignee().getId();
    }

    public void setAssigneeId(String id) {
        if (super.getAssignee() == null) {
            super.setAssignee(new Person());
        }
        super.getAssignee().setId(id);
    }
}
