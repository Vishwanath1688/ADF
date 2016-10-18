package com.altimetrik.adf.Components.ATKCollectionView.CollectionUtils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.FrameLayout;

import com.altimetrik.adf.Components.ATKCollectionView.ATKListView.BorderDrawable;
import com.altimetrik.adf.Components.ATKCollectionView.ATKListView.ListItem;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Util.UIUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;
import static com.altimetrik.adf.Util.Utils.getDeviceDensity;

/**
 * Created by esalazar on 5/25/15.
 */
public class SharedFunctions {

    private static final String TAG = makeLogTag(SharedFunctions.class);


    /**
     * Creates the JSONObject for the header text and places it into the usable Map
     *
     * @param headerText
     * @return Text to be used in the header
     */
    public static HashMap<String, Object> assembleHeaderText(String headerText, int parentViewWidth, JSONObject sectionHeaderDefinition) {
        HashMap<String, Object> layoutMap = new HashMap<>();
        JSONObject headerLayout = new JSONObject();
        JSONObject headerLayoutProperties = new JSONObject();
        JSONObject headerLayoutStyle = sectionHeaderDefinition;

        try {
            headerLayout.put("id", "headerTitleId");
            headerLayout.put("name", "headerTitleName");
            headerLayout.put("class", "ATKLabel");
            headerLayout.put("notificationId", "");
            headerLayout.put("data", new JSONObject());
            headerLayoutStyle.put("x", "0");
            headerLayoutStyle.put("y", "0");
            headerLayoutStyle.put("width", parentViewWidth);
            headerLayoutStyle.put("height", sectionHeaderDefinition.optString("height", "0"));
            headerLayoutStyle.put("backgroundColor", sectionHeaderDefinition.optString("backgroundColor", "#FFFFFF"));
            headerLayout.put("style", headerLayoutStyle);

            headerLayoutProperties.put("text", headerText);
            headerLayout.put("properties", headerLayoutProperties);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        layoutMap.put("headerTitle", headerLayout);
        return layoutMap;
    }

    /**
     * Sets the id for the widget with the format OriginalID-Section-Position
     */
    public static void setWidgetID(ATKWidget widget, String section, String position){
        String origID;

        if(widget.getID().contains("-")){
            origID = widget.getID().split("-")[0];
        }else{
            origID = widget.getID();
        }

        widget.setID(String.format("%s-%s-%s", origID, section, position));
    }

    /**
     * Turns the value into a map for ease of access and distribution
     *
     * @param values JSONArray with values for item layout, be it an array of sections or a simple array
     */
    public static HashMap loadComponentsMap(Object values, Boolean multipleLayouts) {
        LinkedHashMap<String, Object> assembleMap = new LinkedHashMap<>();
        HashMap<String, Object> finalMap = new HashMap<>();
        String defaultLayout = "";
        try {
            if (!multipleLayouts) {
                JSONObject initialJSONObject = (JSONObject) values;
                JSONArray valuesArray = initialJSONObject.getJSONArray("components");
                for (int i = 0; i < valuesArray.length(); i++) {
                    JSONObject comp = (JSONObject) valuesArray.get(i);
                    assembleMap.put(comp.getString("bindKey"), comp.getJSONObject("componentJSON"));
                }

            } else {
                JSONObject valuesObject = (JSONObject) values;
                Iterator<?> jsonKeys = valuesObject.keys();

                // Iterate over the keys of the itemLayouts
                while (jsonKeys.hasNext()) {
                    String currentKey = (String) jsonKeys.next();
                    LinkedHashMap map = new LinkedHashMap<>();

                    // If the current Key is defaultLayout, it must be skipped and set as a value to take into account
                    if (currentKey.equalsIgnoreCase("defaultLayout")) {
                        defaultLayout = valuesObject.getString("defaultLayout");
                    } else {
                        JSONObject currentItem = ((JSONObject) values).getJSONObject(currentKey);
                        JSONArray componentArray = currentItem.getJSONArray("components");
                        // Iterate over the components of the current Section and add them to a map for each section
                        for (int i = 0; i < componentArray.length(); i++) {
                            JSONObject comp = (JSONObject) componentArray.get(i);
                            map.put(comp.getString("bindKey"), comp.getJSONObject("componentJSON"));
                        }
                        assembleMap.put(currentKey, map);
                    }
                }
            }
        } catch (JSONException e) {
            LOGD(TAG, "loadComponentsMap", e);
        }

        finalMap.put("defaultLayout", defaultLayout);
        finalMap.put("layoutDefinition", assembleMap);

        return finalMap;
    }

    /**
     * Sets the colored borders for the header ListItem in case it has any.
     *
     * @param v      Section view to apply borders
     * @param values JSONObject style
     */
    public static void setHeaderTitleBorders(View v, JSONObject values, Context context) {

        float density = getDeviceDensity(context);

        String[] tokens = values.optString("insets", "{0,0}").replace("{", " ").replace("}", " ").trim().split(",");

        int top  = (int) (Double.parseDouble(tokens[0]) * density);
        int left = (int) (Double.parseDouble(tokens[1]) * density);
        v.setPadding(left, top , 0, 0);

        String backgroundColor = values.optString("backgroundColor", "#FFFFFF");
        GradientDrawable backgroundDrawable = new GradientDrawable();
        if (backgroundColor != null && backgroundColor.contains("#")) {
            backgroundDrawable.setColor(UIUtils.parseColor(backgroundColor));
        } else {
            backgroundDrawable.setColor(Color.WHITE);
        }

        View parentView = (View) v.getParent();
        if (parentView != null) {
            parentView.setBackground(backgroundDrawable);
        }

        try {
            JSONObject border = values.optJSONObject("border");
            if (border != null) {
                BorderDrawable borders = new BorderDrawable(null, 0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
                borders.setBackground(backgroundDrawable);

                if (border.has("left"))
                    borders.setLeftBorder((int) (border.getJSONObject("left").getInt("width") * density), UIUtils.parseColor(border.getJSONObject("left").getString("color")));
                if (border.has("bottom"))
                    borders.setBottomBorder((int) (border.getJSONObject("bottom").getInt("width") * density), UIUtils.parseColor(border.getJSONObject("bottom").getString("color")));
                if (border.has("top"))
                    borders.setTopBorder((int) (border.getJSONObject("top").getInt("width") * density), UIUtils.parseColor(border.getJSONObject("top").getString("color")));
                if (border.has("right"))
                    borders.setRightBorder((int) (border.getJSONObject("right").getInt("width") * density), UIUtils.parseColor(border.getJSONObject("right").getString("color")));

                v.setBackground(borders);
            }
        } catch (JSONException e) {
            LOGD(TAG, "setHeaderTitleBorders", e);
        }

    }

    /**
     * Turns JSON data into an array of ListViewItem
     *
     * @param data
     * @return
     */
    public static ArrayList<ListItem> JSONToArray(Object data) {
        ArrayList<ListItem> listItemArray = new ArrayList<>();
        try {

            // Checks if the object is an array (direct data with no header) or has section/s
            JSONArray dataJsonArray = (JSONArray) data;
            if (dataJsonArray != null && dataJsonArray.length() > 0) {
                if (((JSONObject) dataJsonArray.get(0)).has("section_title")) {

                    // Iterate over the collection of sections
                    for (int i = 0; i < dataJsonArray.length(); i++) {

                        JSONObject jsonItem = (JSONObject) dataJsonArray.get(i);
                        String sectionName = jsonItem.getString("section_title");

                        ListItem sectionItem = new ListItem(null, true);
                        sectionItem.setSectionName(sectionName);
                        listItemArray.add(sectionItem);

                        JSONArray sectionItemArray = jsonItem.getJSONArray("items");

                        for (int j = 0; j < sectionItemArray.length(); j++) {
                            JSONObject innerItemJSON = (JSONObject) sectionItemArray.get(j);
                            ListItem sectionListItemFromCollection = new ListItem(innerItemJSON, false);

                            sectionListItemFromCollection.setSectionName(sectionName);

                            /*if (innerItemJSON.has("layout")) {
                                sectionListItemFromCollection.setSectionName(innerItemJSON.getString("layout"));
                            } else {
                                sectionListItemFromCollection.setSectionName(sectionName);
                            }*/

                            listItemArray.add(sectionListItemFromCollection);
                        }
                    }

                } else {
                    for (int i = 0; i < dataJsonArray.length(); i++) {
                        JSONObject jsonItem = (JSONObject) dataJsonArray.get(i);
                        ListItem listItem;
                        listItem = new ListItem(jsonItem, false);
                        listItemArray.add(listItem);
                    }
                }
            }
        } catch (JSONException e) {
            LOGD(TAG, "JSONToArray", e);
        }
        return listItemArray;
    }

    /**
     * Method to add inner elements to the view.
     *
     * @return Itself, drawn with the elements according to the itemLayoutMap
     */
    public static void drawInnerElements(Context context, HashMap<String, Object> itemLayoutMap, FrameLayout layout, HashMap<String, ATKWidget> componentMap) {
        for (Map.Entry<String, Object> entry : itemLayoutMap.entrySet()) {
            if (!entry.getKey().equalsIgnoreCase("layout")) {
                ATKWidget widget = ATKComponentManager.getInstance().presentComponentInView(context, layout, (JSONObject) entry.getValue());
                View widgetView = widget.getDisplayView();
                FrameLayout.LayoutParams widgetParams = new FrameLayout.LayoutParams(widget.getWidth(), widget.getHeight());
                widgetView.setLayoutParams(widgetParams);
                widgetView.requestLayout();
                componentMap.put(entry.getKey(), widget);
            }
        }
    }

    public static class ItemTag {
        private String mSectionName;
        private JSONObject mData;

        public ItemTag(String sectionName, JSONObject data) {
            mSectionName = sectionName;
            mData = data;
        }

        public void setSectionName(String type) {
            mSectionName = type;
        }

        public String getSectionName() {
            return mSectionName;
        }

        public JSONObject getData() {
            return mData;
        }

        public void setData(JSONObject data) {
            mData = data;
        }
    }
}
