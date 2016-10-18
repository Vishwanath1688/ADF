package com.altimetrik.adf.Components.ATKScrollView;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;
import static com.altimetrik.adf.Util.Utils.getDeviceDensity;

/**
 * Created by icabanas on 5/4/15.
 */
public class ATKScrollViewAdapter extends PagerAdapter {

    private static final String TAG = makeLogTag(ATKScrollViewAdapter.class);

    private Context mContext;
    private JSONArray mComponents;
    private List<ViewGroup> mViewGroups;
    private RelativeLayout mScrollView;
    private int mSize;

    public ATKScrollViewAdapter(RelativeLayout scrollView, JSONArray components, Context context, int size) {
        mContext = context;
        mComponents = components;
        mViewGroups = new ArrayList<>();
        mSize = size;
        mScrollView = scrollView;

        //Create a relative layout for each page
        for (int i = 0; i < mSize; i++) {
            RelativeLayout page = new RelativeLayout(mContext);
            page.setLayoutParams(new RelativeLayout.LayoutParams(mScrollView.getLayoutParams().width, mScrollView.getLayoutParams().height));
            mViewGroups.add(page);
        }

        //Create components and add them to their corresponding page using it's position
        for (int i = 0; i < components.length(); i++) {
            ATKWidget widget;
            try {
                JSONObject currentComponent = mComponents.getJSONObject(i);
                int page = getComponentPage(currentComponent);
                widget = ATKComponentManager.getInstance().presentComponentInView(mContext, mViewGroups.get(page), currentComponent);

                widget.getDisplayView().setX(widget.getX());
                widget.getDisplayView().setY(widget.getY());

            } catch (JSONException e) {
                LOGE(TAG, "ATKScrollViewAdapter JSONException", e);
            } catch (Exception e) {
                LOGE(TAG, "ATKScrollViewAdapter Exception", e);
            }
        }
    }

    @Override
    public int getCount() {
        return mSize;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ViewGroup viewGroup = mViewGroups.get(position);
        container.addView(viewGroup);
        return viewGroup;
    }

    public int getComponentPage(JSONObject component) {
        int page = 0;
        float density = getDeviceDensity(mContext);
        int scrollviewWidth = (int) (mScrollView.getLayoutParams().width / density);
        try {
            String componentPosition = component.getJSONObject("style").getString("x");
            if (componentPosition.contains("%")) {
                double componentPositionPercentage = Double.parseDouble(componentPosition.substring(0, componentPosition.indexOf("%")));
                Double calculatedPage = (componentPositionPercentage / 100.0);
                page = calculatedPage.intValue();
                String percentageForPage = (componentPositionPercentage - 100 * page) + "%";
                component.getJSONObject("style").put("x", percentageForPage);
            } else {
                page = (int) Float.parseFloat(componentPosition) / scrollviewWidth;
                int widgetX = (int) Float.parseFloat(componentPosition) - (scrollviewWidth * page);
                component.getJSONObject("style").put("x", widgetX);
            }
        } catch (JSONException e) {
            LOGD(TAG, "getComponentPage", e);
        }
        return page;
    }

}