package com.limseong.onenotereminder.sections;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.limseong.onenotereminder.EventListAdapter;
import com.limseong.onenotereminder.R;
import com.microsoft.graph.models.extensions.OnenoteSection;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SectionsFragment extends Fragment implements SectionsContract.View {



    private ListView mSectionListView;

    private SectionsContract.Presenter mPresenter;

    public static SectionsFragment newInstance() {
        return new SectionsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.sections_frag, container, false);
        mSectionListView = (ListView) root.findViewById(R.id.sections_list);
        return root;
    }

    @Override
    public void setPresenter(SectionsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onResume() {
        super.onResume();
        showProgressBar();
        mPresenter.start();
        hideProgressBar();
    }

    @Override
    public void showSectionsList(final List<OnenoteSection> list) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SectionsAdapter listAdapter = new SectionsAdapter(getActivity(),
                        R.layout.section_item, list);

                mSectionListView.setAdapter(listAdapter);
            }
        });
    }

    private void showProgressBar() {
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

    private void hideProgressBar() {
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
}
