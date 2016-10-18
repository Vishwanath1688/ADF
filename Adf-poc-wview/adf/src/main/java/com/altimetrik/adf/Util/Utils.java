package com.altimetrik.adf.Util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

/**
 * Created by pigounet on 5/18/15.
 */
public class Utils {

    public static boolean isTabletDevice(Context activityContext) {
        Configuration config = activityContext.getResources().getConfiguration();

        //At least 7-inch tablet size (320 phone, 480 large phone, 600 7-inch tablet, 720 10-inch tablet)
        if (config.smallestScreenWidthDp >= 600) {
            return true;
        }
        return false;
    }

    public static boolean isFirstRun(Context context) {
        Integer firstRun = getLatestInstalledVersion(context);
        return firstRun != getAppVersionCode(context);
    }

    public static void saveFirstRunVersion(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putInt("VERSION_FIRST_RUN", Utils.getAppVersionCode(context));
        edit.commit();
    }

    public static int getAppVersionCode(Context context) {
        try {
            final PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {
            return 0;
        }
    }

    public static int getLatestInstalledVersion(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("VERSION_FIRST_RUN", 0);
    }
    
    public static boolean isPositiveValue(String condition) {
        if (condition.toLowerCase().equals("yes") || condition.toLowerCase().equals("true")) {
            return true;
        }
        return false;
    }

    public static float getDeviceDensity(Context context){
        return context.getResources().getDisplayMetrics().density;
    }

    public static boolean isEnhancedDevice(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean("IS_ENHANCED_DEVICE", false);
    }

    public static void setEnhancedDeviceStatus(Context context, boolean status) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("IS_ENHANCED_DEVICE", status);
        edit.commit();
    }

}
