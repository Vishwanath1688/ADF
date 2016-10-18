package com.altimetrik.adf.Components.ATKAccordion;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.Measurement;
import com.altimetrik.adf.Util.UIUtils;
import com.squareup.picasso.Picasso;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 8/11/15.
 */
public class ATKAccordion extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKAccordion.class);

    //region STYLE VARIABLES
    private String mPaddingTop;
    private String mPaddingLeft;
    private String mItemWidth;
    private String mItemHeight;
    private String mItemLeftImageExpanded;
    private String mItemLeftImageCollapsed;
    private String mItemTitleFontName;
    private int mItemTitleFontSize;
    private String mItemTitleFontColor;
    private int mItemTitleX;
    private String mItemBackgroundColor;
    //endregion

    //region DATA VARIABLES
    private String mType;
    private JSONArray mItems;
    //endregion

    private ScrollView mScrollViewWrapper;
    private LinearLayout mContainerView;

    private HashMap<Integer, Boolean> mOpenStatus;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        super.initWithJSON(widgetDefinition, context);

        //Parse Style
        mPaddingTop = getStyle().optString("paddingTop");
        mPaddingLeft = getStyle().optString("paddingLeft");
        mItemWidth = getStyle().optString("itemWidth");
        mItemHeight = getStyle().optString("itemHeight");
        mItemLeftImageExpanded = getStyle().optString("itemLeftImageExpanded");
        mItemLeftImageCollapsed = getStyle().optString("itemLeftImageCollapsed");
        mItemTitleFontName = getStyle().optString("itemTitleFontName");
        mItemTitleFontSize = getStyle().optInt("itemTitleFontSize");
        mItemTitleFontColor = getStyle().optString("itemTitleFontColor");
        mItemTitleX = getStyle().optInt("itemTitleX");
        mItemBackgroundColor = getStyle().optString("itemBackgroundColor");

        //Parse Data
        mType = getData().optString("type");
        if (mType.equals("local")) {
            mItems = getData().optJSONArray("source");
        }

        mOpenStatus = new HashMap<>();

        //Init Views
        mScrollViewWrapper = new ScrollView(context);
        loadParams(mScrollViewWrapper);

        mContainerView = new LinearLayout(context);
        mContainerView.setOrientation(LinearLayout.VERTICAL);
        mContainerView.setLayoutParams(new LinearLayout.LayoutParams(mScrollViewWrapper.getLayoutParams().width, mScrollViewWrapper.getLayoutParams().height));

        loadAccordionItems();
        mScrollViewWrapper.addView(mContainerView);

        onPostInitWithJSON();

        return this;
    }

    //Loads each item into the accordion container (Items include Title and Content)
    private void loadAccordionItems() {
        try {
            for (int i = 0; i < mItems.length(); i++) {
                JSONObject item = (JSONObject) mItems.get(i);
                JSONObject itemComponent = item.optJSONObject("itemContentComponent");
                if (itemComponent != null)
                    mContainerView.addView(getNewItem(i, item.optString("itemTitle"), itemComponent, mContainerView));
            }
        } catch (JSONException e) {
            LOGE(TAG, "loadAccordionItems", e);
        }
    }

    //Creates a new Item with Title and Content
    private LinearLayout getNewItem(final int position, String itemName, JSONObject component, View parent) {

        if (!mOpenStatus.containsKey(position)) {
            mOpenStatus.put(position, false);
        }

        int[] frame = Measurement.generateMeasurement(mPaddingLeft, mPaddingTop, mItemWidth, mItemHeight, mContext, parent);

        //itemContainer -> Main layout that includes both title and content of the item
        final LinearLayout itemContainer = new LinearLayout(mContext);
        itemContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams itemContainerLayoutParams = new LinearLayout.LayoutParams(frame[2], LinearLayout.LayoutParams.WRAP_CONTENT);
        itemContainerLayoutParams.setMargins(frame[0], frame[1], 0, 0);
        itemContainer.setLayoutParams(itemContainerLayoutParams);

        //buttonTitleContainer -> Layout that includes the button and title of the accordion item
        LinearLayout buttonTitleContainer = new LinearLayout(mContext);
        buttonTitleContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams buttonTitleContainerLayoutParams = new LinearLayout.LayoutParams(frame[2], frame[3]);
        buttonTitleContainer.setLayoutParams(buttonTitleContainerLayoutParams);
        if (!mItemBackgroundColor.isEmpty())
            buttonTitleContainer.setBackgroundColor(UIUtils.parseColor(mItemBackgroundColor));

        //buttonImage -> Button in charge of showing and hiding the accordion item
        final ImageView buttonImage = new ImageView(mContext);
        int smallerSize = (frame[2] < frame[3]) ? frame[2] : frame[3];
        LinearLayout.LayoutParams buttonImageLayoutParams = new LinearLayout.LayoutParams(smallerSize, smallerSize);
        buttonImage.setLayoutParams(buttonImageLayoutParams);
        buttonImage.setTag(false);


        //region ACCORDION ITEM BUTTON CLICK LISTENER
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View buttonImageView) {

                String imageFilePath;

                final View component = itemContainer.getChildAt(1);
                boolean isOpen = mOpenStatus.get(position);

                if (isOpen) {

                    //If the item is already opened, the image is changed to the collapsed state and the item content view is hidden
                    imageFilePath = UIUtils.getDeviceFilePath(mContext, mItemLeftImageCollapsed);

                    final int targetHeight = component.getLayoutParams().height;

                    //Animation that progressively shrinks the item content view
                    Animation animation = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            if (interpolatedTime == 1) {
                                component.setVisibility(View.GONE);
                                component.getLayoutParams().height = targetHeight;
                            } else {
                                component.getLayoutParams().height = targetHeight - (int) (targetHeight * interpolatedTime);
                                component.requestLayout();
                            }
                        }

                        @Override
                        public boolean willChangeBounds() {
                            return true;
                        }
                    };
                    animation.setDuration(400);
                    component.startAnimation(animation);

                } else {

                    //If the item is closed, the image is changed to the open state and the item content view is shown
                    imageFilePath = UIUtils.getDeviceFilePath(mContext, mItemLeftImageExpanded);
                    component.setVisibility(View.VISIBLE);

                    final int targetHeight = component.getLayoutParams().height;
                    component.getLayoutParams().height = 0;

                    //Animation that progressively shows the item content view
                    Animation animation = new Animation() {
                        @Override
                        protected void applyTransformation(float interpolatedTime, Transformation t) {
                            component.getLayoutParams().height = (interpolatedTime == 1) ? targetHeight : (int) (targetHeight * interpolatedTime);
                            component.requestLayout();
                        }

                        @Override
                        public boolean willChangeBounds() {
                            return true;
                        }
                    };

                    animation.setDuration(400);
                    component.startAnimation(animation);

                }

                //Swap the item's state
                mOpenStatus.put(position, !isOpen);

                //Change the button's image
                Uri uri = Uri.fromFile(new File(imageFilePath));
                Picasso.with(mContext).load(uri).into((ImageView) buttonImageView);
            }
        });
        //endregion

        String imageFilePath;
        if (mOpenStatus.get(position)) {
            imageFilePath = UIUtils.getDeviceFilePath(mContext, mItemLeftImageExpanded);
        } else {
            imageFilePath = UIUtils.getDeviceFilePath(mContext, mItemLeftImageCollapsed);
        }
        Uri uri = Uri.fromFile(new File(imageFilePath));
        Picasso.with(mContext).load(uri).into(buttonImage);
        buttonTitleContainer.addView(buttonImage);

        //Load item title values
        TextView itemTitle = new TextView(mContext);
        LinearLayout.LayoutParams itemTitleLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        itemTitleLayoutParams.setMargins(mItemTitleX, 0, 0, 0);
        itemTitle.setLayoutParams(itemTitleLayoutParams);
        itemTitle.setGravity(Gravity.CENTER_VERTICAL);
        itemTitle.setText(itemName);
        if (!mItemTitleFontColor.isEmpty())
            itemTitle.setTextColor(UIUtils.parseColor(mItemTitleFontColor));
        if (mItemTitleFontSize > 0) {
            itemTitle.setTextSize(mItemTitleFontSize);
        } else {
            itemTitle.setTextSize(0);
            LOGD(TAG, "Invalid Font Size.");
        }
        if (!mItemTitleFontName.isEmpty()) {
            String fontPath = String.format(Constants.ATK_FONT_DIR, mItemTitleFontName);
            try {
                Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(mContext), fontPath));
                itemTitle.setTypeface(font);
            } catch (Exception e) {
                LOGW(TAG, String.format("Font %s not found.", fontPath));
            }
        }

        buttonTitleContainer.addView(itemTitle);
        itemContainer.addView(buttonTitleContainer);

        //Create and add content inside the container
        ATKComponentBase widget = (ATKComponentBase) ATKComponentManager.getInstance().presentComponentInView(mContext, itemContainer, component);
        if (!mOpenStatus.get(position)) {
            widget.getDisplayView().setVisibility(View.GONE);
        }

        return itemContainer;
    }

    @Override
    public View getDisplayView() {
        return mScrollViewWrapper;
    }

    @Override
    public void setValue(Object attrs, Context context) {

    }

    @Override
    public void resize(int x, int y, int width, int height, Context context) {
        super.resize(x, y, width, height, context);

        int[] frame = Measurement.generateMeasurement( String.valueOf(x), String.valueOf(y), String.valueOf(width), String.valueOf(height), context, (View) mContainerView.getParent());
        mContainerView.getLayoutParams().width = frame[2];
        mContainerView.getLayoutParams().height = frame[3];
        mContainerView.requestLayout();
        mContainerView.removeAllViews();
        loadAccordionItems();
    }

    @Override
    public void clean() {
        mContainerView.removeAllViews();
        mContainerView = null;
        mScrollViewWrapper = null;
        super.clean();
    }
}
