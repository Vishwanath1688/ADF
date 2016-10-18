package com.altimetrik.adf.Components.ATKWebView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.BridgeManager.ATKBridgeManager;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.ContactManager.ATKContactManager;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.Core.Managers.NotificationManager.ATKNotificationManager;
import com.altimetrik.adf.Util.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by pigounet on 3/26/15.
 */
public class ATKWebView extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKWebView.class);

    private WebView mWebView;
    private String mSource;
    private String mType;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        try {

            super.initWithJSON(widgetDefinition, context);

            //Create View
            mWebView = new WebView(context);
            super.loadParams(mWebView);

            //Set particular params
            final ATKBridgeManager bridgeManager = new ATKBridgeManager(context, this);
            mWebView.addJavascriptInterface(bridgeManager, "androidFromJS");
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.getSettings().setUseWideViewPort(true);
            mWebView.getSettings().setDomStorageEnabled(true);
            mWebView.getSettings().setAllowFileAccess(true);
            mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
            mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);

            mWebView.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setMessage(message);
                    alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                    alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                    return true;
                }
            });

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true);
            }

            mWebView.getSettings().setBuiltInZoomControls(true);
            mWebView.getSettings().setDisplayZoomControls(false);

            mWebView.setWebViewClient(new WebViewClient() {

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith(Constants.ATK_REDIRECT_URL_START)) {
                        String encodedValue = url.substring(Constants.ATK_REDIRECT_URL_START.length());
                        String decodedValue = Uri.decode(encodedValue);
                        LOGD(TAG, encodedValue);
                        LOGD(TAG, decodedValue);
                        bridgeManager._bridgeCall(decodedValue);
                        return true;
                    }
                    return (false);
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    handler.proceed();
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    //Toast.makeText(mContext, "CLIENT FINISHED! " + url, Toast.LENGTH_SHORT).show();
                }


            });

            //Load particular params
            mSource = getData().getString("source");
            mType = getData().getString("type");

            loadWebView();

        } catch (JSONException ex) {
            LOGD(TAG, "initWithJSON", ex);
        }
        return this;
    }

    public void loadWebView() {
        switch (mType) {
            case "local":
                mWebView.loadUrl(mSource);
                break;
            case "remote":
                mWebView.loadUrl(mSource);
                mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                break;
            case "HTMLString":
                mWebView.loadData(mSource, "text/html; charset=utf-8", "UTF-8");
                break;
        }
    }

    public void executeJS(String function) {
        if (mWebView != null && function != null) {
            if (function.startsWith("javascript:")) {
                mWebView.loadUrl(function);
            } else {
                mWebView.loadUrl("javascript:" + function);
            }
        }
    }

    @Override
    public void loadData(Object data, Context context) {
        super.loadData(data, context);

        try {
            JSONObject jsonData = ((JSONObject) data).getJSONObject("data");
            mType = jsonData.optString("mType", "");
            mSource = jsonData.optString("mSource", "");
            loadWebView();
        } catch (JSONException e) {
            LOGE(TAG, "loadDataWebView", e);
        }
    }

    @Override
    public View getDisplayView() {
        return mWebView;
    }

    @Override
    public void setValue(Object attrs, Context context) {
    }

    @Override
    public void clean() {
        if(mWebView != null){
            mWebView.removeAllViews();
        }
        mWebView = null;
        super.clean();
    }

}
