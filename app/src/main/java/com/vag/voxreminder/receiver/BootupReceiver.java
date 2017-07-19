package com.vag.voxreminder.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.vag.voxreminder.service.BackgroundService;

public class BootupReceiver extends BroadcastReceiver {
    // starting the app on boot-up

    private final String TAG = "BootUpReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "onReceive");

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                || "RestartTimeService".equals(intent.getAction())) {
            Log.d(TAG, "BootUpReceiver BOOT_COMPLETED");

            Intent i = new Intent(context, BackgroundService.class);
            context.startService(i);
        }
    }
}