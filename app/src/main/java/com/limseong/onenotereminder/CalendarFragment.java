// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.limseong.onenotereminder;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.JsonObject;
import com.limseong.onenotereminder.util.AuthenticationHelper;
import com.limseong.onenotereminder.util.GraphHelper;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.models.extensions.Event;
import com.microsoft.graph.models.extensions.Notebook;
import com.microsoft.graph.models.extensions.OnenotePage;
import com.microsoft.graph.requests.extensions.IEventCollectionPage;
import com.microsoft.graph.requests.extensions.INotebookCollectionPage;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.exception.MsalException;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CalendarFragment extends Fragment {

    private List<Event> mEventList = null;
    private List<Notebook> mNotebookList = null;
    private List<OnenotePage> mOnenotePageList = null;

    // <OnCreateViewSnippet>
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        showProgressBar();

        // Get a current access token
        AuthenticationHelper.getInstance()
                .acquireTokenSilently(new AuthenticationCallback() {
                    @Override
                    public void onSuccess(IAuthenticationResult authenticationResult) {
                        final GraphHelper graphHelper = GraphHelper.getInstance();

                        // Get the user's events
                        //graphHelper.getEvents(authenticationResult.getAccessToken(),
                        //        getEventsCallback());

                        // NOTEBOOK
                        //graphHelper.getNotebooks(authenticationResult.getAccessToken(),
                        //        getNotebooksCallback());

                        // ONENOTEPAGE
                        graphHelper.getOnenotePages(authenticationResult.getAccessToken(),
                                getOnenotePagesCallback());
                    }

                    @Override
                    public void onError(MsalException exception) {
                        Log.e("AUTH", "Could not get token silently", exception);
                        hideProgressBar();
                    }

                    @Override
                    public void onCancel() {
                        hideProgressBar();
                    }
                });

        return view;
    }
    // </OnCreateViewSnippet>

    // <ProgressBarSnippet>
    private void showProgressBar() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().findViewById(R.id.progressbar)
                    .setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.fragment_container)
                    .setVisibility(View.GONE);
            }
        });
    }

    private void hideProgressBar() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().findViewById(R.id.progressbar)
                    .setVisibility(View.GONE);
                getActivity().findViewById(R.id.fragment_container)
                    .setVisibility(View.VISIBLE);
            }
        });
    }
    // </ProgressBarSnippet>

    // <AddEventsToListSnippet>
    private void addEventsToList() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListView eventListView = getView().findViewById(R.id.eventlist);

                EventListAdapter listAdapter = new EventListAdapter(getActivity(),
                        R.layout.event_list_item, mEventList);

                eventListView.setAdapter(listAdapter);
            }
        });
    }
    // </AddEventsToListSnippet>

    private ICallback<IEventCollectionPage> getEventsCallback() {
        return new ICallback<IEventCollectionPage>() {
            // <SuccessSnippet>
            @Override
            public void success(IEventCollectionPage iEventCollectionPage) {
                mEventList = iEventCollectionPage.getCurrentPage();

                addEventsToList();
                hideProgressBar();
            }
            // </SuccessSnippet>

            @Override
            public void failure(ClientException ex) {
                Log.e("GRAPH", "Error getting events", ex);
                hideProgressBar();
            }
        };
    }










    private void addNotebooksToList() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListView eventListView = getView().findViewById(R.id.eventlist);

                NotebookListAdapter listAdapter = new NotebookListAdapter(getActivity(),
                        R.layout.event_list_item, mNotebookList);

                eventListView.setAdapter(listAdapter);
            }
        });
    }

    private ICallback<INotebookCollectionPage> getNotebooksCallback() {
        return new ICallback<INotebookCollectionPage>() {
            // <SuccessSnippet>
            @Override
            public void success(INotebookCollectionPage iNotebookCollectionPage) {
                mNotebookList = iNotebookCollectionPage.getCurrentPage();

                //addEventsToList();

                addNotebooksToList();
                hideProgressBar();
            }
            // </SuccessSnippet>

            @Override
            public void failure(ClientException ex) {
                Log.e("GRAPH", "Error getting events", ex);
                hideProgressBar();
            }
        };
    }






    private void addOnenotePagesToList() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ListView eventListView = getView().findViewById(R.id.eventlist);

                OnenotePageListAdapter listAdapter = new OnenotePageListAdapter(getActivity(),
                        R.layout.event_list_item, mOnenotePageList);

                Log.d("ONENOTE", "TTTTTTTTTT " + mOnenotePageList.size());
                eventListView.setAdapter(listAdapter);
            }
        });
    }

    /*private ICallback<IOnenotePageCollectionPage> getOnenotePagesCallback() {
        return new ICallback<IOnenotePageCollectionPage>() {
            // <SuccessSnippet>
            @Override
            public void success(IOnenotePageCollectionPage iOnenotePageCollectionPage) {
                mOnenotePageList = iOnenotePageCollectionPage.getCurrentPage();

                        addOnenotePagesToList();
                hideProgressBar();
            }
            // </SuccessSnippet>

            @Override
            public void failure(ClientException ex) {
                Log.e("GRAPH", "Error getting events", ex);
                hideProgressBar();
            }
        };
    }*/

    private ICallback<JsonObject> getOnenotePagesCallback() {
        return new ICallback<JsonObject>() {
            // <SuccessSnippet>
            @Override
            //public void success(IOnenotePageCollectionPage iOnenotePageCollectionPage) {
            public void success(JsonObject iOnenotePageCollectionPage) {

                //mOnenotePageList = iOnenotePageCollectionPage.getCurrentPage();
                Log.d("TEST", iOnenotePageCollectionPage.toString());

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListView eventListView = getView().findViewById(R.id.eventlist);

                        OnenotePageListAdapter listAdapter = new OnenotePageListAdapter(getActivity(),
                                R.layout.event_list_item, mOnenotePageList);

                        Log.d("ONENOTE", "TTTTTTTTTT " + mOnenotePageList.size());
                        eventListView.setAdapter(listAdapter);
                    }
                });
                hideProgressBar();
            }
            // </SuccessSnippet>

            @Override
            public void failure(ClientException ex) {
                Log.e("GRAPH", "Error getting events", ex);
                hideProgressBar();
            }
        };
    }
}
