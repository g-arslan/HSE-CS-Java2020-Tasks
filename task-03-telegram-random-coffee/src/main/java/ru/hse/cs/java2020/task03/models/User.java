package ru.hse.cs.java2020.task03.models;

import ru.hse.cs.java2020.task03.telegram.TelegramHandler;
import ru.hse.cs.java2020.task03.tracker.ITrackerClient;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "users")
public class User implements ITrackerClient.IUser {
    @Id
    private Integer telegramUserId;

    private Long organisationId;

    private String trackerAccessToken;

    private TelegramHandler.State state;

    @OneToOne(fetch = FetchType.EAGER)
    @MapsId
    private TrackerIssue issue;

    public User() {
    }

    public User(Integer telegramUserId, TelegramHandler.State state) {
        this.telegramUserId = telegramUserId;
        this.state = state;
    }

    public Integer getTelegramUserId() {
        return telegramUserId;
    }

    public Long getOrganisationId() {
        return organisationId;
    }

    public void setOrganisationId(Long organisationId) {
        this.organisationId = organisationId;
    }

    public String getTrackerAccessToken() {
        return trackerAccessToken;
    }

    public void setTrackerAccessToken(String trackerAccessToken) {
        this.trackerAccessToken = trackerAccessToken;
    }

    public TelegramHandler.State getState() {
        return state;
    }

    public void setState(TelegramHandler.State state) {
        this.state = state;
    }

    public TrackerIssue getIssue() {
        return issue;
    }

    public void setIssue(TrackerIssue issue) {
        this.issue = issue;
    }
}
