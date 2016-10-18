package com.altimetrik.adf.Components.ATKCollectionView.ATKGridView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.altimetrik.adf.Components.ATKCollectionView.ATKCollectionBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.tonicartos.superslim.LayoutManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.altimetrik.adf.Components.ATKCollectionView.CollectionUtils.SharedFunctions.JSONToArray;
import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by esalazar on 4/29/15.
 */
public class ATKCollectionGrid extends ATKCollectionBase {

    private static final String TAG = makeLogTag(ATKCollectionGrid.class);

    private RecyclerView mRecycleView;
    private GridViewAdapter mGridViewAdapter;
    private HashMap<String, Object> mDataMap;

    private JSONObject mIndexView;
    private boolean mShowIndex;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {
        super.initWithJSON(widgetDefinition, context);

        mRecycleView = new RecyclerView(context);
        mShowIndex = getProperties().optBoolean("showIndex");
        mIndexView = getStyle().optJSONObject("indexView");

        super.loadParams(mRecycleView);

        initCollection();

        return this;
    }

    @Override
    public void initCollection() {
        mDataMap = new HashMap<>();
        mDataMap.put("itemsData", JSONToArray(getItems()));

        try {
            if (!"".equals(getSections())) {
                mDataMap.put("sections", getSections());
                mDataMap.put("moreItems", getMoreItems());
                if(getItemLayouts() != null){
                    mDataMap.put("itemLayouts", getItemLayouts());
                }else{
                    mDataMap.put("itemLayout", getItemLayout());
                }
                mDataMap.put("actions", getActions());
                mDataMap.put("itemID", getID());

                if(getStyle().has("sectionHeader")) {
                    JSONObject sectionHeader = getStyle().getJSONObject("sectionHeader");
                    mDataMap.put("sectionHeader", sectionHeader);
                }
                if(getStyle().has("indexView"))
                    mDataMap.put("indexView", getStyle().getJSONObject("indexView"));
                if(getStyle().has("sectionFooter"))
                    mDataMap.put("sectionFooter", getStyle().getJSONObject("sectionFooter"));
                if(getStyle().has("itemBorder"))
                    mDataMap.put("itemBorder", getStyle().getJSONObject("itemBorder"));
                if(getStyle().has("numberOfColumns"))
                    mDataMap.put("numberOfColumns", getStyle().getInt("numberOfColumns"));
                mDataMap.put("gridViewWidth", mRecycleView.getLayoutParams().width);
                mDataMap.put("highlightColor", getHighlightColor());
            }
        } catch (JSONException e) {
            LOGD(TAG, "dataMap", e);
        }

        if (!getType().equals("javascript")) {
            mGridViewAdapter = new GridViewAdapter(mContext, mDataMap);
            mRecycleView.setLayoutManager(new LayoutManager(mContext));
            mRecycleView.setAdapter(mGridViewAdapter);
        }
    }

    @Override
    public View getDisplayView() {
        return mRecycleView;
    }

    @Override
    public void reloadData(JSONArray items) {
        mGridViewAdapter.setItemsData(JSONToArray(items));
        mGridViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void clean() {
        mRecycleView.removeAllViews();
        mRecycleView = null;
        super.clean();
    }
}
