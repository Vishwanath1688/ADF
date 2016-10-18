package com.altimetrik.adf.Components.ATKSlidingComponent;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Util.Measurement;
import com.altimetrik.adf.Util.UIUtils;
import com.altimetrik.adf.Util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 7/31/15.
 */
public class ATKSlidingComponent extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKSlidingComponent.class);
    public static final int ANIMATION_DURATION = 300;
    public static final int ANIMATION_DELAY = 100;

    private RelativeLayout mSlidingComponent;
    private RelativeLayout mSlidingComponentMask;
    private View mParentView;

    private JSONArray mComponents;

    //Style
    private String mOverlayColor;
    private Double mOverlayOpacity;

    //Properties
    private String mTransition;
    private String mTransitionDirection;
    private Boolean mMoveSuperView;

    private boolean mOpenedMenu;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {

        try {
            super.initWithJSON(widgetDefinition, context);

            mOpenedMenu = true;

            mSlidingComponent = new RelativeLayout(context);
            loadParams(mSlidingComponent);

            mOverlayColor = getStyle().optString("overlayColor");
            mOverlayOpacity =  getStyle().optDouble("overlayOpacity");

            mTransition = getProperties().optString("transition");
            mTransitionDirection = getProperties().optString("transitionDirection").toLowerCase();
            mMoveSuperView = Utils.isPositiveValue(getProperties().optString("moveSuperView"));

            mSlidingComponentMask = new RelativeLayout(context);
            mSlidingComponentMask.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            GradientDrawable opacityBackground = new GradientDrawable();
            if (!mOverlayColor.isEmpty() && mOverlayColor.contains("#")) {
                if (mOverlayOpacity != 1) {
                    String bgColor = UIUtils.addAlpha(mOverlayColor, mOverlayOpacity);
                    opacityBackground.setColor(UIUtils.parseColor(bgColor));
                } else {
                    opacityBackground.setColor(UIUtils.parseColor(mOverlayColor));
                }
            }
            mSlidingComponentMask.setBackground(opacityBackground);

            mComponents = widgetDefinition.getJSONArray("components");
            loadComponents();

            mSlidingComponent.setVisibility(View.INVISIBLE);
            Handler handler = new Handler();
            switch(mTransitionDirection) {
                case "left":
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSlidingComponent.setX(mSlidingComponent.getX() + mSlidingComponent.getWidth());
                            mSlidingComponent.setVisibility(View.VISIBLE);
                            mSlidingComponent.animate().translationXBy(-mSlidingComponent.getWidth()).setDuration(ANIMATION_DURATION);
                        }
                    }, ANIMATION_DELAY);
                    break;
                case "right":
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSlidingComponent.setX(mSlidingComponent.getX() - mSlidingComponent.getWidth());
                            mSlidingComponent.setVisibility(View.VISIBLE);
                            mSlidingComponent.animate().translationXBy(mSlidingComponent.getWidth()).setDuration(ANIMATION_DURATION);
                        }
                    }, ANIMATION_DELAY);
                    break;
                case "up":
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSlidingComponent.setY(mSlidingComponent.getY() + mSlidingComponent.getHeight());
                            mSlidingComponent.setVisibility(View.VISIBLE);
                            mSlidingComponent.animate().translationYBy(-(mSlidingComponent.getHeight() + UIUtils.getStatusBarHeight(context))).setDuration(ANIMATION_DURATION);
                        }
                    }, ANIMATION_DELAY);
                    break;
                case "down":
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSlidingComponent.setY(mSlidingComponent.getY() - mSlidingComponent.getHeight());
                            mSlidingComponent.setVisibility(View.VISIBLE);
                            mSlidingComponent.animate().translationYBy(mSlidingComponent.getHeight()).setDuration(ANIMATION_DURATION);
                        }
                    }, ANIMATION_DELAY);
                    break;
            }

        } catch (JSONException e) {
            LOGE(TAG, "initWithJSON", e);
        }

        onPostInitWithJSON();

        return this;
    }

    @Override
    public View getDisplayView() {
        return mSlidingComponent;
    }

    private void loadComponents() {
        try {
            for (int i = 0; i < mComponents.length(); i++) {
                //create component and resize it using it's parent
                JSONObject currentComponent = mComponents.getJSONObject(i);
                ATKComponentBase widget = (ATKComponentBase) ATKComponentManager.getInstance().presentComponentInView(mContext, mSlidingComponent, currentComponent);
                View widgetView = widget.getDisplayView();
                widget.loadParams(widgetView);
                widgetView.setX(widget.getX());
                widgetView.setY(widget.getY());
                widgetView.requestLayout();
            }
        } catch (JSONException e) {
            LOGE(TAG, "loadComponents", e);
        }
    }

    public View getBackgroundMask() {
        mSlidingComponentMask.setAlpha(0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSlidingComponentMask.animate().alpha(1).setDuration(ANIMATION_DURATION);
            }
        }, ANIMATION_DELAY);
        return mSlidingComponentMask;
    }

    public void moveSuperView(final View mainView) {
        if (mMoveSuperView) {
            Handler handler = new Handler();
            switch(mTransitionDirection) {
                case "left":
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mainView.animate().translationXBy(-mSlidingComponent.getWidth()).setDuration(ANIMATION_DURATION);
                        }
                    }, ANIMATION_DELAY);
                    break;
                case "right":
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mainView.animate().translationXBy(mSlidingComponent.getWidth()).setDuration(ANIMATION_DURATION);
                        }
                    }, ANIMATION_DELAY);
                    break;
                case "up":
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mainView.animate().translationYBy(-mSlidingComponent.getHeight()).setDuration(ANIMATION_DURATION);
                        }
                    }, ANIMATION_DELAY);
                    break;
                case "down":
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mainView.animate().translationYBy(mSlidingComponent.getHeight()).setDuration(ANIMATION_DURATION);
                        }
                    }, ANIMATION_DELAY);
                    break;
            }
        }
    }

    public void closeSlidingComponent() {
        if (mOpenedMenu) {
            mOpenedMenu = false;
            if (mMoveSuperView) {
                switch (mTransitionDirection) {
                    case "left":
                        mParentView.animate().translationXBy(mSlidingComponent.getWidth()).setDuration(ANIMATION_DURATION);
                        break;
                    case "right":
                        mParentView.animate().translationXBy(-mSlidingComponent.getWidth()).setDuration(ANIMATION_DURATION);
                        break;
                    case "up":
                        mParentView.animate().translationYBy(mSlidingComponent.getHeight()).setDuration(ANIMATION_DURATION);
                        break;
                    case "down":
                        mParentView.animate().translationYBy(-mSlidingComponent.getHeight()).setDuration(ANIMATION_DURATION);
                        break;
                }
            }
            switch (mTransitionDirection) {
                case "left":
                    mSlidingComponent.animate().translationXBy(mSlidingComponent.getWidth()).setDuration(ANIMATION_DURATION);
                    break;
                case "right":
                    mSlidingComponent.animate().translationXBy(-mSlidingComponent.getWidth()).setDuration(ANIMATION_DURATION);
                    break;
                case "up":
                    mSlidingComponent.animate().translationYBy(mSlidingComponent.getHeight() + UIUtils.getStatusBarHeight(mContext)).setDuration(ANIMATION_DURATION);
                    break;
                case "down":
                    mSlidingComponent.animate().translationYBy(-mSlidingComponent.getHeight()).setDuration(ANIMATION_DURATION);
                    break;
            }
            mSlidingComponentMask.animate().alpha(0).setDuration(ANIMATION_DURATION);

            final Context contextDelayed = mContext;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ((Activity) contextDelayed).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mSlidingComponentMask != null && mSlidingComponentMask.getParent() != null) {
                                ((ViewGroup) (mSlidingComponentMask.getParent())).removeView(mSlidingComponentMask);
                            }
                            if (mSlidingComponent != null && mSlidingComponent.getParent() != null) {
                                ((ViewGroup) (mSlidingComponent.getParent())).removeView(mSlidingComponent);
                            }
                            ATKComponentManager.getInstance().getLoadedComponents().remove(getID());
                        }
                    });
                }
            }, ANIMATION_DURATION);
        }
    }

    @Override
    public void setValue(Object attrs, Context context) {

    }

    @Override
    public void clean() {
        mSlidingComponent = null;
        mSlidingComponentMask = null;
        mParentView = null;
        super.clean();
    }

    public void setParentView(View parentView)
    {
        mParentView = parentView;
    }

    public void rotate() {
        if (mSlidingComponent != null) {
            int previousComponentWidth = mSlidingComponent.getLayoutParams().width;
            int previousComponentHeight = mSlidingComponent.getLayoutParams().height;

            int[] frame = Measurement.generateMeasurement(mStringX, mStringY, mStringWidth, mStringHeight, mContext, null);

            int widthDifference = frame[2] - previousComponentWidth;
            int heightDifference = frame[3] - previousComponentHeight;

            if (mMoveSuperView) {
                switch (mTransitionDirection) {
                    case "left":
                        mParentView.setX(mParentView.getX() - widthDifference);
                        break;
                    case "right":
                        mParentView.setX(mParentView.getX() + widthDifference);
                        break;
                    case "up":
                        mParentView.setY(mParentView.getY() - heightDifference);
                        break;
                    case "down":
                        mParentView.setY(mParentView.getY() + heightDifference);
                        break;
                }
            }

            mSlidingComponent.setX(frame[0]);
            mSlidingComponent.setY(frame[1]);
            mSlidingComponent.getLayoutParams().width = frame[2];
            mSlidingComponent.getLayoutParams().height = frame[3];

            if (mTransitionDirection.equals("up"))
                mSlidingComponent.setY(frame[1] - UIUtils.getStatusBarHeight(mContext));

            mSlidingComponent.requestLayout();
            mSlidingComponent.removeAllViews();
            loadComponents();
        }
    }

}
