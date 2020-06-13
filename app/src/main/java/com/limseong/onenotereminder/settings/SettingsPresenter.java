package com.limseong.onenotereminder.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.google.gson.Gson;
import com.limseong.onenotereminder.data.NotificationSettings;
import com.limseong.onenotereminder.notification.NotificationReceiver;
import com.limseong.onenotereminder.util.FileUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;

public class SettingsPresenter implements SettingsContract.Presenter {
    public static String FILE_NOTIFICATION_SETTINGS = "notification_settings.json";

    @NonNull
    private final SettingsContract.View mSettingsView;

    private Context mContext;

    public SettingsPresenter(@NonNull SettingsContract.View view, Context ctx) {
        //mSectionsView = checkNotNull(view);
        mSettingsView = view;
        mSettingsView.setPresenter(this);

        mContext = ctx;
    }

    @Override
    public void start() {
        //TODO: load notification time and show on listview
    }

    @Override
    public void addNotificationTime(Calendar time) {
        // add and save the new notification time
        Gson gson = new Gson(); // use default Gson
        NotificationSettings notificationSettings = FileUtils.loadFileGson(mContext,
                SettingsPresenter.FILE_NOTIFICATION_SETTINGS, NotificationSettings.class, gson);

        List<Calendar> timeList = notificationSettings.getTimeList();
        timeList.add(time);
        try {
            FileUtils.saveFileGson(mContext, FILE_NOTIFICATION_SETTINGS, notificationSettings, gson);
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Error handling
            return;
        }

        // then add the notification time to the alarm manager
        PackageManager pm = mContext.getPackageManager();
        Intent notificationIntent = new Intent(mContext, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, notificationIntent, 0);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
    }
}
