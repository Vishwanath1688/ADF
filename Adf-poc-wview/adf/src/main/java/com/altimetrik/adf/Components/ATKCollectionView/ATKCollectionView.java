package com.altimetrik.adf.Components.ATKCollectionView;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.altimetrik.adf.Components.ATKCollectionView.ATKGridView.ATKCollectionGrid;
import com.altimetrik.adf.Components.ATKCollectionView.ATKListView.ATKCollectionList;
import com.altimetrik.adf.Components.ATKCollectionView.ATKCoverFlowView.ATKCollectionCover;
import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by esalazar on 4/13/15.
 */
public class ATKCollectionView extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKCollectionView.class);

    private ATKCollectionBase baseView;
    private View tempFutureParent;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        try {
            String collectionType = widgetDefinition.getJSONObject("properties").optString("layoutType",Constants.ATK_COLLECTION_LAYOUT_LIST);
            if (collectionType.equalsIgnoreCase(Constants.ATK_COLLECTION_LAYOUT_COVERFLOW)) {
                baseView = new ATKCollectionCover();
            } else if (collectionType.equalsIgnoreCase(Constants.ATK_COLLECTION_LAYOUT_LIST)) {
                baseView = new ATKCollectionList();
            } else if (collectionType.equalsIgnoreCase(Constants.ATK_COLLECTION_LAYOUT_GRID)) {
                baseView = new ATKCollectionGrid();
            }

            if (tempFutureParent != null) {
                baseView.setFutureParent(tempFutureParent);
            }

            baseView.initWithJSON(widgetDefinition, context);
        } catch (JSONException e) {
            LOGD(TAG, "initWithJSON", e);
        }
        return this;
    }

    @Override
    public int getX() {
        return baseView.getX();
    }

    @Override
    public int getY() {
        return baseView.getY();
    }

    @Override
    public int getWidth() {
        return baseView.getWidth();
    }

    @Override
    public int getHeight() {
        return baseView.getHeight();
    }

    @Override
    public String getID() {
        return baseView.getID();
    }

    @Override
    public String getBackgroundColor() {
        return baseView.getBackgroundColor();
    }

    @Override
    public Boolean getDisabled() {
        return baseView.getDisabled();
    }

    @Override
    public double getBackgroundColorOpacity() {
        return baseView.getBackgroundColorOpacity();
    }

    @Override
    public String getBorderColor() {
        return baseView.getBorderColor();
    }

    @Override
    public double getBorderWidth() {
        return baseView.getBorderWidth();
    }

    @Override
    public int getCornerRadius() {
        return baseView.getCornerRadius();
    }

    @Override
    public String getName() {
        return baseView.getName();
    }

    @Override
    public JSONObject getData() {
        return baseView.getData();
    }

    @Override
    public JSONArray getActions() {
        return baseView.getActions();
    }

    @Override
    public JSONObject getStyle() {
        return baseView.getStyle();
    }

    @Override
    public JSONObject getProperties() {
        return baseView.getProperties();
    }

    @Override
    public View getDisplayView() {
        return baseView.getDisplayView();
    }

    @Override
    public void setValue(Object attrs, Context context) { }

    @Override
    public void resize(int x, int y, int width, int height, Context context) {
       baseView.resize(x, y, width, height, context);
    }

    @Override
    public void setFutureParent(View parent) {
        tempFutureParent = parent;
    }

    public void reloadData(JSONArray items) {  baseView.reloadData(items);  }

    public void updateSourceData(JSONObject data) { baseView.updateSourceData(data); }

    public void initCollection() { baseView.initCollection(); }

    @Override
    public void loadData(final Object data, Context context) {
            ((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
                try {
                    updateSourceData(((JSONObject) data).getJSONObject("data"));
                    initCollection();
                } catch (JSONException e) {
                    LOGD(TAG, "postNotification", e);
                }
            }
        });
    }

    public void removeListItem(JSONArray rows, String animation) {
        if (baseView instanceof ATKCollectionList) {
            ((ATKCollectionList) baseView).removeListItem(rows, animation);
        }
    }
}
