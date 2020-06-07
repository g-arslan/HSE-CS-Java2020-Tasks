package ru.hse.cs.java2020.task03.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pengrad.telegrambot.TelegramBot;
import okhttp3.OkHttpClient;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ru.hse.cs.java2020.task03.dao.ITrackerIssueDao;
import ru.hse.cs.java2020.task03.dao.IUserDao;
import ru.hse.cs.java2020.task03.dao.TrackerIssueDao;
import ru.hse.cs.java2020.task03.dao.UserDao;
import ru.hse.cs.java2020.task03.tracker.ITrackerClient;
import ru.hse.cs.java2020.task03.tracker.TrackerClient;

import java.util.ResourceBundle;

public final class Factory {
    private static final SessionFactory   OUR_SESSION_FACTORY;
    private static final TelegramBot      TELEGRAM_BOT;
    private static final OkHttpClient     HTTP_CLIENT;
    private static final IUserDao         USER_DAO;
    private static final ITrackerIssueDao TRACKER_ISSUE_DAO;
    private static final ITrackerClient   TRACKER_CLIENT;
    private static final ResourceBundle   TELEGRAM_BUNDLE;
    private static final ResourceBundle   TRACKER_BUNDLE;
    private static final GsonBuilder      GSON_BUILDER;
    private static final Gson             GSON;


    static {
        try {
            Configuration configuration = new Configuration();
            configuration.configure();

            OUR_SESSION_FACTORY = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }

        TELEGRAM_BOT = new TelegramBot(System.getenv("TELEGRAM_BOT_TOKEN"));
        HTTP_CLIENT = new OkHttpClient();
        USER_DAO = new UserDao();
        TRACKER_ISSUE_DAO = new TrackerIssueDao();
        TRACKER_CLIENT = new TrackerClient(System.getenv("TRACKER_CLIENT_ID"));
        TELEGRAM_BUNDLE = ResourceBundle.getBundle("telegram");
        TRACKER_BUNDLE = ResourceBundle.getBundle("tracker");
        GSON_BUILDER = new GsonBuilder();
        GSON = GSON_BUILDER.create();
    }

    private Factory() {
    }

    public static SessionFactory getSessionFactory() throws HibernateException {
        return OUR_SESSION_FACTORY;
    }

    public static OkHttpClient getHttpClient() {
        return HTTP_CLIENT;
    }

    public static IUserDao getUserDao() {
        return USER_DAO;
    }

    public static ITrackerIssueDao getTrackerIssueDao() {
        return TRACKER_ISSUE_DAO;
    }

    public static ITrackerClient getTrackerClient() {
        return TRACKER_CLIENT;
    }

    public static ResourceBundle getTelegramBundle() {
        return TELEGRAM_BUNDLE;
    }

    public static ResourceBundle getTrackerBundle() {
        return TRACKER_BUNDLE;
    }

    public static TelegramBot getTelegramBot() {
        return TELEGRAM_BOT;
    }

    public static Gson getGson() {
        return GSON;
    }
}
