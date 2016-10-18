package com.altimetrik.adf.Components.ATKForm;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Components.IATKInput;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by gyordi on 5/6/15.
 */

public class ATKForm extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKForm.class);

    private ScrollView mScrollViewContainer;
    private RelativeLayout mComponentContainer;
    private List<IATKInput> mInputs;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        try {
            super.initWithJSON(widgetDefinition, context);

            mScrollViewContainer = new ScrollView(context);
            mComponentContainer = new RelativeLayout(context);
            super.loadParams(mScrollViewContainer);

            mComponentContainer.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            mScrollViewContainer.addView(mComponentContainer);

            mInputs = new ArrayList<>();

            float containerHeight = 0;
            float containerWidth = 0;

            if (mComponentsArray != null) {
                int len = mComponentsArray.length();
                for (int i = len; i > 0; i--) {
                    JSONObject item = mComponentsArray.getJSONObject(i - 1);
                    ATKWidget widget = ATKComponentManager.getInstance().presentComponentInView(context, mComponentContainer, item);
                    if (widget instanceof IATKInput) {
                        final IATKInput widgetInput = (IATKInput) widget;
                        mInputs.add(widgetInput);
                    }

                    if (widget != null) {
                        float currentContainerHeight = widget.getDisplayView().getLayoutParams().height + widget.getDisplayView().getY();
                        float currentContainerWidth = widget.getDisplayView().getLayoutParams().width + widget.getDisplayView().getX();

                        containerHeight = (currentContainerHeight > containerHeight) ? currentContainerHeight : containerHeight;
                        containerWidth = (currentContainerWidth > containerWidth) ? currentContainerWidth : containerWidth;
                    }
                }

            }

            JSONObject submitButtonJSON = getProperties().getJSONObject("submitButton");
            ATKWidget submitButton = ATKComponentManager.getInstance().presentComponentInView(context, mComponentContainer, submitButtonJSON);

            if (submitButton != null) {

                float currentContainerHeight = submitButton.getDisplayView().getLayoutParams().height + submitButton.getDisplayView().getY();
                float currentContainerWidth = submitButton.getDisplayView().getLayoutParams().width + submitButton.getDisplayView().getX();

                containerHeight = (currentContainerHeight > containerHeight) ? currentContainerHeight : containerHeight;
                containerWidth = (currentContainerWidth > containerWidth) ? currentContainerWidth : containerWidth;

                submitButton.getDisplayView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        List<String> list = new ArrayList<>();

                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("{ 'id' : '");
                        stringBuilder.append(getID());
                        stringBuilder.append("', 'data': { ");

                        int len = mInputs.size();
                        for (int i = len; i > 0; i--) {
                            IATKInput widgetInput = mInputs.get(i - 1);
                            String data = widgetInput.getDataString().replace("\'", "\\\'");
                            String id = ((ATKWidget) widgetInput).getID();
                            list.add(String.format("'%s':'%s'", id, data));
                        }
                        stringBuilder.append(TextUtils.join(",", list));
                        stringBuilder.append(" }"); // close data object
                        stringBuilder.append("}");
                        String json = stringBuilder.toString();

                        if (getActions() != null) {
                            for (int i = 0; i < getActions().length(); i++) {
                                try {
                                    JSONObject action = getActions().getJSONObject(i);
                                    if (action.getString("event").equalsIgnoreCase("ATKActionSubmit")) {
                                        JSONObject data = new JSONObject(json);
                                        data.put("id", getID());
                                        ATKEventManager.excecuteComponentAction(action, data);
                                    }
                                } catch (JSONException e) {
                                    LOGE(TAG, "onSubmit", e);
                                }
                            }
                        }
                    }
                });
            }

            mComponentContainer.getLayoutParams().width = (int) containerWidth;
            mComponentContainer.getLayoutParams().height = (int) containerHeight;

        } catch (JSONException e) {
            LOGE(TAG, "initWithJSON", e);
        }

        onPostInitWithJSON();

        return this;
    }

    @Override
    public void loadData(Object data, Context context) {
        JSONObject inputFieldsData = ((JSONObject) data).optJSONObject("data");
        for (IATKInput input : mInputs) {
            String inputId = input.getInputId();
            Iterator<String> keys = inputFieldsData.keys();
            while(keys.hasNext()) {
                String key = keys.next();
                if (key.equals(inputId)) {
                    input.setValue(inputFieldsData.optString(key));
                }
            }
        }
    }

    @Override
    public View getDisplayView() {
        return mScrollViewContainer;
    }

    @Override
    public void setValue(Object attrs, Context context) { }

    @Override
    public void clean() {
        if (mComponentContainer != null) {
            mComponentContainer.removeAllViews();
            mComponentContainer = null;
        }
        super.clean();
    }
}
