package com.altimetrik.adf.Components.ATKLabel;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
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
 * Created by pigounet on 3/26/15.
 */
public class ATKLabel extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKLabel.class);

    private String mFontName;
    private Double mFontSize;
    private String mFontColor;
    private String mTextAlignment;
    private String mTextOrientation;
    private String mAdjustToContent;
    private String mText;
    private int mNumberOfLines;
    private String mAdjustFontSize;
    private Boolean mHideWhenEmpty;

    private TextView mLabel;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {

        try {
            super.initWithJSON(widgetDefinition, context);

            //Load particular params
            mText = getProperties().getString("text");
            mTextAlignment = getStyle().optString("textAlignment");
            mNumberOfLines = getProperties().optInt("numberOfLines", 0);
            mTextOrientation = getStyle().optString("textOrientation");
            mFontColor = getStyle().optString("fontColor");
            mFontName = getStyle().optString("fontName");
            mFontSize = getStyle().optDouble("fontSize", 0);
            mAdjustToContent = getProperties().optString("adjustToContent");
            mHideWhenEmpty = getProperties().optBoolean("hideWhenEmpty");
            mAdjustFontSize = getProperties().optString("adjustFontSize");

            //Create View
            mLabel = new TextView(context);

            if (Utils.isPositiveValue(mAdjustFontSize))
                mLabel = new AutoResizeTextView(context);

            //Set base params --> super
            super.loadParams(mLabel);

            //Set particular params
            setValue(mText, context);
            if (mNumberOfLines > 0)
                mLabel.setMaxLines(mNumberOfLines);

            mLabel.setEllipsize(TextUtils.TruncateAt.END);

            if (!mFontColor.isEmpty())
                mLabel.setTextColor(UIUtils.parseColor(mFontColor));

            if (mFontSize > 0) {
                mLabel.setTextSize(mFontSize.floatValue());
            } else {
                mLabel.setTextSize(0);
                LOGD(TAG, "Invalid Font Size.");
            }

            if (!mFontName.isEmpty()) {
                String fontPath = String.format(Constants.ATK_FONT_DIR, mFontName);
                try {
                    Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), fontPath));
                    mLabel.setTypeface(font);
                } catch (Exception e) {
                    LOGW(TAG, String.format("Font %s not found.", fontPath));
                }
            }

            if (Utils.isPositiveValue(mAdjustToContent))
                mLabel.getLayoutParams().height = RelativeLayout.LayoutParams.WRAP_CONTENT;

            setAlignmentFromStyle(mTextAlignment);

        } catch (JSONException e) {
            LOGE(TAG, "initWithJSON", e);
        }

        onPostInitWithJSON();

        return this;
    }

    @Override
    public View getDisplayView() {
        return mLabel;
    }

    @Override
    public void setValue(Object attrs, Context context) {
        final String value = (String) attrs;
        if(mHideWhenEmpty) {
            ((Activity) context).runOnUiThread(new Runnable() {
                public void run() {
                    if (!value.isEmpty()) {
                        mLabel.setVisibility(View.VISIBLE);
                    } else {
                        mLabel.setVisibility(View.GONE);
                    }
                }
            });
        }
        mLabel.setText(value);
    }

    /**
     * Simple method to translate from String and set it as text padding or text alignment where applicable.
     * By default centers the text vertically and aligns it to the left of the textview
     *
     * @param alignment
     */
    public void setAlignmentFromStyle(String alignment) {
        switch (alignment) {
            case "left":
                mLabel.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                mLabel.setPadding((int) getBorderWidth(), 0, 0, 0);
                break;
            case "center":
                mLabel.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                break;
            case "right":
                mLabel.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
                mLabel.setPadding(0, 0, (int) getBorderWidth(), 0);
                break;
            case "top":
                mLabel.setGravity(Gravity.TOP | Gravity.LEFT);
                mLabel.setPadding(0, 0, (int) getBorderWidth(), 0);
                break;
            default:
                mLabel.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                mLabel.setPadding((int) getBorderWidth(), 0, 0, 0);
                break;
        }
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            mLabel.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
    }

    /**
     * @param data    Receives the params object containing destinationID and text to setvalue with
     * @param context
     */
    @Override
    public void loadData(final Object data, final Context context) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                try {
                    JSONObject jsonData = (JSONObject) data;
                    setValue(jsonData.getJSONObject("data").getString("text"), context);
                } catch (JSONException e) {
                    LOGD(TAG, "postNotification", e);
                }
            }
        });
    }
}
