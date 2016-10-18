package com.altimetrik.adf.Components.ATKTextField;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Components.IATKInput;
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
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by pigounet on 3/26/15.
 */
public class ATKTextField extends ATKComponentBase implements IATKInput {

    private static final String TAG = makeLogTag(ATKTextField.class);

    private String mFontName;
    private Double mFontSize;
    private String mFontColor;
    private String mPlaceHolderFontColor;

    private String mTextAlignment;
    private String mTextOrientation;
    private String mPlaceholder;
    private String mText;
    private Boolean mSecureTextEntry;

    private Double mPaddingLeft;
    private Double mPaddingRight;

    private String mAccesoryImagePath;
    private String mAccesoryLocation;

    private String mIMEOptions;
    private boolean mDisableAutocorrect;

    private EditText mEditText;

    private Drawable mCloseImage;

    private ViewParent mOriginalParent;
    private RelativeLayout mTouchInterceptor;

    private int mIndexOfChild;
    private boolean mIsMoved;

    private int mDifferenceX;
    private int mDifferenceY;

    private boolean mIsRemovingComponent;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {

        try {
            super.initWithJSON(widgetDefinition, context);

            //Load particular params
            mPlaceholder = getProperties().getString("placeholder");
            mSecureTextEntry = Utils.isPositiveValue(getProperties().optString("secureTextEntry"));

            mText = (getData() != null && getData().has("text")) ? getData().getString("text") : "";
            mTextAlignment = getStyle().optString("textAlignment") ;
            mTextOrientation = getStyle().optString("textOrientation");
            mFontColor = getStyle().optString("fontColor");
            mFontName = getStyle().optString("fontName");
            mFontSize = getStyle().optDouble("fontSize", 0);
            mPlaceHolderFontColor = getStyle().optString("placeHolderFontColor");
            mPaddingLeft = getStyle().optDouble("paddingLeft", 0);
            mPaddingRight = getStyle().optDouble("paddingRight", 0);

            mAccesoryImagePath = getStyle().optString("accesoryImagePath");
            mAccesoryLocation = getStyle().optString("accesoryLocation");

            mIMEOptions = getStyle().optString("returnKey").toLowerCase();

            mDisableAutocorrect = getStyle().optBoolean("disableAutocorrect");

            //Create View
            mEditText = new EditText(context);

            mTouchInterceptor = new RelativeLayout(context);
            mTouchInterceptor.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            mIsMoved = false;
            mIsRemovingComponent = false;
            mDifferenceX = 0;
            mDifferenceY = 0;


            mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus) {
                        InputMethodManager inputMethodManager =(InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        if (mIsMoved && !mIsRemovingComponent) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    mIsMoved = false;

                                    ViewParent actualParent = mEditText.getParent();
                                    ((ViewGroup) actualParent).removeView(mEditText);

                                    mTouchInterceptor.setOnTouchListener(null);
                                    ((ViewGroup) actualParent).removeView(mTouchInterceptor);

                                    if (mOriginalParent instanceof LinearLayout) {
                                        ((LinearLayout) mOriginalParent).addView(mEditText, mIndexOfChild);
                                    } else {
                                        ((ViewGroup) mOriginalParent).addView(mEditText);
                                    }

                                    mEditText.setX(mEditText.getX() - mDifferenceX);
                                    mEditText.setY(mEditText.getY() - mDifferenceY);

                                }
                            });
                        }
                    } else {
                        if (!mIsRemovingComponent) {
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    ViewParent parent = UIUtils.findParentWebView(mEditText);
                                    ViewParent actualParent = mEditText.getParent();

                                    if (parent != null && actualParent != null) {
                                        if (actualParent instanceof LinearLayout) {
                                            mIndexOfChild = ((LinearLayout) actualParent).indexOfChild(mEditText);
                                        }
                                        mOriginalParent = actualParent;

                                        int[] location = new int[2];
                                        mEditText.getLocationOnScreen(location);
                                        int initialX = location[0];
                                        int initialY = location[1];

                                        ((ViewGroup) parent.getParent()).addView(mTouchInterceptor);
                                        ((ViewGroup) actualParent).removeView(mEditText);
                                        ((ViewGroup) parent.getParent()).addView(mEditText);


                                        mTouchInterceptor.setOnTouchListener(new View.OnTouchListener() {
                                            @Override
                                            public boolean onTouch(View v, MotionEvent event) {
                                                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                                    if (mEditText.isFocused()) {
                                                        Rect outRect = new Rect();
                                                        mEditText.getGlobalVisibleRect(outRect);
                                                        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                                                            mEditText.clearFocus();
                                                        }
                                                    }
                                                }
                                                return false;
                                            }
                                        });

                                        mEditText.requestFocus();

                                        mIsMoved = true;

                                        mEditText.getLocationOnScreen(location);
                                        mDifferenceX = initialX - location[0];
                                        mDifferenceY = initialY - location[1];
                                        mEditText.setX(mEditText.getX() + mDifferenceX);
                                        mEditText.setY(mEditText.getY() + mDifferenceY);
                                    }
                                }
                            });
                        }
                    }
                }
            });

            //Set base params --> super
            super.loadParams(mEditText);

            mEditText.setSingleLine();

            //Set particular params
            mEditText.setHint(mPlaceholder);

            if (mPlaceHolderFontColor.isEmpty()) {
                mEditText.setHintTextColor(Color.LTGRAY);
            } else {
                mEditText.setHintTextColor(UIUtils.parseColor(mPlaceHolderFontColor));
            }

            if (!mText.isEmpty()) {
                mEditText.setText(mText);
            }
            if (!mFontColor.isEmpty())
                mEditText.setTextColor(UIUtils.parseColor(mFontColor));

            if (mFontSize > 0) {
                mEditText.setTextSize(mFontSize.floatValue());
            }else{
                mEditText.setTextSize(0);
                LOGE(TAG, "Invalid Font Size");
            }

            mEditText.setPadding(mPaddingLeft.intValue(), 0, mPaddingRight.intValue(), 0);

            if (!mFontName.isEmpty()) {
                String fontPath = String.format(Constants.ATK_FONT_DIR, mFontName);
                try {
                    Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), fontPath));
                    mEditText.setTypeface(font);
                } catch (Exception e) {
                    LOGE(TAG, String.format("Font %s not found.", fontPath));
                }
            }

            if (!mAccesoryImagePath.isEmpty()) {
                try {
                    mEditText.setCompoundDrawablePadding(10);
                    Double accesoryImageSize = mEditText.getLayoutParams().height * 0.8;
                    Drawable accesoryImage = Drawable.createFromPath(UIUtils.getDeviceFilePath(context, mAccesoryImagePath));
                    accesoryImage.setBounds(0, 0, accesoryImageSize.intValue(), accesoryImageSize.intValue());

                    if (mAccesoryLocation.equals("right")) {
                        mEditText.setCompoundDrawables(null, null, accesoryImage, null);
                    } else {
                        mEditText.setCompoundDrawables(accesoryImage, null, null, null);
                    }

                } catch (Exception e) {
                    LOGE(TAG, String.format("Image %s not found.", mAccesoryImagePath), e);
                }
            }

            mEditText.setGravity(getAlignmentFromStyle(mTextAlignment));

            mCloseImage = context.getResources().getDrawable(R.drawable.clear_text_image);
            if(mCloseImage != null) {
                Double closeImageSize = mEditText.getLayoutParams().height * 0.7;
                mCloseImage.setBounds(0, 0, closeImageSize.intValue(), closeImageSize.intValue());
                mCloseImage.setVisible(false, true);
            }

            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Double accesoryImageSize = mEditText.getLayoutParams().height * 0.8;
                    Drawable accesoryImage = (!mAccesoryImagePath.isEmpty()) ? Drawable.createFromPath(UIUtils.getDeviceFilePath(context, mAccesoryImagePath)) : null;
                    if (accesoryImage != null)
                        accesoryImage.setBounds(0, 0, accesoryImageSize.intValue(), accesoryImageSize.intValue());
                    if(s.length() > 0){
                        mEditText.setCompoundDrawables(accesoryImage, null, mCloseImage, null);
                        setOnTouchCloseImageListener(true);
                    }else{
                        mEditText.setCompoundDrawables(accesoryImage, null, null, null);
                        setOnTouchCloseImageListener(false);
                    }

                    JSONArray actions = getActions();
                    if (actions != null) {
                        for (int i = 0; i < actions.length(); i++) {
                            try {
                                JSONObject action = actions.getJSONObject(i);
                                if (action.getString("event").equals(Constants.ATK_ACTION_SELECT)) {
                                    JSONObject data = new JSONObject();
                                    data.put("id", getID());
                                    data.put("value", s);
                                    ATKEventManager.excecuteComponentAction(action, data);
                                }
                            } catch (JSONException e) {
                                LOGD(TAG, "ATKTextField on text change", e);
                            }
                        }
                    }

                }

                @Override
                public void afterTextChanged(Editable s) {}
            });

            if (mSecureTextEntry) {
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            } else {
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            }

            if (!mIMEOptions.isEmpty()) {
                switch (mIMEOptions) {
                    case "actionnone":
                        mEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);
                        break;
                    case "actiongo":
                        mEditText.setImeOptions(EditorInfo.IME_ACTION_GO);
                        break;
                    case "actionsearch":
                        mEditText.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
                        break;
                    case "actionsend":
                        mEditText.setImeOptions(EditorInfo.IME_ACTION_SEND);
                        break;
                    case "actionnext":
                        mEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                        break;
                    case "actiondone":
                        mEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        break;
                }
            }

            if (mDisableAutocorrect) {
                mEditText.setInputType(EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            }

        } catch (JSONException e) {
            LOGE(TAG, "initWithJSON", e);
        }

        onPostInitWithJSON();

        return this;
    }

    private void setOnTouchCloseImageListener(boolean visible){
        if(visible){
            mEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mCloseImage != null && event.getAction() == MotionEvent.ACTION_UP) {
                        boolean tappedX = event.getX() > (mEditText.getWidth() - mEditText.getPaddingRight() - mCloseImage.getIntrinsicWidth());
                        if (tappedX) {
                            mEditText.setText("");
                            return true;
                        }
                    }
                    return false;
                }
            });
        }else{
            mEditText.setOnTouchListener(null);
        }
    }

    @Override
    public View getDisplayView() {
        return mEditText;
    }

    @Override
    public void setValue(Object attrs, Context context) {
        mEditText.setText((String) attrs);
    }

    /*
    * Simple method to translate from String to the necessary int value for Gravity
    */
    public int getAlignmentFromStyle(String alignment) {
        int gravity = Gravity.CENTER_VERTICAL;
        switch (alignment) {
            case "left":
                gravity = gravity | Gravity.LEFT;
                break;
            case "center":
                gravity = gravity | Gravity.CENTER_HORIZONTAL;
                break;
            case "right":
                gravity = gravity | Gravity.RIGHT;
                break;
            default:
                gravity = gravity | Gravity.LEFT;
                break;
        }
        return gravity;
    }

    @NonNull
    @Override
    public String getDataString() {
        return mEditText.getText().toString();
    }

    @NonNull
    @Override
    public String getInputId() {
        return getID();
    }

    @Override
    public void setValue(String value) {
        mEditText.setText(value);
    }

    @Override
    public void clean () {
        if (mIsRemovingComponent) {
            mEditText = null;
            super.clean();
        }
    }

    public void setIsRemovingComponent(boolean isRemoving) {
        mIsRemovingComponent = isRemoving;
    }
}
