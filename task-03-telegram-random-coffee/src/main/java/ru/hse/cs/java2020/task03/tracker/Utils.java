package ru.hse.cs.java2020.task03.tracker;

import ru.hse.cs.java2020.task03.utils.Factory;

import static ru.hse.cs.java2020.task03.utils.Utils.bold;

public class Utils {
    public static String formatIssueWithCommentsPrettyHtml(ITrackerClient.IIssue issue, ITrackerClient.IComment[] comments) {
        StringBuilder text = new StringBuilder(issue.formatPrettyHtml() + "\n\n");

        if (comments.length > 0) {
            text.append(bold(Factory.getTrackerBundle().getString("text.comments"))).append(":");

            for (ITrackerClient.IComment comment : comments) {
                text.append("\n\n").append(comment.formatPrettyHtml());
            }
        } else {
            text.append(Factory.getTrackerBundle().getString("text.noComments"));
        }

        return text.toString();
    }
}
