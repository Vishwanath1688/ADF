package com.altimetrik.adf.Components.ATKCollectionView.ATKCoverFlowView;

import android.content.Context;
import android.graphics.Point;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import static com.altimetrik.adf.Util.LogUtils.LOGD;

/**
 * Created by Dave Smith
 *
 * @devunwired Date: 8/17/12
 * PagerContainer
 */
public class CoverFlowPagerContainer extends RelativeLayout implements ViewPager.OnPageChangeListener {

    public static final String TAG = "CoverFlowPagerContainer";

    boolean mNeedsRedraw = false;
    private ViewPager mPager;
    private Point mCenter = new Point();
    private Point mInitialTouch = new Point();
    private GestureDetector mGestureDetector;

    public CoverFlowPagerContainer(Context context) {
        super(context);
        init();
    }

    public CoverFlowPagerContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CoverFlowPagerContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setClipChildren(false);
        mGestureDetector = new GestureDetector(getContext(), new SingleAndDoubleTapDetector());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        try {
            mPager = (ViewPager) getChildAt(0);
            mPager.setOnPageChangeListener(this);
        } catch (Exception e) {
            throw new IllegalStateException("The root child of PagerContainer must be a ViewPager");
        }
    }

    public ViewPager getViewPager() {
        return mPager;
    }

    public void setViewPager(ViewPager pager) {
        try {
            mPager = pager;
            mPager.setOnPageChangeListener(this);
        } catch (Exception e) {
            throw new IllegalStateException("The root child of PagerContainer must be a ViewPager");
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCenter.x = w / 2;
        mCenter.y = h / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //We capture any touches not already handled by the ViewPager
        // to implement scrolling from a touch outside the pager bounds.

        if (mGestureDetector.onTouchEvent(ev)) {
            LOGD(TAG, "Ignoring tap");
            return true;
        } else {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mInitialTouch.x = (int) ev.getX();
                    mInitialTouch.y = (int) ev.getY();
                default:
                    int x = mCenter.x - mInitialTouch.x;
                    int y = mCenter.y - mInitialTouch.y;
                    ev.offsetLocation(x, y);
                    break;
            }

            return mPager.dispatchTouchEvent(ev);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //Force the container to redraw on scrolling.
        //Without this the outer pages render initially and then stay static
        if (mNeedsRedraw) invalidate();
    }

    @Override
    public void onPageSelected(int position) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mNeedsRedraw = (state != ViewPager.SCROLL_STATE_IDLE);
    }

    private static class SingleAndDoubleTapDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return true;
        }
    }

}