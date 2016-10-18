package com.altimetrik.adf.Components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.altimetrik.adf.Util.Measurement;
import com.altimetrik.adf.Util.UIUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;
import static com.altimetrik.adf.Util.Utils.getDeviceDensity;

/**
 * Created by pigounet on 3/26/15.
 */
public abstract class ATKComponentBase implements ATKWidget {

    private static final String TAG = makeLogTag(ATKComponentBase.class);

    protected JSONArray mComponentsArray;
    protected Context mContext;

    private String mId;
    private String mNotificationId;

    protected String mStringX;
    protected String mStringY;
    protected String mStringWidth;
    protected String mStringHeight;

    private int mX;
    private int mY;
    private int mWidth;
    private int mHeight;

    protected Boolean mHidden;
    private Boolean mDisabled;
    private String mBackgroundColor;
    private double mBackgroundColorOpacity;
    private String mBorderColor;
    private double mBorderWidth;
    private int mCornerRadius;

    private String mName;
    protected JSONObject mData;
    private JSONArray mActions;
    private JSONObject mStyle;
    private JSONObject mProperties;
    private View mFutureParent;

    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {
        try {
            //Mandatory First level attributes
            mContext = context;
            mData = widgetDefinition.optJSONObject("data");
            mActions = widgetDefinition.has("actions") ? widgetDefinition.getJSONArray("actions") : null;
            mStyle = widgetDefinition.getJSONObject("style");
            mProperties = widgetDefinition.getJSONObject("properties");
            mId = widgetDefinition.getString("id");
            mName = widgetDefinition.getString("name");

            //Optional First Level attributes
            mComponentsArray = widgetDefinition.has("components") ? widgetDefinition.getJSONArray("components") : null;
            mNotificationId = widgetDefinition.optString("notificationId");

            //Optional Style attributes
            mBorderWidth = getStyle().optInt("borderWidth", 0);
            mBackgroundColor = getStyle().optString("backgroundColor");
            mBackgroundColorOpacity = getStyle().optDouble("backgroundColorOpacity", 1);
            mBorderColor = getStyle().optString("borderColor");
            mCornerRadius = getStyle().optInt("cornerRadius", 0);

            //Properties attributes
            mDisabled = getProperties().optBoolean("disabled");

            mHidden = getProperties().optBoolean("hidden");

            //Mandatory Style attributes
            Object oX = getStyle().opt("x");
            Object oY = getStyle().opt("y");
            mStringX = oX != null ? oX.toString() : "0";
            mStringY = oY != null ? oY.toString() : "0";

            Object oW = getStyle().opt("width");
            Object oH = getStyle().opt("height");
            mStringWidth = oW != null ? oW.toString() : "0";
            mStringHeight = oH != null ? oH.toString() : "0";

        } catch (JSONException ex) {
            LOGE(TAG, "initWithJSON", ex);
        }
        return this;
    }

    public void onPostInitWithJSON() {
        if (mHidden && getDisplayView() != null) {
            getDisplayView().setVisibility(View.INVISIBLE);
        }
    }

    public abstract View getDisplayView();

    public void loadParams(View v) {
        try {
            updateViewSize(v);

            float density = getDeviceDensity(mContext);
            GradientDrawable border = new GradientDrawable();
            if (getBackgroundColor() != null && !getBackgroundColor().isEmpty()) {
                if (getBackgroundColor().contains("#")) {
                    if (getBackgroundColorOpacity() != 1) {
                        String bgColor = UIUtils.addAlpha(getBackgroundColor(), getBackgroundColorOpacity());
                        border.setColor(UIUtils.parseColor(bgColor));
                    } else {
                        border.setColor(UIUtils.parseColor(getBackgroundColor()));
                    }
                } else {
                    border.setColor(UIUtils.parseColor(getBackgroundColor()));
                }
            } else {
                //Transparent color for 4.1 Android Version
                border.setColor(UIUtils.parseColor("#00FFFFFF"));
            }

            if (!getBorderColor().equals("") && getBorderWidth() != 0) {
                border.setStroke((int) Math.round(getBorderWidth() * density), UIUtils.parseColor(getBorderColor()));
            }
            border.setCornerRadius((float) (getCornerRadius() * 10.0));
            v.setBackground(border);
            v.setPadding(0, 0, 0, 0);
            v.setEnabled(!getDisabled());

        } catch (Exception e) {
            LOGD(TAG, "loadParams", e);
        }
    }

    public void updateViewSize(View v) {
        View parent = (View) v.getParent();
        if (parent == null)  parent = mFutureParent;

        int[] frame = Measurement.generateMeasurement(mStringX, mStringY, mStringWidth, mStringHeight, mContext, parent);
        mX = frame[0];
        mY = frame[1];
        mWidth = frame[2];
        mHeight = frame[3];

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(getWidth(), getHeight());
        params.setMargins(0, 0, 0, 0);
        v.setLayoutParams(params);
    }

    @Override
    public void resize(int x, int y, int width, int height, Context context) {
        mContext = context;
        mStringX = String.valueOf(x);
        mStringY = String.valueOf(y);
        mStringWidth = String.valueOf(width);
        mStringHeight = String.valueOf(height);

        View view = getDisplayView();
        if (view != null) {
            int[] frame = Measurement.generateMeasurement(mStringX, mStringY, mStringWidth, mStringHeight, context, (View) view.getParent());
            mX = frame[0];
            mY = frame[1];
            mWidth = frame[2];
            mHeight = frame[3];

            view.setX(mX);
            view.setY(mY);
            view.getLayoutParams().width = mWidth;
            view.getLayoutParams().height = mHeight;
            view.requestLayout();
        }
    }

    public void setFutureParent(View parent) {
        mFutureParent = parent;
    }

    public void loadData(Object data, Context context) { }

    //region This methods are needed for MapView
    public void onResume() { }

    public void onPause() { }

    public void onDestroy() { }
    //endregion

    public void clean() {
        mContext = null;
    }

    public int getX() {
        return mX;
    }

    public int getY() {
        return mY;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public String getID() {
        return mId;
    }

    public String getBackgroundColor() {
        return mBackgroundColor;
    }

    public Boolean getDisabled() {
        return mDisabled;
    }

    public double getBackgroundColorOpacity() {
        return mBackgroundColorOpacity;
    }

    public String getBorderColor() {
        return mBorderColor;
    }

    public double getBorderWidth() {
        return mBorderWidth;
    }

    public int getCornerRadius() {
        return mCornerRadius;
    }

    public String getName() {
        return mName;
    }

    public @Nullable JSONObject getData() {
        return mData;
    }

    public JSONArray getActions() {
        return mActions;
    }

    public JSONObject getStyle() {
        return mStyle;
    }

    public JSONObject getProperties() {
        return mProperties;
    }

    public void setID(String value) {
        mId = value;
    }

    public void setEnabled (boolean enabled){
        if (getDisplayView() != null) {
            getDisplayView().setEnabled(enabled);
        }
    }

}

