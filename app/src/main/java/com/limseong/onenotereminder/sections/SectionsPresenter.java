package com.limseong.onenotereminder.sections;

import android.content.Context;
import android.util.Log;

import android.view.View;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.limseong.onenotereminder.data.NotificationSettings;
import com.limseong.onenotereminder.settings.SettingsPresenter;
import com.limseong.onenotereminder.util.AuthenticationHelper;
import com.limseong.onenotereminder.util.FileUtils;
import com.limseong.onenotereminder.util.GraphHelper;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.graph.requests.extensions.IOnenoteSectionCollectionPage;
import com.microsoft.graph.models.extensions.OnenoteSection;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.exception.MsalException;

import androidx.annotation.NonNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SectionsPresenter implements SectionsContract.Presenter {
    public static String FILE_SECTIONS = "sections.json";

    @NonNull
    private final SectionsContract.View mSectionsView;

    private List<OnenoteSection> mSectionList;
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
        List<OnenoteSection> sectionList = loadSections();
        if (sectionList == null) {
            // if the section list has not been retrieved from MS Graph yet..
            refreshSections();
        }
        else {
            mSectionList = sectionList;
        }

        // get saved section notification
        NotificationSettings notificationSettings = FileUtils.loadFileGson(mContext,
                SettingsPresenter.FILE_NOTIFICATION_SETTINGS, NotificationSettings.class, mSectionGson);
        if (notificationSettings == null) {
            notificationSettings = new NotificationSettings();
            try {
                FileUtils.saveFileGson(mContext, SettingsPresenter.FILE_NOTIFICATION_SETTINGS,
                        notificationSettings, mSectionGson);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // show sections with loaded data
        mSectionsView.showSectionsList(mSectionList, notificationSettings);
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
    public NotificationSettings toggleSectionNotification(@NonNull View view,
                                                          @NonNull OnenoteSection clickedSection,
                                                          @NonNull boolean notificationState) {
        NotificationSettings notificationSettings = FileUtils.loadFileGson(mContext,
                SettingsPresenter.FILE_NOTIFICATION_SETTINGS, NotificationSettings.class, mSectionGson);
        List<String> notificationSectionIdList = notificationSettings.getSectionIdList();

        // remove or add the clicked section to the list
        if (notificationState) {
            notificationSectionIdList.remove(clickedSection.id);
        }
        else {
            if (notificationSectionIdList.contains(clickedSection.id))
                return notificationSettings;
            notificationSectionIdList.add(clickedSection.id);
        }

        // save the modified data to internal storage
        try {
            FileUtils.saveFileGson(mContext, SettingsPresenter.FILE_NOTIFICATION_SETTINGS,
                    notificationSettings, mSectionGson);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return notificationSettings;
    }

    // callback when retrieving OneNote sections from MS Graph finished
    private ICallback<IOnenoteSectionCollectionPage> getSectionsCallback() {
        return new ICallback<IOnenoteSectionCollectionPage>() {
            @Override
            public void success(IOnenoteSectionCollectionPage sectionCollectionPage) {
                mSectionList = sectionCollectionPage.getCurrentPage();

                try {
                    saveSections(mSectionList);
                }
                catch (IOException e) {
                    Log.e(this.getClass().getName(), "saveSections() failed.", e);
                    mSectionsView.showRefreshError();
                }

                NotificationSettings notificationSettings = FileUtils.loadFileGson(mContext,
                        SettingsPresenter.FILE_NOTIFICATION_SETTINGS, NotificationSettings.class, mSectionGson);
                mSectionsView.showSectionsList(mSectionList, notificationSettings);
                mSectionsView.hideProgressBar();
            }

            @Override
            public void failure(ClientException e) {
                Log.e(this.getClass().getName(), "Error getting events", e);
            }
        };
    }

    private void saveSections(List<OnenoteSection> sectionList) throws IOException {
        String json = mSectionGson.toJson(sectionList);
        FileUtils.saveFile(mContext, FILE_SECTIONS, json);
    }

    private List<OnenoteSection> loadSections() {
        List<OnenoteSection> list = null;
        // load sections file
        try {
            byte[] data = FileUtils.loadFile(mContext, FILE_SECTIONS);
            String json = new String(data);
            OnenoteSection[] arr = mSectionGson.fromJson(json, OnenoteSection[].class);
            list = Arrays.asList(arr);
        }
        catch (FileNotFoundException notFound) {
            // if the file doesn't exists
            ;
        }
        catch (IOException e) {
            // something is wrong
            Log.e(this.getClass().getName(), "loadSections() failed.", e);
        }

        return list;
    }
}
