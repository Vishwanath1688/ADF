package com.altimetrik.adf.Components.ATKSearchBox;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.EditText;

import com.altimetrik.adf.Components.ATKCollectionView.ATKCollectionView;
import com.altimetrik.adf.Components.ATKTextField.ATKTextField;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.BridgeManager.ATKBridgeManager;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.Util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 5/11/15.
 */
public class ATKSearchBox extends ATKTextField {

    private static final String TAG = makeLogTag(ATKSearchBox.class);

    private String mDataType;
    private String mDataSource;

    private Boolean mSections;
    private Boolean mMoreItems;
    private JSONArray mItems;
    private String mDoneEventNotification;

    private Map<String, String> mFilterAttributes;
    private ArrayList<String> mUpdateComponents;

    private ATKWidget mTextField;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {

        mTextField = super.initWithJSON(widgetDefinition, context);

        try {
            JSONArray attributes = getProperties().getJSONArray("filterAttributes");
            mFilterAttributes = new Hashtable<>();
            for(int i=0;i<attributes.length();i++) {
                JSONObject attribute = attributes.getJSONObject(i);
                mFilterAttributes.put(attribute.getString("attribute"), attribute.getString("condition"));
            }

            mUpdateComponents = new ArrayList<>();
            JSONArray components = getProperties().getJSONArray("updateComponents");
            for (int i = 0; i < components.length(); i++) {
                mUpdateComponents.add(components.getString(i));
            }

            if(getProperties().has("ATKDoneEventNotification")){
                mDoneEventNotification = getProperties().getString("ATKDoneEventNotification");
            }
        } catch (JSONException e) {
            LOGD(TAG, "filterComponentsJSON", e);
        }

        mDataType = getData().optString("type");
        mDataSource = getData().optString("source");

        parseData(context);

        final Handler handler = new Handler();

        EditText editText = (EditText) getDisplayView();
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        filterResult(s);
                    }
                }, 800);
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        return mTextField;
    }

    private void filterResult(CharSequence s) {
        JSONArray filteredResult = mItems;
        if (s.length() > 0) {
            filteredResult = applyFilters(s.toString());
        }
        if(mDoneEventNotification == null) {
            for (String component : mUpdateComponents) {
                ATKCollectionView widget = (ATKCollectionView) ATKComponentManager.getInstance().getComponentById(component);
                if (widget != null && filteredResult != null) {
                    widget.reloadData(filteredResult);
                }
            }
        } else {
            postFilteredData(filteredResult, mDoneEventNotification);
        }
    }

    private void postFilteredData(JSONArray filteredResult, String callBack){
        JSONObject action = new JSONObject();
        JSONObject params = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            params.put("webId", "webView1");
            params.put("callbackFunction", callBack);
            action.put("params", params);

            data.put("filteredData", filteredResult);
            data.put("id", getID());
            for(int i = 0; i < mUpdateComponents.size(); i++){
                data.accumulate("componentsToUpdate", mUpdateComponents.get(i));
            }
        } catch (JSONException e) {
            LOGE(TAG, "postFilteredData", e);
        }

        ATKBridgeManager.excecuteJSCall("webView1", callBack, data);
    }

    private void parseData(Context context) {
        String parsedSource = "";
        //Get the json data
        if (mDataType.toLowerCase().equals("local")) {
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(context.getAssets().open(mDataSource)));
                String mLine = reader.readLine();
                while (mLine != null) {
                    stringBuilder.append(mLine);
                    mLine = reader.readLine();
                }

                parsedSource = stringBuilder.toString();
            } catch (IOException e) {
                LOGD(TAG, "openDataSearchBox", e);
            }  finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        LOGD(TAG, "readerSearchBox", e);
                    }
                }
            }
        } else if (mDataType.toLowerCase().equals("jsonstring")) {
            parsedSource = mDataSource;
        }

        try {
            JSONObject source = new JSONObject(parsedSource);
            mItems = source.optJSONArray("items");
            mSections = source.optBoolean("sections", false);
            mMoreItems = source.optBoolean("moreItems", false);
        } catch (JSONException ex1) {
            try {
                mItems = new JSONArray(parsedSource);
            } catch (JSONException ex2) {
                LOGW(TAG, "dataParsingSearchBox");
            }
        }
    }

    //Filters a JSON using the filters for attributes - returns the resulting JSON
    private JSONArray applyFilters(String text) {

        JSONArray filteredItems = null;
        JSONArray filteredResult = new JSONArray();

        try {
            if (mItems != null && mItems.length() > 0) {
                //Clone the initial array
                filteredItems = new JSONArray(mItems.toString());
            }

            if (filteredItems != null) {
                if (((JSONObject) filteredItems.get(0)).has("section_title")) {
                    for (int i = 0; i < filteredItems.length(); i++) {
                        JSONArray sectionItems = (JSONArray) ((JSONObject) filteredItems.get(i)).get("items");
                        JSONArray filteredSectionItems = new JSONArray();
                        for (int j = 0; j < sectionItems.length(); j++) {
                            if (filterItem((JSONObject) sectionItems.get(j), text)) {
                                filteredSectionItems.put(sectionItems.get(j));
                            }
                        }

                        if (filteredSectionItems.length() > 0) {
                            //Override the items list with the filtered ones
                            ((JSONObject) filteredItems.get(i)).put("items", filteredSectionItems);
                            filteredResult.put(filteredItems.get(i));
                        }
                    }
                } else {
                    JSONArray filteredSectionItems = new JSONArray();
                    for (int i = 0; i < filteredItems.length(); i++) {
                        if (filterItem((JSONObject) filteredItems.get(i), text)) {
                            filteredSectionItems.put(filteredItems.get(i));
                        }
                    }
                    filteredResult = filteredSectionItems;
                }
            }
        } catch (JSONException e) {
            LOGE(TAG, "applyFilters", e);
        }

        return filteredResult;
    }

    //Checks if an item matches one of the filters
    private boolean filterItem(JSONObject item, String text) {
        Iterator<Map.Entry<String, String>> it = mFilterAttributes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String key = entry.getKey();
            //Check if the item has the searched key
            if (item.has(key)) {
                //Check the condition
                switch (entry.getValue()) {
                    case Constants.ATK_FILTER_ATTRIBUTE_CONTAINS:
                        try {
                            if (item.getString(key).toLowerCase().contains(text.toLowerCase())) {
                                return true;
                            }
                        } catch (JSONException e) {
                            LOGD(TAG, "filterItemSearchBox", e);
                        }
                        break;
                    case Constants.ATK_FILTER_ATTRIBUTE_LIKE:
                        try {
                            if (item.getString(key).toLowerCase().contains(text.toLowerCase())) {
                                return true;
                            }
                        } catch (JSONException e) {
                            LOGD(TAG, "filterItemSearchBox", e);
                        }
                        break;
                }
            }
        }
        return false;
    }

    @Override
    public void loadData(Object data, Context context) {
        super.loadData(data, context);

        JSONObject jsonData;
        try {
            jsonData = ((JSONObject) data).getJSONObject("data");
            mDataType = jsonData.optString("type", "");
            mDataSource = jsonData.optString("source", "");
            parseData(context);

            EditText editText = (EditText) getDisplayView();
            editText.getText().clear();
        } catch (JSONException e) {
            LOGD(TAG, "loadDataSearchBox", e);
        }
    }
}
