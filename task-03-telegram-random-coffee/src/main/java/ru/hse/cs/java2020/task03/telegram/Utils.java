package ru.hse.cs.java2020.task03.telegram;

import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import ru.hse.cs.java2020.task03.models.User;
import ru.hse.cs.java2020.task03.tracker.ITrackerClient;
import ru.hse.cs.java2020.task03.tracker.models.Filter;
import ru.hse.cs.java2020.task03.tracker.models.FilterRequest;
import ru.hse.cs.java2020.task03.tracker.models.Person;
import ru.hse.cs.java2020.task03.utils.Factory;

import static ru.hse.cs.java2020.task03.utils.Utils.bold;

public class Utils {
    static String generateTextByTickets(ITrackerClient.IResponse<ITrackerClient.IIssue[]> issues, int curPage) {
        StringBuilder text = new StringBuilder();
        int totalPages = Integer.parseInt(issues.getHeadersData().getTotalPages());

        for (ITrackerClient.IIssue issue : issues.getData()) {
            text.append(issue.getKey()).append(": ").append(bold(issue.getSummary())).append("\n")
                .append(Factory.getTelegramBundle()
                               .getString("text.moreInfo")).append(" ").append(generateIssueLink(issue)).append("\n\n");
        }

        if (totalPages > 1) {
            text.append(String.format(Factory.getTelegramBundle().getString("text.pageXFromY"), curPage, totalPages));
        }

        return text.toString();
    }

    static String generateIssueLink(ITrackerClient.IIssue issue) {
        return Factory.getTelegramBundle().getString("command.get_issue") + "_" + issue.getKey().replace('-', '_');
    }

    static InlineKeyboardMarkup generateInlineKeyboardMarkupByPage(int curPage, int totalPages) {
        InlineKeyboardMarkup inlineKeyboardMarkup = null;

        if (totalPages > 1) {
            if (curPage == 1) {
                inlineKeyboardMarkup = new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                        new InlineKeyboardButton(Factory.getTelegramBundle().getString("text.nextPage")).callbackData(
                                Integer.toString(curPage + 1))
                });
            } else if (curPage == totalPages) {
                inlineKeyboardMarkup = new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                        new InlineKeyboardButton(
                                Factory.getTelegramBundle().getString("text.previousPage")).callbackData(
                                Integer.toString(curPage - 1))
                });
            } else {
                inlineKeyboardMarkup = new InlineKeyboardMarkup(new InlineKeyboardButton[]{
                        new InlineKeyboardButton(
                                Factory.getTelegramBundle().getString("text.previousPage")).callbackData(
                                Integer.toString(curPage - 1)),
                        new InlineKeyboardButton(Factory.getTelegramBundle().getString("text.nextPage")).callbackData(
                                Integer.toString(curPage + 1))
                });
            }
        }

        return inlineKeyboardMarkup;
    }

    static ITrackerClient.IResponse<ITrackerClient.IIssue[]> getIssuesByPage(User user,
            ITrackerClient.IResponse<ITrackerClient.IPerson> me, int page, int perPage) {
        me.getData().setDisplay(null);

        return Factory.getTrackerClient().filterIssues(user, new FilterRequest(new Filter(
                (Person) me.getData())), page, perPage);
    }

    static boolean checkHeader(String header) {
        for (int i = 0; i < header.length(); ++i) {
            char c = header.charAt(i);
            if (c != '\t' && (c < '\u0020' || c > '\u007e')) {
                return false;
            }
        }

        return true;
    }

    static boolean checkIsCmd(String text, String cmd) {
        return text.toLowerCase().startsWith(Factory.getTelegramBundle().getString("command." + cmd).toLowerCase())
                || text.toLowerCase().equals(Factory.getTelegramBundle().getString("button." + cmd).toLowerCase());
    }
}
