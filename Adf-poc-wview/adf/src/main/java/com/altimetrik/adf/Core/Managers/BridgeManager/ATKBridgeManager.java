package com.altimetrik.adf.Core.Managers.BridgeManager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;

import com.altimetrik.adf.Components.ATKWebView.ATKWebView;
import com.altimetrik.adf.Core.Managers.ActivityManager.ATKActivityManager;
import com.altimetrik.adf.Core.Managers.AnalyticsManager.ATKAnalyticsManager;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.ContactManager.ATKContactManager;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.Core.Managers.NotificationManager.ATKNotificationManager;
import com.altimetrik.adf.Core.Managers.TransitionManager.ATKTransitionManager;
import com.altimetrik.adf.MainActivity;
import com.altimetrik.adf.Util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 23/10/2015.
 */
public class ATKBridgeManager {

    private static final String TAG = makeLogTag(ATKBridgeManager.class);

    private Context mContext;
    private ATKWebView mWebViewController;

    public ATKBridgeManager(Context context, ATKWebView webViewController) {
        mContext = context;
        mWebViewController = webViewController;
    }

    public interface IPermissionObserver {
        void OnPermissionGranted();

        void OnPermissionNotGranted();
    }

    /**
     * Incoming JS calls
     */
    @JavascriptInterface
    public void _bridgeCall(String jsonString) {

        try {

            final JSONObject jsonObject = new JSONObject(jsonString);
            String command = jsonObject.getString("command").toLowerCase();
            String operation = jsonObject.getString("operation").toLowerCase();

            View webViewContainer = mWebViewController.getDisplayView();

            //Check the command
            switch (command) {
                case Constants.ATK_COMMAND_WIDGET:
                    switch (operation) {
                        case Constants.ATK_OPERATION_CREATE_WIDGET:
                            ATKComponentManager.getInstance().presentComponentInView(mContext, (ViewGroup) webViewContainer, jsonObject.getJSONObject("params").getJSONObject("widgetJSON"));
                            break;
                        case Constants.ATK_OPERATION_CREATE_ALL_WIDGETS:
                            ATKComponentManager.getInstance().presentParentWidgets(mContext, (ViewGroup) webViewContainer, jsonObject.getJSONArray("params"));
                            break;
                        case Constants.ATK_OPERATION_REMOVE_WIDGET:
                            ATKComponentManager.getInstance().removeComponentByID(mContext, jsonObject.getJSONObject("params").getString("componentId"));
                            break;
                        case Constants.ATK_OPERATION_REMOVE_ALL_WIDGETS:
                            ATKComponentManager.getInstance().removeAllComponents(mContext, jsonObject.getJSONArray("params"));
                            ATKComponentManager.getInstance().removeAllProgressHUDs(mContext, (ViewGroup) webViewContainer);
                            break;
                    }
                    break;

                case Constants.ATK_COMMAND_OPEN_APP:
                    switch (operation) {
                        case Constants.ATK_OPERATION_MAP:
                            JSONObject mapData = jsonObject.getJSONObject("params").getJSONObject("mapData");
                            ATKEventManager.openMap(mapData, mContext);
                            break;
                        case Constants.ATK_OPERATION_PHONE:
                            JSONObject phoneData = jsonObject.getJSONObject("params").getJSONObject("phoneData");
                            ATKEventManager.callPhone(phoneData, mContext);
                            break;
                        case Constants.ATK_OPERATION_FILE:
                            JSONObject fileData = jsonObject.getJSONObject("params").getJSONObject("fileData");
                            ATKEventManager.openFile(fileData, mContext);
                            break;
                    }
                    break;

                case Constants.ATK_COMMAND_CONTACT:
                    switch (operation) {
                        case Constants.ATK_OPERATION_CONTACT_ADD:
                            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_CONTACTS)
                                    != PackageManager.PERMISSION_GRANTED) {
                                final MainActivity mainActivity = (MainActivity) mContext;
                                mainActivity.addContactPermissionObserver(new IPermissionObserver() {
                                    @Override
                                    public void OnPermissionGranted() {
                                        try {
                                            JSONObject contactData = jsonObject.getJSONObject("params").getJSONObject("contactData");
                                            ATKContactManager.addContact(contactData, mContext);
                                        } catch (JSONException e) {
                                            LOGE(TAG, "_bridgeCall OnPermissionGranted", e);
                                        }
                                    }

                                    @Override
                                    public void OnPermissionNotGranted() {

                                    }
                                });
                                mainActivity.requestContactPermission();
                            } else {
                                JSONObject contactData = jsonObject.getJSONObject("params").getJSONObject("contactData");
                                ATKContactManager.addContact(contactData, mContext);
                            }
                            break;
                    }
                    break;

                case Constants.ATK_COMMAND_ACTION:
                    switch (operation) {
                        case Constants.ATK_OPERATION_EXECUTE_ACTION:
                            JSONObject params = jsonObject.getJSONObject("params");
                            String action = params.getString("type").toLowerCase();
                            ATKEventManager.excecuteAction(action, params, mContext);
                            break;
                        case Constants.ATK_OPERATION_PREPARE_FOR_TRANSITION:
                            ATKTransitionManager.prepareForTransition(webViewContainer, mContext);
                            break;
                        case Constants.ATK_OPERATION_COMMIT_TRANSITION:
                            ATKTransitionManager.commitForTransition(webViewContainer, jsonObject.getJSONObject("params"), mContext);
                            break;
                        case Constants.ATK_OPERATION_CLOSE_APP:
                            ((Activity) mContext).finish();
                            break;
                        case Constants.ATK_OPERATION_ROTATE:
                            ATKActivityManager.handleDeviceRotation(jsonObject.getJSONArray("params"), mContext);
                            break;
                    }
                    break;

                case Constants.ATK_COMMAND_NOTIFICATION:
                    switch (operation) {
                        case Constants.ATK_OPERATION_POST_NOTIFICATION:
                            ATKEventManager.postNotification(mWebViewController, jsonObject.getJSONObject("params"), mContext);
                            break;
                    }
                    break;

                case Constants.ATK_COMMAND_LOCAL_NOTIFICATION:
                    JSONObject notificationData = jsonObject.getJSONObject("params").getJSONObject("notificationData");
                    switch (operation) {
                        case Constants.ATK_OPERATION_SCHEDULE_LOCAL_NOTIFICATION:
                            ATKNotificationManager.scheduleNotification(notificationData, mContext);
                            break;
                        case Constants.ATK_OPERATION_CANCEL_LOCAL_NOTIFICATION:
                            ATKNotificationManager.cancelNotification(notificationData, mContext);
                            break;
                    }
                    break;

                case Constants.ATK_COMMAND_PROGRESS_HUD:
                    switch (operation) {
                        case Constants.ATK_OPERATION_PROGRESS_HUD_SHOW:
                            ATKComponentManager.getInstance().showProgressHUD(mContext, (ViewGroup) webViewContainer, jsonObject.getJSONObject("params"));
                            break;
                        case Constants.ATK_OPERATION_PROGRESS_HUD_HIDE:
                            ATKComponentManager.getInstance().hideProgressHUD(mContext, (ViewGroup) webViewContainer, jsonObject.getJSONObject("params").getString("componentId"));
                            break;
                        case Constants.ATK_OPERATION_PROGRESS_HUD_SET_PROGRESS:
                            ATKComponentManager.getInstance().updateProgressHUD(mContext, jsonObject.getJSONObject("params").getString("componentId"), (int) (jsonObject.getJSONObject("params").getDouble("progress") * 100));
                            break;
                    }
                    break;

                case Constants.ATK_COMMAND_BADGE:
                    switch (operation) {
                        case Constants.ATK_OPERATION_SET_BADGE_TEXT:
                            ATKEventManager.setBadgeValue(jsonObject.getJSONObject("params"), mContext);
                            break;
                    }
                    break;

                case Constants.ATK_COMMAND_ANALYTICS:
                    switch (operation) {
                        case Constants.ATK_OPERATION_SET_ANALYTICS_INIT:
                            ATKAnalyticsManager.initializeAnalyticsTracker(mContext.getApplicationContext(),
                                    jsonObject.getJSONObject("params").getBoolean("trackUncaughtExceptions"),
                                    jsonObject.getJSONObject("params").getInt("dispatchInterval"),
                                    jsonObject.getJSONObject("params").getBoolean("debug"),
                                    jsonObject.getJSONObject("params").getString("trackingId"),
                                    jsonObject.getJSONObject("params").getString("appVersion"));
                            break;
                        case Constants.ATK_OPERATION_SET_ANALYTICS_SEND_SCREEN_VIEW:
                            ATKAnalyticsManager.sendScreenView(
                                    jsonObject.getJSONObject("params").getString("appName"),
                                    jsonObject.getJSONObject("params").getString("screen"));
                            break;
                    }
                    break;

                default:
                    LOGW(TAG, "Command not available.");
                    break;

            }
        } catch (JSONException e) {
            LOGE(TAG, "_bridgeCall", e);
        }
    }

    /**
     * Outgoing JS call
     */
    public static void excecuteJSCall(String webId, String functionName, Object... params) {

        ATKWebView webView = (ATKWebView) ATKComponentManager.getInstance().getComponentById(webId);
        String parsedFunctionName;

        if (functionName.contains("(")) {
            String function;
            parsedFunctionName = functionName.substring(0, functionName.indexOf("("));
            if (parsedFunctionName.contains(".")) {
                function = String.format("%s('%s', %s", Constants.ATK_ANGULAR_INVOKE_FUNCTION, parsedFunctionName, functionName.substring(functionName.indexOf("(") + 1));
            } else {
                function = functionName;
            }
            webView.executeJS(function);
        } else {
            if (functionName.contains(".")) {
                parsedFunctionName = String.format("%s('%s',", Constants.ATK_ANGULAR_INVOKE_FUNCTION, functionName);
            } else {
                parsedFunctionName = functionName + "(";
            }
            StringBuilder call = new StringBuilder();
            call.append(parsedFunctionName);

            List<String> list = new ArrayList<>();
            int len = params.length;
            for (int i = 0; i < len; i++) {
                Object param = params[i];
                String data;
                if (param instanceof String) {
                    data = (String) param;
                } else {
                    data = param.toString().replace("\'", "\\\'");
                }
                list.add(data);
            }

            call.append(TextUtils.join(",", list));
            call.append(")");
            webView.executeJS(call.toString());
        }

    }

}
