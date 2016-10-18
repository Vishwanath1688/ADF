package com.altimetrik.adf.Util;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.AdapterView;

import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;
import static com.altimetrik.adf.Util.Utils.getDeviceDensity;

/**
 * Created by gyordi on 5/27/15.
 */
public class UIUtils {

    private static final String TAG = makeLogTag(UIUtils.class);

    public static void unbindDrawables(View view) {
        if (view == null) {
            return;
        }
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            if (!(view instanceof AdapterView)) {
                ((ViewGroup) view).removeAllViews();
            }
        }
    }

    public static ViewParent findParentWebView(View view) {
        ViewParent parent = view.getParent();
        while (parent != null && !(parent instanceof WebView)) {
            parent = parent.getParent();
        }
        return parent;
    }

    public synchronized static String getDeviceFilePath(@NonNull Context context, String path) {
        String resultPath = path;
        if (path != null) {

            //Format image data
            String imagePrefix = FilenameUtils.getFullPath(path);
            String imageName = FilenameUtils.getBaseName(path);
            String extension = FilenameUtils.getExtension(path);
            String deviceType = Utils.isTabletDevice(context) ? "tablet" : "phone";
            int density = (int) getDeviceDensity(context);

            //Set possible paths
            ArrayList<String> pathList = new ArrayList<>(4);

            if (density != 1) {
                for (int i = density; i > 1; i--)
                    pathList.add(imagePrefix + imageName + "@" + i + "x~" + deviceType + "." + extension);
                pathList.add(imagePrefix + imageName + "~" + deviceType + "." + extension);
                for (int i = density; i > 1; i--)
                    pathList.add(imagePrefix + imageName + "@" + i + "x." + extension);
                pathList.add(imagePrefix + imageName + "." + extension);
            } else {
                pathList.add(imagePrefix + imageName + "~" + deviceType + "." + extension);
                pathList.add(imagePrefix + imageName + "." + extension);
            }

            //Test if file exists
            HashSet<String> assetsList = ATKComponentManager.getInstance().getImageAssets();
            boolean found = false;
            for (int i = 0; i < pathList.size() && !found; i++) {
                path = pathList.get(i);
                if (assetsList.contains(path)) {
                    resultPath = path;
                    found = true;
                }
            }
            if (!found) {
                LOGE(TAG, String.format("getDeviceFilePath - NOT FOUND - %s", path));
            }
        }

        return FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(context), resultPath);
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static void resizeView(View view, String x, String y, String width, String height, Context context) {
        int[] frame = Measurement.generateMeasurement( x, y, width, height, context, (View) view.getParent());
        view.setX(frame[0]);
        view.setY(frame[1]);
        view.getLayoutParams().width = frame[2];
        view.getLayoutParams().height = frame[3];
        view.requestLayout();
    }

    /**
     * @param originalColor color, without alpha
     * @param alpha         from 0.0 to 1.0
     * @return
     */
    public static String addAlpha(String originalColor, double alpha) {
        long alphaFixed = Math.round(alpha * 255);
        String alphaHex = Long.toHexString(alphaFixed);
        if (alphaHex.length() == 1) {
            alphaHex = "0" + alphaHex;
        }
        originalColor = originalColor.replace("#", "#" + alphaHex);
        return originalColor;
    }

    public static int parseColor(String input) {
        int color = Integer.MIN_VALUE;

        if (input.contains("rgb")) {
            Pattern c = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
            Matcher m = c.matcher(input);
            if (m.matches())
            {
                color = Color.argb(255, Integer.valueOf(m.group(1)),  // r
                        Integer.valueOf(m.group(2)),  // g
                        Integer.valueOf(m.group(3))); // b
            }
        } else if (input.contains("rgba")) {
            Pattern c = Pattern.compile("rgba *\\( *([0-9]+), *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
            Matcher m = c.matcher(input);
            if (m.matches())
            {
                color = Color.argb(Integer.valueOf(m.group(4)), Integer.valueOf(m.group(1)),  // a, r
                        Integer.valueOf(m.group(2)),  // g
                        Integer.valueOf(m.group(3))); // b
            }
        } else {
            color = Color.parseColor(input);
        }

        if (color == Integer.MIN_VALUE) {
            LOGE(TAG, "Failed to parse color " + input);
            color = -1;
        }

        return color;
    }

}
