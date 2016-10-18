package com.altimetrik.adf.Components.ATKCollectionView.ATKGridView;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.altimetrik.adf.Components.ATKCollectionView.ATKListView.ListItem;
import com.altimetrik.adf.Components.ATKCollectionView.CollectionUtils.SharedFunctions.ItemTag;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.UIUtils;
import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LayoutManager;

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
import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;
import static com.altimetrik.adf.Util.Utils.getDeviceDensity;

/**
 * Created by esalazar on 8/18/15.
 */
public class GridViewAdapter extends RecyclerView.Adapter<GridViewHolder> implements View.OnClickListener, View.OnTouchListener {

    private static final String TAG = makeLogTag(GridViewAdapter.class);

    private static final int UNDEFINED_LAYOUT = -1;

    private String mItemID;
    private int mGridViewWidth;
    private int mNumberOfColumns;
    private boolean mMultipleLayouts;
    private String mDefaultLayout;
    private String mHighlightColor;

    protected JSONObject mItemLayout;
    protected LinkedHashMap mComponentMap;
    protected JSONObject mSectionHeader;
    private ArrayList<ListItem> mItemsData;
    private Context mContext;
    private JSONArray mActions;
    private HashMap<String, Integer> mTypesHash;
    private HashMap<String, Integer> mSectionMap;
    private ArrayList<String> mSections;
    private ArrayList<LineItem> mItems;


    public GridViewAdapter(Context context, HashMap<String, Object> dataMap) {

        if (dataMap.get("itemsData") != null) {
            mItemsData = (ArrayList<ListItem>) dataMap.get("itemsData");
            mActions = (JSONArray) dataMap.get("actions");
            mItemID = (String) dataMap.get("itemID");
            mContext = context;

            if (dataMap.get("itemLayout") != null) {
                mItemLayout = (JSONObject) dataMap.get("itemLayout");
                mMultipleLayouts = false;
            } else {
                mItemLayout = (JSONObject) dataMap.get("itemLayouts");
                mMultipleLayouts = true;
            }
            if (dataMap.get("sectionHeader") != null)
                mSectionHeader = (JSONObject) dataMap.get("sectionHeader");

            if (dataMap.get("gridViewWidth") != null)
                mGridViewWidth = (int) dataMap.get("gridViewWidth");
            if (dataMap.get("numberOfColumns") != null) {
                mNumberOfColumns = (int) dataMap.get("numberOfColumns");
            }else{
                mNumberOfColumns = 2;
            }

            mHighlightColor = (String) dataMap.get("highlightColor");

            HashMap<String, Object> layoutMap = loadComponentsMap(mItemLayout, mMultipleLayouts);
            mDefaultLayout = (String) layoutMap.get("defaultLayout");
            mComponentMap = new LinkedHashMap((Map) layoutMap.get("layoutDefinition"));

            mSectionMap = new HashMap<>();
            mSections = new ArrayList<>();

            mItems = new ArrayList();

            if (dataMap.get("sections") != null) {
                int sectionFirstPosition = 0;
                for (int i = 0; i < mItemsData.size(); i++) {
                    ListItem listItem = mItemsData.get(i);
                    if (listItem.isSection()) {
                        sectionFirstPosition = i;
                        LineItem item = new LineItem(mItemsData.get(i).getItemData(), true, mItemsData.get(i).getSectionName(), sectionFirstPosition);
                        mItems.add(item);
                        mSections.add(mItemsData.get(i).getSectionName());
                        mSectionMap.put(mItemsData.get(i).getSectionName(), i);
                    }else{
                        JSONObject itemData = mItemsData.get(i).getItemData();
                        String itemLayout = mItemsData.get(i).getSectionName();
                        if(itemData.has("layout")){
                            try {
                                itemLayout = itemData.getString("layout");
                            } catch (JSONException e) {
                                LOGD(TAG, "GridViewAdapter Constructor - ItemData Layout");
                            }
                        }
                        LineItem item = new LineItem(itemData, false, itemLayout, sectionFirstPosition);
                        mItems.add(item);
                    }
                }
                recomputeViewTypes();
            }
        } else {
            LOGE(TAG, "There must be itemsData in the definition.");
        }
    }

    @Override
    public GridViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        HashMap<String, Object> componentData;
        FrameLayout fLayout = new FrameLayout(mContext);

        if(viewType != 0) {
            componentData = (HashMap<String, Object>) mComponentMap.get(getLayoutNameFromInteger(viewType));
            int[] dimensions = getWidthHeightForSection(getLayoutNameFromInteger(viewType));
            LayoutManager.LayoutParams params = new LayoutManager.LayoutParams((int) (dimensions[0] * getDeviceDensity(mContext)), (int) (dimensions[1] * getDeviceDensity(mContext)));
            params.setMargins(dimensions[2], dimensions[3], 0, 0);
            fLayout.setLayoutParams(params);
        }else{
            componentData = assembleHeaderText("", mGridViewWidth, mSectionHeader);
            JSONObject height = (JSONObject) componentData.get("headerTitle");
            try {
                LayoutManager.LayoutParams params = new LayoutManager.LayoutParams((int) (mGridViewWidth * getDeviceDensity(mContext)), (int) (height.getJSONObject("style").getInt("height") * getDeviceDensity(mContext)));
                params.isHeader = true;
                params.headerDisplay = LayoutManager.LayoutParams.HEADER_INLINE;
                fLayout.setLayoutParams(params);
            } catch (JSONException e) {
               LOGW(TAG, "Grid Header", e);
            }
        }

        GridViewHolder gridItem = new GridViewHolder(fLayout);
        gridItem.drawInnerElements(mContext, componentData);
        return gridItem;
    }

    private int[] getWidthHeightForSection(String section){
        int[] res = new int[4];
        res[0] = 0;
        res[1] = 0;
        res[2] = 0;
        res[3] = 0;
        if(mItemLayout.has(section)){
            try {
                JSONObject layout = (JSONObject) mItemLayout.get(section.equals("defaultLayout") ? (String) mItemLayout.get("defaultLayout") : section);
                res[0] = layout.getInt("width");
                res[1] = layout.getInt("height");
                res[2] = layout.getInt("x");
                res[3] = layout.getInt("y");
            } catch (JSONException e) {
                LOGW(TAG, "getWidthHeightForSection", e);
            }
        }
        return res;
    }

    @Override
    public void onBindViewHolder(GridViewHolder gridViewHolder, int position) {
        LineItem item = mItems.get(position);
        final GridSLM.LayoutParams lp = GridSLM.LayoutParams.from(gridViewHolder.itemView.getLayoutParams());

        if(item.isHeader()){
            for (Object entry : gridViewHolder.getComponentMap().entrySet()) {
                Map.Entry dataEntry = (Map.Entry) entry;
                ATKWidget widget = (ATKWidget) dataEntry.getValue();
                widget.setValue(item.getSectionManager(), mContext);
                setHeaderTitleBorders(gridViewHolder.itemView, mSectionHeader, mContext);
            }
        }else{
            try{
                HashMap<String, ATKWidget> componentMap = gridViewHolder.getComponentMap();
                Iterator<String> keys = componentMap.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    ATKWidget widget = componentMap.get(key);
                    if (mSections.size() > 0) {
                        Integer currentSectionPosition = mSectionMap.get(item.getSectionManager());
                        Integer currentSection = mSections.indexOf(item.getSectionManager());
                        String sectionPosition = String.valueOf(position - (currentSectionPosition + 1));

                        setWidgetID(widget, String.valueOf(currentSection), sectionPosition);
                    } else {
                        setWidgetID(widget, "0", String.valueOf(position));
                    }
                    if (item.getData().has(key)) {
                        widget.setValue(item.getData().get(key), mContext);
                    } else {
                        LOGD(TAG, key + " key not found");
                    }
                }
            } catch (JSONException e) {
                LOGE(TAG, "", e);
            }
        }

        gridViewHolder.itemView.setOnClickListener(this);
        if (!item.isHeader()) {
            gridViewHolder.itemView.setOnTouchListener(this);
        }

        lp.setSlm(GridSLM.ID);
        lp.setNumColumns(mNumberOfColumns);
        lp.setFirstPosition(item.getSectionFirstPosition());
        gridViewHolder.itemView.setLayoutParams(lp);

        gridViewHolder.itemView.setTag(new ItemTag(item.getSectionManager(), item.getData()));
    }

    public void setItemsData(ArrayList<ListItem> itemsData) {
        mItemsData = itemsData;

        for (int i = 0; i < itemsData.size(); i++) {
            if (itemsData.get(i).isSection()) {
                mSectionMap.put(itemsData.get(i).getSectionName(), i);
            }
        }

        recomputeViewTypes();
    }

    @Override
    public int getItemViewType(int position) {
        LineItem item = mItems.get(position);
        if (item.isHeader()) {
            return 0;
        } else {
            String itemLayout = item.getSectionManager();
            if (!itemLayout.isEmpty() && mTypesHash.containsKey(itemLayout)) {
                return mTypesHash.get(itemLayout);
            } else {
                if (!mDefaultLayout.isEmpty()) {
                    return mTypesHash.get("defaultLayout");
                }
            }
        }
        return UNDEFINED_LAYOUT;
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

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private String getLayoutNameFromInteger(int type){
        for(Map.Entry<String, Integer> entry : mTypesHash.entrySet()){
            if(entry.getValue() == type){
                if (entry.getKey().equals("defaultLayout")) {
                    return mDefaultLayout;
                }
                return entry.getKey();
            }
        }
        return "";
    }

    public JSONArray getActions() {
        return mActions;
    }

    @Override
    public void onClick(View v) {
        if (getActions() != null) {
            ItemTag dataTag = (ItemTag) v.getTag();
            for (int i = 0; i < getActions().length(); i++) {
                try {
                    JSONObject action = getActions().getJSONObject(i);
                    if (action.getString("event").compareTo(Constants.ATK_ACTION_SELECT) == 0) {

                        JSONObject payload = new JSONObject();
                        payload.put("id", mItemID);
                        payload.put("data", dataTag.getData());

                        ATKEventManager.excecuteComponentAction(action, payload);
                    }
                } catch (JSONException e) {
                    LOGD(TAG, "ListItemOnClickListener onClick", e);
                }
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            v.setBackgroundColor(UIUtils.parseColor(mHighlightColor));
            v.invalidate();
        } else if (event.getAction() == MotionEvent.ACTION_UP  || event.getAction() == MotionEvent.ACTION_CANCEL) {
            v.setBackgroundColor(Color.TRANSPARENT);
            v.invalidate();
        }
        return false;
    }


    private static class LineItem {

        private String mSectionManager;
        private int mSectionFirstPosition;
        private boolean mIsHeader;
        private JSONObject mData;

        public LineItem(JSONObject data, boolean isHeader, String sectionManager,
                        int sectionFirstPosition) {
            mIsHeader = isHeader;
            mData = data;
            mSectionManager = sectionManager;
            mSectionFirstPosition = sectionFirstPosition;
        }

        public String getSectionManager() {
            return mSectionManager;
        }

        public int getSectionFirstPosition() {
            return mSectionFirstPosition;
        }

        public boolean isHeader() {
            return mIsHeader;
        }

        public JSONObject getData() {
            return mData;
        }
    }
}
