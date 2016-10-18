package com.altimetrik.adf.Components.ATKCollectionView.ATKCoverFlowView;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gyordi on 4/22/15.
 */
public class CoverFlowTile extends RelativeLayout {

    public CoverFlowTile(Context context, HashMap<String, JSONObject> componentMap, ArrayList<String> componentKeys, HashMap<String, ATKWidget> componentCellMap) {
        super(context);

        int containerHeight = 0;
        int containerWidth = 0;
        int containerBorder = 15;

        RelativeLayout container = new RelativeLayout(context);

        //For each component in map, create a widget
        for (String key : componentKeys) {
            JSONObject value = componentMap.get(key);

            ATKWidget widget = ATKComponentManager.getInstance().presentComponentInView(context, null, value);
            componentCellMap.put(key, widget);

            View widgetView = widget.getDisplayView();
            containerHeight = (widget.getHeight() > containerHeight) ? widget.getHeight() : containerHeight;
            containerWidth = (widget.getWidth() > containerWidth) ? widget.getWidth() : containerWidth;

            LayoutParams widgetParams = new LayoutParams(widget.getWidth() - containerBorder, widget.getHeight());
            widgetView.setLayoutParams(widgetParams);
            widgetView.requestLayout();

            container.addView(widgetView);
        }

        RelativeLayout.LayoutParams containerParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, containerHeight);
        containerParams.setMargins(containerBorder, containerBorder, containerBorder, containerBorder);
        container.setLayoutParams(containerParams);

        RelativeLayout background = new RelativeLayout(context);
        background.setLayoutParams(new RelativeLayout.LayoutParams(containerWidth, containerHeight + containerBorder * 2));
        background.setBackground(ContextCompat.getDrawable(context, R.drawable.shadow));

        addView(background);
        addView(container);
    }
}
