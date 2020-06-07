package ru.hse.cs.java2020.task03.tracker.models;

import ru.hse.cs.java2020.task03.tracker.ITrackerClient;

public class HeadersData implements ITrackerClient.IHeadersData {
    private String totalPages;

    public HeadersData(String totalPages) {
        this.totalPages = totalPages;
    }

    public String getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(String totalPages) {
        this.totalPages = totalPages;
    }
}
