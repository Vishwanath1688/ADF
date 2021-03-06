package com.altimetrik.adf.Core.Managers.NotificationManager.Services;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by esalazar on 9/29/15.
 */
public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Explicitly specify that GcmMessageHandler will handle the intent.
        ComponentName component = new ComponentName(context.getPackageName(), GCMNotificationIntentService.class.getName());

        // Start the service, keeping the device awake while it is launching.
        startWakefulService(context, (intent.setComponent(component)));
        setResultCode(Activity.RESULT_OK);
    }
}
