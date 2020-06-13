package com.limseong.onenotereminder.util;

import com.google.gson.JsonObject;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.options.Option;
import com.microsoft.graph.options.QueryOption;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.IEventCollectionPage;
import com.microsoft.graph.requests.extensions.INotebookCollectionPage;
import com.microsoft.graph.requests.extensions.IOnenoteSectionCollectionPage;

import java.util.LinkedList;
import java.util.List;

public class GraphHelper implements IAuthenticationProvider {
    private static GraphHelper INSTANCE = null;
    private IGraphServiceClient mClient = null;
    private String mAccessToken = null;

    private GraphHelper() {
        mClient = GraphServiceClient.builder()
                .authenticationProvider(this).buildClient();
    }

    public static synchronized GraphHelper getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GraphHelper();
        }

        return INSTANCE;
    }

    // Part of the Graph IAuthenticationProvider interface
    // This method is called before sending the HTTP request
    @Override
    public void authenticateRequest(IHttpRequest request) {
        // Add the access token in the Authorization header
        request.addHeader("Authorization", "Bearer " + mAccessToken);
    }

    public void getUser(String accessToken, ICallback<User> callback) {
        mAccessToken = accessToken;

        // GET /me (logged in user)
        mClient.me().buildRequest().get(callback);
    }

    public void getNotebooks(String accessToken, ICallback<INotebookCollectionPage> callback) {
        mAccessToken = accessToken;

        // Use query options to sort by created time
        final List<Option> options = new LinkedList<Option>();
        options.add(new QueryOption("orderby", "createdDateTime DESC"));

        mClient.me().onenote().notebooks().buildRequest().get(callback);
    }

    public void getPagesInSection(String accessToken, String sectionId,
                                         ICallback<JsonObject> callback) {
        mAccessToken = accessToken;

        final List<Option> options = new LinkedList<Option>();
        options.add(new QueryOption("expand", "parentSection"));
        options.add(new QueryOption("select", "id,title,links,parentSection"));
        options.add(new QueryOption("top", "100"));

        // GET /me/onenote/sections/{id}/pages
        String uri = "/me/onenote/sections/" + sectionId + "/pages";

        //CustomRequestBuilder builder = mClient.customRequest(uri);
        //CustomRequest req = builder.buildRequest();
        //req.get(callback);

        mClient.customRequest(uri).buildRequest(options).get(callback);
    }


    public void getEvents(String accessToken, ICallback<IEventCollectionPage> callback) {
        mAccessToken = accessToken;

        // Use query options to sort by created time
        final List<Option> options = new LinkedList<Option>();
        options.add(new QueryOption("orderby", "createdDateTime DESC"));

        // GET /me/events
        mClient.me().events().buildRequest(options)
                .select("subject,organizer,start,end")
                .get(callback);

    }

    public void getSections(String accessToken, ICallback<IOnenoteSectionCollectionPage> callback) {
        mAccessToken = accessToken;

        final List<Option> options = new LinkedList<Option>();
        options.add(new QueryOption("top", "100"));

        // GET /me/onenote/sections/top=100
        mClient.me().onenote().sections().buildRequest(options).get(callback);
    }
}