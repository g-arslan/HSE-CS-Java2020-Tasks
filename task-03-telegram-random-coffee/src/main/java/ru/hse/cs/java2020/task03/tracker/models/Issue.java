package ru.hse.cs.java2020.task03.tracker.models;

import ru.hse.cs.java2020.task03.tracker.ITrackerClient;
import ru.hse.cs.java2020.task03.utils.Factory;

import static ru.hse.cs.java2020.task03.utils.Utils.*;

public class Issue implements ITrackerClient.IIssue {
    private String   summary;
    private Queue    queue;
    private String   description;
    private Person   assignee;
    private Person   createdBy;
    private Person[] followers;
    private String   key;

    public Issue() {
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public ITrackerClient.IQueue getQueue() {
        return queue;
    }

    public void setQueue(ITrackerClient.IQueue queue) {
        this.queue = (Queue) queue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ITrackerClient.IPerson getAssignee() {
        return assignee;
    }

    public void setAssignee(ITrackerClient.IPerson assignee) {
        this.assignee = (Person) assignee;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public ITrackerClient.IPerson getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(ITrackerClient.IPerson createdBy) {
        this.createdBy = (Person) createdBy;
    }

    public ITrackerClient.IPerson[] getFollowers() {
        return followers;
    }

    public void setFollowers(ITrackerClient.IPerson[] followers) {
        this.followers = (Person[]) followers;
    }

    public String toString() {
        StringBuilder issue = new StringBuilder(key + ": " + summary + "\n");

        if (description != null) {
            issue.append(description);
        } else {
            issue.append("(").append(Factory.getTrackerBundle().getString("text.noDescription")).append(")");
        }

        issue.append("\n\n").append(Factory.getTrackerBundle().getString("text.author")).append(": ")
             .append(createdBy.getDisplay());

        if (assignee != null) {
            issue.append("\n").append(Factory.getTrackerBundle().getString("text.assignee")).append(": ")
                 .append(assignee.getDisplay());
        }

        if (followers == null) {
            return issue.toString();
        }

        if (followers.length == 1) {
            issue.append("\n").append(Factory.getTrackerBundle().getString("text.follower")).append(": ")
                 .append(followers[0].getDisplay());
        } else if (followers.length > 1) {
            issue.append("\n").append(Factory.getTrackerBundle().getString("text.followers")).append(":");
            for (ITrackerClient.IPerson follower : followers) {
                issue.append("\n ").append(follower.getDisplay());
            }
        }

        return issue.toString();
    }

    public String formatPrettyHtml() {
        StringBuilder issue = new StringBuilder(
                ahref(key, Factory.getTrackerBundle().getString("url.baseTrackerUrl") + "/" + key) + ": " + bold(summary)
                        + "\n");

        if (description != null) {
            issue.append(description);
        } else {
            issue.append("(").append(italic(Factory.getTrackerBundle().getString("text.noDescription"))).append(")");
        }

        issue.append("\n\n").append(bold(Factory.getTrackerBundle().getString("text.author"))).append(": ")
             .append(createdBy.getDisplay());

        if (assignee != null) {
            issue.append("\n").append(bold(Factory.getTrackerBundle().getString("text.assignee"))).append(": ")
                 .append(assignee.getDisplay());
        }

        if (followers == null) {
            return issue.toString();
        }

        if (followers.length == 1) {
            issue.append("\n").append(bold(Factory.getTrackerBundle().getString("text.follower"))).append(": ")
                 .append(followers[0].getDisplay());
        } else if (followers.length > 1) {
            issue.append("\n").append(bold(Factory.getTrackerBundle().getString("text.followers"))).append(":");
            for (ITrackerClient.IPerson follower : followers) {
                issue.append("\n ").append(follower.getDisplay());
            }
        }

        return issue.toString();
    }
}
