package com.altimetrik.adf.Components.ATKNavigationContainer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.UIUtils;
import com.altimetrik.adf.Util.Utils;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 9/17/15.
 */
public class ATKNavigationContainer extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKNavigationContainer.class);

    private int mBarHeight;
    private String mBarBackgroundColor;
    private String mBarBackgroundImage;
    private String mBarBackImage;
    private String mBarRightImage;
    private String mBarBubbleImage;
    private String mTitleFontName;
    private int mTitleFontSize;
    private String mTitleFontColor;
    private int mTitlePosition;
    private String mRootTitle;
    private JSONObject mRootComponent;

    private RelativeLayout mContainer;
    private LinearLayout mFirstContainer;
    private RelativeLayout mNavigationBar;
    private FrameLayout mComponentContainer;
    private TextView mNavBarTitle;
    private ImageView mBarRightImageView;
    private ImageView mBarBubbleImageView;
    private RelativeLayout mFloatingContainer;

    private List<ATKComponentBase> mStackedBundles;

    private float firstX;
    private float firstY;
    private float defaultThreshold = 20;
    private int defaultBubbleSize = 220;
    private int defaultBubblePadding = 30;
    private int defaultAnimationDuration = 300;
    private int defaultAnimationDelay = 100;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        super.initWithJSON(widgetDefinition, context);

        mStackedBundles = new ArrayList<>();

        //Main container
        mContainer = new RelativeLayout(context);
        super.loadParams(mContainer);

        //Contains navigation bar + root component
        mFirstContainer = new LinearLayout(context);
        mFirstContainer.setOrientation(LinearLayout.VERTICAL);
        mContainer.addView(mFirstContainer);
        super.loadParams(mFirstContainer);


        //Container that includes the back floating button
        mFloatingContainer = new RelativeLayout(context);
        mContainer.addView(mFloatingContainer);
        super.loadParams(mFloatingContainer);

        mRootTitle = getProperties().optString("rootTitle");
        mRootComponent = getProperties().optJSONObject("rootComponent");

        JSONObject navigationBar = getStyle().optJSONObject("navigationBar");
        if (navigationBar != null) {

            mBarHeight = navigationBar.optInt("barHeight", 0);
            mBarBackgroundColor = navigationBar.optString("backgroundColor");
            mBarBackgroundImage = navigationBar.optString("backgroundImage");
            mBarBackImage = navigationBar.optString("backImage");
            mBarRightImage = navigationBar.optString("rightImage");
            mBarBubbleImage = navigationBar.optString("bubbleImage");
            mTitleFontName = navigationBar.optString("titleFontName");
            mTitleFontSize = navigationBar.optInt("titleFontSize");
            mTitleFontColor = navigationBar.optString("titleFontColor");
            mTitlePosition = navigationBar.optInt("titleXPosition");

            mNavigationBar = new RelativeLayout(context);
            mNavigationBar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) (Utils.getDeviceDensity(context) * mBarHeight)));

            if (!mBarBackgroundColor.isEmpty()) {
                mNavigationBar.setBackgroundColor(UIUtils.parseColor(mBarBackgroundColor));
            } else {
                if (!mBarBackgroundImage.isEmpty()) {
                    try {
                        mNavigationBar.setBackground(Drawable.createFromPath(UIUtils.getDeviceFilePath(context, mBarBackgroundImage)));
                    } catch (Exception e) {
                        if (e.getMessage().contains("NotFound")) {
                            LOGE(TAG, String.format("File %s not Found", mBarBackgroundImage));
                        } else {
                            LOGE(TAG, "Get bar background drawable", e);
                        }
                    }
                }
            }

            if (!mBarRightImage.isEmpty()) {
                mBarRightImageView = new ImageView(context);
                RelativeLayout.LayoutParams rightBarImageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                rightBarImageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                int barRightImagePadding = (int) (Utils.getDeviceDensity(context) * mBarHeight * 0.2);
                mBarRightImageView.setPadding(0, barRightImagePadding, barRightImagePadding, barRightImagePadding);
                mBarRightImageView.setLayoutParams(rightBarImageParams);
                mBarRightImageView.setImageDrawable(Drawable.createFromPath(UIUtils.getDeviceFilePath(context, mBarRightImage)));
                mBarRightImageView.setAdjustViewBounds(true);
                mNavigationBar.addView(mBarRightImageView);
            }

            if (!mBarBubbleImage.isEmpty()) {
                mBarBubbleImageView = new ImageView(context);
                mBarBubbleImageView.setLayoutParams(new RelativeLayout.LayoutParams(defaultBubbleSize, defaultBubbleSize));
                mBarBubbleImageView.setImageDrawable(Drawable.createFromPath(UIUtils.getDeviceFilePath(context, mBarBubbleImage)));
                mBarBubbleImageView.setAdjustViewBounds(true);
                mBarBubbleImageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent event) {
                        float x = event.getRawX();
                        float y = event.getRawY();
                        switch (event.getAction() & MotionEvent.ACTION_MASK) {
                            case MotionEvent.ACTION_DOWN:
                                firstX = x;
                                firstY = y;
                                break;
                            case MotionEvent.ACTION_UP:
                                float xDiff = Math.abs(firstX - x);
                                float yDiff =  Math.abs(firstY - y);
                                if (!(xDiff > defaultThreshold || yDiff > defaultThreshold))
                                    pop();
                                break;
                            case MotionEvent.ACTION_MOVE:
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(view.getWidth(), view.getHeight());
                                view.setX((int) (x - (view.getWidth() / 2)));
                                view.setY((int) (y - view.getHeight() / 2));
                                view.setLayoutParams(params);
                                break;
                        }
                        return true;
                    }
                });
                mBarBubbleImageView.setX(context.getResources().getDisplayMetrics().widthPixels - (defaultBubbleSize + defaultBubblePadding));
                mBarBubbleImageView.setY(defaultBubblePadding);
                mBarBubbleImageView.setVisibility(View.GONE);

                mFloatingContainer.addView(mBarBubbleImageView);
            }

            mNavBarTitle = new TextView(context);
            mNavBarTitle.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            mNavBarTitle.setGravity(Gravity.CENTER_VERTICAL);
            mNavBarTitle.setPadding((int) (Utils.getDeviceDensity(context) * mTitlePosition), 0, 0, 0);

            mNavBarTitle.setText(mRootTitle);
            if (!mTitleFontColor.isEmpty())
                mNavBarTitle.setTextColor(UIUtils.parseColor(mTitleFontColor));

            if (mTitleFontSize > 0) {
                mNavBarTitle.setTextSize(mTitleFontSize);
            } else {
                mNavBarTitle.setTextSize(0);
                LOGD(TAG, "Invalid Font Size.");
            }

            if (!mTitleFontName.isEmpty()) {
                String fontPath = String.format(Constants.ATK_FONT_DIR, mTitleFontName);
                try {
                    Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), fontPath));
                    mNavBarTitle.setTypeface(font);
                } catch (Exception e) {
                    LOGW(TAG, String.format("Font %s not found.", fontPath));
                }
            }
            mNavigationBar.addView(mNavBarTitle);
            mFirstContainer.addView(mNavigationBar);
        }

        mComponentContainer = new FrameLayout(context);
        mComponentContainer.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, 1));
        mFirstContainer.addView(mComponentContainer);

        ATKComponentBase widget = (ATKComponentBase) ATKComponentManager.getInstance().presentComponentInView(context, mComponentContainer, mRootComponent);
        widget.getDisplayView().setX(widget.getX());
        widget.getDisplayView().setY(widget.getY());

        mStackedBundles.add(widget);

        onPostInitWithJSON();

        return this;
    }

    public void setTitle(String newTitle) {
        mNavBarTitle.setText(newTitle);
    }

    public void setNavBarBackground(String imageUrl, String color) {
        if (!color.isEmpty()) {
            mNavigationBar.setBackgroundColor(UIUtils.parseColor(color));
        } else {
            if (!imageUrl.isEmpty()) {
                try {
                    mNavigationBar.setBackground(Drawable.createFromPath(UIUtils.getDeviceFilePath(mContext, imageUrl)));
                } catch (Exception e) {
                    if (e.getMessage().contains("NotFound")) {
                        LOGE(TAG, String.format("File %s not Found", imageUrl));
                    } else {
                        LOGE(TAG, "Get bar background drawable", e);
                    }
                }
            }
        }
    }

    public void push(final JSONObject input) {

        final int currentBundle = mStackedBundles.size() - 1;
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //Create and animate component
                ATKComponentBase widget = (ATKComponentBase) ATKComponentManager.getInstance().presentComponentInView(mContext, mContainer, input);
                widget.getDisplayView().setY(widget.getY());
                widget.getDisplayView().setX(mContext.getResources().getDisplayMetrics().widthPixels);
                widget.getDisplayView().animate()
                        .setDuration(defaultAnimationDuration)
                        .translationX(0)
                        .setStartDelay(defaultAnimationDelay)
                        .setInterpolator(new AccelerateDecelerateInterpolator());

                //Hide copmonent after animation
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (currentBundle >= 1) {
                            ATKComponentBase currentBundleWidget = mStackedBundles.get(currentBundle);
                            currentBundleWidget.getDisplayView().setVisibility(View.GONE);
                        } else {
                            mFirstContainer.setVisibility(View.GONE);
                        }
                    }
                }, defaultAnimationDelay + defaultAnimationDuration);

                mStackedBundles.add(widget);
                mBarBubbleImageView.setVisibility(View.VISIBLE);
                mFloatingContainer.bringToFront();
            }
        });

    }

    public void pop() {

        final int currentBundleIndex = mStackedBundles.size() - 1;
        final ATKComponentBase currentBundle = mStackedBundles.get(currentBundleIndex);

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (currentBundleIndex == 1) {
                    mBarBubbleImageView.setVisibility(View.GONE);
                    mFirstContainer.setVisibility(View.VISIBLE);
                } else {
                    mStackedBundles.get(currentBundleIndex - 1).getDisplayView().setVisibility(View.VISIBLE);
                }

                //Animation hide
                currentBundle.getDisplayView().animate()
                        .setDuration(defaultAnimationDuration)
                        .translationX(mContext.getResources().getDisplayMetrics().widthPixels)
                        .setStartDelay(defaultAnimationDelay)
                        .setInterpolator(new AccelerateDecelerateInterpolator());

                //Remove component after hide animation
                new Handler().postDelayed( new Runnable() {
                    @Override
                    public void run() {
                        ATKComponentManager.getInstance().removeComponentByID(mContext, mStackedBundles.get(currentBundleIndex).getID());
                        mStackedBundles.remove(currentBundleIndex);
                    }
                } , defaultAnimationDuration + defaultAnimationDelay);
            }
        });
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
        mNavigationBar = null;
        mComponentContainer = null;
        mFirstContainer = null;
        mFloatingContainer = null;
        mContainer = null;
        mStackedBundles.clear();
        mStackedBundles = null;
        super.clean();
    }

}
