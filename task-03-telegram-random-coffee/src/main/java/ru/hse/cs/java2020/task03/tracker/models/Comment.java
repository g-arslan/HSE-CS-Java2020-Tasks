package ru.hse.cs.java2020.task03.tracker.models;

import ru.hse.cs.java2020.task03.tracker.ITrackerClient;

import static ru.hse.cs.java2020.task03.utils.Utils.italic;

public class Comment implements ITrackerClient.IComment {
    private Person createdBy;
    private String text;

    public ITrackerClient.IPerson getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(ITrackerClient.IPerson createdBy) {
        this.createdBy = (Person) createdBy;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String formatPrettyHtml() {
        return createdBy.getDisplay() + ":\n" + italic(text);
    }
}
