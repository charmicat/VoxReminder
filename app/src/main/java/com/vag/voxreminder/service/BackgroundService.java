package com.vag.voxreminder.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.vag.voxreminder.R;
import com.vag.voxreminder.db.DAO;
import com.vag.voxreminder.receiver.BootupReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Luiza Utsch on 7/3/17.
 */

public class BackgroundService extends Service {

    final private String TAG = "BackgroundService";

    final private int DAILY_IN_HOURS = 24;
    final private int WEEKLY_IN_HOURS = 168;
    final private int MONTHLY_IN_HOURS = 720; //30 days
    final private int YEARLY_IN_HOURS = 8760; //not leap

    private TextToSpeech tts;
    private boolean isOn;
    private final int sdkVersion = Build.VERSION.SDK_INT;
    private DAO m_dao;

    private TextToSpeech.OnInitListener ttsListener = new TextToSpeech.OnInitListener() {
        @SuppressWarnings("deprecation")
        @Override
        public void onInit(int status) {
            Log.d(TAG, "TTS engine started");

            isOn = (status == TextToSpeech.SUCCESS);
            Locale current = getResources().getConfiguration().locale;
            Log.i(TAG, "Current locale " + current.getDisplayName());

            tts.setLanguage(current);

            if (sdkVersion < Build.VERSION_CODES.LOLLIPOP) {
                if (tts.speak("bojangles", TextToSpeech.QUEUE_ADD, null) != TextToSpeech.SUCCESS) {
                    Log.e(TAG, "TTS queueing failed. Trying again");
                    tts.speak("bojangles", TextToSpeech.QUEUE_ADD, null);
                }
            } else {
                // I rather repeat code than use giant one-liners
                if (tts.speak("bojangles", TextToSpeech.QUEUE_ADD, null) != TextToSpeech.SUCCESS) {
                    Log.e(TAG, "TTS queueing failed. Trying again");
                    tts.speak("bojangles", TextToSpeech.QUEUE_ADD, null);
                }

            }
        }
    };

    public int onStartCommand(Intent intent, int flags, int startId) {

        startNotification();

        getConfiguration();

        if (sdkVersion < Build.VERSION_CODES.LOLLIPOP) {
            //countdown
            checkSchedule(); // primeira checagem, proxima daqui 30 segundos

            CountDownTimer minutesTimer = new CountDownTimer(30000, 30000) {

                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    checkSchedule();
                    start();
                }
            };

            minutesTimer.start();
        } else {
            //JobScheduler
        }

        return Service.START_STICKY;
    }

    public void checkSchedule() {
        Calendar now = Calendar.getInstance();


    }

    private void startNotification() {
        Log.d(TAG, "Starting notification");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notify_service).setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.serviceRunning));

        Intent i = new Intent(this, com.vag.voxreminder.MainActivity.class);

        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pi = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pi);

        startForeground(42066, mBuilder.build());
    }

    private void getConfiguration() {
        m_dao = new DAO(getApplicationContext());

        SQLiteDatabase db = m_dao.getReadableDatabase();

        String[] projection = {
                DAO.ScheduleTable._ID,
                DAO.ScheduleTable.COLUMN_NAME_DATE,
                DAO.ScheduleTable.COLUMN_NAME_REPEAT,
                DAO.ScheduleTable.COLUMN_NAME_TEXT
        };

        // Filter results WHERE "date" = '100011072017'
        String selection = DAO.ScheduleTable.COLUMN_NAME_DATE + " = ?";
        String[] selectionArgs = {"100011072017"};

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                DAO.ScheduleTable.COLUMN_NAME_DATE + " DESC";

        Cursor cursor = db.query(
                DAO.ScheduleTable.TABLE_NAME,             // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        List itemIds = new ArrayList<>();
        while (cursor.moveToNext()) {
            long itemId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DAO.ScheduleTable._ID));
            itemIds.add(itemId);
        }
        cursor.close();
    }

    private void updateAlarms() {
        //TODO:add new AlarmManager when config changes

        //if alarm present, use this:
        ComponentName receiver = new ComponentName(getApplicationContext(), BootupReceiver.class);
        PackageManager pm = getApplicationContext().getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        //if not present:
        //ComponentName receiver = new ComponentName(getApplicationContext(), BootupReceiver.class);
        //PackageManager pm = getApplicationContext().getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        //end

    }

    private long HOUR_IN_MS = 3600000L;

    private void setAlarmManager() {
        //https://developer.android.com/training/scheduling/alarms.html
        Intent intent = new Intent(this, BackgroundService.class);
        intent.setAction("HourElapsed");
        PendingIntent sender = PendingIntent.getBroadcast(this, 2, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        long l = new Date().getTime();
        if (l < new Date().getTime()) {
            l += HOUR_IN_MS; // start at next hour
        }
        am.setRepeating(AlarmManager.RTC_WAKEUP, l, HOUR_IN_MS, sender); // 86400000


        // Set the alarm to start at 8:30 a.m.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 8);
        calendar.set(Calendar.MINUTE, 30);

        // setRepeating() lets you specify a precise custom interval--in this case,
        // 20 minutes.
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                1000 * 60 * 20, sender);
    }

    private void startTTS() {
        if (sdkVersion < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            tts = new TextToSpeech(this, ttsListener);
            tts.setEngineByPackageName("com.svox.pico");
        } else {
            tts = new TextToSpeech(this, ttsListener, "com.svox.pico");
        }
    }

    private void stopTTS() {
        if (tts != null) {
            tts.shutdown();
            tts = null;
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
