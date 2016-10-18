package com.altimetrik.adf.Components.ATKActionSheet;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 5/18/15.
 */
public class ATKActionSheet extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKActionSheet.class);

    private String mTitle;
    private String mActionNotificationId;
    private String mCancelNotificationId;
    private String mOpenerComponentId;

    private JSONArray mButtons;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        super.initWithJSON(widgetDefinition, context);

        //Get Properties
        mTitle = getProperties().optString("title");
        mActionNotificationId = getProperties().optString("actionNotificationId");
        mCancelNotificationId = getProperties().optString("cancelNotificationId");
        mOpenerComponentId = getProperties().optString("openerComponentId");
        mButtons = getProperties().optJSONArray("buttons");

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setTitle(mTitle);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.select_dialog_item);

        for (int i = 0; i < mButtons.length(); i++) {
            try {
                JSONObject currentButton = ((JSONObject) mButtons.get(i));
                if (Utils.isPositiveValue(currentButton.optString("isCancelButton"))) {
                    dialogBuilder.setNegativeButton(currentButton.getString("title"),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (getActions() != null) {
                                        for (int i = 0; i < getActions().length(); i++) {
                                            try {
                                                JSONObject action = getActions().getJSONObject(i);
                                                if (action.getString("event").compareTo(Constants.ATK_ACTION_CANCEL) == 0) {
                                                    JSONObject data = new JSONObject();
                                                    data.put("id", getID());
                                                    ATKEventManager.excecuteComponentAction(action, data);
                                                }
                                            } catch (JSONException e) {
                                                LOGD(TAG, "ATKActionSheet ATKActionCancel onClick", e);
                                            }
                                        }
                                    }
                                    dialog.dismiss();
                                }
                            });
                } else {
                    arrayAdapter.add(currentButton.getString("title"));
                }
            } catch (JSONException e) {
                LOGE(TAG, "initWithJSON", e);
            }
        }

        dialogBuilder.setAdapter(arrayAdapter,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (getActions() != null) {
                        for (int i = 0; i < getActions().length(); i++) {
                            try {
                                JSONObject action = getActions().getJSONObject(i);
                                if (action.getString("event").compareTo(Constants.ATK_ACTION_SELECT) == 0) {
                                    JSONObject data = new JSONObject();
                                    data.put("id", getID());
                                    data.put("selectedIndex", which);
                                    data.put("selectedButtonTitle", arrayAdapter.getItem(which));
                                    ATKEventManager.excecuteComponentAction(action, data);
                                }
                            } catch (JSONException e) {
                                LOGD(TAG, "ATKActionSheet ATKActionSelect onClick", e);
                            }
                        }
                    }
                }
            });

        dialogBuilder.show();

        return this;
    }

    @Override
    public View getDisplayView() {
        return null;
    }

    @Override
    public void setValue(Object attrs, Context context) { }

}
