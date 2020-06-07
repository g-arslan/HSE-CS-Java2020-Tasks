package ru.hse.cs.java2020.task03.dao;

import ru.hse.cs.java2020.task03.models.TrackerIssue;

public interface ITrackerIssueDao {
    TrackerIssue getTrackerIssue(Integer telegramId);

    void saveTrackerIssue(TrackerIssue trackerIssue);

    void updateTrackerIssue(TrackerIssue trackerIssue);
}
