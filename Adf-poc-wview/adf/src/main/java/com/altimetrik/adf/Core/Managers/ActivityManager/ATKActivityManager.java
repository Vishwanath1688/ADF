package com.altimetrik.adf.Core.Managers.ActivityManager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.altimetrik.adf.Components.ATKDateTimePicker.ATKDateTimePicker;
import com.altimetrik.adf.Components.ATKSlidingComponent.ATKSlidingComponent;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.BridgeManager.ATKBridgeManager;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.R;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;

import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGI;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 23/10/2015.
 */
public class ATKActivityManager {

    private static final String TAG = makeLogTag(ATKActivityManager.class);

    public static void initApp(final ViewGroup mainContainer, final Context context) {

        Window window = ((Activity) context).getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.BLACK);
        }
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        final RelativeLayout splashScreen = new RelativeLayout(context);
        splashScreen.setTag(Constants.ATK_SPLASH_SCREEN);
        splashScreen.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        ImageView splashScreenImage = new ImageView(context);
        splashScreenImage.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        splashScreenImage.setImageDrawable(context.getResources().getDrawable(R.drawable.splash));
        splashScreenImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        splashScreen.addView(splashScreenImage);

        mainContainer.addView(splashScreen);

        if (Utils.isFirstRun(context)) {
            final ProgressDialog pd = ProgressDialog.show(context, "Please wait...", null, true, false);
            pd.setCanceledOnTouchOutside(false);

            final long timeBefore = System.nanoTime();
            ATKContentManager.copyAssetsFromZip(context, new ATKContentManager.ICallbacks() {
                @Override
                public void onSuccess() {
                    pd.dismiss();
                    int zipExtractDuration = (int) ((System.nanoTime() - timeBefore) / 1E9);
                    Utils.setEnhancedDeviceStatus(context, zipExtractDuration <= Constants.ATK_ENHANCED_DEVICE_THRESHOLD);
                    LOGI(TAG, "Files successfully extracted from APK. " + zipExtractDuration + " seconds");
                    Utils.saveFirstRunVersion(context);
                    new MainTask(context, mainContainer, splashScreen).execute();
                }

                @Override
                public void onError(Exception exception) {
                    pd.dismiss();
                    Toast.makeText(context,
                            "Error extracting files from APK." + ((exception == null) ? "" : " Exception: " + exception.getMessage()),
                            Toast.LENGTH_LONG).show();
                }
            });
        } else {
            new MainTask(context, mainContainer, splashScreen).execute();
        }
    }

    public static void handleBackPressed() {
        ATKBridgeManager.excecuteJSCall("webView1", "MainCtrl.onSelectBackButton", 42); // TODO: get real webview id. Fix call without params (42 is a workaround)
    }

    public static void handleDeviceRotation(JSONArray params, final Context context) {
        try {
            for (int i = 0; i < params.length(); i++) {
                JSONObject currentItem = (JSONObject) params.get(i);
                JSONObject frame = currentItem.getJSONObject("frame");
                String id = currentItem.getString("id");

                final int x = frame.getInt("x");
                final int y = frame.getInt("y");
                final int width = frame.getInt("width");
                final int height = frame.getInt("height");
                final ATKWidget widget = ATKComponentManager.getInstance().getComponentById(id);

                if (widget != null) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            widget.resize(x, y, width, height, context);
                        }
                    });
                }
            }
        } catch (JSONException e) {
            LOGE(TAG, "handleDeviceRotation", e);
        }
    }

    public static void handleNativeDeviceRotation() {
        ATKSlidingComponent slidingComponent = ATKComponentManager.getInstance().getSlidingComponent();
        if (slidingComponent != null) {
            slidingComponent.rotate();
        }

        //Resize DateTimePickers
        Iterator it = ATKComponentManager.getInstance().getLoadedComponents().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            ATKWidget currentWidget = (ATKWidget) pair.getValue();
            if (currentWidget instanceof ATKDateTimePicker) {
                ((ATKDateTimePicker) currentWidget).rotateDialog();
            }
        }
    }

    //Resume all services when activity resumes
    public static void resumeServices() {
        //Starts the camera of all active barcode scanners
        ATKComponentManager.getInstance().resumeBarcodeScanners();
    }

    //Pause all services when activity pauses
    public static void pauseServices() {
        //Stops the camera of all active barcode scanners
        ATKComponentManager.getInstance().stopBarcodeScanners();
    }


}
