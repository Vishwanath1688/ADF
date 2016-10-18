package com.altimetrik.adf.Components.ATKSlider;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
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
import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 7/28/15.
 */
public class ATKSlider extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKSlider.class);

    private SeekBar mSlider;
    private TextView mTitle;
    private LinearLayout mContainer;

    private String mFontName;
    private Double mFontSize;
    private String mFontColor;

    private String mTitleText;
    private int mStep;
    private int mMax;
    private int mMin;
    private Boolean mShowSelectedValue;
    private int mDefaultValue;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {

        try {
            super.initWithJSON(widgetDefinition, context);

            //Style
            mFontColor = getStyle().has("fontColor") ? getStyle().getString("fontColor") : "";
            mFontName = getStyle().has("fontName") ? getStyle().getString("fontName") : "";
            mFontSize = getStyle().has("fontSize") ? getStyle().getDouble("fontSize") : 0;

            //Properties
            mTitleText = getProperties().optString("title");
            mMax = (int) Double.parseDouble(getProperties().optString("maxValue", "0"));
            mMin = (int) Double.parseDouble(getProperties().optString("minValue", "0"));
            mStep = getProperties().optInt("integerSliderValues", 1);
            mShowSelectedValue = getProperties().has("showSelectedValue") ? Utils.isPositiveValue(getProperties().optString("showSelectedValue")) : true;
            Double auxDouble = getData().has("defaultValue") ? Double.parseDouble(getData().optString("defaultValue")) : mMin;
            mDefaultValue = auxDouble.intValue();

            if(mDefaultValue < mMin || mDefaultValue > mMax){
                mDefaultValue = mMin;
                LOGW(TAG, "Default value is out of range");
            }
            mSlider = new SeekBar(context);
            mSlider.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            mTitle = new TextView(context);
            mTitle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            mSlider.setMax((mMax - mMin) / mStep);
            mSlider.setProgress((mDefaultValue - mMin) / mStep);
            mSlider.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                                                      boolean fromUser) {
                            int value = mMin + (seekBar.getProgress() * mStep);

                            if (mShowSelectedValue)
                                mTitle.setText(mTitleText + " " + value);

                            if (getActions() != null) {
                                for (int i = 0; i < getActions().length(); i++) {
                                    try {
                                        JSONObject action = getActions().getJSONObject(i);
                                        if (action.getString("event").compareTo(Constants.ATK_ACTION_SELECT) == 0) {
                                            JSONObject data = new JSONObject();
                                            data.put("id", getID());
                                            data.put("value", value);
                                            ATKEventManager.excecuteComponentAction(action, data);
                                        }
                                    } catch (JSONException e) {
                                        LOGD(TAG, "ATKButtonOnClickListener onClick", e);
                                    }
                                }
                            }
                        }
                    }
            );

            mContainer = new LinearLayout(context);
            mContainer.setOrientation(LinearLayout.VERTICAL);
            mContainer.addView(mTitle);
            mContainer.addView(mSlider);

            super.loadParams(mContainer);

            mTitle.setText(mTitleText);

            if (!mFontColor.isEmpty())
                mTitle.setTextColor(UIUtils.parseColor(mFontColor));

            if (mFontSize > 0) {
                mTitle.setTextSize(mFontSize.floatValue());
            } else {
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

        } catch (JSONException e) {
            LOGE(TAG, "initWithJSON", e);
        }

        onPostInitWithJSON();

        return this;
    }

    @Override
    public View getDisplayView() {
        return mContainer;
    }

    @Override
    public void setValue(Object attrs, Context context) {
    }

    @Override
    public void clean() {
        mSlider.setOnSeekBarChangeListener(null);
        mSlider = null;
        super.clean();
    }
}