// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

// <HomeSnippet>
package com.limseong.onenotereminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.limseong.onenotereminder.sections.SectionsPresenter;
import com.limseong.onenotereminder.settings.SettingsPresenter;
import com.limseong.onenotereminder.util.FileUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {
    private static final String USER_NAME = "userName";

    private String mUserName;

    public HomeFragment() {

    }

    public static HomeFragment createInstance(String userName) {
        HomeFragment fragment = new HomeFragment();

        // Add the provided username to the fragment's arguments
        Bundle args = new Bundle();
        args.putString(USER_NAME, userName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserName = getArguments().getString(USER_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View homeView = inflater.inflate(R.layout.fragment_home, container, false);

        // If there is a username, replace the "Please sign in" with the username
        if (mUserName != null) {
            TextView userName = homeView.findViewById(R.id.home_page_username);
            userName.setText(mUserName);
        }




        ///////////////////////////////////////////////////////////////////////////////////////
        final String NOTIFICATION_CHANNEL_ID = "11111";
        final NotificationManager notificationManager =
                (NotificationManager) getActivity().getSystemService(getContext().NOTIFICATION_SERVICE);
        final Intent intent = new Intent(getContext(), MainActivity.class);


        intent.putExtra("notificationId", 99009900); //전달할 값
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) ;
//        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent,  PendingIntent.FLAG_UPDATE_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(getContext(), NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground))
                .setContentTitle("상태바 드래그시 보이는 타이틀")
                .setContentText("상태바 드래그시 보이는 서브타이틀")
                .setStyle(new NotificationCompat.BigTextStyle().bigText("더 많은 내용을 보여줘야 하는 경우..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true);

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName  = "노티페케이션 채널";
            String description = "오레오 이상을 위한 것임";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName , importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);

        }
        else
            builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert notificationManager != null;
        ////////////////////////////////////////////////////////////////////////////////////////////


        FloatingActionButton fabDebug = homeView.findViewById(R.id.fab_debug);
        fabDebug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean hasDeleted = FileUtils.deleteFile(getContext(), SectionsPresenter.FILE_SECTIONS);
                FileUtils.deleteFile(getContext(), SettingsPresenter.FILE_NOTIFICATION_SETTINGS);
                Log.d("hasDeleted", hasDeleted+"");


                // NotificationManager notifiy
                //notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작시킴


                // BroadcastReceiver
                //LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent());


                // Service
                /*IntentFilter mIntentFilter = new IntentFilter(NotificationService.ACTION_CALC_DONE);
                Intent serviceIntent = new Intent(NotificationService.ACTION_CALC);
                serviceIntent.setClass(getContext(), NotificationService.class);
                getContext().startService(serviceIntent);*/

                //https://webnautes.tistory.com/1365
                /*Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis() + 5000);
                //calendar.set(Calendar.MINUTE, System.currentTimeMillis() + ;
                //calendar.set(Calendar.SECOND, 0);
                Date currentDateTime = calendar.getTime();
                String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
                Toast.makeText(getContext(),date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();

                PackageManager pm = getContext().getPackageManager();
                //ComponentName receiver = new ComponentName(getContext(), NotificationReceiver.class);
                Intent alarmIntent = new Intent(getContext(), NotificationReceiver.class);
                Log.d("@@@@@@@@@@", getContext().getPackageName());
                PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, alarmIntent, 0);
                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY, pendingIntent);
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);*/

                Log.d("FAB", "FAB");
            }
        });

        return homeView;
    }
}
// </HomeSnippet>
