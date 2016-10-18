package com.altimetrik.adf.Components.ATKImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.NinePatchUtils;
import com.altimetrik.adf.Util.SVGUtils;
import com.altimetrik.adf.Util.UIUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;
import static com.altimetrik.adf.Util.UIUtils.getDeviceFilePath;

/**
 * Created by pigounet on 3/26/15.
 */
public class ATKImageView extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKImageView.class);

    private String mType;
    private String mSource;
    private int[] mImageCapInsets;
    private ImageView mImageView;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {

        //Load base params --> super
        super.initWithJSON(widgetDefinition, context);

        //Load particular params
        mType = getData().optString("type", null);
        mSource = getData().optString("source", null);

        //Create View
        mImageView = new ImageView(context);

        //Set base params --> super
        super.loadParams(mImageView);

        String sImageCapInsets = getStyle().optString("imageCapInsets", null);
        if (sImageCapInsets != null) {
            mImageCapInsets = NinePatchUtils.parseInsetsArray(sImageCapInsets);
        }

        loadImageView();

        String scale = getProperties().optString("scaleMode").toLowerCase();
        switch (scale) {
            case Constants.ATK_IMAGE_SCALE_TO_FILL:
                mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                break;
            case Constants.ATK_IMAGE_SCALE_ASPECT_FIT:
                mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                break;
            case Constants.ATK_IMAGE_SCALE_ASPECT_FILL:
                mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                break;
            default:
                mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                break;
        }

        onPostInitWithJSON();

        return this;
    }

    @Override
    public void clean() {
        // Fix for RecyclerView clean method breaking up ImageViews.
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        boolean calledFromRecyclerView = false;
        for(StackTraceElement element : stackTraceElements){
            String className = RecyclerView.class.getName();
            if(element.getClassName().contains(className)){
                calledFromRecyclerView = true;
                break;
            }
        }
        UIUtils.unbindDrawables(mImageView);
        if(!calledFromRecyclerView){
            mImageView = null;
        }
        super.clean();
    }

    @Override
    public View getDisplayView() {
        return mImageView;
    }

    @Override
    public void setValue(Object attrs, Context context) {
        String attrsClass = attrs.getClass().toString();
        if (attrsClass.equalsIgnoreCase(JSONObject.class.toString())) {
            JSONObject values = (JSONObject) attrs;
            try {
                getData().put("type", values.getString("type"));
                getData().put("source", values.getString("source"));

                mSource = values.getString("source");
                mType = values.getString("type");
                mContext = context;
                mImageView.setImageDrawable(null);

                loadImageView();

            } catch (JSONException e) {
                LOGE(TAG, String.format("setValue - File %s not found", mSource));
            }
        }
    }

    public void loadImageView() {
        if (mSource != null && !mSource.isEmpty()) {
            if (mType.equalsIgnoreCase("local")) {
                try {
                    String filePath = getDeviceFilePath(mContext, mSource);
                    if (mImageCapInsets == null) {
                        if (filePath.toLowerCase().endsWith(".svg")) {
                            mImageView.setImageDrawable(SVGUtils.getBitmapDrawable(mContext, filePath, getWidth(), getHeight()));
                        } else {
                            Uri uri = Uri.fromFile(new File(filePath));
                            Picasso.with(mContext).load(uri).into(mImageView);
                        }
                    } else {
                        mImageView.setImageDrawable(NinePatchUtils.displayNinePatch(filePath, mImageCapInsets, mContext));
                    }
                } catch (Exception e) {
                    LOGE(TAG, String.format("setValue setImageDrawable - File %s not found", mImageView));
                }
            } else if (mType.equalsIgnoreCase("remote")) {
                Picasso.with(mContext).load(mSource.replace(" ", "%20")).into(mImageView);
            } else if (mType.equalsIgnoreCase("base64")) {
                InputStream stream = new ByteArrayInputStream(Base64.decode(mSource.getBytes(), Base64.DEFAULT));
                Bitmap decodedBitmap = BitmapFactory.decodeStream(stream);
                mImageView.setImageBitmap(decodedBitmap);
            }
        }
    }

}
