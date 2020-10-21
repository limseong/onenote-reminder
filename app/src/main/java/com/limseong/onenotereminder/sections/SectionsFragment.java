package com.limseong.onenotereminder.sections;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.limseong.onenotereminder.R;
import com.microsoft.graph.models.extensions.OnenoteSection;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class SectionsFragment extends Fragment implements SectionsContract.View {

    private ListView mSectionListView;
    private SectionsContract.Presenter mPresenter;
    private SectionsListener mSectionsListener;
    private SectionsAdapter mSectionsAdapter;

    public static SectionsFragment newInstance() {
        return new SectionsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sections_frag, container, false);

        mSectionListView = (ListView) root.findViewById(R.id.sections_list);

        // defines listener actions
        mSectionsListener = new SectionsListener() {
            @Override
            public void onSectionClick(View view, OnenoteSection clickedSection, boolean notificationState) {
                List<String> enabledSectionList =
                        mPresenter.toggleSectionNotification(view, clickedSection, notificationState);
                mSectionsAdapter.updateEnabledSectionIdList(enabledSectionList);
                mSectionsAdapter.notifyDataSetChanged(); // reload ListView
            }

            @Override
            public void onRefreshClick() {
                mPresenter.refreshSections();
            }
        };

        // set refresh fab event listener
        FloatingActionButton fabRefresh = root.findViewById(R.id.fab_refresh);
        fabRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSectionsListener.onRefreshClick();
            }
        });

        return root;
    }

    @Override
    public void setPresenter(SectionsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onResume() {
        super.onResume();

        mPresenter.start();
    }

    @Override
    public void showSectionsList(final List<OnenoteSection> sectionList,
                                 final List<String> enabledSectionIdList) {
        if (sectionList == null)
            return;

        // sort before pass the list to the adapter
        Collections.sort(sectionList, (a, b) -> {
            int cmp = a.parentNotebook.displayName.compareTo(b.parentNotebook.displayName);
            if (cmp == 0) {
//                cmp = a.parentSectionGroup.displayName.compareTo(b.parentSectionGroup.displayName);
//                if (cmp == 0) {
                    cmp = a.displayName.compareTo(b.displayName);
//                }
            }
            return cmp;
        });

        getActivity().runOnUiThread(() -> {
            // create adapter
            mSectionsAdapter = new SectionsAdapter(getActivity(),
                    R.layout.section_item, sectionList, mSectionsListener, enabledSectionIdList);

            // set adapter to show items on ListView
            mSectionListView.setAdapter(mSectionsAdapter);
        });
    }

    @Override
    public void showProgressBar() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().findViewById(R.id.progressbar)
                    .setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.fragment_container)
                    .setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void hideProgressBar() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().findViewById(R.id.progressbar)
                    .setVisibility(View.GONE);
                getActivity().findViewById(R.id.fragment_container)
                    .setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Adapter for ListView in Manage Section Fragment. MVP, adapter is placed in View
     */
    private static class SectionsAdapter extends ArrayAdapter<OnenoteSection> {
        private Context mContext;
        private int mResource;
        private SectionsListener mListener;
        private List<String> mEnabledSectionIdList;

        private static class SectionViewHolder {
            TextView title;
            TextView parentSectionGroup;
            TextView parentNotebook;
        }

        public SectionsAdapter(Context context, int resource, List<OnenoteSection> sections,
                               SectionsListener listener, List<String> enabledSectionIdList) {
            super(context, resource, sections);
            mContext = context;
            mResource = resource;
            mListener = listener;
            mEnabledSectionIdList = enabledSectionIdList;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final OnenoteSection section = getItem(position);

            SectionViewHolder holder;

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(mResource, parent, false);

                holder = new SectionViewHolder();
                holder.title = convertView.findViewById(R.id.section_title);
                holder.parentSectionGroup = convertView.findViewById(R.id.section_parentSectionGroup);
                holder.parentNotebook = convertView.findViewById(R.id.section_parentNotebook);

                convertView.setTag(holder);
            } else {
                // already exists
                holder = (SectionViewHolder) convertView.getTag();
            }

            // draw ListView item
            holder.title.setText(section.displayName);
            if (section.parentNotebook != null)
                holder.parentNotebook.setText(section.parentNotebook.displayName);

            if (section.parentSectionGroup != null)
                holder.parentSectionGroup.setText(section.parentSectionGroup.displayName);
            else
                convertView.findViewById(R.id.section_parentSectionGroupLayout).setVisibility(View.GONE);

            // set background color if the item is set to be notification on
            final boolean isNotificationSet =
                    mEnabledSectionIdList.contains(section.id);
            if (isNotificationSet) {
                convertView.setBackgroundColor(
                        ContextCompat.getColor(getContext(), R.color.colorSectionNotificationOn));
            }
            else {
                convertView.setBackgroundColor(
                        ContextCompat.getColor(getContext(), R.color.colorSectionNotificationOff));
            }

            // set click listener
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onSectionClick(view, section, isNotificationSet);
                }
            });

            return convertView;
        }

        public void updateEnabledSectionIdList(List<String> enabledSectionIdList) {
            mEnabledSectionIdList = enabledSectionIdList;
        }
    }

    /**
     * Custom onEvent actions for SectionsFragment
     */
    public interface SectionsListener {

        /**
         * Handles event when each item in ListView is clicked
         */
        void onSectionClick(View view, OnenoteSection clickedSection, boolean notificationState);

        /**
         * Handles event when refresh button is clicked
         */
        void onRefreshClick();
    }
}
