package com.limseong.onenotereminder.sections;

import androidx.annotation.NonNull;

import com.limseong.onenotereminder.BaseView;
import com.limseong.onenotereminder.BasePresenter;
import com.limseong.onenotereminder.data.SectionNotification;
import com.microsoft.graph.models.extensions.OnenoteSection;

import java.util.List;

public interface SectionsContract {

    interface View extends BaseView<Presenter> {

        void showSectionsList(List<OnenoteSection> list, SectionNotification notification);

        void showRefreshError();

        void showProgressBar();

        void hideProgressBar();

    }

    interface Presenter extends BasePresenter {

        void refreshSections();

        SectionNotification toggleSectionNotification(@NonNull android.view.View view,
                                       @NonNull OnenoteSection section,
                                       @NonNull boolean notificationState);

    }
}
