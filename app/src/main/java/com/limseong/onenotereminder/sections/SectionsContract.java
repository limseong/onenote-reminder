package com.limseong.onenotereminder.sections;

import androidx.annotation.NonNull;

import com.limseong.onenotereminder.BaseView;
import com.limseong.onenotereminder.BasePresenter;
import com.microsoft.graph.models.extensions.OnenoteSection;

import java.util.List;

public interface SectionsContract {

    interface View extends BaseView<Presenter> {

        /**
         * Show the given data on ListView
         * @param sectionList Onenote sections retrieved from the server
         * @param enabledSectionIdList the list of section ids previously set to be noti-enabled
         */
        void showSectionsList(List<OnenoteSection> sectionList, List<String> enabledSectionIdList);

        void showProgressBar();

        void hideProgressBar();

    }

    interface Presenter extends BasePresenter {

        void refreshSections();

        List<String> toggleSectionNotification(@NonNull android.view.View view,
                                               @NonNull OnenoteSection section,
                                               @NonNull boolean notificationState);

    }
}
