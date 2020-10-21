package com.limseong.onenotereminder.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.limseong.onenotereminder.R;
import com.limseong.onenotereminder.data.Page;
import com.limseong.onenotereminder.sections.SectionsPresenter;
import com.limseong.onenotereminder.util.AuthenticationHelper;
import com.limseong.onenotereminder.util.GraphHelper;
import com.limseong.onenotereminder.util.PreferencesUtil;
import com.microsoft.graph.concurrency.ICallback;
import com.microsoft.graph.core.ClientException;
import com.microsoft.identity.client.AuthenticationCallback;
import com.microsoft.identity.client.IAuthenticationResult;
import com.microsoft.identity.client.exception.MsalException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 8888;
    private static final String CHANNEL_ID = "channel_reminder";

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmBroadcastReceiver", "onReceive");

        mContext = context;

        // load the list of sections set to be notified
        String[] sectionIdArray = PreferencesUtil.getPreferences(mContext,
                SectionsPresenter.PREF_ENABLED_SECTION_ID_LIST, String[].class);
        List<String> sectionIdList;
        if (sectionIdArray == null)
            sectionIdList = new LinkedList<>();
        else
            sectionIdList = new LinkedList<String>(Arrays.asList(sectionIdArray));

        Log.d("sectionIdList", Arrays.toString(sectionIdList.toArray()));

        if (sectionIdList == null || sectionIdList.isEmpty())
            return;

        // pick a section to be notified
        String pickedSectionId = sectionIdList.get((new Random()).nextInt(sectionIdList.size()));

        // retrieve pages of the section from MS Graph
        // callback func will send a notification
        AuthenticationHelper.getInstance()
                .acquireTokenSilently(new AuthenticationCallback() {
                    @Override
                    public void onSuccess(IAuthenticationResult authenticationResult) {
                        GraphHelper.getInstance().getPagesInSection(authenticationResult.getAccessToken(),
                                pickedSectionId, getPagesInSectionCallback());
                    }

                    @Override
                    public void onError(MsalException exception) {
                        Log.e("AUTH", "Could not get token silently", exception);
                    }

                    @Override
                    public void onCancel() {}
                });

    }

    // callback when retrieving pages from MS Graph finished
    private ICallback<JsonObject> getPagesInSectionCallback() {
        return new ICallback<JsonObject>() {
            @Override
            public void success(JsonObject pageCollection) {
                JsonArray pageArray = pageCollection.getAsJsonArray("value");

                // pick random page
                int randIdx = new Random().nextInt(pageArray.size());
                JsonObject pickedPage = pageArray.get(randIdx).getAsJsonObject();

                // extract info and make an object
                String id = pickedPage.get("id").getAsString();
                String title = pickedPage.get("title").getAsString();
                String sectionId = pickedPage.get("parentSection").getAsJsonObject()
                        .get("id").getAsString();
                String sectionTitle = pickedPage.get("parentSection").getAsJsonObject()
                        .get("displayName").getAsString();
                String clientUrl = pickedPage.get("links").getAsJsonObject()
                        .get("oneNoteClientUrl").getAsJsonObject()
                        .get("href").getAsString();
                Page page = new Page(id, title, sectionId, sectionTitle, clientUrl);

                // notify
                notifyPage(mContext, page);
            }

            @Override
            public void failure(ClientException e) {
                Log.e(this.getClass().getName(), "Error getting events", e);
            }
        };
    }

    /**
     * Sends a push notification with the given Onenote Page info
     */
    private void notifyPage(Context context, Page page) {
        // set intent to the Onenote application
        Uri onenoteUri = Uri.parse(page.getClientUrl());
        Intent intent = new Intent(Intent.ACTION_VIEW, onenoteUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // set notification manager and builder
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        // >= SDK 26
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder.setSmallIcon(R.drawable.ic_style_onenote_24dp);

            String channelName ="OneNote Reminder";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName,
                    NotificationManager.IMPORTANCE_HIGH);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        else {
            builder.setSmallIcon(R.mipmap.ic_launcher);
        }

        builder.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(page.getSectionTitle())
                .setContentText(page.getTitle())
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(context, R.color.colorOneNote))
                //.setTicker("")
                //.setContentInfo("")
                ;

        //TODO: addAction snooze

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
