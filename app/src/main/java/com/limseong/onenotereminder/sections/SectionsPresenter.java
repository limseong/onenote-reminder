package com.limseong.onenotereminder.sections;

import android.content.Context;
import android.util.Log;

import android.view.View;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.limseong.onenotereminder.data.SectionNotification;
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
    public static String FILE_SECTION_NOTIFICATION = "section_notification.json";

    @NonNull
    private final SectionsContract.View mSectionsView;

    private List<OnenoteSection> mSectionList;
    private SectionNotification mSectionNotification; // has list of section ids with noti on

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
            refreshSections();
        }
        else {
            mSectionList = sectionList;
        }

        // get saved section notification
        SectionNotification sn = loadSectionNotification();
        if (sn == null)
            mSectionNotification = new SectionNotification();
        else
            mSectionNotification = sn;

        // show sections with loaded data
        mSectionsView.showSectionsList(mSectionList, mSectionNotification);
        mSectionsView.hideProgressBar();
    }

    @Override
    public void refreshSections() {
        // progress bar is displayed until the refresh job is finished
        mSectionsView.showProgressBar();

        // Get a current access token
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
    public SectionNotification toggleSectionNotification(@NonNull View view,
                                          @NonNull OnenoteSection clickedSection,
                                          @NonNull boolean notificationState) {
        List<String> notificationSectionIdList = mSectionNotification.getSectionIdList();

        // remove or add the clicked section to the list
        if (notificationState) {
            notificationSectionIdList.remove(clickedSection.id);
        }
        else {
            if (notificationSectionIdList.contains(clickedSection.id))
                return mSectionNotification;
            notificationSectionIdList.add(clickedSection.id);
        }

        // save the modified data to internal storage
        try {
            saveSectionNotification(mSectionNotification);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mSectionNotification;
    }

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

                mSectionsView.showSectionsList(mSectionList, mSectionNotification);
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

    private void saveSectionNotification(SectionNotification sectionNotification)
            throws IOException {
        String json = mSectionGson.toJson(sectionNotification);
        FileUtils.saveFile(mContext, FILE_SECTION_NOTIFICATION, json);
    }

    private SectionNotification loadSectionNotification() {
        // load sections file
        SectionNotification sn = null;

        try {
            byte[] data = FileUtils.loadFile(mContext, FILE_SECTION_NOTIFICATION);
            String json = new String(data);
            sn = mSectionGson.fromJson(json, SectionNotification.class);
        }
        catch (FileNotFoundException notFound) {
            // if the file doesn't exists
            ;
        }
        catch (IOException e) {
            // something is wrong
            Log.e(this.getClass().getName(), "loadSections() failed.", e);
        }

        return sn;
    }
}
