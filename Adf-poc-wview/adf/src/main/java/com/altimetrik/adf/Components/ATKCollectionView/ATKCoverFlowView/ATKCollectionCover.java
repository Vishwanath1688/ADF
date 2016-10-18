package com.altimetrik.adf.Components.ATKCollectionView.ATKCoverFlowView;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.altimetrik.adf.Components.ATKCollectionView.ATKCollectionBase;
import com.altimetrik.adf.Components.ATKWidget;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;
import static com.altimetrik.adf.Util.Utils.getDeviceDensity;

/**
 * Created by esalazar on 4/29/15.
 */
public class ATKCollectionCover extends ATKCollectionBase {

    private static final String TAG = makeLogTag(ATKCollectionCover.class);

    private RelativeLayout mCollectionView;

    private CoverFlowAdapter mCoverFlowAdapter;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {
        super.initWithJSON(widgetDefinition, context);

        try {
            mCollectionView = new RelativeLayout(context);
            loadParams(mCollectionView);
            mCollectionView.setGravity(RelativeLayout.CENTER_HORIZONTAL);

            CoverFlowPagerContainer coverFlowPagerContainer = new CoverFlowPagerContainer(context);
            coverFlowPagerContainer.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            ViewPager pager = new ViewPager(context);
            pager.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            pager.setOverScrollMode(View.OVER_SCROLL_NEVER);

            HashMap<String, Object> pagerDataMap = new HashMap();
            pagerDataMap.put("data", getItems());
            pagerDataMap.put("layout", getItemLayout());
            pagerDataMap.put("actions", getActions());
            pagerDataMap.put("itemID", getID());

            mCoverFlowAdapter = new CoverFlowAdapter(pagerDataMap, context);
            pager.setAdapter(mCoverFlowAdapter);
            mCoverFlowAdapter.setPager(pager);

            //Necessary or the pager will only have one extra page to show
            // make this at least however many pages you can see
            pager.setOffscreenPageLimit(mCoverFlowAdapter.getCount());

            //A little space between pages
            pager.setPageMargin(-80);

            //If hardware acceleration is enabled, you should also remove
            // clipping on the pager for its children.
            pager.setClipChildren(false);

            pager.setPageTransformer(true, new CarouselPageTransformer());

            float density = getDeviceDensity(context);
            int horizontalMargin = (int) ((this.getWidth() - getItemLayout().getInt("width") * density) / 2);
            int verticalMargin = (int) ((this.getHeight() - getItemLayout().getInt("height") * density) / 2);

            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) pager.getLayoutParams();
            marginLayoutParams.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
            pager.setLayoutParams(marginLayoutParams);

            coverFlowPagerContainer.addView(pager);
            coverFlowPagerContainer.setViewPager(pager);
            mCollectionView.addView(coverFlowPagerContainer);
        } catch (JSONException e) {
            LOGD(TAG, "initWithJSON", e);
        }
        return this;
    }

    @Override
    public void clean() {
        mCollectionView.removeAllViews();
        mCollectionView = null;
        mCoverFlowAdapter.setPager(null);
        super.clean();
    }

    @Override
    public View getDisplayView() {
        return mCollectionView;
    }

    public static class CarouselPageTransformer implements android.support.v4.view.ViewPager.PageTransformer {
        @Override
        public void transformPage(View page, float position) {
            float scaleFactor = 0.3f;
            float rotationFactor = 25;

            if (position < 0) {
                page.setRotationY(rotationFactor * -position);

                float scale = 1 + scaleFactor * position;
                page.setScaleX(scale);
                page.setScaleY(scale);

            } else {

                page.setRotationY(rotationFactor * -position);
                float scale = 1 - scaleFactor * position;
                page.setScaleX(scale);
                page.setScaleY(scale);
            }

            page.setBackgroundColor(Color.TRANSPARENT);
        }
    }
}
