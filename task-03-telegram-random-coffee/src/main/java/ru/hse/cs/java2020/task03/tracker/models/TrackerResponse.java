package ru.hse.cs.java2020.task03.tracker.models;

import ru.hse.cs.java2020.task03.tracker.ITrackerClient;

public class TrackerResponse<T> implements ITrackerClient.IResponse<T> {
    private final String body;
    private final boolean isSuccessful;
    private final int code;
    private Error error;
    private ITrackerClient.IHeadersData headersData;
    private T data;

    public TrackerResponse(String body, boolean isSuccessful, int code) {
        this.body = body;
        this.isSuccessful = isSuccessful;
        this.code = code;
    }

    public String getBody() {
        return body;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public int code() {
        return code;
    }

    public ITrackerClient.IHeadersData getHeadersData() {
        return headersData;
    }

    public void setHeadersData(ITrackerClient.IHeadersData headersData) {
        this.headersData = headersData;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public ITrackerClient.IError getError() {
        return error;
    }

    @Override
    public void setError(ITrackerClient.IError error) {
        this.error = (Error) error;
    }
}
