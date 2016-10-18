package com.altimetrik.adf.Components.ATKModal;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.R;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.UIUtils;
import com.altimetrik.adf.Util.Utils;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 5/4/15.
 */
public class ATKModal extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKModal.class);

    private Boolean mDisplayHeader;
    private String mHeaderBackgroundColor;
    private String mHeaderFontName;
    private int mHeaderFontSize;
    private String mHeaderFontColor;
    private String mHeaderTitle;
    private String mHeaderCloseImagePath;
    private Boolean mCancellable;
    private String mDisplayTransition;

    private AlertDialog mDialog;
    private JSONArray mComponents;

    private ModalView mDialogContainer;
    private RelativeLayout mComponentContainer;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        super.initWithJSON(widgetDefinition, context);

        try {
            mDisplayHeader = Utils.isPositiveValue(getStyle().optString("displaysHeader"));
            mHeaderBackgroundColor = getStyle().optString("headerBackgroundColor", "#4d4d4d");
            mHeaderFontName = getStyle().optString("headerFontName");
            mHeaderFontSize = getStyle().optInt("headerFontSize", 16);
            mHeaderFontColor = getStyle().optString("headerFontColor", "#FFFFFF");
            mHeaderTitle = getStyle().optString("headerTitle");
            mHeaderCloseImagePath = getStyle().optString("headerCloseImage");
            mDisplayTransition = getProperties().optString("transition");

            mCancellable = getProperties().optBoolean("cancellable", true);

            mComponents = widgetDefinition.getJSONArray("components");

            //Create the content of the modal
            LinearLayout popupContent = new LinearLayout(context);
            popupContent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            popupContent.setOrientation(LinearLayout.VERTICAL);
            popupContent.setGravity(Gravity.CENTER);
            popupContent.setBackgroundColor(Color.WHITE);

            mComponentContainer = new RelativeLayout(context);
            RelativeLayout.LayoutParams componentContainerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            mComponentContainer.setLayoutParams(componentContainerParams);

            float containerWidth = 0;
            for (int i = 0; i < mComponents.length(); i++) {
                ATKComponentBase widget;
                try {
                    //create component and resize it using it's parent
                    JSONObject currentComponent = mComponents.getJSONObject(i);
                    widget = (ATKComponentBase) ATKComponentManager.getInstance().presentComponentInView(context, mComponentContainer, currentComponent);
                    widget.loadParams(widget.getDisplayView());
                    widget.getDisplayView().setX(widget.getX());
                    widget.getDisplayView().setY(widget.getY());

                    float currentContainerWidth = widget.getWidth() + widget.getX();
                    containerWidth = (currentContainerWidth > containerWidth) ? currentContainerWidth : containerWidth;

                } catch (JSONException e) {
                    LOGD(TAG, "initWithJSON", e);
                }
            }

            //Create header if enabled
            if (mDisplayHeader) {
                RelativeLayout header = new RelativeLayout(context);
                header.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

                if (!mHeaderBackgroundColor.isEmpty())
                    header.setBackgroundColor(UIUtils.parseColor(mHeaderBackgroundColor));

                //Header Title implementation
                TextView headerTitle = new TextView(context);

                RelativeLayout.LayoutParams headerTitleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                headerTitle.setLayoutParams(headerTitleParams);

                headerTitle.setText(mHeaderTitle);
                headerTitle.setTextSize(mHeaderFontSize);
                headerTitle.setTextColor(UIUtils.parseColor(mHeaderFontColor));
                headerTitle.setPadding(15, 15, 15, 15);

                if (!mHeaderFontName.isEmpty()) {
                    String fontPath = String.format(Constants.ATK_FONT_DIR, mHeaderFontName);
                    try {
                        Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), fontPath));
                        headerTitle.setTypeface(font);
                    } catch (Exception e) {
                        LOGD(TAG, String.format("Font %s not found.", fontPath), e);
                    }
                }

                header.addView(headerTitle);

                //Close button implementation
                ImageView closeButton = new ImageView(context);

                RelativeLayout.LayoutParams closeButtonParams = new RelativeLayout.LayoutParams(60, 60);
                closeButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                closeButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);
                closeButtonParams.setMargins(0, 0, 15, 0);
                closeButton.setLayoutParams(closeButtonParams);

                try {
                    closeButton.setImageDrawable(Drawable.createFromPath(UIUtils.getDeviceFilePath(context, mHeaderCloseImagePath)));
                } catch (Exception e) {
                    LOGW(TAG, "Close button image path not found.", e);
                }

                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.dismiss();
                    }
                });

                header.addView(closeButton);

                popupContent.addView(header);
            }

            popupContent.addView(mComponentContainer);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setCancelable(mCancellable);
            mDialog = alertDialogBuilder.create();

            //Fade In is the default animation, so the animation is different there has to be an override
            if (mDisplayTransition.equals(Constants.ATK_MODAL_TRANSITION_FADE_IN))
                mDialog.getWindow().getAttributes().windowAnimations = R.style.FadeInAnimation;

            //Animation Slide In Right Animation
            if (mDisplayTransition.equals(Constants.ATK_MODAL_TRANSITION_SLIDE_IN_FROM_RIGHT))
                mDialog.getWindow().getAttributes().windowAnimations = R.style.SlideInRightAnimation;

            //Animation Fall Down
            if (mDisplayTransition.equals(Constants.ATK_MODAL_TRANSITION_FALL_DOWN))
                mDialog.getWindow().getAttributes().windowAnimations = R.style.FallDownAnimation;

            mDialog.show();
            mDialog.setContentView(popupContent);

            int maxModalWidth = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.9);
            if (containerWidth > maxModalWidth) {
                containerWidth = maxModalWidth;
            }

            WindowManager.LayoutParams dialogParams = new WindowManager.LayoutParams();
            dialogParams.copyFrom(mDialog.getWindow().getAttributes());
            dialogParams.width = (int) (containerWidth + (30 * Utils.getDeviceDensity(context)));
            mDialog.getWindow().setAttributes(dialogParams);

            mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            mDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (getActions() != null) {
                        try {
                            for (int i = 0; i < getActions().length(); i++) {
                                JSONObject action = getActions().getJSONObject(i);
                                if (action.getString("event").compareTo(Constants.ATK_ACTION_CANCEL) == 0) {
                                    JSONObject data = new JSONObject();
                                    data.put("id", getID());
                                    ATKEventManager.excecuteComponentAction(action, data);
                                }
                            }
                        } catch (JSONException e) {
                            LOGD(TAG, "ATKModal ATKActionCancel onClick", e);
                        }
                    }
                    ATKComponentManager.getInstance().removeComponentByID(mContext, getID());
                }
            });

            mDialogContainer = new ModalView(context, mDialog);
            mDialogContainer.setVisibility(View.GONE);

        } catch (JSONException e) {
            LOGE(TAG, "initWithJSON", e);
        }

        onPostInitWithJSON();

        return this;
    }

    @Override
    public View getDisplayView() {
        return mDialogContainer;
    }

    @Override
    public void setValue(Object attrs, Context context) {
    }

    @Override
    public void loadData(Object data, Context context) {
        LOGW(TAG, "loadData on ATKModal"); // no-op because Modal doesn't have x, y, width or height
    }

    @Override
    public void clean() {
        mDialogContainer = null;
        super.clean();
    }

    public RelativeLayout getComponentContainer() {
        return mComponentContainer;
    }

    private class ModalView extends View {
        AlertDialog mDialog;

        public ModalView(Context context, AlertDialog dialog) {
            super(context);
            mDialog = dialog;
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            mDialog.dismiss();
        }
    }
}
