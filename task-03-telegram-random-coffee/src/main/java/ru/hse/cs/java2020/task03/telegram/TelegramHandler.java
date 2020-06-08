package ru.hse.cs.java2020.task03.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.SendMessage;
import ru.hse.cs.java2020.task03.dao.ITrackerIssueDao;
import ru.hse.cs.java2020.task03.dao.IUserDao;
import ru.hse.cs.java2020.task03.models.TrackerIssue;
import ru.hse.cs.java2020.task03.models.User;
import static ru.hse.cs.java2020.task03.telegram.Utils.checkIsCmd;
import static ru.hse.cs.java2020.task03.telegram.Utils.generateIssueLink;
import ru.hse.cs.java2020.task03.tracker.ITrackerClient;
import static ru.hse.cs.java2020.task03.tracker.Utils.formatIssueWithCommentsPrettyHtml;
import ru.hse.cs.java2020.task03.tracker.models.Queue;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class TelegramHandler {
    private final TelegramBot      bot;
    private final ITrackerClient   trackerClient;
    private final IUserDao         userDao;
    private final ITrackerIssueDao trackerIssueDao;
    private final ResourceBundle   bundle;

    public TelegramHandler(TelegramBot bot, ITrackerClient trackerClient,
            IUserDao userDao, ITrackerIssueDao trackerIssueDao, ResourceBundle bundle) {
        this.bot = bot;
        this.trackerClient = trackerClient;
        this.userDao = userDao;
        this.trackerIssueDao = trackerIssueDao;
        this.bundle = bundle;
    }

    public void startListener() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                handleUpdate(update);
            }

            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    public void stopListener() {
        bot.removeGetUpdatesListener();
    }

    public TelegramBot getBot() {
        return bot;
    }

    public void handleUpdate(Update update) {
        if (update.callbackQuery() != null) {
            handleCallbackQuery(userDao.getUser(update.callbackQuery().from().id()), update);
            return;
        }

        User user = userDao.getUser(update.message().from().id());

        if (update.message().text() == null) {
            sendTextMessage(user, update, bundle.getString("text.unknownCommand"));
            return;
        }

        if (user == null) {
            user = new User(update.message().from().id(), State.NEED_ORG_ID);
            user.setIssue(new TrackerIssue(update.message().from().id()));
            trackerIssueDao.saveTrackerIssue(user.getIssue());
            userDao.saveUser(user);
        } else if (user.getOrganisationId() == null && user.getState() != State.EXPECT_ORG_ID) {
            updateState(user, State.NEED_ORG_ID);
        } else if (user.getTrackerAccessToken() == null && user.getState() != State.EXPECT_ACCESS_TOKEN
                && user.getState() != State.EXPECT_ORG_ID) {
            updateState(user, State.NEED_ACCESS_TOKEN);
        }

        switch (user.getState()) {
            case NEED_ORG_ID:
                handleNeedSmth(user, update, bundle.getString("text.needOrgId"),
                        State.EXPECT_ORG_ID);
                break;
            case EXPECT_ORG_ID:
                handleExpectOrgId(user, update);
                break;
            case NEED_ACCESS_TOKEN:
                handleNeedSmth(user, update,
                        String.format(bundle.getString("text.needAccessToken"),
                                trackerClient.getAuthLink()), State.EXPECT_ACCESS_TOKEN);
                break;
            case EXPECT_ACCESS_TOKEN:
                handleExpectAccessToken(user, update);
                break;
            case NEED_QUEUE:
                handleNeedSmth(user, update, bundle.getString("text.needQueue"),
                        State.EXPECT_QUEUE);
                break;
            case EXPECT_QUEUE:
                handleExpectQueue(user, update);
                break;
            case NEED_SUMMARY:
                handleNeedSmth(user, update, bundle.getString("text.needSummary"),
                        State.EXPECT_SUMMARY);
                break;
            case EXPECT_SUMMARY:
                handleExpectSummary(user, update);
                break;
            case NEED_DESCRIPTION:
                handleNeedSmth(user, update, bundle.getString("text.needDescription"),
                        State.EXPECT_DESCRIPTION);
                break;
            case EXPECT_DESCRIPTION:
                handleExpectDescription(user, update);
                break;
            case NEED_ASSIGN_YOU:
                handleNeedSmth(user, update, bundle.getString("text.needAssignYou"),
                        State.EXPECT_ASSIGN_YOU);
                break;
            case EXPECT_ASSIGN_YOU:
                handleAssignYou(user, update);
                break;
            case NEED_ISSUE_KEY:
                handleNeedSmth(user, update, bundle.getString("text.needIssueKey"),
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
        if (Utils.checkIsCmd(text, "cancel", bundle)) {
            return Command.CANCEL;
        } else if (Utils.checkIsCmd(text, "create_issue", bundle)) {
            return Command.CREATE_ISSUE;
        } else if (Utils.checkIsCmd(text, "get_issues_for_me", bundle)) {
            return Command.GET_ISSUES_FOR_ME;
        } else if (Utils.checkIsCmd(text, "get_issue", bundle)) {
            return Command.GET_ISSUE;
        } else if (Utils.checkIsCmd(text, "changeOrgId", bundle)) {
            return Command.CHANGE_ORG_ID;
        } else if (Utils.checkIsCmd(text, "changeAccessToken", bundle)) {
            return Command.CHANGE_ACCESS_TOKEN;
        } else if (text.toLowerCase().equals(bundle.getString("button.yes").toLowerCase())) {
            return Command.YES;
        } else if (text.toLowerCase().equals(bundle.getString("button.no").toLowerCase())) {
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
                            new String[]{bundle.getString("button.create_issue"),
                                    bundle.getString("button.get_issue")},
                            new String[]{bundle.getString("button.get_issues_for_me")},
                            new String[]{bundle.getString("button.changeOrgId"),
                                    bundle.getString("button.changeAccessToken")}
                    ).resizeKeyboard(true));
                    break;
                case EXPECT_ASSIGN_YOU:
                    sendMessage.replyMarkup(new ReplyKeyboardMarkup(
                            new String[]{bundle.getString("button.cancel")},
                            new String[]{bundle.getString("button.no"),
                                    bundle.getString("button.yes")}
                    ).resizeKeyboard(true));
                    break;
                case EXPECT_DESCRIPTION:
                case EXPECT_ISSUE_KEY:
                case EXPECT_QUEUE:
                case EXPECT_SUMMARY:
                case EXPECT_ACCESS_TOKEN:
                case EXPECT_ORG_ID:
                    sendMessage.replyMarkup(new ReplyKeyboardMarkup(
                            new String[]{bundle.getString("button.cancel")}
                    ).resizeKeyboard(true));
                    break;
                default:
                    sendMessage.replyMarkup(new ReplyKeyboardRemove());
            }
        }
        bot.execute(sendMessage.parseMode(ParseMode.HTML));
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
                sendTextMessage(user, update, bundle.getString("text.unknownCommand"));
        }
    }

    private void handleGetIssue(User user, Update update) {
        if (!update.message().text().startsWith(bundle.getString("command.get_issue"))
                || update.message().text().length()
                <= bundle.getString("command.get_issue").length() + 1) {
            updateState(user, State.NEED_ISSUE_KEY);
            handleUpdate(update);
        } else {
            String data = update.message().text()
                                .substring(bundle.getString("command.get_issue").length() + 1)
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
        ITrackerClient.IResponse<ITrackerClient.IPerson> me = trackerClient.getMe(user);

        if (checkAndHandleErrors(user, update, me, true)) {
            return;
        }

        ITrackerClient.IResponse<ITrackerClient.IIssue[]> issues = Utils.getIssuesByPage(user, me, 1,
                Integer.parseInt(bundle.getString("constant.perPage")), trackerClient);

        if (checkAndHandleErrors(user, update, issues, true)) {
            return;
        }

        sendTextMessage(user, update, Utils.generateTextByTickets(issues, 1, bundle),
                Utils.generateInlineKeyboardMarkupByPage(1,
                        Integer.parseInt(issues.getHeadersData().getTotalPages()), bundle));
    }

    private void handleCallbackQuery(User user, Update update) {
        int curPage = Integer.parseInt(update.callbackQuery().data());

        ITrackerClient.IResponse<ITrackerClient.IPerson> me = trackerClient.getMe(user);

        if (checkAndHandleErrors(user, update, me, true, true)) {
            return;
        }

        ITrackerClient.IResponse<ITrackerClient.IIssue[]> issues = Utils.getIssuesByPage(user, me, curPage,
                Integer.parseInt(bundle.getString("constant.perPage")), trackerClient);

        if (checkAndHandleErrors(user, update, issues, true, true)) {
            return;
        }

        bot.execute(new EditMessageText(update.callbackQuery().message().chat().id(),
                update.callbackQuery().message().messageId(), Utils.generateTextByTickets(issues, curPage, bundle))
                .replyMarkup(Utils.generateInlineKeyboardMarkupByPage(curPage,
                        Integer.parseInt(issues.getHeadersData().getTotalPages()), bundle)).parseMode(ParseMode.HTML));
    }

    private boolean checkAndHandleCancel(User user, Update update) {
        if (checkIsCmd(update.message().text(), "cancel", bundle)) {
            updateState(user, State.MAIN_MENU);
            sendTextMessage(user, update, bundle.getString("text.cancelAction"));

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

            text = bundle.getString("text.unknownError");
        } else {
            StringBuilder errorText = new StringBuilder();

            if (response.getError() != null && response.getError().getErrorMessages() != null) {
                for (String error : response.getError().getErrorMessages()) {
                    errorText.append(error).append("\n");
                }
            }

            if (response.code() == Integer.parseInt(bundle.getString("code.401")) || response.code() == Integer
                    .parseInt(bundle.getString("code.403"))) {
                updateState(user, State.MAIN_MENU);

                text = String.format(bundle.getString("text.forbidden"), errorText.toString());
            } else {
                if (forceReturn) {
                    updateState(user, State.MAIN_MENU);
                }

                text = String.format(bundle.getString("text.error"), errorText.toString());
            }
        }

        if (isCallbackQuery) {
            bot.execute(new EditMessageText(update.callbackQuery().message().chat().id(),
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
                trackerClient.isQueueValid(user, update.message().text());

        if (checkAndHandleErrors(user, update, queue, false)) {
            return;
        }

        user.getIssue().setQueue(new Queue(update.message().text().toUpperCase()));
        trackerIssueDao.updateTrackerIssue(user.getIssue());

        updateState(user, State.NEED_SUMMARY);
        handleUpdate(update);
    }

    private void handleExpectSummary(User user, Update update) {
        if (checkAndHandleCancel(user, update)) {
            return;
        }

        user.getIssue().setSummary(update.message().text());
        trackerIssueDao.updateTrackerIssue(user.getIssue());

        updateState(user, State.NEED_DESCRIPTION);
        handleUpdate(update);
    }

    private void handleExpectDescription(User user, Update update) {
        if (checkAndHandleCancel(user, update)) {
            return;
        }

        user.getIssue().setDescription(update.message().text());
        trackerIssueDao.updateTrackerIssue(user.getIssue());

        updateState(user, State.NEED_ASSIGN_YOU);
        handleUpdate(update);
    }

    private void handleExpectIssueKey(User user, Update update, String key) {
        if (checkAndHandleCancel(user, update)) {
            return;
        }

        if (!Pattern.matches("[A-Z]+-[0-9]+", key.toUpperCase())) {
            sendTextMessage(user, update, bundle.getString("text.incorrectIssueKey"));
        } else {
            ITrackerClient.IResponse<ITrackerClient.IIssue> issue =
                    trackerClient.getIssue(user, key.toUpperCase());

            if (checkAndHandleErrors(user, update, issue, false)) {
                return;
            }

            ITrackerClient.IResponse<ITrackerClient.IComment[]> comments =
                    trackerClient.getComments(user, issue.getData());

            if (checkAndHandleErrors(user, update, comments, true)) {
                return;
            }

            updateState(user, State.MAIN_MENU);

            sendTextMessage(user, update,
                    formatIssueWithCommentsPrettyHtml(issue.getData(), comments.getData(), trackerClient.getBundle()));
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
                user.getIssue().setAssignee(trackerClient.getMe(user).getData());
                trackerIssueDao.updateTrackerIssue(user.getIssue());
                break;
            case NO:
                user.getIssue().setAssignee(null);
                trackerIssueDao.updateTrackerIssue(user.getIssue());
                break;
            case UNKNOWN:
            default:
                sendTextMessage(user, update, bundle.getString("text.incorrectAnswer"));
                return;
        }

        ITrackerClient.IResponse<ITrackerClient.IIssue> issue =
                trackerClient.createIssue(user, user.getIssue());

        if (checkAndHandleErrors(user, update, issue, true)) {
            return;
        }

        updateState(user, State.MAIN_MENU);

        sendTextMessage(user, update,
                String.format(bundle.getString("text.createIssueSuccess"),
                        issue.getData().getSummary(), issue.getData().getKey(),
                        generateIssueLink(issue.getData(), bundle)));
    }

    private void handleExpectAccessToken(User user, Update update) {
        if (checkAndHandleCancel(user, update)) {
            return;
        }

        if (Utils.checkHeader(update.message().text())
                && trackerClient.isTokenValid(user, update.message().text()).isSuccessful()) {

            user.setTrackerAccessToken(update.message().text());
            updateState(user, State.MAIN_MENU);

            sendTextMessage(user, update,
                    bundle.getString("text.authSuccess"));
        } else {
            String text = bundle.getString("text.authError")
                    + "\n"
                    + String.format(bundle.getString("text.needAccessToken"),
                    trackerClient.getAuthLink());
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
            String text = bundle.getString("text.parseOrgIdError")
                    + "\n"
                    + bundle.getString("text.needOrgId");
            sendTextMessage(user, update, text);
            return;
        }

        StringBuilder text = new StringBuilder(bundle.getString("text.parseOrgIdSuccess"));

        user.setOrganisationId(organisationId);
        if (user.getTrackerAccessToken() == null) {
            updateState(user, State.EXPECT_ACCESS_TOKEN);
            text.append("\n").append(String.format(bundle.getString("text.needAccessToken"),
                    trackerClient.getAuthLink()));
        } else {
            updateState(user, State.MAIN_MENU);
        }

        sendTextMessage(user, update, text.toString());
    }

    private void updateState(User user, State state) {
        user.setState(state);
        userDao.updateUser(user);
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
