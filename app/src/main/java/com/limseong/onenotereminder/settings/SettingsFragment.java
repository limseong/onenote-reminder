package com.limseong.onenotereminder.settings;

import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.limseong.onenotereminder.R;
import com.limseong.onenotereminder.data.NotificationSetting;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsFragment extends Fragment implements SettingsContract.View,
        TimePickerDialog.OnTimeSetListener {

    private SettingsContract.Presenter mPresenter;
    private SettingsListener mSettingsListener;
    private NotificationAdapter mNotificationAdapter;
    private RecyclerView mNotificationRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.settings_frag, container, false);

        //TODO: init listview and adapter
        mNotificationRecyclerView = (RecyclerView) root.findViewById(R.id.recyclerview_notification);

        // set layout manager and add a decoration
        mLinearLayoutManager = new LinearLayoutManager(container.getContext());
        mNotificationRecyclerView.setLayoutManager(mLinearLayoutManager); // to retrieve orientation
        DividerItemDecoration dividerItemDecoration =
                new DividerItemDecoration(mNotificationRecyclerView.getContext(),
                        mLinearLayoutManager.getOrientation());
        mNotificationRecyclerView.addItemDecoration(dividerItemDecoration);

        // defines listener actions
        mSettingsListener = new SettingsListener() {
            @Override
            public void onAddNotificationClick(View view) {
                Calendar now = Calendar.getInstance();
                TimePickerDialog tpd = TimePickerDialog.newInstance(
                        SettingsFragment.this,
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE),
                        false
                );
                tpd.setThemeDark(false);
                tpd.setVersion(true ? TimePickerDialog.Version.VERSION_2 : TimePickerDialog.Version.VERSION_1);
                tpd.setAccentColor(ContextCompat.getColor(getContext(), R.color.colorOneNote));
                tpd.setOnCancelListener(dialogInterface -> { /*cancel action*/ });
                tpd.show(requireFragmentManager(), "Timepickerdialog");
            }

            @Override
            public void onNotificationTimeSet(TimePickerDialog view, int hourOfDay, int minute,
                                              int second) {
                NotificationSetting ns = new NotificationSetting(hourOfDay, minute, true);
                mPresenter.addNotificationTime(ns);
            }

            @Override
            public void onNotificationDisableTimeSet(View view) {
//                EditText disableTimeStartEditText =
//                        (EditText)getActivity().findViewById(R.id.disable_time_start);
//                EditText disableTimeEndEditText =
//                        (EditText)getActivity().findViewById(R.id.disable_time_end);
//
//                String disableTimeStartVal = disableTimeStartEditText.getText().toString();
//                String disableTimeEndVal = disableTimeEndEditText.getText().toString();
//
//                if (disableTimeStartVal == null || disableTimeEndVal == null
//                        || disableTimeStartVal.equals("") || disableTimeEndVal.equals("")) {
//                    Toast.makeText(getContext(),"Invalid notification disable time range", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                int disableTimeStart = Integer.parseInt(disableTimeStartVal);
//                int disableTimeEnd = Integer.parseInt(disableTimeEndVal);
            }

        };

        // set notification time add button listener
        Button buttonAddNotification = root.findViewById(R.id.button_add_notification);
        buttonAddNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSettingsListener.onAddNotificationClick(view);
            }
        });

        // set disable time set button listener
//        Button buttonDisableTimeSet = root.findViewById(R.id.button_disable_time_set);
//        buttonDisableTimeSet.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mSettingsListener.onNotificationDisableTimeSet(view);
//            }
//        });

        return root;
    }

    @Override
    public void setPresenter(SettingsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onResume() {
        super.onResume();

        mPresenter.start();
    }

    @Override
    // TimePickerDialog.OnTimeSetListener
    public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
        mSettingsListener.onNotificationTimeSet(view, hourOfDay, minute, second);
    }

    // MVP, adapter is placed in View
    private static class NotificationAdapter
            extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

        private List<NotificationSetting> mNotificationSettingList;

        private static class NotificationViewHolder extends RecyclerView.ViewHolder{
            TextView time;
            Switch enabled;

            public NotificationViewHolder(View view) {
                super(view);
                this.time = (TextView) view.findViewById(R.id.notification_time);
                this.enabled = (Switch) view.findViewById(R.id.notification_switch);
            }

        }

        public NotificationAdapter(List<NotificationSetting> list) {
            this.mNotificationSettingList = list;
        }

        @Override
        public NotificationViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.notification_item, viewGroup, false);
            NotificationViewHolder viewHolder = new NotificationViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(NotificationViewHolder viewHolder, int idx) {
            viewHolder.time.setText(mNotificationSettingList.get(idx).toString());
            viewHolder.enabled.setChecked(mNotificationSettingList.get(idx).getEnabled());
        }

        @Override
        public int getItemCount() {
            if (mNotificationSettingList == null)
                return 0;
            else
                return mNotificationSettingList.size();
        }

        public void deleteAt(int position) {
            mNotificationSettingList.remove(position);
            notifyItemRemoved(position);
        }
    }

    /**
     * Custom callback for swipe to delete action
     */
    private static class NotificationSettingCallback extends ItemTouchHelper.SimpleCallback {
        private NotificationAdapter mNotificationAdapter;

        public NotificationSettingCallback(NotificationAdapter notificationAdapter) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            mNotificationAdapter = notificationAdapter;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            mNotificationAdapter.deleteAt(viewHolder.getAdapterPosition());

            //TODO: implements delete from the pref feature
        }
    }

    @Override
    public void showNotificationSettingList(List<NotificationSetting> notificationSettingList) {
        if (notificationSettingList == null)
            return;

        // sort before pass the list to the adapter
        notificationSettingList.sort(Comparator.comparing(NotificationSetting::getHour)
                .thenComparing(NotificationSetting::getMinute));

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // create ands set adapter
                mNotificationAdapter = new NotificationAdapter(notificationSettingList);
                mNotificationRecyclerView.setAdapter(mNotificationAdapter);

                // set a simple callback to add swipe to delete handling
                NotificationSettingCallback callback =
                        new NotificationSettingCallback(mNotificationAdapter);
                ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
                itemTouchHelper.attachToRecyclerView(mNotificationRecyclerView);
            }
        });
    }

    // Custom onEvent actions
    private interface SettingsListener {

        // when add notification button clicked
        void onAddNotificationClick(View view);

        // TimePickerDialog.OnTimeSetListener.onTimeSet() is redirected to here
        void onNotificationTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second);

        // disable time set button clicked
        void onNotificationDisableTimeSet(View view);
    }
}
