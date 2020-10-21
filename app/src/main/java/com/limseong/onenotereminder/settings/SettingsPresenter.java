package com.limseong.onenotereminder.settings;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.limseong.onenotereminder.data.NotificationSetting;
import com.limseong.onenotereminder.notification.NotificationReceiver;
import com.limseong.onenotereminder.util.PreferencesUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import androidx.annotation.NonNull;

public class SettingsPresenter implements SettingsContract.Presenter {
    public static String PREF_NOTIFICATION_SETTING_LIST = "notification_setting_list";

    @NonNull
    private final SettingsContract.View mSettingsView;

    private Context mContext;
    private List<NotificationSetting> mNotificationSettingList;

    public SettingsPresenter(@NonNull SettingsContract.View view, Context ctx) {
        //mSectionsView = checkNotNull(view);
        mSettingsView = view;
        mSettingsView.setPresenter(this);

        mContext = ctx;
    }

    @Override
    public void start() {
        mNotificationSettingList = loadNotificationSettingList();

        // show notification settings with loaded data
        mSettingsView.showNotificationSettingList(mNotificationSettingList);
    }

    @Override
    public void addNotificationTime(NotificationSetting notificationSetting) {
        // add and save the new notification setting
        mNotificationSettingList.add(notificationSetting);
        mSettingsView.showNotificationSettingList(mNotificationSettingList);
        PreferencesUtil.setPreferences(mContext, PREF_NOTIFICATION_SETTING_LIST, mNotificationSettingList);

        // then add the notification time to the alarm manager
        PackageManager pm = mContext.getPackageManager();
        Intent notificationIntent = new Intent(mContext, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, notificationIntent, 0);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, notificationSetting.getHour());
        time.set(Calendar.MINUTE, notificationSetting.getMinute());
        time.set(Calendar.SECOND, 0);

        //TODO: change alarm setting
        /*alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                //AlarmManager.INTERVAL_DAY, pendingIntent);
                1000*3, pendingIntent);*/

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(),
                //AlarmManager.INTERVAL_DAY, pendingIntent);
                1000*3, pendingIntent);

        //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), pendingIntent);
    }

    private List<NotificationSetting> loadNotificationSettingList() {
        NotificationSetting[] notificationSettingArray = PreferencesUtil.getPreferences(mContext,
                PREF_NOTIFICATION_SETTING_LIST, NotificationSetting[].class);
        if (notificationSettingArray == null)
            return new LinkedList<>();
        else
            return new LinkedList<>(Arrays.asList(notificationSettingArray));
    }
}
