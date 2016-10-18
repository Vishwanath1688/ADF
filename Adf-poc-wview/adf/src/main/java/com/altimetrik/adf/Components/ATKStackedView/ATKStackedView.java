package com.altimetrik.adf.Components.ATKStackedView;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Util.UIUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 7/2/15.
 */
public class ATKStackedView extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKStackedView.class);

    private ViewGroup mContainerView;
    private LinearLayout mStackedView;

    //Properties
    private String mScrollDirection;

    //Style
    private String mBackgroundColor;

    private JSONArray mComponents;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        //Load base params --> super
        super.initWithJSON(widgetDefinition, context);

        //Load Properties
        mScrollDirection = getProperties().optString("scrollDirection");

        //Load style
        mBackgroundColor = getStyle().optString("backgroundColor");

        mComponents = widgetDefinition.optJSONArray("components");

        mStackedView = new LinearLayout(context);

        if (mScrollDirection.equals("vertical")) {
            mStackedView.setOrientation(LinearLayout.VERTICAL);
            mContainerView = new ScrollView(context);
        } else {
            mStackedView.setOrientation(LinearLayout.HORIZONTAL);
            mContainerView = new HorizontalScrollView(context);
        }

        loadParams(mContainerView);
        loadParams(mStackedView);

        if (!mBackgroundColor.isEmpty()) {
            mStackedView.setBackgroundColor(UIUtils.parseColor(mBackgroundColor));
        }

        for (int i = 0; i < mComponents.length(); i++) {
            ATKComponentBase widget;
            try {

                //create component and resize it using it's parent
                JSONObject currentComponent =  mComponents.getJSONObject(i);
                widget = (ATKComponentBase) ATKComponentManager.getInstance().presentComponentInView(context, mStackedView, currentComponent);

                if (mScrollDirection.equals("vertical")) {
                    widget.getDisplayView().setPadding(0, widget.getY(), 0, 0);
                    widget.getDisplayView().setY(0);
                } else {
                    widget.getDisplayView().setPadding(widget.getX(), 0, 0, 0);
                    widget.getDisplayView().setX(0);
                }

            } catch (JSONException e) {
                LOGD(TAG, "initWithJSON", e);
            }
        }

        mContainerView.addView(mStackedView);
        mStackedView.setFocusable(true);
        mStackedView.setFocusableInTouchMode(true);
        mStackedView.setMotionEventSplittingEnabled(false);

        onPostInitWithJSON();

        return this;
    }

    @Override
    public View getDisplayView() {
        return mContainerView;
    }

    @Override
    public void setValue(Object attrs, Context context) {  }

    @Override
    public void clean() {
        mContainerView.removeAllViews();
        mContainerView = null;
        super.clean();
    }
}
