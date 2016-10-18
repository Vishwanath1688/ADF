package com.altimetrik.adf.Core.Managers.NotificationManager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.altimetrik.adf.MainActivity;
import com.altimetrik.adf.R;
import com.altimetrik.adf.Util.Constants;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 9/18/15.
 */
public class ATKNotificationManager {

    private static final String TAG = makeLogTag(ATKNotificationManager.class);

    public static void scheduleNotification(JSONObject notificationData, Context context) {

        String appName = context.getString(context.getApplicationInfo().labelRes);

        int id = notificationData.optInt("id", 0);
        String preview = notificationData.optString("preview");
        String title = notificationData.optString("title", appName);
        String description = notificationData.optString("description");
        String dateTimeFormat = notificationData.optString("dateTimeFormat", "MM-dd-YYYY HH:mm");
        String dateTime = notificationData.optString("dateTime");


        if (preview.isEmpty() && !title.isEmpty())
            preview = title;

        long scheduleTime = System.currentTimeMillis();
        if (!dateTime.isEmpty()) {
            try {
                SimpleDateFormat timeFormatter = new SimpleDateFormat(dateTimeFormat);
                Date date = timeFormatter.parse(dateTime);
                scheduleTime = date.getTime();
            } catch (ParseException e) {
                LOGE(TAG, "notification date parse", e);
            }
        }

        //Intent that will open the app when the notification is pressed
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.setAction(Constants.ATK_NOTIFICATION_ACTION);
        PendingIntent contentIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(preview)
                .setContentTitle(title)
                .setContentText(description)
                .setContentIntent(contentIntent);

        //Intent that will trigger the notification creation when scheduled
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, id);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION,  builder.build());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, scheduleTime, pendingIntent);

    }

    public static void cancelNotification(JSONObject notificationData, Context context) {
        int id = notificationData.optInt("id", 0);

        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}
