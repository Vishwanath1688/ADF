package com.altimetrik.adf.Components.ATKScrollView;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Util.Measurement;
import com.altimetrik.adf.Util.UIUtils;
import com.altimetrik.adf.Util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;
import static com.altimetrik.adf.Util.Utils.getDeviceDensity;

/**
 * Created by icabanas on 4/28/15.
 */
public class ATKScrollView extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKScrollView.class);

    //Properties
    private Boolean mScrollable;
    private Boolean mPagingEnabled;
    private Boolean mShowsDots;
    private int mNumberOfPages;

    //Style
    private String mBackgroundColor;
    private String mCurrentPageIndicatorColor;
    private String mPageIndicatorColor;
    private String mPageIndicatorPositionY;

    private JSONArray mComponents;

    private RelativeLayout mScrollView;
    private ViewPager mViewPager;
    private ViewGroup mVerticalScroll;
    private HScroll mHorizontalScroll;

    private float mx, my;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {

        try {
            //Load base params --> super
            super.initWithJSON(widgetDefinition, context);

            //Load properties
            mScrollable =  getProperties().has("scrollable") ? Utils.isPositiveValue(getProperties().getString("scrollable")) : false;
            mPagingEnabled = getProperties().has("pagingEnabled") ? Utils.isPositiveValue(getProperties().getString("pagingEnabled")) : false;
            mShowsDots = getProperties().has("showsDots") ? Utils.isPositiveValue(getProperties().getString("showsDots")) : false;
            mNumberOfPages = getProperties().has("numberOfPages") ? getProperties().getInt("numberOfPages") : 1;

            //Load style
            mBackgroundColor = getStyle().optString("backgroundColor");
            mCurrentPageIndicatorColor = getStyle().optString("currentPageIndicatorColor");
            mPageIndicatorColor = getStyle().optString("pageIndicatorColor");
            mPageIndicatorPositionY = getStyle().optString("pageIndicatorPositionY", "0");

            mComponents = widgetDefinition.getJSONArray("components");

            mScrollView = new RelativeLayout(context);
            loadParams(mScrollView);

            if (!mBackgroundColor.isEmpty()) {
                mScrollView.setBackgroundColor(UIUtils.parseColor(mBackgroundColor));
            }

            //If paging enabled, creates a View Pager. If not, creates an horizontal/vertical scroll view
            if (mPagingEnabled) {

                mViewPager = new OptionalSwipeViewPager(context, mScrollable);
                //mViewPager.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                mScrollView.addView(mViewPager);
                updateViewSize(mViewPager);

                PagerAdapter adapter = new ATKScrollViewAdapter(mScrollView, mComponents, context, mNumberOfPages);
                mViewPager.setAdapter(adapter);
                mViewPager.setOffscreenPageLimit(adapter.getCount());
                mViewPager.setClipChildren(false);

                try {
                    Field mScroller = ViewPager.class.getDeclaredField("mScroller");
                    mScroller.setAccessible(true);
                    mScroller.set(mViewPager, new FixedSpeedScroller(mViewPager.getContext(),  new DecelerateInterpolator()));
                } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
                    LOGD(TAG, "scroller", e);
                }

                //If dots need to be shown, creates a page indicator
                if (mShowsDots) {
                    CirclePageIndicator pageIndicator = new CirclePageIndicator(context);
                    RelativeLayout.LayoutParams pageIndicatorLayoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.MATCH_PARENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);

                    int numericPositionY = Measurement.getIntValue(mPageIndicatorPositionY, getHeight(), "height", context, false);
                    if (numericPositionY > 0) {
                        pageIndicatorLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        pageIndicatorLayoutParams.setMargins(20, numericPositionY, 0, 0);
                    } else {
                        pageIndicatorLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                        pageIndicatorLayoutParams.setMargins(20, 0, 0, 20);
                    }

                    pageIndicator.setLayoutParams(pageIndicatorLayoutParams);
                    pageIndicator.setViewPager(mViewPager);

                    final float density = getDeviceDensity(context);
                    pageIndicator.setRadius(4.5f * density);
                    if (!mCurrentPageIndicatorColor.isEmpty()) {
                        pageIndicator.setFillColor(UIUtils.parseColor(mCurrentPageIndicatorColor));
                    }
                    if (!mPageIndicatorColor.isEmpty()) {
                        pageIndicator.setPageColor(UIUtils.parseColor(mPageIndicatorColor));
                    }

                    mScrollView.addView(pageIndicator);
                }

            } else {

                //init vertical scroll component
                if (mScrollable) {
                    mVerticalScroll = new VScroll(context);
                } else {
                    mVerticalScroll = new RelativeLayout(context);
                }
                mScrollView.addView(mVerticalScroll);
                updateViewSize(mVerticalScroll);

                //init component container
                RelativeLayout componentContainer = new RelativeLayout(context);
		        componentContainer.setMotionEventSplittingEnabled(false);

                if (mScrollable) {
                    componentContainer.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                } else {
                    updateViewSize(componentContainer);
                }

                mVerticalScroll.addView(componentContainer);

                float containerHeight = 0;
                float containerWidth = 0;

                for (int i = 0; i < mComponents.length(); i++) {
                    //create component and resize it using it's parent
                    JSONObject currentComponent =  mComponents.getJSONObject(i);
                    ATKComponentBase widget = (ATKComponentBase) ATKComponentManager.getInstance().presentComponentInView(context, componentContainer, currentComponent);

                    widget.getDisplayView().setX(widget.getX());
                    widget.getDisplayView().setY(widget.getY());

                    float currentContainerHeight = widget.getDisplayView().getLayoutParams().height + widget.getDisplayView().getY();
                    float currentContainerWidth = widget.getDisplayView().getLayoutParams().width + widget.getDisplayView().getX();

                    containerHeight = (currentContainerHeight > containerHeight) ? currentContainerHeight : containerHeight;
                    containerWidth = (currentContainerWidth > containerWidth) ? currentContainerWidth : containerWidth;
                }

                if (containerWidth > context.getResources().getDisplayMetrics().widthPixels && mScrollable) {

                    mVerticalScroll.removeAllViews();

                    //init horizontal scroll component
                    mHorizontalScroll = new HScroll(context);
                    mHorizontalScroll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

                    //resize container so it shows every component added
                    componentContainer.getLayoutParams().width = (int) containerWidth;
                    componentContainer.getLayoutParams().height = (int) containerHeight;

                    mHorizontalScroll.addView(componentContainer);
                    mVerticalScroll.addView(mHorizontalScroll);

                } else {
                    //resize container so it shows every component added
                    componentContainer.getLayoutParams().width = (int) containerWidth;
                    componentContainer.getLayoutParams().height = (int) containerHeight;
                }

                if (mScrollable) {
                    //Implement the touch listener so it supports both horizontal and vertical scrolling
                    mScrollView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent event) {
                            float curX, curY;

                            switch (event.getAction()) {

                                case MotionEvent.ACTION_DOWN:
                                    mx = event.getX();
                                    my = event.getY();
                                    break;
                                case MotionEvent.ACTION_MOVE:
                                    curX = event.getX();
                                    curY = event.getY();
                                    mVerticalScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                                    if (mHorizontalScroll != null)
                                        mHorizontalScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                                    mx = curX;
                                    my = curY;
                                    break;
                                case MotionEvent.ACTION_UP:
                                    curX = event.getX();
                                    curY = event.getY();
                                    mVerticalScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                                    if (mHorizontalScroll != null)
                                        mHorizontalScroll.scrollBy((int) (mx - curX), (int) (my - curY));
                                    break;
                            }

                            return true;
                        }
                    });
                }
            }
        } catch (JSONException e) {
            LOGE(TAG, "initWithJSON", e);
        }

        onPostInitWithJSON();

        return this;
    }

    public void goToPage(int pageNum) {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(pageNum);

        } else {
            LOGD(TAG, "Not a paged Scroll View.");
        }
    }

    @Override
    public View getDisplayView() {
        return mScrollView;
    }

    @Override
    public void setValue(Object attrs, Context context) { }

    @Override
    public void clean() {
        if (mScrollView != null) {
            mScrollView.removeAllViews();
            mScrollView = null;
        }
        if (mHorizontalScroll != null) {
            mHorizontalScroll.removeAllViews();
            mHorizontalScroll = null;
        }
        if (mVerticalScroll != null) {
            mVerticalScroll.removeAllViews();
            mVerticalScroll = null;
        }
        super.clean();
    }

    private class FixedSpeedScroller extends Scroller {

        private int mDuration = 600;

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        public void setScrollDuration(int duration) {
            mDuration = duration;
        }

    }

    public class OptionalSwipeViewPager extends ViewPager {

        private boolean mEnabled;

        public OptionalSwipeViewPager(Context context, boolean enabled) {
            super(context);
            mEnabled = enabled;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (mEnabled) {
                return super.onTouchEvent(event);
            }

            return false;
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            if (mEnabled) {
                return super.onInterceptTouchEvent(event);
            }
            return false;
        }

    }

}