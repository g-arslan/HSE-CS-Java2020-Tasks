package ru.hse.cs.java2020.task03.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import ru.hse.cs.java2020.task03.models.User;
import ru.hse.cs.java2020.task03.utils.Factory;

public class UserDao implements IUserDao {
    public User getUser(Integer telegramId) {
        return Factory.getSessionFactory().openSession().get(User.class, telegramId);
    }

    public void saveUser(User user) {
        Session session = Factory.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.save(user);
        transaction.commit();
        session.close();
    }

    public void updateUser(User user) {
        Session session = Factory.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        session.update(user);
        transaction.commit();
        session.close();
    }
}
