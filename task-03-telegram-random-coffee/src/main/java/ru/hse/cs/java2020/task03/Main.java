package ru.hse.cs.java2020.task03;

import com.pengrad.telegrambot.TelegramBot;
import okhttp3.OkHttpClient;
import ru.hse.cs.java2020.task03.dao.TrackerIssueDao;
import ru.hse.cs.java2020.task03.dao.UserDao;
import ru.hse.cs.java2020.task03.telegram.TelegramHandler;
import ru.hse.cs.java2020.task03.tracker.TrackerClient;

import java.util.ResourceBundle;

public class Main {
    public static void main(String[] args) {
        TelegramHandler telegramClient = new TelegramHandler(
                new TelegramBot(System.getenv("TELEGRAM_BOT_TOKEN")),
                new TrackerClient(System.getenv("TRACKER_CLIENT_ID"), ResourceBundle.getBundle("tracker"), new OkHttpClient()),
                new UserDao(),
                new TrackerIssueDao(),
                ResourceBundle.getBundle("telegram"));

        telegramClient.startListener();
    }
}
