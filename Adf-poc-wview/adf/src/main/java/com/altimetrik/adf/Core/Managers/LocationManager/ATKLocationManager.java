package com.altimetrik.adf.Core.Managers.LocationManager;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.altimetrik.adf.Core.Managers.BridgeManager.ATKBridgeManager;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;

import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by gyordi on 6/25/15.
 */
public class ATKLocationManager implements LocationListener {

    private static final String TAG = makeLogTag(ATKLocationManager.class);

    private static final long MIN_TIME_BW_UPDATES = 1000;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 50;

    private static final int TWO_MINUTES = 1000 * 60 * 2;


    private static ATKLocationManager ourInstance = new ATKLocationManager();

    private boolean mCanGetLocation;
    private Context mContext;
    private JSONObject mLastParams;


    public static ATKLocationManager getInstance() {
        return ourInstance;
    }

    private ATKLocationManager() {
        mCanGetLocation = false;
    }

    public double[] getUserLocation(Context context, JSONObject params) {

        mContext = context;
        mLastParams = params;
        double[] toReturn = null;

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, ourInstance);
        }
        // getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGPSEnabled && isNetworkEnabled) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, ourInstance);
        }

        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
            ourInstance.mCanGetLocation = false;

        } else {
            ourInstance.mCanGetLocation = true;

            Location gpsLocation = null, networkLocation = null, bestLocation;

            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }

            if (isNetworkEnabled) {
                networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (gpsLocation == null && networkLocation == null) {
                bestLocation = null;
            } else if (gpsLocation == null) {
                bestLocation = networkLocation;
            } else if (networkLocation == null) {
                bestLocation = gpsLocation;
            } else {
                bestLocation = isBetterLocation(gpsLocation, networkLocation) ? gpsLocation : networkLocation;
            }

            if (bestLocation != null) {
                toReturn = new double[2];
                toReturn[0] = bestLocation.getLatitude();
                toReturn[1] = bestLocation.getLongitude();
            }
        }
        return toReturn;
    }

    // http://developer.android.com/guide/topics/location/strategies.html
    /**
     * Determines whether one Location reading is better than the current Location fix
     *
     * @param location            The new Location that you want to evaluate
     * @param currentBestLocation The current Location fix, to which you want to compare the new one
     */
    protected static boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public static void startUpdateLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnabled && !isNetworkEnabled) {
            // no network provider is enabled
            ourInstance.mCanGetLocation = false;
        } else {
            ourInstance.mCanGetLocation = true;
            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, ourInstance);
                LOGD(TAG, "RLOC: GPS Enabled");
            }

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, ourInstance);
                LOGD(TAG, "LOC Network Enabled");
            }
        }
    }

    public static void stopUpdateLocation(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(ourInstance);
    }

    @Override
    public void onLocationChanged(Location location) {
        stopUpdateLocation(mContext);

        final JSONObject payload = new JSONObject();
        try {
            payload.put("latitude", location.getLatitude());
            payload.put("longitude", location.getLongitude());
        } catch (JSONException e) {
            LOGE(TAG, "onLocationChanged", e);
        }

        ((Activity) mContext).runOnUiThread(new Runnable() {
            public void run() {
                try {
                    ATKBridgeManager.excecuteJSCall(mLastParams.getString("webId"), mLastParams.getString("passiveCallbackFunction"), payload.toString());
                } catch (JSONException e) {
                    LOGE(TAG, "postNotification", e);
                }
            }
        });

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
