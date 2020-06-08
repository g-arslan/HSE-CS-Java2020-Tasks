package ru.hse.cs.java2020.task03;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.response.BaseResponse;
import org.junit.Test;
import ru.hse.cs.java2020.task03.dao.TrackerIssueDao;
import ru.hse.cs.java2020.task03.dao.UserDao;
import ru.hse.cs.java2020.task03.telegram.TelegramHandler;
import ru.hse.cs.java2020.task03.tracker.ITrackerClient;
import ru.hse.cs.java2020.task03.tracker.TrackerClient;
import ru.hse.cs.java2020.task03.tracker.models.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MainTest {
    private static final Gson GSON = new GsonBuilder().create();

    private TrackerClient getTrackerClient() {
        TrackerClient trackerClient = mock(TrackerClient.class);

        Person person = new Person();
        person.setId("111");
        person.setDisplay("Вася");

        ITrackerClient.IQueue queue = new Queue("Q");
        queue.setDisplay("QD");
        queue.setId("222");

        ITrackerClient.IIssue issue = new Issue();
        issue.setSummary("Задача");
        issue.setDescription("Описание");
        issue.setKey("Q-1");
        issue.setQueue(queue);
        issue.setAssignee(person);
        issue.setCreatedBy(person);
        issue.setFollowers(new Person[]{person, person});

        ITrackerClient.IComment comment = new Comment();
        comment.setText("Комментарий");
        comment.setCreatedBy(person);

        ITrackerClient.IResponse<ITrackerClient.IIssue> issueResponse = new TrackerResponse<>("", true, 200);
        issueResponse.setData(issue);

        ITrackerClient.IResponse<ITrackerClient.IIssue[]> issuesResponse = new TrackerResponse<>("", true, 200);
        issuesResponse.setData(new ITrackerClient.IIssue[]{issue});
        issuesResponse.setHeadersData(new HeadersData("2"));

        ITrackerClient.IResponse<ITrackerClient.IPerson> personResponse = new TrackerResponse<>("", true, 200);
        personResponse.setData(person);

        ITrackerClient.IResponse<Boolean> trueResponse = new TrackerResponse<>("", true, 200);
        trueResponse.setData(true);

        ITrackerClient.IResponse<Boolean> falseResponse = new TrackerResponse<>("", false, 410);
        trueResponse.setData(false);

        ITrackerClient.IResponse<ITrackerClient.IComment[]> commentsResponse = new TrackerResponse<>("", true, 200);
        commentsResponse.setData(new ITrackerClient.IComment[]{comment, comment});

        when(trackerClient.isTokenValid(any(), eq("aaaaa"))).thenReturn(trueResponse);
        when(trackerClient.isTokenValid(any(), eq("bbbbb"))).thenReturn(falseResponse);
        when(trackerClient.isQueueValid(any(), eq("Q"))).thenReturn(trueResponse);
        when(trackerClient.isQueueValid(any(), eq("B"))).thenReturn(falseResponse);
        when(trackerClient.createIssue(any(), any())).thenReturn(issueResponse);
        when(trackerClient.filterIssues(any(), any(), anyInt(), anyInt())).thenReturn(issuesResponse);
        when(trackerClient.getIssue(any(), any())).thenReturn(issueResponse);
        when(trackerClient.getMe(any())).thenReturn(personResponse);
        when(trackerClient.getComments(any(), any())).thenReturn(commentsResponse);
        when(trackerClient.getAuthLink()).thenReturn("authlink");
        when(trackerClient.getBundle()).thenReturn(ResourceBundle.getBundle("tracker"));

        return trackerClient;
    }

    private ArrayList<String> feedUpdates(ArrayList<String> messages) {
        TelegramHandler telegramClient = new TelegramHandler(
                new MockTelegramBot(""),
                getTrackerClient(),
                new UserDao(),
                new TrackerIssueDao(),
                ResourceBundle.getBundle("telegram"));

        for (String message : messages) {
            Update update = GSON.fromJson(String.format(
                    "{\"message\": {\"chat\": {\"id\": 666}, \"text\": \"%s\", \"from\": {\"id\": 333, \"username\": \"vasya\"}}}",
                    message),
                    Update.class);
            telegramClient.handleUpdate(update);
        }

        return (ArrayList<String>) ((MockTelegramBot) telegramClient.getBot()).getMessages();
    }

    @Test
    public void testBot() {
        assertEquals(new ArrayList<>(Arrays.asList(
                "Пришлите ID вашей организии, посмотреть его можно здесь https://tracker.yandex.ru/settings",
                "ID организации успешно изменен\n"
                        + "Перейдите по адресу authlink и пришлите в ответ полученную строку",
                "Невалидный токен, попробуйте ещё раз\n"
                        + "Перейдите по адресу authlink и пришлите в ответ полученную строку",
                "Авторизация прошла успешно",
                "Пришлите очередь",
                "Произошла ошибка\n"
                        + "Попробуйте ещё раз",
                "Пришлите название задачи",
                "Пришлите описание задачи",
                "Назначить вас исполнителем?",
                "Некорректный ответ, попробуйте ещё раз",
                "Задача \"Задача\" успешно создана по ключу Q-1\n"
                        + "Подробнее /get_issue_Q_1",
                "Пришлите очередь",
                "Пришлите название задачи",
                "Пришлите описание задачи",
                "Назначить вас исполнителем?",
                "Задача \"Задача\" успешно создана по ключу Q-1\n"
                        + "Подробнее /get_issue_Q_1",
                "Пришлите очередь",
                "Пришлите название задачи",
                "Пришлите описание задачи",
                "Назначить вас исполнителем?",
                "Действие отменено",
                "Пришлите очередь",
                "Пришлите название задачи",
                "Пришлите описание задачи",
                "Действие отменено",
                "Пришлите очередь",
                "Пришлите название задачи",
                "Действие отменено",
                "Пришлите очередь",
                "Действие отменено",
                "Неизвестная команда",
                "Введите ключ задачи",
                "Неккоректный формат ключа, попробуйте ещё раз",
                "<a href=\"https://tracker.yandex.ru/Q-1\">Q-1</a>: <b>Задача</b>\n"
                        + "Описание\n"
                        + "\n"
                        + "<b>Автор</b>: Вася\n"
                        + "<b>Исполнитель</b>: Вася\n"
                        + "<b>Наблюдатели</b>:\n"
                        + " Вася\n"
                        + " Вася\n"
                        + "\n"
                        + "<b>Комментарии</b>:\n"
                        + "\n"
                        + "Вася:\n"
                        + "<i>Комментарий</i>\n"
                        + "\n"
                        + "Вася:\n"
                        + "<i>Комментарий</i>",
                "Введите ключ задачи",
                "Действие отменено",
                "Неккоректный формат ключа, попробуйте ещё раз",
                "<a href=\"https://tracker.yandex.ru/Q-1\">Q-1</a>: <b>Задача</b>\n"
                        + "Описание\n"
                        + "\n"
                        + "<b>Автор</b>: Вася\n"
                        + "<b>Исполнитель</b>: Вася\n"
                        + "<b>Наблюдатели</b>:\n"
                        + " Вася\n"
                        + " Вася\n"
                        + "\n"
                        + "<b>Комментарии</b>:\n"
                        + "\n"
                        + "Вася:\n"
                        + "<i>Комментарий</i>\n"
                        + "\n"
                        + "Вася:\n"
                        + "<i>Комментарий</i>",
                "<a href=\"https://tracker.yandex.ru/Q-1\">Q-1</a>: <b>Задача</b>\n"
                        + "Описание\n"
                        + "\n"
                        + "<b>Автор</b>: Вася\n"
                        + "<b>Исполнитель</b>: Вася\n"
                        + "<b>Наблюдатели</b>:\n"
                        + " Вася\n"
                        + " Вася\n"
                        + "\n"
                        + "<b>Комментарии</b>:\n"
                        + "\n"
                        + "Вася:\n"
                        + "<i>Комментарий</i>\n"
                        + "\n"
                        + "Вася:\n"
                        + "<i>Комментарий</i>",
                "<a href=\"https://tracker.yandex.ru/Q-1\">Q-1</a>: <b>Задача</b>\n"
                        + "Описание\n"
                        + "\n"
                        + "<b>Автор</b>: Вася\n"
                        + "<b>Исполнитель</b>: Вася\n"
                        + "<b>Наблюдатели</b>:\n"
                        + " Вася\n"
                        + " Вася\n"
                        + "\n"
                        + "<b>Комментарии</b>:\n"
                        + "\n"
                        + "Вася:\n"
                        + "<i>Комментарий</i>\n"
                        + "\n"
                        + "Вася:\n"
                        + "<i>Комментарий</i>",
                "Неккоректный формат ключа, попробуйте ещё раз",
                "Q-1: <b>Задача</b>\n"
                        + "Подробнее /get_issue_Q_1\n"
                        + "\n"
                        + "Страница 1 из 2"
        )), feedUpdates(new ArrayList<>(Arrays.asList(
                "/start",
                "555",
                "bbbbb",
                "aaaaa",
                "/create_issue",
                "B",
                "Q",
                "name",
                "desc",
                "aaa",
                "да",
                "/create_issue",
                "Q",
                "name",
                "desc",
                "нет",
                "/create_issue",
                "Q",
                "name",
                "desc",
                "/cancel",
                "/create_issue",
                "Q",
                "name",
                "/cancel",
                "/create_issue",
                "Q",
                "/cancel",
                "/create_issue",
                "/cancel",
                "/cancel",
                "/get_issue",
                "Qsdads",
                "Q-1",
                "/get_issue",
                "/cancel",
                "/get_issue adadsl",
                "/get_issue Q-1",
                "/get_issue       Q-1",
                "/get_issue_Q_1",
                "/get_issue_asdasd",
                "/get_issues_for_me"
        ))));
    }

    private static class MockTelegramBot extends TelegramBot {
        private final List<String> messages;

        public MockTelegramBot(String botToken) {
            super(botToken);
            messages = new ArrayList<>();
        }

        @Override
        public <T extends BaseRequest, R extends BaseResponse> R execute(BaseRequest<T, R> request) {
            messages.add((String) request.getParameters().get("text"));
            return null;
        }

        public List<String> getMessages() {
            return messages;
        }
    }

}
