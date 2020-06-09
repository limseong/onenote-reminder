package com.limseong.onenotereminder.sections;

import android.util.Log;
import com.limseong.onenotereminder.util.AuthenticationHelper;
import com.limseong.onenotereminder.util.GraphHelper;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.requests.extensions.IOnenoteSectionCollectionPage;
import com.microsoft.graph.models.extensions.OnenoteSection;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.exception.MsalException;

import androidx.annotation.NonNull;

import java.util.List;

public class SectionsPresenter implements SectionsContract.Presenter {

    @NonNull
    private final SectionsContract.View mSectionsView;

    private List<OnenoteSection> mSectionList;

    public SectionsPresenter(@NonNull SectionsContract.View view) {
        //mSectionsView = checkNotNull(view);
        mSectionsView = view;
        mSectionsView.setPresenter(this);
    }

    @Override
    public void start() {
        Log.d("SectionsPresenter", "start() called");

        // Get a current access token
        AuthenticationHelper.getInstance()
                .acquireTokenSilently(new AuthenticationCallback() {
                    @Override
                    public void onSuccess(IAuthenticationResult authenticationResult) {
                        final GraphHelper graphHelper = GraphHelper.getInstance();

                        graphHelper.getSections(authenticationResult.getAccessToken(),
                                getNotebooksCallback());

                        Log.d("SectionsPresenter", "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
                    }

                    @Override
                    public void onError(MsalException exception) {
                        Log.e("AUTH", "Could not get token silently", exception);
                    }

                    @Override
                    public void onCancel() {}
                });
    }

    @Override
    public void toggleSectionNotification(@NonNull int id) {
        Log.d("SectionsPresenter", "toggle() called");
    }

    private ICallback<IOnenoteSectionCollectionPage> getNotebooksCallback() {
        return new ICallback<IOnenoteSectionCollectionPage>() {
            @Override
            public void success(IOnenoteSectionCollectionPage sectionCollectionPage) {
                mSectionList = sectionCollectionPage.getCurrentPage();
                for (OnenoteSection sec : mSectionList)
                    Log.d("TTTTTTT", sec.displayName);
                mSectionsView.showSectionsList(mSectionList);
            }

            @Override
            public void failure(ClientException ex) {
                Log.e("GRAPH", "Error getting events", ex);
            }
        };
    }
}
