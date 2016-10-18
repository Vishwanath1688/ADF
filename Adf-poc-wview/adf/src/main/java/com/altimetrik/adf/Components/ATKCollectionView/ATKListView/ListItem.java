package com.altimetrik.adf.Components.ATKCollectionView.ATKListView;

import org.json.JSONObject;

/**
 * Created by gyordi on 6/12/15.
 */
public class ListItem {
    private boolean mIsSection;
    private JSONObject mItemData;
    private String mSectionName;

    public ListItem(JSONObject itemData, Boolean isSection) {
        mItemData = itemData;
        mIsSection = isSection;
    }

    public boolean isSection() {
        return mIsSection;
    }

    public JSONObject getItemData() {
        return mItemData;
    }

    public String getSectionName() {
        return mSectionName;
    }

    public void setSectionName(String sectionName) {
        mSectionName = sectionName;
    }
}
