package com.altimetrik.adf.Components.ATKButton;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.Measurement;
import com.altimetrik.adf.Util.NinePatchUtils;
import com.altimetrik.adf.Util.UIUtils;
import com.altimetrik.adf.Util.Utils;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGI;
import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;
import static com.altimetrik.adf.Util.Utils.getDeviceDensity;

/**
 * Created by pigounet on 3/26/15.
 */
public class ATKButton extends ATKComponentBase implements View.OnClickListener, View.OnTouchListener {

    private static final String TAG = makeLogTag(ATKButton.class);

    public static final int DISABLE_CLICK_FOR_MILLIS = 500;

    private String mTitle;
    private String mImage;
    private String mImageSelected;
    private String mImagePressed;
    private int[] mImageIconInsets;
    private int[] mImageCapInsets;
    private int[] mImagePressedCapInsets;
    private int[] mImageSelectedCapInsets;
    private String mFontName;
    private Double mFontSize;
    private String mDisabled;
    private String mFontColor, mFontColorHighlighted, mFontColorSelected;
    private String mImageIcon, mImageIconSelected, mImageIconPressed;
    private Drawable mImageIconDrawable, mImageIconSelectedDrawable, mImageIconPressedDrawable;
    private Drawable mImageBackgroundDrawable, mImageBackgroundSelectedDrawable, mImageBackgroundPressedDrawable;
    private Drawable mDefaultBackgroundDrawable;

    private JSONObject mBadge;
    private String mBadgeText;
    private String mBadgeAlignment;
    private String mBadgeTextColor;
    private String mBadgeBackgroundColor;
    private Boolean mBadgeHidesWhenZero;
    private String mBadgeFontName;
    private int mBadgeFontSize;

    private FrameLayout mButtonBadgeContainer;
    private Button mButton;
    private ImageButton mImageButton;
    private BadgeView mBadgeView;

    private AtomicBoolean mSelected;
    private Boolean mHasDeselectedAction;

    private long mLastClickTime = 0;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {
        try {
            //Load base params --> super
            super.initWithJSON(widgetDefinition, context);

            //Load particular params
            mTitle = getProperties().optString("title");
            mDisabled = getProperties().optString("disabled");
            mImage = getStyle().optString("image");
            mImageSelected = getStyle().optString("imageSelected");
            mImagePressed = getStyle().optString("imagePressed");
            mFontName = getStyle().optString("fontName");
            mFontSize = getStyle().optDouble("fontSize", 0);
            mFontColor = getStyle().optString("fontColor");
            mFontColorHighlighted = getStyle().optString("fontColorHighlighted");
            mFontColorSelected = getStyle().optString("fontColorSelected");
            mImageIcon = getStyle().optString("imageIcon");
            mImageIconSelected = getStyle().optString("imageIconSelected");
            mImageIconPressed = getStyle().optString("imageIconPressed");

            String sImageCapInsets = getStyle().optString("imageCapInsets", null);
            if (sImageCapInsets != null) {
                mImageCapInsets = NinePatchUtils.parseInsetsArray(sImageCapInsets);
                mImagePressedCapInsets = mImageCapInsets;
                mImageSelectedCapInsets = mImageCapInsets;
            }
            String sImagePressedCapInsets = getStyle().optString("imagePressedCapInsets", null);
            if (sImagePressedCapInsets != null) {
                mImagePressedCapInsets = NinePatchUtils.parseInsetsArray(sImagePressedCapInsets);
            }
            String sImageSelectedCapInsets = getStyle().optString("imageSelectedCapInsets", null);
            if (sImageSelectedCapInsets != null) {
                mImageSelectedCapInsets = NinePatchUtils.parseInsetsArray(sImageSelectedCapInsets);
            }

            String sImageIconInsets = getStyle().optString("imageIconInsets", null);
            if (sImageIconInsets != null) {
                mImageIconInsets = NinePatchUtils.parseInsetsArray(sImageIconInsets);
            }

            if (mTitle.equals("")) {
                mImageButton = new ImageButton(context);
            } else {
                //Create View
                mButton = new Button(context);
            }

            View btn = mButton == null ? mImageButton : mButton;

            //Set base params --> super
            super.loadParams(btn);

            //Set button params
            if (mButton != null && !mFontName.isEmpty()) {
                String fontPath = String.format(Constants.ATK_FONT_DIR, mFontName);
                try {
                    Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), fontPath));
                    mButton.setTypeface(font);
                } catch (Exception e) {
                    LOGD(TAG, String.format("Font %s not found.", fontPath));
                }
            }

            // for Button
            if (mButton != null) {
                if (!mFontColor.isEmpty())
                    mButton.setTextColor(UIUtils.parseColor(mFontColor));

                if (mFontSize > 0) {
                    mButton.setTextSize(mFontSize.floatValue());
                } else {
                    mButton.setTextSize(0);
                    LOGD(TAG, "Invalid Font Size.");
                }

                mButton.setHorizontallyScrolling(true);
                mButton.setText(mTitle);
                mButton.setEllipsize(TextUtils.TruncateAt.END);

                if (!mImageIcon.equals("")) {
                    mButton.setPadding(10, 0, 10, 0);
                    mButton.setCompoundDrawablesWithIntrinsicBounds(getImageIconDrawable(),
                            null,
                            null,
                            null);
                }
            }

            // for ImageButton
            if (mImageButton != null && !mImageIcon.equals("")) {
                mImageButton.setImageDrawable(getImageIconDrawable());
                mImageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);

                if (mImageIconInsets != null) {
                    float density = getDeviceDensity(context);
                    mImageButton.setPadding((int) (mImageIconInsets[1] * density), (int) (mImageIconInsets[0] * density), (int) (mImageIconInsets[3] * density), (int) (mImageIconInsets[2] * density));
                } else {
                    mImageButton.setPadding(getWidth() / 4, getHeight() / 4, getWidth() / 4, getHeight() / 4);
                }
                mImageButton.setAdjustViewBounds(true);
            }

            if (!mImage.isEmpty()) {
                try {
                    btn.setBackground(getImageBackgroundDrawable());
                } catch (Exception e) {
                    LOGE(TAG, "initWithJSON", e);
                }
            } else {
                mDefaultBackgroundDrawable = btn.getBackground();
            }

            //region BUTTON WITH BADGE
            mBadge = getProperties().optJSONObject("badge");
            if (mBadge != null) {
                mBadgeText = mBadge.optString("text", "0");
                mBadgeAlignment = mBadge.optString("alignment");
                JSONObject badgeFont = mBadge.optJSONObject("font");
                if (badgeFont != null) {
                    mBadgeFontName = badgeFont.optString("name");
                    mBadgeFontSize = badgeFont.optInt("size");
                }
                mBadgeTextColor = mBadge.optString("textColor");
                mBadgeBackgroundColor = mBadge.optString("backgroundColor");
                mBadgeHidesWhenZero = mBadge.optBoolean("hidesWhenZero");

                mButtonBadgeContainer = new FrameLayout(context);
                mButtonBadgeContainer.addView(btn);

                mBadgeView = new BadgeView(context, btn);
                mBadgeView.setText(mBadgeText);

                switch (mBadgeAlignment.toLowerCase()) {
                    case Constants.ATK_BADGE_VIEW_ALIGNMENT_TOP_RIGHT:
                        mBadgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                        break;
                    case Constants.ATK_BADGE_VIEW_ALIGNMENT_TOP_LEFT:
                        mBadgeView.setBadgePosition(BadgeView.POSITION_TOP_LEFT);
                        break;
                    case Constants.ATK_BADGE_VIEW_ALIGNMENT_TOP_CENTER:
                        mBadgeView.setBadgePosition(BadgeView.POSITION_TOP_CENTER);
                        break;
                    case Constants.ATK_BADGE_VIEW_ALIGNMENT_BOTTOM_RIGHT:
                        mBadgeView.setBadgePosition(BadgeView.POSITION_BOTTOM_RIGHT);
                        break;
                    case Constants.ATK_BADGE_VIEW_ALIGNMENT_BOTTOM_LEFT:
                        mBadgeView.setBadgePosition(BadgeView.POSITION_BOTTOM_LEFT);
                        break;
                    case Constants.ATK_BADGE_VIEW_ALIGNMENT_BOTTOM_CENTER:
                        mBadgeView.setBadgePosition(BadgeView.POSITION_BOTTOM_CENTER);
                        break;
                    case Constants.ATK_BADGE_VIEW_ALIGNMENT_CENTER:
                        mBadgeView.setBadgePosition(BadgeView.POSITION_CENTER);
                        break;
                    case Constants.ATK_BADGE_VIEW_ALIGNMENT_CENTER_LEFT:
                        mBadgeView.setBadgePosition(BadgeView.POSITION_CENTER_LEFT);
                        break;
                    case Constants.ATK_BADGE_VIEW_ALIGNMENT_CENTER_RIGHT:
                        mBadgeView.setBadgePosition(BadgeView.POSITION_CENTER_RIGHT);
                        break;
                    default:
                        mBadgeView.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
                        break;
                }

                if (!mBadgeBackgroundColor.isEmpty())
                    mBadgeView.setBadgeBackgroundColor(UIUtils.parseColor(mBadgeBackgroundColor));

                if (!mBadgeTextColor.isEmpty())
                    mBadgeView.setTextColor(UIUtils.parseColor(mBadgeTextColor));

                if (mBadgeFontSize > 0)
                    mBadgeView.setTextSize(mBadgeFontSize);

                if (mBadgeFontName != null && !mBadgeFontName.isEmpty()) {
                    String fontPath = String.format(Constants.ATK_FONT_DIR, mBadgeFontName);
                    try {
                        Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), fontPath));
                        mBadgeView.setTypeface(font);
                    } catch (Exception e) {
                        LOGW(TAG, String.format("Font %s not found.", fontPath));
                    }
                }

                if (mBadgeHidesWhenZero && mBadgeText.equals("0")) {
                    mBadgeView.hide();
                } else {
                    mBadgeView.show();
                }
            }
            //endregion

            mSelected = new AtomicBoolean(false);
            boolean isSelected = getProperties().optBoolean("selected", false);
            if (isSelected) {
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            switchSelected();
                        } catch (Exception e) {
                            LOGD(TAG, "initWithJSON", e);
                        }
                    }
                });
            }

            mHasDeselectedAction = false;
            if (getActions() != null) {
                for (int i = 0; i < getActions().length(); i++) {
                    try {
                        JSONObject action = getActions().getJSONObject(i);
                        if (action.getString("event").compareTo(Constants.ATK_ACTION_DESELECT) == 0) {
                            mHasDeselectedAction = true;
                            break;
                        }
                    } catch (JSONException e) {
                        LOGD(TAG, "Deselected Action Search", e);
                    }
                }
            }

            updateEnabledStatus();

            onPostInitWithJSON();
        } catch (Exception ex) {
            LOGE(TAG, "initWithJSON", ex);
        }

        return this;
    }

    private synchronized void updateEnabledStatus() {
        View displayView = mButton == null ? mImageButton : mButton;
        if (Utils.isPositiveValue(mDisabled)) {
            displayView.setOnClickListener(null);
            displayView.setOnTouchListener(null);
            if (displayView.getBackground() != null) {
                displayView.getBackground().setColorFilter(UIUtils.parseColor("#F2F2F2"), PorterDuff.Mode.LIGHTEN);
            }
            if (mBadge == null && mButton != null) {
                mButton.setTextColor(Color.LTGRAY);
            }
        } else {
            displayView.setOnClickListener(this);
            displayView.setOnTouchListener(this);
            if (displayView.getBackground() != null) {
                displayView.getBackground().clearColorFilter();
            }
            if (mFontColor != null && !mFontColor.isEmpty() && mBadge == null && mButton != null) {
                mButton.setTextColor(UIUtils.parseColor(mFontColor));
            }
        }
    }

    private Drawable getImageIconDrawable() {
        if (mImageIconDrawable == null) {
            try {
                mImageIconDrawable = Drawable.createFromPath(UIUtils.getDeviceFilePath(mContext, mImageIcon));
            } catch (Exception e) {
                if (e.getMessage().contains("NotFound")) {
                    LOGE(TAG, String.format("File %s not Found", mImageIcon));
                } else {
                    LOGE(TAG, "getImageIconDrawable", e);
                }
            }
        }
        return mImageIconDrawable;
    }

    private Drawable getImageIconSelectedDrawable() {
        if (mImageIconSelectedDrawable == null) {
            try {
                mImageIconSelectedDrawable = Drawable.createFromPath(UIUtils.getDeviceFilePath(mContext, mImageIconSelected));
            } catch (Exception e) {
                if (e.getMessage().contains("NotFound")) {
                    LOGE(TAG, String.format("File %s not Found", mImageIconSelected));
                } else {
                    LOGE(TAG, "getImageIconSelectedDrawable", e);
                }
            }
        }
        return mImageIconSelectedDrawable;
    }

    private Drawable getImageIconPressedDrawable() {
        if (mImageIconPressedDrawable == null) {
            try {
                mImageIconPressedDrawable = Drawable.createFromPath(UIUtils.getDeviceFilePath(mContext, mImageIconPressed));
            } catch (Exception e) {
                if (e.toString().contains("NotFound")) {
                    LOGE(TAG, String.format("File %s not Found", mImageIconPressed));
                } else {
                    LOGE(TAG, "getImageBackgroundDrawable", e);
                }
            }
        }
        return mImageIconPressedDrawable;
    }

    private Drawable getImageBackgroundDrawable() {
        if (mImageBackgroundDrawable == null) {
            if (mImageCapInsets == null) {
                try {
                    mImageBackgroundDrawable = Drawable.createFromPath(UIUtils.getDeviceFilePath(mContext, mImage));
                } catch (Exception e) {
                    if (e.toString().contains("NotFound")) {
                        LOGE(TAG, String.format("File %s not Found", mImage));
                    } else {
                        LOGE(TAG, "getImageBackgroundDrawable", e);
                    }
                }
            } else {
                mImageBackgroundDrawable = (NinePatchUtils.displayNinePatch(UIUtils.getDeviceFilePath(mContext, mImage), mImageCapInsets, mContext));
            }
        }
        return mImageBackgroundDrawable;
    }

    private Drawable getImageBackgroundSelectedDrawable() {
        if (mImageBackgroundSelectedDrawable == null) {
            if (mImageSelectedCapInsets == null) {
                try {
                    mImageBackgroundSelectedDrawable = Drawable.createFromPath(UIUtils.getDeviceFilePath(mContext, mImageSelected));
                } catch (Exception e) {
                    if (e.toString().contains("NotFound")) {
                        LOGE(TAG, String.format("File %s not Found", mImageSelected));
                    } else {
                        LOGE(TAG, "getImageBackgroundDrawable", e);
                    }
                }
            } else {
                mImageBackgroundSelectedDrawable = (NinePatchUtils.displayNinePatch(UIUtils.getDeviceFilePath(mContext, mImageSelected), mImageSelectedCapInsets, mContext));
            }
        }
        return mImageBackgroundSelectedDrawable;
    }

    private Drawable getImageBackgroundPressedDrawable() {
        if (mImageBackgroundPressedDrawable == null) {
            if (mImagePressedCapInsets == null) {
                try {
                    mImageBackgroundPressedDrawable = Drawable.createFromPath(UIUtils.getDeviceFilePath(mContext, mImagePressed));
                } catch (Exception e) {
                    if (e.toString().contains("NotFound")) {
                        LOGE(TAG, String.format("File %s not Found", mImagePressed));
                    } else {
                        LOGE(TAG, "getImageBackgroundDrawable", e);
                    }
                }
            } else {
                mImageBackgroundPressedDrawable = (NinePatchUtils.displayNinePatch(UIUtils.getDeviceFilePath(mContext, mImagePressed), mImagePressedCapInsets, mContext));
            }
        }
        return mImageBackgroundPressedDrawable;
    }

    @Override
    public View getDisplayView() {
        if (mBadge != null) {
            return mButtonBadgeContainer;
        }
        return mButton == null ? mImageButton : mButton;
    }

    @Override
    public void setValue(Object attrs, Context context) {
        LOGW(TAG, "setValue NO-OP");
    }

    public synchronized void switchSelected() {
        try {
            View btn = mButton == null ? mImageButton : mButton;
            if (mSelected.compareAndSet(true, false)) {
                setNormalBackground(btn);
            } else {
                mSelected.set(true);
                setSelectedBackground(btn);
            }
        } catch (Exception e) {
            LOGD(TAG, "switchSelected", e);
        }
    }

    public synchronized void switchSelectedAction(boolean setSelected) {
        if ((setSelected && !mSelected.get()) || (!setSelected && mSelected.get())) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        switchSelected();
                    } catch (Exception e) {
                        LOGD(TAG, "switchSelectedAction", e);
                    }
                }
            });
        }
    }

    @Override
    public void onClick(View v) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < DISABLE_CLICK_FOR_MILLIS) {
            LOGI(TAG, "Clicks too close");
            return;
        }
        mLastClickTime = SystemClock.elapsedRealtime();
        switchSelected();
        if (getActions() != null) {
            for (int i = 0; i < getActions().length(); i++) {
                try {
                    JSONObject action = getActions().getJSONObject(i);
                    boolean shouldCLick = mHasDeselectedAction ? (action.getString("event").compareTo(Constants.ATK_ACTION_SELECT) == 0 && mSelected.get()) || (action.getString("event").compareTo(Constants.ATK_ACTION_DESELECT) == 0 && !mSelected.get()) : (action.getString("event").compareTo(Constants.ATK_ACTION_SELECT) == 0);
                    if (shouldCLick) {
                        JSONObject data = new JSONObject();
                        data.put("id", getID());
                        ATKEventManager.excecuteComponentAction(action, data);
                    }
                } catch (JSONException e) {
                    LOGD(TAG, "ATKButtonOnClickListener onClick", e);
                }
            }
        }
    }

    @Override
    public synchronized boolean onTouch(View v, MotionEvent event) {
        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                setPressedBackground(v);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mSelected.get()) {
                    setSelectedBackground(v);
                } else {
                    setNormalBackground(v);
                }
        }
        return false;
    }

    private synchronized void setNormalBackground(View v) {
        if (v != null) {
            if (v.getBackground() != null)
                v.getBackground().clearColorFilter();
            v.invalidate();
            if (!mImage.isEmpty()) {
                v.setBackground(getImageBackgroundDrawable());
            } else {
                v.setBackground(mDefaultBackgroundDrawable);
            }
            // for Button
            if (mButton != null) {
                if (!mImageIcon.isEmpty()) {
                    mButton.setCompoundDrawablesWithIntrinsicBounds(getImageIconDrawable(),
                            null,
                            null,
                            null);
                }

                if (!mFontColor.isEmpty())
                    mButton.setTextColor(UIUtils.parseColor(mFontColor));
            }

            // for ImageButton
            if (mImageButton != null && !mImageIcon.isEmpty()) {
                mImageButton.setImageDrawable(getImageIconDrawable());
            }
        }
    }

    private synchronized void setPressedBackground(View v) {
        if (v != null) {
            if (!mImagePressed.isEmpty()) {
                v.setBackground(getImageBackgroundPressedDrawable());
            }
            // for Button
            if (mButton != null) {
                if (!mImageIconPressed.isEmpty()) {
                    mButton.setCompoundDrawablesWithIntrinsicBounds(getImageIconPressedDrawable(),
                            null,
                            null,
                            null);
                }
                if (!mFontColorHighlighted.isEmpty())
                    mButton.setTextColor(UIUtils.parseColor(mFontColorHighlighted));
            }

            // for ImageButton
            if (mImageButton != null && !mImageIconPressed.isEmpty()) {
                mImageButton.setImageDrawable(getImageIconPressedDrawable());
            }
        }
    }

    private synchronized void setSelectedBackground(View v) {
        if (v != null) {
            if (!mImageSelected.isEmpty())
                v.setBackground(getImageBackgroundSelectedDrawable());

            // for Button
            if (mButton != null) {
                if (!mImageIconSelected.isEmpty()) {
                    mButton.setCompoundDrawablesWithIntrinsicBounds(getImageIconSelectedDrawable(),
                            null,
                            null,
                            null);
                }
                if (!mFontColorSelected.isEmpty()) {
                    mButton.setTextColor(UIUtils.parseColor(mFontColorSelected));
                } else if (!mFontColor.isEmpty()) {
                    mButton.setTextColor(UIUtils.parseColor(mFontColor));
                }
            }
            // for ImageButton
            if (mImageButton != null && !mImageIconSelected.isEmpty()) {
                mImageButton.setImageDrawable(getImageIconSelectedDrawable());
            }
        }
    }

    public void setBadgeValue(String value) {
        if (mBadgeView != null) {
            mBadgeView.setText(value);
            if (value.equals("0") && mBadgeHidesWhenZero) {
                mBadgeView.hide();
            } else {
                mBadgeView.show();
            }
        }

    }

    @Override
    public synchronized void setEnabled(boolean enabled) {
        if (enabled) {
            mDisabled = "NO";
        } else {
            mDisabled = "YES";
        }
        updateEnabledStatus();
    }

    @Override
    public void loadData(Object data, Context context) {
        String buttonText = ((JSONObject) data).optString("data");
        if (mButton != null && !buttonText.isEmpty()) {
            mButton.setText(buttonText);
        }
    }

    @Override
    public void clean() {
        if (mButtonBadgeContainer != null) {
            UIUtils.unbindDrawables(mButtonBadgeContainer);
            mButtonBadgeContainer = null;
        }
        if (mBadgeView != null) {
            mBadgeView.clean();
            mBadgeView = null;
        }
        Drawable[] dArray = new Drawable[]{
                mImageIconDrawable, mImageIconSelectedDrawable,
                mImageIconPressedDrawable, mImageBackgroundDrawable,
                mImageBackgroundSelectedDrawable, mImageBackgroundPressedDrawable
        };
        for (Drawable d : dArray) {
            if (d != null)
                d.setCallback(null);
        }
        super.clean();
    }

    @Override
    public void resize(int x, int y, int width, int height, Context context) {
        super.resize(x, y, width, height, context);
        if (mBadge != null) {
            View view = mButton == null ? mImageButton : mButton;
            int[] frame = Measurement.generateMeasurement(String.valueOf(x), String.valueOf(y), String.valueOf(width), String.valueOf(height), context, (View) view.getParent());
            view.getLayoutParams().width = frame[2];
            view.getLayoutParams().height = frame[3];
        }
    }
}