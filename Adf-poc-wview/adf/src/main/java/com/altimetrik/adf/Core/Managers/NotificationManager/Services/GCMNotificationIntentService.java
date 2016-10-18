package com.altimetrik.adf.Core.Managers.NotificationManager.Services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import com.altimetrik.adf.Core.Managers.NotificationManager.ATKNotificationManager;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGI;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by esalazar on 9/29/15.
 */
public class GCMNotificationIntentService extends IntentService {

    private static final String TAG = makeLogTag(GCMNotificationIntentService.class);

    public GCMNotificationIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        try {
            JSONObject notificationData = new JSONObject((String) extras.get("notificationData"));
            LOGI(TAG, "New Push Notification - " + notificationData.toString());
            ATKNotificationManager.scheduleNotification(notificationData, getApplicationContext());
            GCMBroadcastReceiver.completeWakefulIntent(intent);
        } catch (JSONException e) {
            LOGE(TAG, "onHandleIntent ", e);
        }
    }
}
