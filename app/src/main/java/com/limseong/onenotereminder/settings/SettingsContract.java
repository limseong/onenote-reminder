package com.limseong.onenotereminder.settings;

import com.limseong.onenotereminder.BasePresenter;
import com.limseong.onenotereminder.BaseView;
import com.limseong.onenotereminder.data.NotificationSetting;

import java.util.List;

public interface SettingsContract {

    interface View extends BaseView<Presenter> {
        void showNotificationSettingList(List<NotificationSetting> notificationSettingList);
    }

    interface Presenter extends BasePresenter {
        /**
         * Add the given notification setting to the list and refresh RecyclerView.
         * Then let the alarm manager know the time
         */
        void addNotificationTime(NotificationSetting notificationSetting);
    }
}
