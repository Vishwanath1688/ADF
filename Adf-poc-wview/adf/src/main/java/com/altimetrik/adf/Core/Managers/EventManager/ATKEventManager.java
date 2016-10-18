package com.altimetrik.adf.Core.Managers.EventManager;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.Toast;

import com.altimetrik.adf.Components.ATKButton.ATKButton;
import com.altimetrik.adf.Components.ATKCollectionView.ATKCollectionView;
import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKMapView.ATKMapView;
import com.altimetrik.adf.Components.ATKNavigationContainer.ATKNavigationContainer;
import com.altimetrik.adf.Components.ATKScrollView.ATKScrollView;
import com.altimetrik.adf.Components.ATKWebView.ATKWebView;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.BridgeManager.ATKBridgeManager;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Core.Managers.LocationManager.ATKLocationManager;
import com.altimetrik.adf.Core.Managers.NotificationManager.Services.GCMClientManager;
import com.altimetrik.adf.Core.Managers.SyncContentManager.ATKSyncContentManager;
import com.altimetrik.adf.MainActivity;
import com.altimetrik.adf.R;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGI;
import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;
import static com.altimetrik.adf.Util.Utils.getDeviceDensity;

/**
 * Created by pigounet on 3/26/15.
 */
public class ATKEventManager {

    private static final String TAG = makeLogTag(ATKEventManager.class);

    public static final String DATA = "data";

    public static void excecuteAction(String action, JSONObject data, Context context) {
        try {
            String id;
            JSONObject params = data.getJSONObject("params");

            switch (action) {
                case Constants.ATK_ACTION_SCROLL_TO_PAGE:
                    id = params.getString("scrollViewId");
                    int pageNumber = params.getInt("pageNumber");
                    ATKScrollView scrollView = (ATKScrollView) ATKComponentManager.getInstance().getComponentById(id);
                    scrollView.goToPage(pageNumber);
                    break;

                case Constants.ATK_ACTION_SYNC_CONTENT:
                    String urlString = params.getString("url");
                    String authorizationHeader = params.getJSONObject("headers").getString("Authorization");
                    String callbackFunction = params.getString("callbackFunction");
                    String senderId = data.getString("senderId");
                    ATKSyncContentManager.sync(context, urlString, authorizationHeader, callbackFunction, senderId);
                    break;

                case Constants.ATK_ACTION_SET_SELECTED:
                    id = params.getString("buttonId");
                    boolean selected = Utils.isPositiveValue(params.getString("selected"));
                    ATKButton button = (ATKButton) ATKComponentManager.getInstance().getComponentById(id);
                    button.switchSelectedAction(selected);
                    break;

                case Constants.ATK_ACTION_JAVASCRIPT:
                    JSONObject jsParams = new JSONObject();
                    jsParams.put("id", data.getString("senderId"));
                    jsParams.put("data", data.getJSONObject("data"));
                    ATKEventManager.excecuteComponentAction(data, jsParams);
                    break;

                default:
                    LOGD(TAG, "Action not available.");
                    break;
            }
        } catch (JSONException e) {
            LOGE(TAG, "excecuteAction", e);
        }
    }

    public static void excecuteComponentAction(JSONObject action, JSONObject data) {
        try {
            JSONObject params = action.getJSONObject("params");
            String componentId = String.format("'%s'", data.getString("id"));
            ATKBridgeManager.excecuteJSCall(params.getString("webId"), params.getString("callbackFunction"), componentId, data.toString().replace("\'", "\\\'"));
        } catch (JSONException e) {
            LOGE(TAG, "excecuteComponentAction", e);
        }
    }

    public static void openMap(JSONObject data, Context context) {
        try {
            Double destinationLatitude = data.getDouble("latitude");
            Double destinationLongitude = data.getDouble("longitude");

            String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f", destinationLatitude, destinationLongitude);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException ex) {
                try {
                    Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    context.startActivity(unrestrictedIntent);
                } catch (ActivityNotFoundException innerEx) {
                    Toast.makeText(context, "Please install a maps application.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (JSONException e) {
            LOGE(TAG, "openApp", e);
            Toast.makeText(context, "Error opening map app.", Toast.LENGTH_LONG).show();
        }
    }

    public static void callPhone(JSONObject data, Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(String.format("tel:%s", data.getString("phone"))));
            try {
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "This device cannot make calls. Try installing a phone call app.", Toast.LENGTH_LONG).show();
                }
            } catch (Exception innerEx) {
                LOGE(TAG, "callPhone", innerEx);
            }
        } catch (JSONException e) {
            LOGW(TAG, "callPhone", e);
            Toast.makeText(context, "Error making call.", Toast.LENGTH_LONG).show();
        }
    }

    public static void openFile(JSONObject data, Context context) {
        String filePath = data.optString("path");
        if (!filePath.isEmpty()) {
            String fullFilePath = FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), filePath);
            File file = new File(fullFilePath);
            MimeTypeMap map = MimeTypeMap.getSingleton();
            String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
            String type = map.getMimeTypeFromExtension(ext);

            if (type == null)
                type = "*/*";

            File externalStorageTempDir = new File(context.getExternalCacheDir() + "/tmp/");
            File externalStorageFile = new File(externalStorageTempDir, file.getName());
            if(externalStorageFile.exists())
                externalStorageFile.delete();

            try {
                FileUtils.copyFile(file, externalStorageFile);
                externalStorageFile.setReadable(true, false);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.fromFile(externalStorageFile);
                intent.setDataAndType(uri, type);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, "No handler for this type of file.", Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                LOGE(TAG, "ERROR COPYING FILE", e);
            }


        } else {
            LOGE(TAG, "FILE NOT FOUND");
        }
    }

    public static void postNotification(@NonNull final ATKWebView webViewController, @NonNull final JSONObject data, @NonNull final Context context) {
        try {
            final View view = webViewController.getDisplayView();

            switch (data.optString("notificationId")) {

                //region ATK_NOTIFICATION_REMOVE_SPLASH_SCREEN
                case Constants.ATK_NOTIFICATION_REMOVE_SPLASH_SCREEN:
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            final View splashScreen = ((ViewGroup) view.getParent()).findViewWithTag(Constants.ATK_SPLASH_SCREEN);

                            if (splashScreen != null) {
                                Animation fadeOut = new AlphaAnimation(1, 0);
                                fadeOut.setInterpolator(new AccelerateInterpolator());
                                fadeOut.setDuration(400);

                                fadeOut.setAnimationListener(new Animation.AnimationListener() {
                                    public void onAnimationEnd(Animation animation) {
                                        ((ViewGroup) view.getParent()).removeView(splashScreen);
                                        ((Activity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    }

                                    public void onAnimationRepeat(Animation animation) {
                                    }

                                    public void onAnimationStart(Animation animation) {
                                    }
                                });
                                splashScreen.startAnimation(fadeOut);
                            }
                        }
                    });
                    break;
                //endregion

                //region ATK_NOTIFICATION_GET_USER_LOCATION
                case Constants.ATK_NOTIFICATION_GET_USER_LOCATION:
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        // Location permission has not been granted.
                        ((MainActivity) context).requestLocationPermission();
                    } else {
                        // Location permissions is already available.
                        LOGI(TAG, "ACCESS_COARSE_LOCATION permission has already been granted.");
                        LOGI(TAG, "ACCESS_FINE_LOCATION permission has already been granted.");

                        final JSONObject params_ = data.getJSONObject("data").getJSONObject("params");
                        double[] latlng = ATKLocationManager.getInstance().getUserLocation(context, params_);


                        final JSONObject payload = new JSONObject();
                        if (latlng != null) {
                            payload.put("latitude", latlng[0]);
                            payload.put("longitude", latlng[1]);
                        }

                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    ATKBridgeManager.excecuteJSCall(params_.getString("webId"), params_.getString("callbackFunction"), payload.toString());
                                } catch (JSONException e) {
                                    LOGE(TAG, "postNotification", e);
                                }
                            }
                        });
                    }

                    break;
                //endregion

                //region ATK_NOTIFICATION_STOP_UPDATING_USER_LOCATION
                case Constants.ATK_NOTIFICATION_STOP_UPDATING_USER_LOCATION:
                    LOGD(TAG, "ATKStopUpdatingUserLocation");
                    break;
                //endregion

                //region ATK_NOTIFICATION_ANIMATION
                case Constants.ATK_NOTIFICATION_ANIMATION:
                    JSONObject animationsParams = data.getJSONObject("data").getJSONObject("params");

                    String componentId = animationsParams.optString("componentId");
                    String property = animationsParams.optString("property");
                    String duration = animationsParams.optString("duration");
                    String byValue = animationsParams.optString("byValue");
                    String timingFunction = animationsParams.optString("timingFunction");

                    float density = getDeviceDensity(context);

                    if (!byValue.isEmpty() && !componentId.isEmpty()) {
                        String[] values = byValue.substring(1, byValue.length() - 1).split(",");
                        int x = (int) (Integer.parseInt(values[0]) * density);
                        int y = (int) (Integer.parseInt(values[1]) * density);

                        ATKWidget widgetToAnimate = ATKComponentManager.getInstance().getComponentById(componentId);

                        int defaultDuration = 600;
                        if (!duration.isEmpty()) {
                            defaultDuration = (int) (Float.parseFloat(duration) * 1000);
                        }

                        widgetToAnimate.getDisplayView().animate()
                                .translationXBy(x)
                                .translationYBy(y)
                                .setDuration(defaultDuration)
                                .setInterpolator(new AccelerateDecelerateInterpolator());
                    }
                    break;
                //endregion

                //region ATK_NOTIFICATION_EXECUTE_ACTION
                case Constants.ATK_NOTIFICATION_EXECUTE_ACTION:
                    switch (data.getJSONObject("data").getString("type")) {

                        //region ATK_NOTIFICATION_EXECUTE_ACTION_DISMISS_POPUP
                        case Constants.ATK_NOTIFICATION_EXECUTE_ACTION_DISMISS_POPUP:
                            JSONObject dismissParams = data.getJSONObject("data").getJSONObject("params");
                            final ATKMapView mapView = (ATKMapView) ATKComponentManager.getInstance().getComponentById(dismissParams.getString("componentId"));
                            if (mapView != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        mapView.dismissPopover();
                                    }
                                });
                            }
                            break;
                        //endregion

                        //region ATK_NOTIFICATION_EXECUTE_ACTION_SET_ENABLED
                        case Constants.ATK_NOTIFICATION_EXECUTE_ACTION_SET_ENABLED:
                            JSONObject params = data.getJSONObject("data").getJSONObject("params");
                            final boolean enabled = params.getBoolean("enabled");
                            final ATKComponentBase widget = (ATKComponentBase)ATKComponentManager.getInstance().getComponentById(params.getString("componentId"));
                            if(widget != null) {
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        widget.setEnabled(enabled);
                                    }
                                });
                            }
                            break;
                        //endregion

                    }
                    break;
                //endregion

                //region ATK_NOTIFICATION_SHOW_SPLASH_SCREEN
                case Constants.ATK_NOTIFICATION_SHOW_SPLASH_SCREEN:
                    // no-op
                    break;
                //endregion

                //region ATK_NOTIFICATION_RESTART_APP
                case Constants.ATK_NOTIFICATION_RESTART_APP:
                    // cache clear
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            WebView webview = (WebView) view;
                            webview.loadUrl("about:blank");
                            ((WebView) view).clearCache(true);
                        }
                    });

                    ATKComponentManager.getInstance().init();
                    PicassoTools.clearCache(Picasso.with(context));
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            ((Activity) context).recreate();
                        }
                    });

                    break;
                //endregion

                //region ATK_NOTIFICATION_CLEAR_CACHE_AND_RELOAD_APP
                case Constants.ATK_NOTIFICATION_CLEAR_CACHE_AND_RELOAD_APP:
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            WebView webview = (WebView) view;
                            webview.loadUrl("about:blank");
                            webview.clearCache(true);

                            webViewController.loadWebView();
                        }
                    });

                    break;
                //endregion

                //region ATK_NOTIFICATION_NAVIGATION
                case Constants.ATK_NOTIFICATION_NAVIGATION:
                    String type = data.getJSONObject("data").getString("type");
                    final JSONObject navParams = data.getJSONObject("data").getJSONObject("params");
                    final ATKNavigationContainer navBar = (ATKNavigationContainer) ATKComponentManager.getInstance().getComponentById(navParams.getString("navigationId"));

                    switch (type) {
                        case Constants.ATK_NOTIFICATION_NAVIGATION_TYPE_CHANGE_BACKGROUND_COLOR:
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    navBar.setNavBarBackground(navParams.optString("newImage"), navParams.optString("newColor"));
                                }
                            });
                            break;
                        case Constants.ATK_NOTIFICATION_NAVIGATION_TYPE_CHANGE_TITLE:
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    navBar.setTitle(navParams.optString("newTitle"));
                                }
                            });
                            break;
                        case Constants.ATK_NOTIFICATION_NAVIGATION_TYPE_PUSH_BUNDLE:
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    navBar.push(navParams.optJSONObject("bundle"));
                                }
                            });
                            break;
                        case Constants.ATK_NOTIFICATION_NAVIGATION_TYPE_PUSH_COMPONENT:
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    navBar.push(navParams.optJSONObject("component"));
                                }
                            });
                            break;
                        case Constants.ATK_NOTIFICATION_NAVIGATION_TYPE_POP:
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    navBar.pop();
                                }
                            });
                            break;
                    }
                    break;
                //endregion

                //region ATK_NOTIFICATION_CONFIGURE_REMOTE_NOTIFICATIONS
                case Constants.ATK_NOTIFICATION_CONFIGURE_REMOTE_NOTIFICATIONS:
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            GCMClientManager mPushClientManager = new GCMClientManager((Activity) context, context.getString(R.string.gcm_api_key));
                            mPushClientManager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
                                @Override
                                public void onSuccess(String registrationId, boolean isNewRegistration) {
                                    String deviceId = Settings.Secure.getString(context.getContentResolver(),
                                            Settings.Secure.ANDROID_ID);
                                    String deviceType = Utils.isTabletDevice(context) ? Constants.ATK_ANDROID_TABLET_TAG : Constants.ATK_ANDROID_PHONE_TAG;
                                    ATKBridgeManager.excecuteJSCall("webView1", "RemoteNotificationsService.registerDeviceToken", String.format("'%s'", deviceId), String.format("'%s'", registrationId), String.format("'%s'", android.os.Build.VERSION.RELEASE), String.format("'%s'", deviceType));
                                }

                                @Override
                                public void onFailure(String ex) {
                                    super.onFailure(ex);
                                    LOGI(TAG, ex);
                                }
                            });
                        }
                    });
                    break;
                //endregion

                //region ATK_NOTIFICATION_DEVICE_LANGUAGE
                case Constants.ATK_NOTIFICATION_DEVICE_LANGUAGE:
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            String deviceLanguage = Locale.getDefault().getLanguage();
                            ATKBridgeManager.excecuteJSCall("webView1", "InitCtrl.setDeviceLanguage", String.format("'%s'", deviceLanguage));
                        }
                    });
                    break;
                //endregion

                //region ATK_NOTIFICATION_REMOVE_LIST_ITEM
                case Constants.ATK_NOTIFICATION_REMOVE_LIST_ITEM:
                    final JSONObject params = data.getJSONObject("data");
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                ATKComponentManager compManager = ATKComponentManager.getInstance();
                                ATKWidget widget = compManager.getComponentById(params.getString("componentId"));
                                if (widget != null && widget instanceof ATKCollectionView) {
                                    ATKCollectionView list = (ATKCollectionView) widget;
                                    list.removeListItem(params.getJSONArray("rows"), params.getString("animation"));
                                }
                            } catch (JSONException e) {
                                LOGE(TAG, "ATK_NOTIFICATION_REMOVE_LIST_ITEM", e);
                            }
                        }
                    });
                    break;
                //endregion

                //region DEFAULT
                default:
                    if (data.has("data")) {
                        final JSONObject paramsData = data.getJSONObject("data").getJSONObject("params");
                        String destinationId = paramsData.optString("destinationId");
                        if (destinationId != null && !destinationId.isEmpty()) {
                            final ATKWidget widget = ATKComponentManager.getInstance().getComponentById(destinationId);
                            if (widget != null)
                                ((Activity) context).runOnUiThread(new Runnable() {
                                    public void run() {
                                        widget.loadData(paramsData, context);
                                    }
                                });
                            sendLocalNotification(destinationId, data.getJSONObject("data").toString(), context);
                        }
                    }
                    break;
                //endregion
            }
        } catch (JSONException e) {
            LOGE(TAG, "postNotification JSONException", e);
        } catch (Exception e) {
            LOGE(TAG, "postNotification Exception", e);
        }
    }

    private static void sendLocalNotification(String id, String data, Context context) {
        Intent intent = new Intent(id);
        intent.putExtra(DATA, data);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    
    public static void setBadgeValue(final JSONObject params, final Context context) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                try {
                    ATKComponentManager compManager = ATKComponentManager.getInstance();
                    ATKWidget widget = compManager.getComponentById(params.getString("componentId"));
                    if (context != null && widget != null) {
                        ATKButton button = (ATKButton) widget;
                        button.setBadgeValue(params.getString("value"));
                    }
                } catch (JSONException e) {
                    LOGE(TAG, "setBadgeValue", e);
                }
            }
        });
    }

    public static void onResume() {
        try {
            ATKBridgeManager.excecuteJSCall("webView1", "$scope.onResume", 42); // TODO: get real webview id. Fix call without params (42 is a workaround)
        } catch (Exception e) {
            LOGW(TAG, "$scope.onResume", e);
        }
    }

}