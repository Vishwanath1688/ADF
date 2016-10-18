package com.altimetrik.adf.Components.ATKTextView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.UIUtils;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by esalazar on 5/18/15.
 */
public class ATKTextView extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKTextView.class);

    private TextView mTextView;
    private Boolean mSelectable;
    private Boolean mEditable;
    private Boolean mShowLines;

    private String mFontName;
    private Double mFontSize;
    private String mFontColor;

    private String mText;
    private String mType;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        try {

            //Load base params --> super
            super.initWithJSON(widgetDefinition, context);

            //Load particular params
            mText = getData().optString("text");
            mType = getData().optString("type");
            mEditable = getProperties().optBoolean("editable");
            mSelectable = getProperties().optBoolean("selectable");
            mShowLines = getProperties().optBoolean("showLines");

            mFontColor = getStyle().optString("fontColor");
            mFontName = getStyle().optString("fontName");
            mFontSize = getStyle().optDouble("fontSize", 0);

            //Create View
            mTextView = new EditText(context);

            //Set base params --> super
            super.loadParams(mTextView);

            if (!mEditable) {
                mTextView = new TextView(context);
            }

            if (!mSelectable) {
                mTextView.setClickable(false);
                mTextView.setFocusableInTouchMode(false);
                mTextView.setKeyListener(null);
            } else {
                mTextView.setMovementMethod(LinkMovementMethod.getInstance());
            }

            mTextView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        InputMethodManager inputMethodManager =(InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            });

            if (!mFontColor.isEmpty())
                mTextView.setTextColor(UIUtils.parseColor(mFontColor));

            if (mFontSize > 0) {
                mTextView.setTextSize(mFontSize.floatValue());
            }else{
                mTextView.setTextSize(0);
                LOGE(TAG, "Invalid Font Size");
            }

            if (!mFontName.isEmpty()) {
                String fontPath = String.format(Constants.ATK_FONT_DIR, mFontName);
                try {
                    Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), fontPath));
                    mTextView.setTypeface(font);
                } catch (Exception e) {
                    LOGE(TAG, String.format("Font %s not found.", fontPath));
                }
            }

            if (!mText.isEmpty() && !mType.isEmpty()) {
                if (mType.equalsIgnoreCase("html")) {
                    mTextView.setText(Html.fromHtml(mText));
                } else if (mType.equalsIgnoreCase("plain")) {
                    mTextView.setText(mText);
                }
            }

            mTextView.setGravity(Gravity.TOP | Gravity.LEFT);

        } catch (Exception e) {
            LOGE(TAG, "initWithJSON", e);
        }

        onPostInitWithJSON();

        return this;
    }

    @Override
    public View getDisplayView() {
        return mTextView;
    }

    /**
     * @param attrs   JSONObject (String text, String data)
     * @param context
     */
    @Override
    public void setValue(Object attrs, Context context) {
        if(attrs.getClass().toString().contains("JSON")){
            JSONObject jsonData = (JSONObject) attrs;
            try {
                String type = jsonData.getString("type");
                String text = jsonData.getString("text");

                if (type.equalsIgnoreCase("html")) {
                    mTextView.setText(Html.fromHtml(text));
                } else if (type.equalsIgnoreCase("plain")) {
                    mTextView.setText(text);
                }

            } catch (JSONException e) {
                LOGD(TAG, "textViewSetValue", e);
            }
        }else{
            mTextView.setText((String) attrs);
        }
    }

    @Override
    public void clean() {
        mTextView = null;
        super.clean();
    }
}
