package com.altimetrik.adf.Components.ATKCollectionView.ATKGridView;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.altimetrik.adf.Components.ATKCollectionView.CollectionUtils.SharedFunctions;
import com.altimetrik.adf.Components.ATKWidget;

import java.util.HashMap;

/**
 * Created by esalazar on 8/18/15.
 */
public class GridViewHolder extends RecyclerView.ViewHolder {

    private HashMap<String, ATKWidget> mComponentMap;
    private FrameLayout mDrawnLayout;

    public GridViewHolder(View itemView) {
        super(itemView);
        mDrawnLayout = (FrameLayout) itemView;
        mComponentMap = new HashMap<>();
    }

    public HashMap<String, ATKWidget> getComponentMap() {
        return mComponentMap;
    }

    /**
     * Method to add inner elements to the view.
     *
     * @return Itself, drawn with the elements according to the itemLayoutMap
     */
    public void drawInnerElements(Context context, HashMap<String, Object> itemLayoutMap) {
        SharedFunctions.drawInnerElements(context, itemLayoutMap, mDrawnLayout, mComponentMap);
    }

}
