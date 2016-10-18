package com.altimetrik.adf.Components;

import android.content.Context;
import android.view.View;

import org.json.JSONObject;

/**
 * Created by pigounet on 3/26/15.
 */
public interface ATKWidget {
    ATKWidget initWithJSON(JSONObject widgetDefinition, Context context);
    View getDisplayView();
    int getX();
    int getY();
    int getWidth();
    int getHeight();
    String getID();
    void setID(String value);
    void setValue(Object attrs, Context context);
    void loadData(Object data, Context context);
    void setFutureParent(View view);
    void clean();
    void resize(int x, int y, int width, int height, Context context);
}
