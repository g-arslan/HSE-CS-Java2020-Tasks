package ru.hse.cs.java2020.task03.utils;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public final class Factory {
    private static final SessionFactory OUR_SESSION_FACTORY;

    static {
        try {
            Configuration configuration = new Configuration();
            configuration.configure();

            OUR_SESSION_FACTORY = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private Factory() {
    }

    public static SessionFactory getSessionFactory() throws HibernateException {
        return OUR_SESSION_FACTORY;
    }
}
