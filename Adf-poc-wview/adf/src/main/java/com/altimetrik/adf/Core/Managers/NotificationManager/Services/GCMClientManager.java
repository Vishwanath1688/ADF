package com.altimetrik.adf.Core.Managers.NotificationManager.Services;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.util.Log;

import com.altimetrik.adf.Util.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGI;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

public class GCMClientManager {

    private static final String TAG = makeLogTag(GCMClientManager.class);

    // Constants
    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    // Member variables
    private GoogleCloudMessaging gcm;
    private String regid;
    private String projectNumber;
    private WeakReference<Activity> weakActivity;

    public static abstract class RegistrationCompletedHandler {
        public abstract void onSuccess(String registrationId, boolean isNewRegistration);
        public void onFailure(String ex) {
            // If there is an error, don't just keep trying to register.
            // Require the user to click a button again, or perform
            // exponential back-off.
            LOGE(TAG, ex);
        }
    }

    public GCMClientManager(Activity activity, String projectNumber) {
        this.weakActivity = new WeakReference<Activity>(activity);
        this.projectNumber = projectNumber;
        this.gcm = GoogleCloudMessaging.getInstance(activity);
    }

    // Register if needed or fetch from local store
    public void registerIfNeeded(final RegistrationCompletedHandler handler) {
        if(getContext() == null){
            return;
        }
        if (checkPlayServices()) {
            regid = getRegistrationId(getContext());

            if (regid.isEmpty()) {
                registerInBackground(handler);
            } else { // got id from cache
                LOGI(TAG, regid);
                handler.onSuccess(regid, false);
            }
        } else { // no play services
            LOGI(TAG, "No valid Google Play Services APK found.");
        }
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground(final RegistrationCompletedHandler handler) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                if(getContext() == null) {
                    return null;
                }
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(getContext());
                    }
                    regid = gcm.register(projectNumber);
                    LOGI(TAG, regid);

                    // Persist the regID - no need to register again.
                    storeRegistrationId(getContext(), regid);

                } catch (IOException ex) {
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                    handler.onFailure("Error :" + ex.getMessage());
                }
                return regid;
            }

            @Override
            protected void onPostExecute(String regId) {
                if (regId != null) {
                    handler.onSuccess(regId, true);
                }
            }
        }.execute(null, null, null);
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context) != null ? getGCMPreferences(context) : null;
        String registrationId = getGCMPreferences(context) != null ? prefs.getString(PROPERTY_REG_ID, "") : null;
        if (registrationId != null && registrationId.isEmpty()) {
            LOGI(TAG, "Registration not found.");
            return "";
        }

        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = Utils.getAppVersionCode(context);
        if (registeredVersion != currentVersion) {
            LOGI(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context) != null ? getGCMPreferences(context) : null;
        int appVersion = Utils.getAppVersionCode(context);
        LOGI(TAG, "Saving regId on app version " + appVersion);
        if(prefs != null) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PROPERTY_REG_ID, regId);
            editor.putInt(PROPERTY_APP_VERSION, appVersion);
            editor.commit();
        }
    }

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getContext() != null ? getContext().getSharedPreferences(context.getPackageName(),
                Context.MODE_PRIVATE) : null;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, getActivity(),
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                LOGI(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }

    private Context getContext() {
        return weakActivity.get();
    }

    private Activity getActivity() {
        return weakActivity.get();
    }
}