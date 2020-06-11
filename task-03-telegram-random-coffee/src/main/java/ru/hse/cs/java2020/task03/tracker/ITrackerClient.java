package ru.hse.cs.java2020.task03.tracker;

import java.util.ResourceBundle;

public interface ITrackerClient {
    ResourceBundle getBundle();

    String getAuthLink();

    IResponse<Boolean> isTokenValid(IUser user, String token);

    IResponse<Boolean> isQueueValid(IUser user, String queue);

    IResponse<IIssue> createIssue(IUser user, IIssue issue);

    IResponse<IPerson> getMe(IUser user);

    IResponse<IIssue[]> filterIssues(IUser user, IFilterRequest filterRequest, int page, int perPage);

    IResponse<IIssue> getIssue(IUser user, String key);

    IResponse<IComment[]> getComments(IUser user, IIssue issue);

    interface IHeadersData {
        String getTotalPages();

        void setTotalPages(String totalPages);
    }

    interface IError {
        String[] getErrorMessages();

        void setErrorMessages(String[] errorMessages);
    }

    interface IResponse<T> {
        String getBody();

        boolean isSuccessful();

        int code();

        IError getError();

        void setError(IError error);

        IHeadersData getHeadersData();

        void setHeadersData(IHeadersData headersData);

        T getData();

        void setData(T data);
    }

    interface IUser {
        Long getOrganisationId();

        String getTrackerAccessToken();
    }

    interface IFilter {
        ITrackerClient.IPerson getAssignee();

        void setAssignee(ITrackerClient.IPerson assignee);

        ITrackerClient.IQueue getQueue();

        void setQueue(ITrackerClient.IQueue queue);
    }

    interface IFilterRequest {
        ITrackerClient.IFilter getFilter();

        void setFilter(ITrackerClient.IFilter filter);

        String getQuery();

        void setQuery(String query);
    }

    interface IComment {
        IPerson getCreatedBy();

        void setCreatedBy(IPerson createdBy);

        String getText();

        void setText(String text);

        String formatPrettyHtml();
    }

    interface IPerson {
        String getId();

        void setId(String id);

        String getDisplay();

        void setDisplay(String display);
    }

    interface IQueue {
        String getId();

        void setId(String id);

        String getDisplay();

        void setDisplay(String display);

        String getKey();

        void setKey(String key);
    }

    interface IIssue {
        String getSummary();

        void setSummary(String summary);

        IQueue getQueue();

        void setQueue(IQueue queue);

        String getDescription();

        void setDescription(String description);

        IPerson getAssignee();

        void setAssignee(IPerson assignee);

        String getKey();

        void setKey(String key);

        ITrackerClient.IPerson getCreatedBy();

        void setCreatedBy(ITrackerClient.IPerson createdBy);

        ITrackerClient.IPerson[] getFollowers();

        void setFollowers(ITrackerClient.IPerson[] followers);

        String formatPrettyHtml(ResourceBundle bundle);
    }
}
