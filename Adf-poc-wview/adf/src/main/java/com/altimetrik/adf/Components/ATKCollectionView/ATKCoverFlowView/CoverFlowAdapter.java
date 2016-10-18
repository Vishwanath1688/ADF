package com.altimetrik.adf.Components.ATKCollectionView.ATKCoverFlowView;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.UIUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.altimetrik.adf.Components.ATKCollectionView.CollectionUtils.SharedFunctions.setWidgetID;
import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

public class CoverFlowAdapter extends PagerAdapter {

    private static final String TAG = makeLogTag(CoverFlowAdapter.class);

    private Context mContext;

    private JSONArray mData;
    private JSONArray mActions;

    private JSONObject mItemLayout;
    private ArrayList<String> mComponentsIdArray;
    private HashMap<String, JSONObject> mComponentMap;  // Map filled with layout information for inner elements
    private String mItemID;

    private ViewPager mViewPager;

    public CoverFlowAdapter(HashMap<String, Object> pagerDataMap, Context context) {
        mContext = context;
        mData = (JSONArray) pagerDataMap.get("data");
        mActions = (JSONArray) pagerDataMap.get("actions");
        mItemLayout = (JSONObject) pagerDataMap.get("layout");
        mItemID = (String) pagerDataMap.get("itemID");
        try {
            loadComponentsMap(mItemLayout.getJSONArray("components"));
        } catch (JSONException e) {
            LOGD(TAG, "CoverFlowAdapter loadComponentsMap", e);
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        RelativeLayout viewGroup = getViewGroup(position);
        container.addView(viewGroup);
        return viewGroup;
    }

    protected RelativeLayout getViewGroup(int position) {
        CoverFlowTile customViewGroup = null;
        try {
            HashMap<String, ATKWidget> componentCellMap = new HashMap<>();
            customViewGroup = new CoverFlowTile(mContext, mComponentMap, mComponentsIdArray, componentCellMap);

            JSONObject jObject = (JSONObject) getItem(position);
            Iterator<?> keys = jObject.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (componentCellMap.containsKey(key)) {
                    ATKWidget widget = componentCellMap.get(key);
                    setWidgetID(widget, "0", String.valueOf(position));
                    widget.setValue((jObject).get(key), mContext);
                }
            }

            String color = mItemLayout.getString("backgroundColor");
            GradientDrawable border = new GradientDrawable();
            if (color != null && color.contains("#")) {
                border.setColor(UIUtils.parseColor(color));
            }
            customViewGroup.setBackground(border);

            JSONObject viewData = new JSONObject();
            viewData.put("id", mItemID);
            viewData.put("section", mData.getJSONObject(0).optString("section_title"));
            viewData.put("selectedIndex", position);
            viewData.put("data", getItem(position));
            loadActions(customViewGroup, viewData, position);
        } catch (JSONException e) {
            LOGD(TAG, "getViewGroup", e);
        }

        return customViewGroup;
    }

    private void loadComponentsMap(JSONArray values) {
        HashMap<String, JSONObject> map = new HashMap<>();
        ArrayList<String> array = new ArrayList<>();
        try {
            for (int i = 0; i < values.length(); i++) {
                JSONObject comp = (JSONObject) values.get(i);
                map.put(comp.getString("bindKey"), comp.getJSONObject("componentJSON"));
                array.add(i, comp.getString("bindKey"));
            }
        } catch (JSONException e) {
            LOGD(TAG, "loadComponentsMap", e);
        }
        mComponentMap = map;
        mComponentsIdArray = array;
    }

    public Object getItem(int i) {
        try {
            return mData.getJSONObject(0).getJSONArray("items").get(i);
        } catch (JSONException e) {
            LOGD(TAG, "getItem", e);
        }
        return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        try {
            return mData.getJSONObject(0).getJSONArray("items").length();
        } catch (JSONException e) {
            LOGD(TAG, "getCount", e);
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    /**
     * Method to load mActions into the views of the collection.
     *  @param v    View to associate the onClickListener
     * @param data JSONObject with necessary data for calling the javascript function
     * @param position
     */
    private void loadActions(View v, final JSONObject data, final int position) {
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActions != null && mActions.length() > 0) {
                    if (mViewPager != null && mViewPager.getCurrentItem() == position) {
                        for (int i = 0; i < mActions.length(); i++) {
                            try {
                                JSONObject action = mActions.getJSONObject(i);
                                if (action.getString("event").equals(Constants.ATK_ACTION_SELECT)) {
                                    ATKEventManager.excecuteComponentAction(action, data);
                                }
                            } catch (JSONException e) {
                                LOGD(TAG, "loadActions onClick", e);
                            }
                        }
                    }
                }
            }
        });
    }

    public void setPager(ViewPager pager) {
        mViewPager = pager;
    }
}
