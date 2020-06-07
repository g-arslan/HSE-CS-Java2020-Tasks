package ru.hse.cs.java2020.task03.tracker;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ru.hse.cs.java2020.task03.tracker.models.Error;
import ru.hse.cs.java2020.task03.tracker.models.*;
import ru.hse.cs.java2020.task03.utils.Factory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;

public class TrackerClient implements ITrackerClient {
    private final String clientId;

    public TrackerClient(String clientId) {
        this.clientId = clientId;
    }

    public String getAuthLink() {
        return String.format(Factory.getTrackerBundle().getString("url.authLink"), clientId);
    }

    private <T> IResponse<T> makeApiRequest(IUser user, ArrayList<String> method, ArrayList<String> queryArgs,
            RequestBody body) {
        return makeApiRequest(user, method, queryArgs, body, user.getTrackerAccessToken());
    }

    private <T> IResponse<T> makeApiRequest(IUser user, ArrayList<String> method, ArrayList<String> queryArgs,
            RequestBody body,
            String token) {
        HttpUrl.Builder urlBuilder = new HttpUrl.Builder()
                .scheme(Factory.getTrackerBundle().getString("url.scheme"))
                .host(Factory.getTrackerBundle().getString("url.baseUrl"))
                .addPathSegment(Factory.getTrackerBundle().getString("path.version"));

        for (String path : method) {
            urlBuilder.addPathSegment(path);
        }

        if (queryArgs != null) {
            for (int i = 1; i < queryArgs.size(); i += 2) {
                urlBuilder.addQueryParameter(queryArgs.get(i - 1), queryArgs.get(i));
            }
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(urlBuilder.build())
                .addHeader(Factory.getTrackerBundle().getString("header.auth"),
                        Factory.getTrackerBundle().getString("header.authType") + " " + token);

        if (user.getOrganisationId() != null) {
            requestBuilder.addHeader(Factory.getTrackerBundle().getString("header.organisationId"),
                    Long.toString(user.getOrganisationId()));
        }

        if (body != null) {
            requestBuilder.post(body);
        }

        Request request = requestBuilder.build();

        System.out.println(request.toString());

        try (Response response = Factory.getHttpClient().newCall(request).execute()) {
            TrackerResponse<T> trackerResponse =
                    new TrackerResponse<>(response.body().string(), response.isSuccessful(), response.code());
            trackerResponse.setHeadersData(
                    new HeadersData(response.header(Factory.getTrackerBundle().getString("header.totalPages"))));

            if (!trackerResponse.isSuccessful()) {
                try {
                    trackerResponse.setError(Factory.getGson().fromJson(trackerResponse.getBody(), Error.class));
                } catch (JsonSyntaxException e) {
                    // It's okay
                }
            }

            System.out.println(trackerResponse.getBody());
            return trackerResponse;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public IResponse<Boolean> isTokenValid(IUser user, String token) {
        if (token == null) {
            IResponse<Boolean> response = new TrackerResponse<>(null, false, 0);
            response.setData(false);
            return response;
        }

        return makeApiRequest(user, new ArrayList<>(
                        Collections.singletonList(Factory.getTrackerBundle().getString("path.queues"))),
                new ArrayList<>(Arrays.asList(Factory.getTrackerBundle().getString("queryarg.perPage"), "1")),
                null, token);
    }

    public IResponse<Boolean> isQueueValid(IUser user, String queue) {
        queue = queue.toUpperCase();
        if (!Pattern.matches("[A-Z]+", queue)) {
            IResponse<Boolean> response = new TrackerResponse<>(null, false, 0);
            response.setData(false);
            return response;
        }

        return makeApiRequest(user,
                new ArrayList<>(Arrays.asList(Factory.getTrackerBundle().getString("path.queues"), queue)), null, null);
    }

    private <T, C> IResponse<T> checkResponse(IResponse<T> response, Class<C> tClass) {
        if (response == null) {
            return null;
        } else if (!response.isSuccessful()) {
            return response;
        }

        response.setData(Factory.getGson().fromJson(response.getBody(), (Type) tClass));

        return response;
    }

    public IResponse<IIssue> createIssue(IUser user, IIssue issue) {
        IResponse<IIssue> response = makeApiRequest(user,
                new ArrayList<>(Collections.singletonList(Factory.getTrackerBundle().getString("path.issues"))), null,
                RequestBody.create(Factory.getGson().toJson(issue, Issue.class).getBytes()));

        return checkResponse(response, Issue.class);
    }

    public IResponse<IPerson> getMe(IUser user) {
        IResponse<IPerson> response = makeApiRequest(user,
                new ArrayList<>(Collections.singletonList(Factory.getTrackerBundle().getString("path.myself"))), null,
                null);

        if (response == null) {
            return null;
        } else if (!response.isSuccessful()) {
            return response;
        }

        response.setData(Factory.getGson().fromJson(response.getBody(), Person.class));
        response.getData().setId(JsonParser.parseString(response.getBody()).getAsJsonObject().get("uid")
                                           .toString());  // broken handler on tracker side :(

        return response;
    }

    public IResponse<IIssue[]> filterIssues(IUser user, IFilterRequest filterRequest, int page, int perPage) {
        IResponse<IIssue[]> response = makeApiRequest(user, new ArrayList<>(
                        Arrays.asList(Factory.getTrackerBundle().getString("path.issues"),
                                Factory.getTrackerBundle().getString("path.search"))),
                new ArrayList<>(Arrays.asList(Factory.getTrackerBundle().getString("queryarg.order"),
                        Factory.getTrackerBundle().getString("constant.orderby"),
                        Factory.getTrackerBundle().getString("queryarg.perPage"),
                        Integer.toString(perPage),
                        Factory.getTrackerBundle().getString("queryarg.page"), Integer.toString(page))),
                RequestBody.create(Factory.getGson().toJson(filterRequest,
                        FilterRequest.class).getBytes()));

        return checkResponse(response, Issue[].class);
    }

    public IResponse<IIssue> getIssue(IUser user, String key) {
        IResponse<IIssue> response = makeApiRequest(user,
                new ArrayList<>(Arrays.asList(Factory.getTrackerBundle().getString("path.issues"), key)), null, null);

        return checkResponse(response, Issue.class);
    }

    public IResponse<IComment[]> getComments(IUser user, IIssue issue) {
        IResponse<IComment[]> response = makeApiRequest(user, new ArrayList<>(
                Arrays.asList(Factory.getTrackerBundle().getString("path.issues"), issue.getKey(),
                        Factory.getTrackerBundle().getString("path.comments"))), null, null);

        return checkResponse(response, Comment[].class);
    }
}
