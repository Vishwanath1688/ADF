package com.altimetrik.adf.Components.ATKCollectionView.ATKListView;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.altimetrik.adf.Components.ATKCollectionView.CollectionUtils.SharedFunctions;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by esalazar on 4/30/15.
 * Auxiliary class to use with ATKCollectionList
 */
public class ListViewItem extends FrameLayout {

    private HashMap<String, ATKWidget> mComponentMap;

    public ListViewItem(Context context) {
        super(context);
        mComponentMap = new HashMap<>();
    }

    public HashMap<String, ATKWidget> getComponentMap() { return mComponentMap; }

    /**
     * Method to add inner elements to the view.
     *
     * @return Itself, drawn with the elements according to the itemLayoutMap
     */
    public void drawInnerElements(Context context, HashMap<String, Object> itemLayoutMap) {
        SharedFunctions.drawInnerElements(context, itemLayoutMap, this, mComponentMap);
    }

}
