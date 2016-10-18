package com.altimetrik.adf.Components.ATKToggle;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.UIUtils;
import com.altimetrik.adf.Util.Utils;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 7/28/15.
 */
public class ATKToggle extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKToggle.class);

    private SwitchCompat mToggle;
    private TextView mTitle;
    private RelativeLayout mContainer;

    private String mFontName;
    private Double mFontSize;
    private String mFontColor;
    private String mOnTint;

    private String mTitleText;
    private Boolean mDefaultValue;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {

        super.initWithJSON(widgetDefinition, context);

        //Style
        mFontColor = getStyle().optString("fontColor");
        mFontName = getStyle().optString("fontName");
        mFontSize = getStyle().optDouble("fontSize", 0);
        mOnTint = getStyle().optString("onTint");

        //Properties
        mTitleText = getProperties().optString("title");
        mDefaultValue = Utils.isPositiveValue(getData().optString("defaultValue"));

        mToggle = new SwitchCompat(context);
        RelativeLayout.LayoutParams toggleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        toggleParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        toggleParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mToggle.setLayoutParams(toggleParams);
        mToggle.setChecked(mDefaultValue);

        if (mDefaultValue && !mOnTint.isEmpty())
            mToggle.getThumbDrawable().setColorFilter(new
                    PorterDuffColorFilter(UIUtils.parseColor(mOnTint), PorterDuff.Mode.SRC_ATOP));


        mToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    if (!mOnTint.isEmpty())
                        mToggle.getThumbDrawable().setColorFilter(new
                                PorterDuffColorFilter(UIUtils.parseColor(mOnTint), PorterDuff.Mode.SRC_ATOP));

                } else {
                    if (!mOnTint.isEmpty())
                        mToggle.getThumbDrawable().clearColorFilter();
                }

                if (getActions() != null) {
                    for (int i = 0; i < getActions().length(); i++) {
                        try {
                            JSONObject action = getActions().getJSONObject(i);
                            if (action.getString("event").compareTo(Constants.ATK_ACTION_SELECT) == 0) {
                                JSONObject data = new JSONObject();
                                data.put("id", getID());
                                data.put("value", isChecked);
                                ATKEventManager.excecuteComponentAction(action, data);
                            }
                        } catch (JSONException e) {
                            LOGE(TAG, "ATKButtonOnClickListener onClick", e);
                        }
                    }
                }
            }
        });

        mTitle = new TextView(context);
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        titleParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        titleParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mTitle.setLayoutParams(titleParams);

        mContainer = new RelativeLayout(context);
        mContainer.addView(mTitle);
        mContainer.addView(mToggle);

        super.loadParams(mContainer);

        mTitle.setText(mTitleText);

        if (!mFontColor.isEmpty())
            mTitle.setTextColor(UIUtils.parseColor(mFontColor));

        if (mFontSize > 0) {
            mTitle.setTextSize(mFontSize.floatValue());
        }else{
            mTitle.setTextSize(0);
            LOGE(TAG, "Invalid Font Size");
        }

        if (!mFontName.isEmpty()) {
            String fontPath = String.format(Constants.ATK_FONT_DIR, mFontName);
            try {
                Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), fontPath));
                mTitle.setTypeface(font);
            } catch (Exception e) {
                LOGE(TAG, String.format("Font %s not found.", fontPath));
            }
        }

        onPostInitWithJSON();

        return this;
    }

    @Override
    public View getDisplayView() {
        return mContainer;
    }

    @Override
    public void setValue(Object attrs, Context context) { }

    @Override
    public void clean() {
        mToggle = null;
        mTitle = null;
        mContainer = null;
        super.clean();
    }
}