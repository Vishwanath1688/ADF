package com.altimetrik.adf.Components.ATKChartManager.ATKCharts;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.altimetrik.adf.Components.ATKChartManager.ATKBaseChart;
import com.altimetrik.adf.Components.ATKChartManager.HoloCircularProgressBar;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.UIUtils;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 8/3/15.
 */
public class ATKCircleChart extends ATKBaseChart {

    private static final String TAG = makeLogTag(ATKCircleChart.class);

    private RelativeLayout mProgressBarContainer;
    private HoloCircularProgressBar mHoloCircularProgressBar;

    private ObjectAnimator mProgressBarAnimator;

    private JSONObject mValues;
    private Float mTotalValue;
    private Float mCurrentValue;

    private String mStrokeColor;
    private String mShadowColor;
    private String mFontName;
    private int mFontSize;
    private String mFontColor;

    private String mTitle;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {
        super.initWithJSON(widgetDefinition, context);

        mHoloCircularProgressBar = new HoloCircularProgressBar(context);
        mProgressBarContainer = new RelativeLayout(context);
        mProgressBarContainer.setGravity(Gravity.CENTER);
        mProgressBarContainer.addView(mHoloCircularProgressBar);
        addChartToContainer(mProgressBarContainer);

        mTotalValue = Float.parseFloat(mValues.optString("totalValue", "1"));
        mCurrentValue = Float.parseFloat(mValues.optString("currentValue", "0"));
        mShadowColor = getStyle().optString("shadowColor");
        mStrokeColor = getStyle().optString("strokeColor");

        mTitle = getProperties().optString("title");
        mFontName = getStyle().optString("fontName");
        mFontColor = getStyle().optString("fontColor");
        mFontSize = getStyle().optInt("fontSize");

        mTitleLabel = new TextView(context);
        mTitleLabel.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mTitleLabel.setGravity(Gravity.CENTER);
        mTitleLabel.setText(mTitle);

        if (!mFontColor.isEmpty())
            mTitleLabel.setTextColor(UIUtils.parseColor(mFontColor));

        if (mFontSize > 0)
            mTitleLabel.setTextSize(mFontSize);

        if (!mFontName.isEmpty()) {
            String fontPath = String.format(Constants.ATK_FONT_DIR, mFontName);
            try {
                Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), fontPath));
                mTitleLabel.setTypeface(font);
            } catch (Exception e) {
                LOGW(TAG, String.format("Font %s not found.", fontPath));
            }
        }

        mContainer.addView(mTitleLabel, 0);

        mHoloCircularProgressBar.setProgressColor(UIUtils.parseColor(mStrokeColor));
        mHoloCircularProgressBar.setProgressBackgroundColor(UIUtils.parseColor(mShadowColor));
        mHoloCircularProgressBar.setWheelSize(20);

        animate(mHoloCircularProgressBar, null, mCurrentValue / mTotalValue);

        return this;
    }

    @Override
    protected void readData() {
        mType = getData().optString("type");
        if (mType.equals("local")) {
            mValues = getData().optJSONObject("source");
        }
    }

    @Override
    public void loadData(Object data, Context context) {
        JSONObject chartData = ((JSONObject) data).optJSONObject("data");

        if (chartData != null) {
            mType = chartData.optString("type");
            if (mType.equals("local")) {
                mValues = chartData.optJSONObject("source");
            }
        }

        mTotalValue = Float.parseFloat(mValues.optString("totalValue", "1"));
        mCurrentValue = Float.parseFloat(mValues.optString("currentValue", "0"));
        animate(mHoloCircularProgressBar, null, mCurrentValue / mTotalValue);

    }

    private void animate(final HoloCircularProgressBar progressBar,
                         final Animator.AnimatorListener listener, float progress) {
        int duration = DEFAULT_ANIMATION_DURATION;
        animate(progressBar, listener, progress, duration);
    }

    private void animate(final HoloCircularProgressBar progressBar, final Animator.AnimatorListener listener,
                         final float progress, final int duration) {

        mProgressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progress);
        mProgressBarAnimator.setDuration(duration);

        mProgressBarAnimator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationCancel(final Animator animation) {
            }

            @Override
            public void onAnimationEnd(final Animator animation) {
                progressBar.setProgress(progress);
            }

            @Override
            public void onAnimationRepeat(final Animator animation) {
            }

            @Override
            public void onAnimationStart(final Animator animation) {
            }
        });
        if (listener != null) {
            mProgressBarAnimator.addListener(listener);
        }
        mProgressBarAnimator.reverse();
        mProgressBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(final ValueAnimator animation) {
                progressBar.setProgress((Float) animation.getAnimatedValue());
            }
        });
        progressBar.setMarkerProgress(progress);
        mProgressBarAnimator.start();
    }

    @Override
    public void clean() {
        mProgressBarContainer.removeAllViews();
        mProgressBarContainer = null;
        mHoloCircularProgressBar = null;
        mProgressBarAnimator = null;
        super.clean();
    }
}
