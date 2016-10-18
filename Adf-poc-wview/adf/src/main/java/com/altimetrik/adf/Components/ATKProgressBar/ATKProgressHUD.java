package com.altimetrik.adf.Components.ATKProgressBar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.UIUtils;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 6/18/15.
 */
public class ATKProgressHUD {

    private static final String TAG = makeLogTag(ATKProgressHUD.class);

    //Properties
    private JSONObject mProperties;
    private String mComponentId;
    private String mTitle;
    private String mSubtitle;
    private String mMode;
    private String mImage;
    private boolean mAnimated;
    private String mAnimation;
    private int mGraceTime;
    private int mMinShowTime;

    //Style
    private JSONObject mStyle;
    private String mBackgroundColor;
    private JSONObject mTitleFont;
    private String mTitleFontName;
    private int mTitleSize;
    private String mTitleColor;
    private JSONObject mSubtitleFont;
    private String mSubtitleFontName;
    private int mSubtitleSize;
    private String mSubtitleColor;
    private int mXOffset;
    private int mYOffset;
    private double mMargin;
    private double mCornerRadius;
    private boolean mDimBackground;

    //Progress Bar Layout
    private LinearLayout mProgressBarContainer;

    //Progress Bar
    private ProgressBar mProgressBar;

    public ATKProgressHUD(Context context, JSONObject definition) {

        mComponentId = definition.optString("componentId");

        mProperties = definition.optJSONObject("properties");
        mTitle = mProperties.optString("title");
        mSubtitle = mProperties.optString("subtitle");
        mMode = mProperties.optString("mode");
        mImage = mProperties.optString("image");
        mAnimated = mProperties.optBoolean("animated");
        mAnimation = mProperties.optString("animation");
        mGraceTime = mProperties.optInt("graceTime");
        mMinShowTime = mProperties.optInt("minShowTime");

        mStyle = definition.optJSONObject("style");
        mBackgroundColor = mStyle.optString("backgroundColor");

        mTitleFont = mStyle.optJSONObject("titleFont");
        if (mTitleFont != null) {
            mTitleFontName = mTitleFont.optString("name");
            mTitleSize = mTitleFont.optInt("size");
            mTitleColor = mTitleFont.optString("color");
        }

        mSubtitleFont = definition.optJSONObject("titleFont");
        if (mSubtitleFont != null) {
            mSubtitleFontName = mSubtitleFont.optString("name");
            mSubtitleSize = mSubtitleFont.optInt("size");
            mSubtitleColor = mSubtitleFont.optString("color");
        }

        mXOffset = mStyle.optInt("xOffset");
        mYOffset = mStyle.optInt("yOffset");
        mMargin = mStyle.optDouble("margin");
        mCornerRadius = mStyle.optDouble("cornerRadius");
        mDimBackground = mStyle.optBoolean("dimBackground");

        //Progress Bar Background
        mProgressBarContainer = new LinearLayout(context);
        mProgressBarContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams progressContainerParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        int progressContainerMargin = 0;
        if (mMargin != Double.NaN)
            progressContainerMargin = (int) mMargin;
        progressContainerParams.setMargins(progressContainerMargin + mXOffset, progressContainerMargin + mYOffset, progressContainerMargin, progressContainerMargin);
        mProgressBarContainer.setLayoutParams(progressContainerParams);

        float cornerRadius = 15;
        if (!Double.isNaN(mCornerRadius))
            cornerRadius = (float) mCornerRadius;

        float[] roundedCorners = new float[] { cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius};
        ShapeDrawable progressBackground = new ShapeDrawable(new RoundRectShape(roundedCorners, null, null));
        progressBackground.getPaint().setColor(UIUtils.parseColor(mBackgroundColor));
        mProgressBarContainer.setBackground(progressBackground);
        mProgressBarContainer.setPadding(15,15,15,15);

        //Progress Bar
        switch (mMode.toLowerCase()) {
            case Constants.ATK_PROGRESS_HUD_INDETERMINATE:
                mProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyle);
                mProgressBar.setIndeterminate(true);
                break;
            case Constants.ATK_PROGRESS_HUD_DETERMINATE:
                mProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
                mProgressBar.setIndeterminate(false);
                break;
            case Constants.ATK_PROGRESS_BAR_DETERMINATE_HORIZONTAL_BAR:
                mProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
                mProgressBar.setIndeterminate(false);
                break;
            default:
                mProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyle);
                mProgressBar.setIndeterminate(true);
                break;
        }

        mProgressBar.setMax(100);
        mProgressBar.getIndeterminateDrawable().setColorFilter(0xFFFFFFFF, PorterDuff.Mode.SRC_ATOP);
        mProgressBarContainer.addView(mProgressBar);

        if (!mTitle.isEmpty()) {
            TextView titleView = new TextView(context);
            titleView.setText(mTitle);
            if (!mTitleColor.isEmpty())
                titleView.setTextColor(UIUtils.parseColor(mTitleColor));
            if (mTitleSize > 0) {
                titleView.setTextSize(mTitleSize);
            }
            if (!mTitleFontName.isEmpty()) {
                String fontPath = String.format(Constants.ATK_FONT_DIR, mTitleFontName);
                try {
                    Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), fontPath));
                    titleView.setTypeface(font);
                } catch (Exception e) {
                    LOGW(TAG, String.format("Font %s not found.", fontPath));
                }
            }
            mProgressBarContainer.addView(titleView);
        }

        if (!mSubtitle.isEmpty()) {
            TextView subTitleView = new TextView(context);
            subTitleView.setText(mSubtitle);
            if (!mSubtitleColor.isEmpty())
                subTitleView.setTextColor(UIUtils.parseColor(mSubtitleColor));
            if (mSubtitleSize > 0) {
                subTitleView.setTextSize(mSubtitleSize);
            }
            if (!mSubtitleFontName.isEmpty()) {
                String fontPath = String.format(Constants.ATK_FONT_DIR, mSubtitleFontName);
                try {
                    Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), fontPath));
                    subTitleView.setTypeface(font);
                } catch (Exception e) {
                    LOGW(TAG, String.format("Font %s not found.", fontPath));
                }
            }
            mProgressBarContainer.addView(subTitleView);
        }
    }

    public LinearLayout getProgressBar() {
        return mProgressBarContainer;
    }

    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
    }

    public String getComponentId() {
        return mComponentId;
    }

    public void clean() {
        if (mProgressBarContainer != null) {
            mProgressBarContainer.removeAllViews();
            mProgressBarContainer = null;
        }
    }
}
