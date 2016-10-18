package com.altimetrik.adf.Components.ATKCollectionView;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWebView.ATKWebView;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.BridgeManager.ATKBridgeManager;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.FileUtils;
import com.altimetrik.adf.Util.UIUtils;
import com.altimetrik.adf.Util.Utils;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by esalazar on 4/29/15.
 */
public class ATKCollectionBase extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKCollectionBase.class);

    //Style
    private int mHeightHeaderSection;

    //Properties
    private String mScrollDirection;
    private String mLayoutType;
    private JSONObject mItemLayout;
    private JSONObject mItemLayouts;
    private Boolean mAutomaticallyShowProgressHUD;
    private Boolean mShowShadow;
    private Boolean mLoadMoreButton;
    private String mHighlightColor;
    protected JSONObject mItemSeparator;

    //Data
    protected Object mSource;
    private String mType;
    private JSONArray mItems;
    private String mSections;
    private String mMoreItems;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {
        super.initWithJSON(widgetDefinition, context);

        try {
            if (getProperties().has("itemLayouts")) {
                mItemLayouts = getProperties().getJSONObject("itemLayouts");
            } else {
                mItemLayout = getProperties().getJSONObject("itemLayout");
                mComponentsArray = mItemLayout.getJSONArray("components"); // Views each element of the collection view are to be shown
                mShowShadow = mItemLayout.optBoolean("showShadow");
            }

            mItemSeparator = getProperties().optJSONObject("itemSeparator");
            mHeightHeaderSection = getStyle().optInt("heightHeaderSection", 0);
            mLayoutType = getProperties().optString("layoutType");
            mScrollDirection = getProperties().optString("scrollDirection");
            mLoadMoreButton = getProperties().optBoolean("loadMoreButton");
            mAutomaticallyShowProgressHUD = getProperties().optBoolean("automaticallyShowProgressHud", true);
            mHighlightColor = getStyle().optString("highlightColor", "#AAEEEEEE");
            if (mHighlightColor.isEmpty()) {
                mHighlightColor = "#AAEEEEEE";
            }

            mType = getData().optString("type").toLowerCase();

            switch (mType) {
                case Constants.ATK_DATA_TYPE_JAVASCRIPT:
                    handleJavascriptSource();
                    break;
                case Constants.ATK_DATA_TYPE_JSONSTRING:
                    mSource = getData().get("source");
                    handleJSONStringSource();
                    break;
                case Constants.ATK_DATA_TYPE_LOCAL:
                    handleLocalSource(context);
                    break;
                default:
                    LOGD(TAG, "ATKCollectionBase - unknown datasource");
                    break;
            }
        } catch (JSONException e) {
            LOGE(TAG, "initWithJSON", e);
        }

        onPostInitWithJSON();

        return this;
    }

    private void handleJSONStringSource() throws JSONException {
        String sourceClassName = mSource.getClass().toString();
        if (sourceClassName.equalsIgnoreCase(JSONObject.class.toString())) {
            JSONObject source = (JSONObject) mSource;
            mItems = source.has("items") ? source.getJSONArray("items") : null; // Data for each element of the collection view
            mSections = source.optString("sections");
            mMoreItems = source.optString("moreItems");
        } else if (sourceClassName.equalsIgnoreCase(JSONArray.class.toString())) {
            mItems = (JSONArray) mSource;
        }
    }

    private void handleLocalSource(Context context) throws JSONException {
        String filePath = FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), getData().getString("source"));
        mSource = new JSONObject(FileUtils.readFile(filePath));
        handleJSONStringSource();
    }

    private void handleJavascriptSource() throws JSONException {
        //If the list is loaded by JS, show a progress HUD and then execute the JS call
        final String webViewId = getData().getJSONObject("params").getString("webId");
        final ATKWebView webViewWidget = (ATKWebView) ATKComponentManager.getInstance().getComponentById(webViewId);
        if (mAutomaticallyShowProgressHUD) {
            showProgressHUD(webViewWidget);
        }
        int delay = Utils.isEnhancedDevice(mContext) ? Constants.ATK_COLLECTION_JS_DELAY / 2 : Constants.ATK_COLLECTION_JS_DELAY;
        new Handler().postDelayed( new Runnable() {
            @Override
            public void run() {
                try {
                    ATKBridgeManager.excecuteJSCall(webViewId, getData().getString("source"), "'" + getData().getJSONObject("params").getString("componentId") + "'");
                } catch (JSONException e) {
                    LOGD(TAG, "ATKCollectionBase Javascript", e);
                }
            }
        } , delay);
        mItems = new JSONArray();
    }

    public void updateSourceData(JSONObject data) {
        try {
            Object paramsObject = getData().get("params");
            JSONObject params = (paramsObject instanceof JSONObject) ? (JSONObject) paramsObject : null;
            String webViewId = (params != null) ? params.optString("webId", "webView1") : "webView1";
            switch (data.getString("type")) {
                case Constants.ATK_DATA_TYPE_JAVASCRIPT:
                    ATKBridgeManager.excecuteJSCall(webViewId, getData().getString("source"), params.getString("componentId"));
                    break;
                default:
                    if (mAutomaticallyShowProgressHUD && params != null) {
                        ATKWebView webViewWidget = (ATKWebView) ATKComponentManager.getInstance().getComponentById(webViewId);
                        if (webViewWidget != null) {
                            hideProgressHUD(webViewWidget);
                        }
                    }
                    mType = data.getString("type");
                    JSONObject source = data.getJSONObject("source");
                    mItems = source.has("items") ? source.getJSONArray("items") : null;
                    mSections = source.has("sections") ? source.getString("sections") : "";
                    mMoreItems = source.has("moreItems") ? source.getString("moreItems") : "";
                    break;
            }
        } catch (JSONException e) {
            LOGD(TAG, "updateSourceData", e);
        }
    }

    private void showProgressHUD(final ATKWebView webView) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    ATKComponentManager.getInstance().showProgressHUD(mContext, (ViewGroup) webView.getDisplayView(), new JSONObject("{ \"componentId\": \"" + getID() + "\", \"properties\": { \"animated\": true }, \"style\": { \"backgroundColor\": \"#2AA4CD\" } }"));
                } catch (JSONException e) {
                    LOGD(TAG, "ATKCollection showProgressHUD", e);
                }
            }
        }, 100);
    }

    private void hideProgressHUD(ATKWebView webView) {
        ATKComponentManager.getInstance().hideProgressHUD(mContext, (ViewGroup) webView.getDisplayView(), getID());
    }

    public Boolean getLoadMoreButton() { return mLoadMoreButton; }

    public String getLayoutType() { return mLayoutType; }

    public String getScrollDirection() {
        return mScrollDirection;
    }

    public String getSections() {
        return mSections;
    }

    public String getMoreItems() {
        return mMoreItems;
    }

    public JSONObject getItemLayout() {
        return mItemLayout;
    }

    public JSONArray getItems() {
        return mItems;
    }

    public int getHeightHeaderSection() {
        return mHeightHeaderSection;
    }

    public Boolean getShowShadow() { return mShowShadow; }

    public String getHighlightColor() {
        return mHighlightColor;
    }

    public Object getSource() { return mSource;  }

    public String getType() { return mType; }

    public JSONObject getItemLayouts() { return mItemLayouts; }

    public void reloadData(JSONArray items) {}

    public void initCollection() {}

    @Override
    public View getDisplayView() { return null; }

    @Override
    public void setValue(Object attrs, Context context) { }


}
