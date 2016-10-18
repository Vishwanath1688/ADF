package com.altimetrik.adf.Util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by gyordi on 5/13/15.
 */
public class NinePatchUtils {

    private static final String TAG = makeLogTag(NinePatchUtils.class);

    public static int[] parseInsetsArray(String sImageCapInsets) {
        String[] vector = sImageCapInsets.replace('{', ' ').replace('}', ' ').trim().split(",");
        int[] toReturn = new int[4];
        toReturn[0] = Integer.parseInt(vector[0].trim());
        toReturn[1] = Integer.parseInt(vector[1].trim());
        toReturn[2] = Integer.parseInt(vector[2].trim());
        toReturn[3] = Integer.parseInt(vector[3].trim());
        return toReturn;
    }

    public static Drawable displayNinePatch(String path, int[] sImageCapInsets, Context context) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        byte[] chunk = bitmap.getNinePatchChunk();
        if (NinePatch.isNinePatchChunk(chunk)) {
            LOGD(TAG, "NinePatchDrawable loaded: " + path);
            return new NinePatchDrawable(context.getResources(), bitmap, chunk, new Rect(), null);
        } else {
            int top, left, right, bottom;

                int width = bitmap.getWidth();
                int height = bitmap.getHeight();

                if (sImageCapInsets == null) {
                    top = height / 2;
                    left = width / 2;
                    bottom = top + 1;
                    right = left + 1;
                } else {
                    top = sImageCapInsets[0];
                    left = sImageCapInsets[1];
                    bottom = height - sImageCapInsets[2];
                    right = width - sImageCapInsets[3];
                    /*
                    bottom = sImageCapInsets[2];
                    right = sImageCapInsets[3];
                    */
                }

                //LOGD(TAG, String.format("NinePathWithCapInsets  %d, %d, %d, %d", top, left, bottom, right));
                return NinePatchBitmapFactory.createNinePathWithCapInsets(context.getResources(), bitmap, top, left, bottom, right, null);
            }
    }
}
