package com.altimetrik.adf.Components.ATKCollectionView.ATKListView;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;

import com.altimetrik.adf.Components.ATKCollectionView.CollectionUtils.SharedFunctions.ItemTag;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.Measurement;
import com.altimetrik.adf.Util.UIUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.altimetrik.adf.Components.ATKCollectionView.CollectionUtils.SharedFunctions.assembleHeaderText;
import static com.altimetrik.adf.Components.ATKCollectionView.CollectionUtils.SharedFunctions.loadComponentsMap;
import static com.altimetrik.adf.Components.ATKCollectionView.CollectionUtils.SharedFunctions.setHeaderTitleBorders;
import static com.altimetrik.adf.Components.ATKCollectionView.CollectionUtils.SharedFunctions.setWidgetID;
import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGI;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;
import static com.altimetrik.adf.Util.Utils.getDeviceDensity;

/**
 * Created by esalazar on 4/29/15.
 */
public class ListViewAdapter extends ArrayAdapter<ListItem> implements SectionIndexer, PinnedSectionListView.PinnedSectionListAdapter, View.OnTouchListener, View.OnClickListener {

    private static final String TAG = makeLogTag(ListViewAdapter.class);

    private static final int UNDEFINED_LAYOUT = -1;
    private static final String SEPARATOR_LINE_ID = "separator_line";

    public static final int DISABLE_CLICK_FOR_MILLIS = 500;

    private Context mContext;

    private boolean mMultipleLayouts;
    private boolean mHasIndex;
    private int mListViewWidth;
    private String mHighlightColor;

    private String mItemID;
    private ArrayList<ListItem> mItemsData;
    private String mDefaultLayout;
    private JSONObject mItemSeparator;

    private JSONArray mActions;
    private HashMap<String, Integer> mTypesHash;
    private HashMap<String, Integer> mSectionMap;
    private ArrayList<String> mSections;
    protected JSONObject mItemLayout;
    protected LinkedHashMap mComponentMap;  // Map filled with layout information for inner elements
    protected JSONObject mSectionHeader;

    private long mLastClickTime = 0;

    public ListViewAdapter(Context context, HashMap<String, Object> dataMap, boolean hasIndex) {
        super(context, 0);

        mContext = context;
        mHasIndex = hasIndex;
        mItemID = (String) dataMap.get("itemID");
        mActions = (JSONArray) dataMap.get("actions");

        if (dataMap.get("itemsData") != null) {
            mItemsData = (ArrayList<ListItem>) dataMap.get("itemsData");
        } else {
            mItemsData = new ArrayList<>();
        }

        mItemSeparator = (JSONObject) dataMap.get("itemSeparator");

        if (dataMap.get("itemLayout") != null) {
            mItemLayout = (JSONObject) dataMap.get("itemLayout");
            mMultipleLayouts = false;
        } else {
            mItemLayout = (JSONObject) dataMap.get("itemLayouts");
            mMultipleLayouts = true;
        }

        if (dataMap.get("sectionHeader") != null) {
            mSectionHeader = (JSONObject) dataMap.get("sectionHeader");
        }

        if (dataMap.get("listViewWidth") != null)
            mListViewWidth = (int) dataMap.get("listViewWidth");

        mHighlightColor = (String) dataMap.get("highlightColor");

        HashMap<String, Object> layoutMap = loadComponentsMap(mItemLayout, mMultipleLayouts);
        mDefaultLayout = (String) layoutMap.get("defaultLayout");
        mComponentMap = new LinkedHashMap((Map) layoutMap.get("layoutDefinition"));

        mSectionMap = new HashMap<>();
        mSections = new ArrayList<>();

        if (dataMap.get("sections") != null) {
            for (int i = 0; i < mItemsData.size(); i++) {
                if (mItemsData.get(i).isSection()) {
                    mSections.add(mItemsData.get(i).getSectionName());
                    mSectionMap.put(mItemsData.get(i).getSectionName(), i);
                }
            }
        }

        recomputeViewTypes();
    }

    public void setItemsData(ArrayList<ListItem> itemsData) {
        mItemsData = itemsData;

        mSections.clear();
        mSectionMap.clear();

        for (int i = 0; i < itemsData.size(); i++) {
            if (itemsData.get(i).isSection()) {
                mSections.add(mItemsData.get(i).getSectionName());
                mSectionMap.put(itemsData.get(i).getSectionName(), i);
            }
        }
        recomputeViewTypes();
    }

    @Override
    public int getCount() {
        return mItemsData.size();
    }

    @Override
    public ListItem getItem(int position) {
        return mItemsData.get(position);
    }

    public JSONArray getActions() {
        return mActions;
    }

    private void recomputeViewTypes() {
        int typeCount = 1;
        mTypesHash = new HashMap<>();
        for (Object key : mComponentMap.keySet()) {
            mTypesHash.put((String) key, typeCount);
            typeCount++;
        }
        if (!mDefaultLayout.isEmpty()) {
            int defaultLayout = mTypesHash.get(mDefaultLayout);
            mTypesHash.put("defaultLayout", defaultLayout);
        }
    }

    /**
     * Discerns whether the type of the item is a Section or an Item object
     *
     * @param position
     * @return Return 0 if the item is section, or 1 if it is a data object
     */
    @Override
    public int getItemViewType(int position) {
        if (mItemsData.size() > position) {
            ListItem item = mItemsData.get(position);
            if (item.isSection()) {
                return 0;
            } else {
                String itemLayout = item.getItemData().optString("layout");
                if (!itemLayout.isEmpty()) {
                    if (mTypesHash.containsKey(itemLayout)) {
                        return mTypesHash.get(itemLayout);
                    } else {
                        return mTypesHash.get("defaultLayout");
                    }
                } else {
                    if (!mDefaultLayout.isEmpty()) {
                        return mTypesHash.get("defaultLayout");
                    }
                }
            }
        } else {
            return 0;
        }
        return UNDEFINED_LAYOUT;
    }

    @Override
    public int getViewTypeCount() {
        return mTypesHash.size() + 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ListItem listItem = getItem(position);
        ListViewItem itemView;
        ItemTag itemTag = null;

        if (convertView != null)
            itemTag = (ItemTag) convertView.getTag();

        String sectionName = listItem.getSectionName();
        String paramItemLayout = "";
        String currentSectionName = "";
        String currentItemLayout = "";

        if (!listItem.isSection())
            paramItemLayout = listItem.getItemData().optString("layout");

        if (itemTag != null && itemTag.getSectionName() != null && itemTag.getData() != null) {
            currentSectionName = itemTag.getSectionName();
            currentItemLayout = itemTag.getData().optString("layout");
        }

        if (convertView == null || !currentSectionName.equals(sectionName) || !currentItemLayout.equals(paramItemLayout) || getItemViewType(position) == UNDEFINED_LAYOUT) {

            long startGetViewTime = System.nanoTime();

            itemView = new ListViewItem(mContext);
            itemView.setOnClickListener(this);
            if (!listItem.isSection()) {
                itemView.setOnTouchListener(this);
            }

            HashMap<String, Object> componentData = null;
            if (!mMultipleLayouts) {
                componentData = mComponentMap;
            } else {
                if (listItem.isSection()) {
                    componentData = assembleHeaderText(sectionName, mListViewWidth, mSectionHeader);
                } else if (!paramItemLayout.isEmpty()) {
                    componentData = (HashMap<String, Object>) mComponentMap.get(paramItemLayout);
                } else if (mComponentMap.get(sectionName) != null) {
                    componentData = (HashMap<String, Object>) mComponentMap.get(sectionName);
                } else {
                    componentData = (HashMap<String, Object>) mComponentMap.get(mDefaultLayout);
                }
            }
            try {
                int[] layoutValues = null;
                if (!mMultipleLayouts) {
                    layoutValues = Measurement.generateMeasurement("", "", String.valueOf(mItemLayout.get("width")), String.valueOf(mItemLayout.get("height")), mContext, null);
                } else {
                    JSONObject layout;
                    if (!paramItemLayout.isEmpty()) {
                        layout = (JSONObject) mItemLayout.get(paramItemLayout);
                    } else if (sectionName != null && mItemLayout.has(sectionName)) {
                        layout = (JSONObject) mItemLayout.get(sectionName);
                    } else {
                        layout = (JSONObject) mItemLayout.get(mDefaultLayout);
                    }
                    if (listItem.isSection()) { // In case it is a section, it has its own width/height
                        float density = getDeviceDensity(mContext);
                        for (Object entry : componentData.entrySet()) {
                            Map.Entry dataEntry = (Map.Entry) entry;
                            layout = ((JSONObject) dataEntry.getValue()).getJSONObject("style");
                            layoutValues = Measurement.generateMeasurement("", "", String.valueOf(layout.get("width")), String.valueOf(layout.get("height")), mContext, null);
                            layoutValues[2] = (int) (layoutValues[2] / density);
                        }
                    } else {
                        layoutValues = Measurement.generateMeasurement("", "", String.valueOf(layout.get("width")), String.valueOf(layout.get("height")), mContext, null);
                    }
                }
                itemView.setLayoutParams(new AbsListView.LayoutParams(layoutValues[2], layoutValues[3]));
            } catch (JSONException e) {
                LOGE(TAG, "getView", e);
            }
            LOGI(TAG, "creating a new ListViewItem time " + (System.nanoTime() - startGetViewTime) / 1E6);

            itemView.drawInnerElements(mContext, componentData);
        } else {
            itemView = (ListViewItem) convertView;
        }

        long startGetViewTime = System.nanoTime();
        JSONObject data = listItem.getItemData();

        if (!listItem.isSection()) {
            try {

                if (mItemSeparator != null) {

                    //Create separator if doesn't exist
                    RelativeLayout separatorView = (RelativeLayout) itemView.findViewWithTag(SEPARATOR_LINE_ID);
                    if (separatorView == null) {
                        separatorView = new RelativeLayout(mContext);
                        separatorView.setTag(SEPARATOR_LINE_ID);

                        separatorView.setBackgroundColor(UIUtils.parseColor(mItemSeparator.optString("color", "#FFFFFF")));
                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int) mItemSeparator.optDouble("height", 1));
                        params.gravity = Gravity.BOTTOM;

                        JSONObject itemSeparatorPadding = mItemSeparator.optJSONObject("padding");
                        if (itemSeparatorPadding != null) {
                            int itemSeparatorPaddingLeft = itemSeparatorPadding.optInt("left", 0);
                            int itemSeparatorPaddingRight = itemSeparatorPadding.optInt("right", 0);
                            params.setMargins(itemSeparatorPaddingLeft, 0, itemSeparatorPaddingRight, 0);
                        }

                        itemView.addView(separatorView, params);
                    }

                    //Hide separator of the last item before a section
                    separatorView.setVisibility(View.VISIBLE);
                    if (mSections.size() > 0) {
                        int nextSection = mSections.indexOf(sectionName) + 1;
                        if (nextSection < mSections.size()) {
                            int nextSectionPosition = mSectionMap.get(mSections.get(nextSection));
                            if (position == (nextSectionPosition - 1)) {
                                separatorView.setVisibility(View.GONE);
                            }
                        }
                    }
                }

                HashMap<String, ATKWidget> itemMap = itemView.getComponentMap();
                Iterator<String> keys = itemMap.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();

                    ATKWidget widget = itemView.getComponentMap().get(key);

                    if (mSections.size() > 0) {
                        Integer currentSectionPosition = mSectionMap.get(sectionName);
                        Integer currentSection = mSections.indexOf(sectionName);
                        String sectionPosition = String.valueOf(position - (currentSectionPosition + 1));

                        setWidgetID(widget, String.valueOf(currentSection), sectionPosition);
                    } else {
                        setWidgetID(widget, "0", String.valueOf(position));
                    }

                    if (data.has(key)) {
                        widget.setValue(data.get(key), mContext);
                    } else {
                        LOGD(TAG, key + " key not found");
                    }
                }
            } catch (JSONException e) {
                LOGE(TAG, "", e);
            }
            LOGI(TAG, "common item time " + (System.nanoTime() - startGetViewTime) / 1E6);
        } else {

            for (Object entry : itemView.getComponentMap().entrySet()) {
                Map.Entry dataEntry = (Map.Entry) entry;
                ATKWidget widget = (ATKWidget) dataEntry.getValue();
                widget.setValue((getItem(position)).getSectionName(), mContext);
                setHeaderTitleBorders(widget.getDisplayView(), mSectionHeader, mContext);
            }
            LOGI(TAG, "section item time " + (System.nanoTime() - startGetViewTime) / 1E6);
        }
        itemView.setTag(new ItemTag(sectionName, data));
        return itemView;
    }

    @Override
    public Object[] getSections() {
        if (mHasIndex) {
            mSections.clear();
            for (int i = 0; i < mItemsData.size(); i++) {
                if (mItemsData.get(i).isSection()) {
                    mSections.add(mItemsData.get(i).getSectionName());
                    mSectionMap.put(mItemsData.get(i).getSectionName(), i);
                }
            }
            String[] sections = new String[mSections.size()];
            for (int i = 0; i < mSections.size(); i++)
                sections[i] = mSections.get(i);
            return sections;
        }
        return new Object[]{};
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        int positionForSection = 1;
        if (sectionIndex < mSections.size()) {
            String section = mSections.get(sectionIndex);
            positionForSection = mSectionMap.get(section);
        }
        return positionForSection;
    }

    @Override
    public int getSectionForPosition(int position) {
        int index = 1;
        try {
            ListItem item = mItemsData.get(position);
            index = mSections.indexOf(item.getSectionName());
            if (index >= 0) {
                return index;
            }
        } catch (Exception e) {
            LOGE(TAG, "Error obtaining section", e);
        }
        return index;
    }

    public void setListViewWidth(int listViewWidth) {
        mListViewWidth = listViewWidth;
    }

    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == 0;
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < DISABLE_CLICK_FOR_MILLIS) {
            LOGI(TAG, "Clicks too close");
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        if (getActions() != null) {
            ItemTag dataTag = (ItemTag) v.getTag();
            if (dataTag != null && dataTag.getData() != null) {
                try {
                    for (int i = 0; i < getActions().length(); i++) {
                        JSONObject action = getActions().getJSONObject(i);
                        if (action.getString("event").compareTo(Constants.ATK_ACTION_SELECT) == 0) {
                            JSONObject payload = new JSONObject();
                            payload.put("id", mItemID);
                            payload.put("data", dataTag.getData());

                            ATKEventManager.excecuteComponentAction(action, payload);
                        }
                    }
                } catch (JSONException e) {
                    LOGD(TAG, "ListItemOnClickListener onClick", e);
                }
            }
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean hasActionSelect = false;
        try {
            for (int i = 0; i < getActions().length(); i++) {
                JSONObject action = getActions().getJSONObject(i);
                if (action.getString("event").equals(Constants.ATK_ACTION_SELECT)) {
                    hasActionSelect = true;
                    break;
                }
            }
        } catch (JSONException e) {
            LOGD(TAG, "ListItemOnTouchListener onTouch", e);
        }

        if (hasActionSelect) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.setBackgroundColor(UIUtils.parseColor(mHighlightColor));
                v.invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                v.setBackgroundColor(Color.TRANSPARENT);
                v.invalidate();
            }
        }

        return false;
    }

    synchronized public void remove(int position) {
        mItemsData.remove(position);
        setItemsData(mItemsData);
        notifyDataSetChanged();
    }

    public boolean hasSections() {
        return mSections.size() > 0;
    }
}
