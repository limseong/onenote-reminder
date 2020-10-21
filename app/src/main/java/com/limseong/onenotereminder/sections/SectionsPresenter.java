package com.limseong.onenotereminder.sections;

import android.content.Context;
import android.util.Log;

import android.view.View;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.limseong.onenotereminder.util.AuthenticationHelper;
import com.limseong.onenotereminder.util.GraphHelper;
import com.limseong.onenotereminder.util.PreferencesUtil;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.requests.extensions.IOnenoteSectionCollectionPage;
import com.microsoft.graph.models.extensions.OnenoteSection;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.exception.MsalException;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class SectionsPresenter implements SectionsContract.Presenter {
    public static String PREF_SECTION_LIST = "section_list";
    public static String PREF_ENABLED_SECTION_ID_LIST = "enabled_section_id_list";

    @NonNull
    private final SectionsContract.View mSectionsView;

    private List<OnenoteSection> mSectionList;
    private List<String> mEnabledSectionIdList;
    private Gson mSectionGson;
    private Context mContext;

    public SectionsPresenter(@NonNull SectionsContract.View view, Context ctx) {
        //mSectionsView = checkNotNull(view);
        mSectionsView = view;
        mSectionsView.setPresenter(this);

        mContext = ctx;

        // build a custom Gson with exclusion strategy
        // https://github.com/microsoftgraph/msgraph-sdk-java/blob/dev/src/main/java/com/microsoft/graph/models/extensions/OnenoteSection.java
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                // skip unnecessary fields
                return f.getName().equals("rawObject") || f.getName().equals("serializer");
            }

            @Override
            public boolean shouldSkipClass(Class<?> c) {
                return false; //c == JsonObject.class;
            }
        });
        mSectionGson = gsonBuilder.create();
    }

    @Override
    public void start() {
        // progress bar is displayed until the job is finished
        mSectionsView.showProgressBar();

        // get saved sections first in internal storage
        List<OnenoteSection> sectionList = loadSectionList();
        if (sectionList == null) {
            // if the section list has not been retrieved from MS Graph yet..
            refreshSections();
        }
        else {
            mSectionList = sectionList;
        }

        mEnabledSectionIdList = loadEnabledSectionIdList();

        // show sections with loaded data
        mSectionsView.showSectionsList(mSectionList, mEnabledSectionIdList);
        mSectionsView.hideProgressBar();
    }

    @Override
    public void refreshSections() {
        // progress bar is displayed until the refresh job is finished
        mSectionsView.showProgressBar();

        // Retrieve OneNote sections & pages in the sections.
        AuthenticationHelper.getInstance()
                .acquireTokenSilently(new AuthenticationCallback() {
                    @Override
                    public void onSuccess(IAuthenticationResult authenticationResult) {
                        final GraphHelper graphHelper = GraphHelper.getInstance();

                        // retrieved data is handled by the callback func
                        graphHelper.getSections(authenticationResult.getAccessToken(),
                                getSectionsCallback());
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
    public List<String> toggleSectionNotification(@NonNull View view,
                                                          @NonNull OnenoteSection clickedSection,
                                                          @NonNull boolean notificationState) {
        // toggle and save to the pref
        if (notificationState) {
            mEnabledSectionIdList.remove(clickedSection.id);
        }
        else {
            if (mEnabledSectionIdList.contains(clickedSection.id))
                return mEnabledSectionIdList;
            mEnabledSectionIdList.add(clickedSection.id);
        }
        PreferencesUtil.setPreferences(mContext, PREF_ENABLED_SECTION_ID_LIST, mEnabledSectionIdList);
        return mEnabledSectionIdList;
    }

    // callback when retrieving OneNote sections from MS Graph finished
    private ICallback<IOnenoteSectionCollectionPage> getSectionsCallback() {
        return new ICallback<IOnenoteSectionCollectionPage>() {
            @Override
            public void success(IOnenoteSectionCollectionPage sectionCollectionPage) {
                mSectionList = sectionCollectionPage.getCurrentPage();

                // save and refresh
                PreferencesUtil.setPreferencesGson(mContext, PREF_SECTION_LIST, mSectionList, mSectionGson);
                mSectionsView.showSectionsList(mSectionList, mEnabledSectionIdList);
                mSectionsView.hideProgressBar();
            }

            @Override
            public void failure(ClientException e) {
                Log.e(this.getClass().getName(), "Error getting events", e);
            }
        };
    }

    /**
     * Returns the saved list of Onenote sections
     */
    private List<OnenoteSection> loadSectionList() {
        OnenoteSection[] sectionArray = PreferencesUtil.getPreferencesGson(mContext,
                PREF_SECTION_LIST, OnenoteSection[].class, mSectionGson);
        return sectionArray == null ? null : new ArrayList<>(Arrays.asList(sectionArray));
    }

    /**
     * Returns the saved list of  notification-enabled Onenote section ids
     */
    private List<String> loadEnabledSectionIdList() {
        String[] sectionIdArray = PreferencesUtil.getPreferences(mContext,
                PREF_ENABLED_SECTION_ID_LIST, String[].class);
        if (sectionIdArray == null)
            return new LinkedList<>();
        else
            return new LinkedList<>(Arrays.asList(sectionIdArray));
    }
}
