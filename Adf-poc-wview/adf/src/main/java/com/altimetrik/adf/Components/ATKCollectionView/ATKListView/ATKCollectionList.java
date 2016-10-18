package com.altimetrik.adf.Components.ATKCollectionView.ATKListView;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

import com.altimetrik.adf.Components.ATKCollectionView.ATKCollectionBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.R;
import com.altimetrik.adf.Util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static com.altimetrik.adf.Components.ATKCollectionView.CollectionUtils.SharedFunctions.JSONToArray;
import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by esalazar on 4/29/15.
 */
public class ATKCollectionList extends ATKCollectionBase {

    private static final String TAG = makeLogTag(ATKCollectionList.class);

    private ListView mListView;
    private ListViewAdapter mListViewAdapter;
    private HashMap<String, Object> mDataMap;

    private JSONObject mIndexView;
    private boolean mShowIndex;
    private boolean mDisableStickyHeaders;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {
        super.initWithJSON(widgetDefinition, context);

        mShowIndex = getProperties().optBoolean("showIndex");
        mDisableStickyHeaders = getStyle().optBoolean("disableStickyHeaders", false);
        mIndexView = getStyle().optJSONObject("indexView");

        if (mDisableStickyHeaders) {
            mListView = new ListView(context);
        } else {
            mListView = new PinnedSectionListView(context);
        }

        if (mShowIndex) {
            mListView.setFastScrollEnabled(true);
            mListView.setFastScrollAlwaysVisible(true);
        }

        mListView.setDividerHeight(0);
        mListView.setDivider(null);

        super.loadParams(mListView);

        initCollection();

        /*mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View rowView, int positon, long id) {
                List<Integer> positions = new ArrayList<>(Arrays.asList(positon, positon + 1));
                removeListItem(positions, "slideToLeft");
            }
        });*/

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
                if (getItemLayouts() != null) {
                    mDataMap.put("itemLayouts", getItemLayouts());
                } else {
                    mDataMap.put("itemLayout", getItemLayout());
                }
                mDataMap.put("actions", getActions());
                mDataMap.put("itemID", getID());

                if (getStyle().has("sectionHeader")) {
                    JSONObject sectionHeader = getStyle().getJSONObject("sectionHeader");
                    mDataMap.put("sectionHeader", sectionHeader);
                }
                if (mItemSeparator != null) {
                    mDataMap.put("itemSeparator", mItemSeparator);
                }
                if (getStyle().has("indexView"))
                    mDataMap.put("indexView", getStyle().getJSONObject("indexView"));
                if (getStyle().has("sectionFooter"))
                    mDataMap.put("sectionFooter", getStyle().getJSONObject("sectionFooter"));
                if (getStyle().has("itemBorder"))
                    mDataMap.put("itemBorder", getStyle().getJSONObject("itemBorder"));
                mDataMap.put("listViewWidth", mListView.getLayoutParams().width);
                mDataMap.put("highlightColor", getHighlightColor());
            }
        } catch (JSONException e) {
            LOGD(TAG, "dataMap", e);
        }

        if (!getType().equals("javascript")) {
            mListViewAdapter = new ListViewAdapter(mContext, mDataMap, mShowIndex);
            mListView.setAdapter(mListViewAdapter);
        }
    }

    private void removeListItem(final List<Integer> positions, String animation) {
        if (positions != null) {
            Collections.sort(positions, new Comparator<Integer>() {
                @Override
                public int compare(Integer lhs, Integer rhs) {
                    return lhs.compareTo(rhs) * -1;
                }
            });

            for (int position : positions) {
                removeListItem(position, animation);
            }
        }
    }

    private void removeListItem(final int position, String animation) {
        final int firstListItemPosition = mListView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + mListView.getChildCount() - 1;

        if (position >= firstListItemPosition && position <= lastListItemPosition) {
            final int childIndex = position - firstListItemPosition;
            View child = mListView.getChildAt(childIndex);
            if (child != null) {
                Animation anim = null;
                switch (animation) {
                    case "slideToRight":
                        anim = AnimationUtils.loadAnimation( mContext, android.R.anim.slide_out_right);
                        break;
                    case "slideToLeft":
                        anim = AnimationUtils.loadAnimation( mContext, R.anim.slide_to_left);
                        break;
                }
                if (anim != null) {
                    child.startAnimation(anim);
                }
            }
        }

        mListView.postDelayed(new Runnable() {
            public void run() {
                mListViewAdapter.remove(position);
            }
        }, mContext.getResources().getInteger(android.R.integer.config_mediumAnimTime));
    }

    @Override
    public View getDisplayView() {
        return mListView;
    }

    @Override
    public void reloadData(JSONArray items) {
        mListViewAdapter.setItemsData(JSONToArray(items));
        mListViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void resize(int x, int y, int width, int height, Context context) {
        mListViewAdapter.setListViewWidth((int) (width * Utils.getDeviceDensity(context)));
        super.resize(x, y, width, height, context);
    }

    public void removeListItem(JSONArray rows, String animation) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < rows.length(); i++) {
            try {
                JSONObject row = rows.getJSONObject(i);
                if (mListViewAdapter.hasSections()) {
                    list.add(mListViewAdapter.getPositionForSection(row.getInt("section")) + row.getInt("row") + 1);
                } else {
                    list.add(row.getInt("row"));
                }
            } catch (JSONException e) {
                LOGE(TAG, "removeListItem index " + i, e);
            }
        }
        removeListItem(list, animation);
    }
}
