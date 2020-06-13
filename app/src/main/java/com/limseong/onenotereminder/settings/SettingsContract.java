package com.limseong.onenotereminder.settings;

import com.limseong.onenotereminder.BasePresenter;
import com.limseong.onenotereminder.BaseView;

import java.util.Calendar;

public interface SettingsContract {

    interface View extends BaseView<Presenter> {

    }

    interface Presenter extends BasePresenter {

        void addNotificationTime(Calendar time);
    }
}
