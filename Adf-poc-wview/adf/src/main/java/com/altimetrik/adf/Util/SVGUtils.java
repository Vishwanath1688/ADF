package com.altimetrik.adf.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.PictureDrawable;
import android.support.annotation.Nullable;

import com.caverock.androidsvg.PreserveAspectRatio;
import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by gyordi on 6/3/15.
 */
public class SVGUtils {

    private static final String TAG = makeLogTag(SVGUtils.class);

    @Nullable
    public static Bitmap getBitmap(String path, int width, int height) {
        try {
            InputStream is = new FileInputStream(path);
            SVG svg = SVG.getFromInputStream(is);
            svg.setDocumentPreserveAspectRatio(PreserveAspectRatio.FULLSCREEN_START);
            svg.setDocumentHeight(height);
            svg.setDocumentWidth(width);
            Picture picture = svg.renderToPicture();

            width = picture.getWidth();
            height = picture.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            PictureDrawable drawable = new PictureDrawable(picture);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(canvas);
            LOGD(TAG, "canvas.getWidth(), canvas.getHeight() " + canvas.getWidth() + ", " + canvas.getHeight());
            return bitmap;
        } catch (SVGParseException e) {
            LOGE(TAG, "getPictureDrawable SVGParseException", e);
        } catch (FileNotFoundException e) {
            LOGE(TAG, "getPictureDrawable FileNotFoundException", e);
        }
        return null;
    }

    public static BitmapDrawable getBitmapDrawable(Context context, String path, int width, int height) {
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), getBitmap(path, width, height));
        bd.setAntiAlias(true);
        return bd;
    }

}