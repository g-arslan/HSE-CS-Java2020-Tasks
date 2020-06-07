package ru.hse.cs.java2020.task03.tracker.models;

import ru.hse.cs.java2020.task03.tracker.ITrackerClient;

public class FilterRequest implements ITrackerClient.IFilterRequest {
    private Filter filter;
    private String query;

    public FilterRequest(Filter filter) {
        this.filter = filter;
    }

    public ITrackerClient.IFilter getFilter() {
        return filter;
    }

    public void setFilter(ITrackerClient.IFilter filter) {
        this.filter = (Filter) filter;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
