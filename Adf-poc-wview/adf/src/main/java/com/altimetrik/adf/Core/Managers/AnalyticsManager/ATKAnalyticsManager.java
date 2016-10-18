package com.altimetrik.adf.Core.Managers.AnalyticsManager;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import static com.altimetrik.adf.Util.LogUtils.LOGD;

public class ATKAnalyticsManager {

    private final static String TAG = "ATKAnalyticsManager";
    private static Context sAppContext = null;
    private static Tracker mTracker;

    public final static String USER_ACTION_CATEGORY = "USER_ACTION";
    public final static String USER_ACTION_ACTION = "USER_ACTION_ACTION";

    private static boolean canSend() {
        // We can only send Google Analytics when ALL the following conditions are true:
        //    1. This module has been initialized.
        //    2. The user has accepted the ToS.
        //    3. Analytics is enabled in Settings.
        return sAppContext != null && mTracker != null;
    }

    public static void sendScreenView(String appName, String screenName) {
        if (canSend()) {
            mTracker.setAppName(appName);
            mTracker.setScreenName(screenName);
            mTracker.send(new HitBuilders.AppViewBuilder().build());
            LOGD(TAG, "Screen View recorded: " + screenName);
        } else {
            LOGD(TAG, "Screen View NOT recorded (analytics disabled or not ready).");
        }
    }

    public static void sendEvent(String category, String action, String label, long value) {
        if (canSend()) {
            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .setLabel(label)
                    .setValue(value)
                    .build());

            LOGD(TAG, "Event recorded:");
            LOGD(TAG, "\tCategory: " + category);
            LOGD(TAG, "\tAction: " + action);
            LOGD(TAG, "\tLabel: " + label);
            LOGD(TAG, "\tValue: " + value);
        } else {
            LOGD(TAG, "Analytics event ignored (analytics disabled or not ready).");
        }
    }

    public static void sendEvent(String category, String action, String label) {
        sendEvent(category, action, label, 0);
    }

    public static void sendException(String message, boolean fatal) {
        if (canSend()) {
            mTracker.send(new HitBuilders.ExceptionBuilder()
                    .setDescription(message)
                    .setFatal(fatal)
                    .build());

            LOGD(TAG, "Exception recorded:");
            LOGD(TAG, "\tException message: " + message);
            LOGD(TAG, "\tFatal: " + fatal);
        } else {
            LOGD(TAG, "Analytics exception ignored (analytics disabled or not ready).");
        }
    }

    public static void sendException(Throwable e, boolean fatal) {
        sendException(new StandardExceptionParser(sAppContext, null)
                        .getDescription(Thread.currentThread().getName(), e),
                fatal);
    }

    public static synchronized void initializeAnalyticsTracker(Context context,
                                                               boolean trackUncaughtExceptions,
                                                               int dispatchInterval,
                                                               boolean debug,
                                                               String trackingId,
                                                               String appVersion) {
        sAppContext = context;
        if (mTracker == null) {
            mTracker = GoogleAnalytics.getInstance(context).newTracker(trackingId);
            mTracker.enableAdvertisingIdCollection(true);
            mTracker.enableExceptionReporting(trackUncaughtExceptions);
            mTracker.setSessionTimeout(dispatchInterval);
            mTracker.setAppVersion(appVersion);
        }
    }

    public Tracker getTracker() {
        return mTracker;
    }

    public static synchronized void setTracker(Tracker tracker) {
        mTracker = tracker;
    }
}
