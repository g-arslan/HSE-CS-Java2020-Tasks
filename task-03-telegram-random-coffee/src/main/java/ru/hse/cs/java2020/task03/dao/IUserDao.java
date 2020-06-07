package ru.hse.cs.java2020.task03.dao;

import ru.hse.cs.java2020.task03.models.User;

public interface IUserDao {
    User getUser(Integer telegramId);

    void saveUser(User user);

    void updateUser(User user);
}
