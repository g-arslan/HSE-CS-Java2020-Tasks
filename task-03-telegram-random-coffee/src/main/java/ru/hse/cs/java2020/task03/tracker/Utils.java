package ru.hse.cs.java2020.task03.tracker;

import java.util.ResourceBundle;

import static ru.hse.cs.java2020.task03.utils.Utils.bold;

public final class Utils {
    private Utils() {
    }

    public static String formatIssueWithCommentsPrettyHtml(ITrackerClient.IIssue issue,
            ITrackerClient.IComment[] comments, ResourceBundle bundle) {
        StringBuilder text = new StringBuilder(issue.formatPrettyHtml(bundle) + "\n\n");

        if (comments.length > 0) {
            text.append(bold(bundle.getString("text.comments"))).append(":");

            for (ITrackerClient.IComment comment : comments) {
                text.append("\n\n").append(comment.formatPrettyHtml());
            }
        } else {
            text.append(bundle.getString("text.noComments"));
        }

        return text.toString();
    }
}
