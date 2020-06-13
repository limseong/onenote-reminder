package com.limseong.onenotereminder.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.limseong.onenotereminder.R;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment implements SettingsContract.View,
        TimePickerDialog.OnTimeSetListener {

    //private ListView mNotificationTimeListView;
    //private Adapter mNotificationTimeAdapter;
    private SettingsContract.Presenter mPresenter;
    private SettingsListener mSettingsListener;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.settings_frag, container, false);

        //TODO: init listview and adapter

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
                //tpd.setTitle("Reminder Time");
                tpd.setOnCancelListener(dialogInterface -> {
                    //cancel action
                });
                tpd.show(requireFragmentManager(), "Timepickerdialog");
            }

            @Override
            public void onNotificationTimeSet(TimePickerDialog view, int hourOfDay, int minute,
                                              int second) {
                Calendar time = Calendar.getInstance();
                time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                time.set(Calendar.MINUTE, minute);
                //time.set(Calendar.SECOND, second);

                mPresenter.addNotificationTime(time);
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

    // Custom onEvent actions
    private interface SettingsListener {

        // when add notification button clicked
        void onAddNotificationClick(View view);

        // TimePickerDialog.OnTimeSetListener.onTimeSet() is redirected to here
        void onNotificationTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second);

    }
}
