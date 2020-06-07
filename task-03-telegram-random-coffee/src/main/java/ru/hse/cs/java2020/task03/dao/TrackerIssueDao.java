package ru.hse.cs.java2020.task03.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.hse.cs.java2020.task03.models.TrackerIssue;
import ru.hse.cs.java2020.task03.utils.Factory;

public class TrackerIssueDao implements ITrackerIssueDao {
    public TrackerIssue getTrackerIssue(Integer telegramId) {
        return Factory.getSessionFactory().openSession().get(TrackerIssue.class, telegramId);
    }

    public void saveTrackerIssue(TrackerIssue trackerIssue) {
        Session session = Factory.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.save(trackerIssue);
        transaction.commit();
        session.close();
    }

    public void updateTrackerIssue(TrackerIssue trackerIssue) {
        Session session = Factory.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.update(trackerIssue);
        transaction.commit();
        session.close();
    }
}
