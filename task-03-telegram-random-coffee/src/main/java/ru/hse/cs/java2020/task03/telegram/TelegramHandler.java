package ru.hse.cs.java2020.task03.telegram;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import ru.hse.cs.java2020.task03.models.TrackerIssue;
import ru.hse.cs.java2020.task03.models.User;
import ru.hse.cs.java2020.task03.tracker.ITrackerClient;
import ru.hse.cs.java2020.task03.tracker.models.Queue;
import ru.hse.cs.java2020.task03.utils.Factory;

import java.util.regex.Pattern;

import static ru.hse.cs.java2020.task03.telegram.Utils.checkIsCmd;
import static ru.hse.cs.java2020.task03.telegram.Utils.generateIssueLink;
import static ru.hse.cs.java2020.task03.tracker.Utils.formatIssueWithCommentsPrettyHtml;

public class TelegramHandler {
    public TelegramHandler() {
        Factory.getTelegramBot().setUpdatesListener(updates -> {
            for (Update update : updates) {
                handleUpdate(update);
            }

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void handleUpdate(Update update) {
        if (update.callbackQuery() != null) {
            handleCallbackQuery(Factory.getUserDao().getUser(update.callbackQuery().from().id()), update);
            return;
        }

        User user = Factory.getUserDao().getUser(update.message().from().id());
        if (user == null) {
            user = new User(update.message().from().id(), State.NEED_ORG_ID);
            user.setIssue(new TrackerIssue(update.message().from().id()));
            Factory.getTrackerIssueDao().saveTrackerIssue(user.getIssue());
            Factory.getUserDao().saveUser(user);
        } else if (user.getOrganisationId() == null && user.getState() != State.EXPECT_ORG_ID) {
            updateState(user, State.NEED_ORG_ID);
        } else if (user.getTrackerAccessToken() == null && user.getState() != State.EXPECT_ACCESS_TOKEN
                && user.getState() != State.EXPECT_ORG_ID) {
            updateState(user, State.NEED_ACCESS_TOKEN);
        }

        System.out.println(update.message().from().username() + ": " + update.message().text());

        switch (user.getState()) {
            case NEED_ORG_ID:
                handleNeedSmth(user, update, Factory.getTelegramBundle().getString("text.needOrgId"),
                        State.EXPECT_ORG_ID);
                break;
            case EXPECT_ORG_ID:
                handleExpectOrgId(user, update);
                break;
            case NEED_ACCESS_TOKEN:
                handleNeedSmth(user, update,
                        String.format(Factory.getTelegramBundle().getString("text.needAccessToken"),
                                Factory.getTrackerClient().getAuthLink()), State.EXPECT_ACCESS_TOKEN);
                break;
            case EXPECT_ACCESS_TOKEN:
                handleExpectAccessToken(user, update);
                break;
            case NEED_QUEUE:
                handleNeedSmth(user, update, Factory.getTelegramBundle().getString("text.needQueue"),
                        State.EXPECT_QUEUE);
                break;
            case EXPECT_QUEUE:
                handleExpectQueue(user, update);
                break;
            case NEED_SUMMARY:
                handleNeedSmth(user, update, Factory.getTelegramBundle().getString("text.needSummary"),
                        State.EXPECT_SUMMARY);
                break;
            case EXPECT_SUMMARY:
                handleExpectSummary(user, update);
                break;
            case NEED_DESCRIPTION:
                handleNeedSmth(user, update, Factory.getTelegramBundle().getString("text.needDescription"),
                        State.EXPECT_DESCRIPTION);
                break;
            case EXPECT_DESCRIPTION:
                handleExpectDescription(user, update);
                break;
            case NEED_ASSIGN_YOU:
                handleNeedSmth(user, update, Factory.getTelegramBundle().getString("text.needAssignYou"),
                        State.EXPECT_ASSIGN_YOU);
                break;
            case EXPECT_ASSIGN_YOU:
                handleAssignYou(user, update);
                break;
            case NEED_ISSUE_KEY:
                handleNeedSmth(user, update, Factory.getTelegramBundle().getString("text.needIssueKey"),
                        State.EXPECT_ISSUE_KEY);
                break;
            case EXPECT_ISSUE_KEY:
                handleExpectIssueKey(user, update);
                break;
            case MAIN_MENU:
            default:
                handleDefault(user, update);
        }
    }

    private Command classifyMessage(String text) {
        if (Utils.checkIsCmd(text, "cancel")) {
            return Command.CANCEL;
        } else if (Utils.checkIsCmd(text, "create_issue")) {
            return Command.CREATE_ISSUE;
        } else if (Utils.checkIsCmd(text, "get_issues_for_me")) {
            return Command.GET_ISSUES_FOR_ME;
        } else if (Utils.checkIsCmd(text, "get_issue")) {
            return Command.GET_ISSUE;
        } else if (Utils.checkIsCmd(text, "changeOrgId")) {
            return Command.CHANGE_ORG_ID;
        } else if (Utils.checkIsCmd(text, "changeAccessToken")) {
            return Command.CHANGE_ACCESS_TOKEN;
        } else if (text.toLowerCase().equals(Factory.getTelegramBundle().getString("button.yes").toLowerCase())) {
            return Command.YES;
        } else if (text.toLowerCase().equals(Factory.getTelegramBundle().getString("button.no").toLowerCase())) {
            return Command.NO;
        }
        return Command.UNKNOWN;
    }

    private void sendTextMessage(User user, Update update, String text) {
        sendTextMessage(user, update, text, null);
    }

    private void sendTextMessage(User user, Update update, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage(update.message().chat().id(), text);
        if (inlineKeyboardMarkup != null) {
            sendMessage.replyMarkup(inlineKeyboardMarkup);
        } else {
            switch (user.getState()) {
                case MAIN_MENU:
                    sendMessage.replyMarkup(new ReplyKeyboardMarkup(
                            new String[]{Factory.getTelegramBundle().getString("button.create_issue"),
                                    Factory.getTelegramBundle().getString("button.get_issue")},
                            new String[]{Factory.getTelegramBundle().getString("button.get_issues_for_me")},
                            new String[]{Factory.getTelegramBundle().getString("button.changeOrgId"),
                                    Factory.getTelegramBundle().getString("button.changeAccessToken")}
                    ).resizeKeyboard(true));
                    break;
                case EXPECT_ASSIGN_YOU:
                    sendMessage.replyMarkup(new ReplyKeyboardMarkup(
                            new String[]{Factory.getTelegramBundle().getString("button.cancel")},
                            new String[]{Factory.getTelegramBundle().getString("button.no"),
                                    Factory.getTelegramBundle().getString("button.yes")}
                    ).resizeKeyboard(true));
                    break;
                case EXPECT_DESCRIPTION:
                case EXPECT_ISSUE_KEY:
                case EXPECT_QUEUE:
                case EXPECT_SUMMARY:
                case EXPECT_ACCESS_TOKEN:
                case EXPECT_ORG_ID:
                    sendMessage.replyMarkup(new ReplyKeyboardMarkup(
                            new String[]{Factory.getTelegramBundle().getString("button.cancel")}
                    ).resizeKeyboard(true));
                    break;
                default:
                    sendMessage.replyMarkup(new ReplyKeyboardRemove());
            }
        }
        Factory.getTelegramBot().execute(sendMessage.parseMode(ParseMode.HTML));
    }

    private void handleDefault(User user, Update update) {
        Command command = classifyMessage(update.message().text());

        switch (command) {
            case CREATE_ISSUE:
                handleCreateIssue(user, update);
                break;
            case GET_ISSUE:
                handleGetIssue(user, update);
                break;
            case GET_ISSUES_FOR_ME:
                handleGetIssuesForMe(user, update);
                break;
            case CHANGE_ORG_ID:
                updateState(user, State.NEED_ORG_ID);
                handleUpdate(update);
                break;
            case CHANGE_ACCESS_TOKEN:
                updateState(user, State.NEED_ACCESS_TOKEN);
                handleUpdate(update);
                break;
            case UNKNOWN:
            default:
                sendTextMessage(user, update, Factory.getTelegramBundle().getString("text.unknownCommand"));
        }
    }

    private void handleGetIssue(User user, Update update) {
        if (!update.message().text().startsWith(Factory.getTelegramBundle().getString("command.get_issue"))
                || update.message().text().length()
                <= Factory.getTelegramBundle().getString("command.get_issue").length() + 1) {
            updateState(user, State.NEED_ISSUE_KEY);
            handleUpdate(update);
        } else {
            String data = update.message().text()
                                .substring(Factory.getTelegramBundle().getString("command.get_issue").length() + 1)
                                .replace('_', '-').strip();
            handleExpectIssueKey(user, update, data);
        }
    }

    private void handleNeedSmth(User user, Update update, String message, State nextState) {
        updateState(user, nextState);

        sendTextMessage(user, update, message);
    }

    private void handleCreateIssue(User user, Update update) {
        updateState(user, State.NEED_QUEUE);
        handleUpdate(update);
    }

    private void handleGetIssuesForMe(User user, Update update) {
        ITrackerClient.IResponse<ITrackerClient.IPerson> me = Factory.getTrackerClient().getMe(user);

        if (checkAndHandleErrors(user, update, me, true)) {
            return;
        }

        ITrackerClient.IResponse<ITrackerClient.IIssue[]> issues = Utils.getIssuesByPage(user, me, 1,
                Integer.parseInt(Factory.getTelegramBundle().getString("constant.perPage")));

        if (checkAndHandleErrors(user, update, issues, true)) {
            return;
        }

        sendTextMessage(user, update, Utils.generateTextByTickets(issues, 1),
                Utils.generateInlineKeyboardMarkupByPage(1,
                        Integer.parseInt(issues.getHeadersData().getTotalPages())));
    }

    private void handleCallbackQuery(User user, Update update) {
        int curPage = Integer.parseInt(update.callbackQuery().data());

        ITrackerClient.IResponse<ITrackerClient.IPerson> me = Factory.getTrackerClient().getMe(user);

        if (checkAndHandleErrors(user, update, me, true, true)) {
            return;
        }

        ITrackerClient.IResponse<ITrackerClient.IIssue[]> issues = Utils.getIssuesByPage(user, me, curPage,
                Integer.parseInt(Factory.getTelegramBundle().getString("constant.perPage")));

        if (checkAndHandleErrors(user, update, issues, true, true)) {
            return;
        }

        Factory.getTelegramBot().execute(new EditMessageText(update.callbackQuery().message().chat().id(),
                update.callbackQuery().message().messageId(), Utils.generateTextByTickets(issues, curPage))
                .replyMarkup(Utils.generateInlineKeyboardMarkupByPage(curPage,
                        Integer.parseInt(issues.getHeadersData().getTotalPages()))).parseMode(ParseMode.HTML));
    }

    private boolean checkAndHandleCancel(User user, Update update) {
        if (checkIsCmd(update.message().text(), "cancel")) {
            updateState(user, State.MAIN_MENU);
            sendTextMessage(user, update, Factory.getTelegramBundle().getString("text.cancelAction"));

            return true;
        }

        return false;
    }

    private <T> boolean checkAndHandleErrors(User user, Update update, ITrackerClient.IResponse<T> response,
            boolean forceReturn) {
        return checkAndHandleErrors(user, update, response, forceReturn, false);
    }

    private <T> boolean checkAndHandleErrors(User user, Update update, ITrackerClient.IResponse<T> response,
            boolean forceReturn, boolean isCallbackQuery) {
        String text;

        if (response != null && response.isSuccessful()) {
            return false;
        } else if (response == null) {
            updateState(user, State.MAIN_MENU);

            text = Factory.getTelegramBundle().getString("text.unknownError");
        } else {
            StringBuilder errorText = new StringBuilder();

            if (response.getError() != null && response.getError().getErrorMessages() != null) {
                for (String error : response.getError().getErrorMessages()) {
                    errorText.append(error).append("\n");
                }
            }

            if (response.code() == 401 || response.code() == 403) {
                updateState(user, State.MAIN_MENU);

                text = String.format(Factory.getTelegramBundle().getString("text.forbidden"), errorText.toString());
            } else {
                if (forceReturn) {
                    updateState(user, State.MAIN_MENU);
                }

                text = String.format(Factory.getTelegramBundle().getString("text.error"), errorText.toString());
            }
        }

        if (isCallbackQuery) {
            Factory.getTelegramBot().execute(new EditMessageText(update.callbackQuery().message().chat().id(),
                    update.callbackQuery().message().messageId(), text).replyMarkup(new InlineKeyboardMarkup()));
        } else {
            sendTextMessage(user, update, text);
        }

        return true;
    }

    private void handleExpectQueue(User user, Update update) {
        if (checkAndHandleCancel(user, update)) {
            return;
        }

        ITrackerClient.IResponse<Boolean> queue =
                Factory.getTrackerClient().isQueueValid(user, update.message().text());

        if (checkAndHandleErrors(user, update, queue, false)) {
            return;
        }

        user.getIssue().setQueue(new Queue(update.message().text().toUpperCase()));
        Factory.getTrackerIssueDao().updateTrackerIssue(user.getIssue());

        updateState(user, State.NEED_SUMMARY);
        handleUpdate(update);
    }

    private void handleExpectSummary(User user, Update update) {
        if (checkAndHandleCancel(user, update)) {
            return;
        }

        user.getIssue().setSummary(update.message().text());
        Factory.getTrackerIssueDao().updateTrackerIssue(user.getIssue());

        updateState(user, State.NEED_DESCRIPTION);
        handleUpdate(update);
    }

    private void handleExpectDescription(User user, Update update) {
        if (checkAndHandleCancel(user, update)) {
            return;
        }

        user.getIssue().setDescription(update.message().text());
        Factory.getTrackerIssueDao().updateTrackerIssue(user.getIssue());

        updateState(user, State.NEED_ASSIGN_YOU);
        handleUpdate(update);
    }

    private void handleExpectIssueKey(User user, Update update, String key) {
        if (checkAndHandleCancel(user, update)) {
            return;
        }

        if (!Pattern.matches("[A-Z]+-[0-9]+", key.toUpperCase())) {
            sendTextMessage(user, update, Factory.getTelegramBundle().getString("text.incorrectIssueKey"));
        } else {
            ITrackerClient.IResponse<ITrackerClient.IIssue> issue =
                    Factory.getTrackerClient().getIssue(user, key.toUpperCase());

            if (checkAndHandleErrors(user, update, issue, false)) {
                return;
            }

            ITrackerClient.IResponse<ITrackerClient.IComment[]> comments =
                    Factory.getTrackerClient().getComments(user, issue.getData());

            if (checkAndHandleErrors(user, update, comments, true)) {
                return;
            }

            updateState(user, State.MAIN_MENU);

            sendTextMessage(user, update,
                    formatIssueWithCommentsPrettyHtml(issue.getData(), comments.getData()));
        }
    }

    private void handleExpectIssueKey(User user, Update update) {
        handleExpectIssueKey(user, update, update.message().text());
    }

    private void handleAssignYou(User user, Update update) {
        if (checkAndHandleCancel(user, update)) {
            return;
        }

        Command command = classifyMessage(update.message().text());

        switch (command) {
            case YES:
                user.getIssue().setAssignee(Factory.getTrackerClient().getMe(user).getData());
                Factory.getTrackerIssueDao().updateTrackerIssue(user.getIssue());
                break;
            case NO:
                user.getIssue().setAssignee(null);
                Factory.getTrackerIssueDao().updateTrackerIssue(user.getIssue());
                break;
            case UNKNOWN:
            default:
                sendTextMessage(user, update, Factory.getTelegramBundle().getString("text.incorrectAnswer"));
                return;
        }

        ITrackerClient.IResponse<ITrackerClient.IIssue> issue =
                Factory.getTrackerClient().createIssue(user, user.getIssue());

        if (checkAndHandleErrors(user, update, issue, true)) {
            return;
        }

        updateState(user, State.MAIN_MENU);

        sendTextMessage(user, update,
                String.format(Factory.getTelegramBundle().getString("text.createIssueSuccess"),
                        issue.getData().getSummary(), issue.getData().getKey(),
                        generateIssueLink(issue.getData())));
    }

    private void handleExpectAccessToken(User user, Update update) {
        if (checkAndHandleCancel(user, update)) {
            return;
        }

        if (Utils.checkHeader(update.message().text())) {
            ITrackerClient.IResponse<Boolean> response =
                    Factory.getTrackerClient().isTokenValid(user, update.message().text());

            if (checkAndHandleErrors(user, update, response, false)) {
                return;
            }

            user.setTrackerAccessToken(update.message().text());
            updateState(user, State.MAIN_MENU);

            sendTextMessage(user, update,
                    Factory.getTelegramBundle().getString("text.authSuccess"));
        } else {
            String text = Factory.getTelegramBundle().getString("text.authError")
                    + "\n"
                    + String.format(Factory.getTelegramBundle().getString("text.needAccessToken"),
                    Factory.getTrackerClient().getAuthLink());
            sendTextMessage(user, update, text);
        }
    }

    private void handleExpectOrgId(User user, Update update) {
        if (checkAndHandleCancel(user, update)) {
            return;
        }

        long organisationId;
        try {
            organisationId = Long.parseLong(update.message().text());
        } catch (NumberFormatException e) {
            String text = Factory.getTelegramBundle().getString("text.parseOrgIdError")
                    + "\n"
                    + Factory.getTelegramBundle().getString("text.needOrgId");
            sendTextMessage(user, update, text);
            return;
        }

        StringBuilder text = new StringBuilder(Factory.getTelegramBundle().getString("text.parseOrgIdSuccess"));

        user.setOrganisationId(organisationId);
        if (user.getTrackerAccessToken() == null) {
            updateState(user, State.EXPECT_ACCESS_TOKEN);
            text.append("\n").append(String.format(Factory.getTelegramBundle().getString("text.needAccessToken"),
                    Factory.getTrackerClient().getAuthLink()));
        } else {
            updateState(user, State.MAIN_MENU);
        }

        sendTextMessage(user, update, text.toString());
    }

    private void updateState(User user, State state) {
        user.setState(state);
        Factory.getUserDao().updateUser(user);
    }

    public enum State {
        MAIN_MENU,
        NEED_ORG_ID,
        EXPECT_ORG_ID,
        NEED_ACCESS_TOKEN,
        EXPECT_ACCESS_TOKEN,
        NEED_QUEUE,
        EXPECT_QUEUE,
        NEED_SUMMARY,
        EXPECT_SUMMARY,
        NEED_DESCRIPTION,
        EXPECT_DESCRIPTION,
        NEED_ASSIGN_YOU,
        EXPECT_ASSIGN_YOU,
        NEED_ISSUE_KEY,
        EXPECT_ISSUE_KEY,
    }

    public enum Command {
        CANCEL,
        CREATE_ISSUE,
        GET_ISSUE,
        GET_ISSUES_FOR_ME,
        CHANGE_ORG_ID,
        CHANGE_ACCESS_TOKEN,
        YES,
        NO,
        UNKNOWN,
    }
}
