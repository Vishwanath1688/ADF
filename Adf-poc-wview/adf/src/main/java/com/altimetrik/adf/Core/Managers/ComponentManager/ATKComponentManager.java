package com.altimetrik.adf.Core.Managers.ComponentManager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.altimetrik.adf.Components.ATKModal.ATKModal;
import com.altimetrik.adf.Components.ATKBarcodeScanner.ATKBarcodeScanner;
import com.altimetrik.adf.Components.ATKProgressBar.ATKProgressHUD;
import com.altimetrik.adf.Components.ATKSearchBox.ATKSearchBox;
import com.altimetrik.adf.Components.ATKSlidingComponent.ATKSlidingComponent;
import com.altimetrik.adf.Components.ATKTextField.ATKTextField;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.R;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGI;
import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by pigounet on 3/26/15.
 */
public class ATKComponentManager {

    private static final String TAG = makeLogTag(ATKComponentManager.class);

    private static ATKComponentManager ourInstance = new ATKComponentManager();

    private String mAbsoluteAssetsPath;
    private volatile HashMap<String, ATKWidget> mLoadedComponents;
    private volatile HashMap<String, ATKProgressHUD> mCurrentProgressHUDs;
    private HashSet<String> mImageAssets;

    public static ATKComponentManager getInstance() {
        return ourInstance;
    }

    private ATKComponentManager() {
        init();
    }

    public void init() {
        mLoadedComponents = new HashMap<>();
        mCurrentProgressHUDs = new HashMap<>();
        mImageAssets = new HashSet<>();
    }

    public HashMap<String, ATKWidget> getLoadedComponents() {
        return mLoadedComponents;
    }

    public ATKWidget presentComponentInView(final Context context, final ViewGroup parentView, final JSONObject widgetConfig) {
        try {
            String className = widgetConfig.getString("class");
            Class cls = Class.forName(String.format("com.altimetrik.adf.Components.%s.%s", className, className));
            if(cls != null) {
                final ATKWidget component = (ATKWidget) cls.newInstance();


                ((Activity)context).runOnUiThread(new Runnable(){
                    public void run() {
                        if (parentView != null)
                            component.setFutureParent(parentView);

                        component.initWithJSON(widgetConfig, context);
                        View componentView = component.getDisplayView();

                        //Add View to parent view
                        if(parentView != null && componentView != null) {
                            //Sliding component needs to be treated different
                            if (component instanceof ATKSlidingComponent) {
                                final ATKSlidingComponent slidingComponent = (ATKSlidingComponent) component;
                                slidingComponent.setParentView(parentView);

                                //Create background mask for Sliding Component
                                final View backgroundMask = slidingComponent.getBackgroundMask();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        backgroundMask.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                slidingComponent.closeSlidingComponent();
                                            }
                                        });
                                    }
                                }, ATKSlidingComponent.ANIMATION_DURATION + ATKSlidingComponent.ANIMATION_DELAY);

                                //Move parent view if needed
                                slidingComponent.moveSuperView(parentView);
                                ((ViewGroup) (parentView.getParent())).addView(backgroundMask);
                                ((ViewGroup) (parentView.getParent())).addView(componentView);
                            } else {
                                parentView.addView(componentView);
                            }
                        }

                        //Move component and set detach listener
                        if (componentView != null) {
                            componentView.setX(component.getX());
                            componentView.setY(component.getY());
                            componentView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                                @Override
                                public void onViewAttachedToWindow(View v) { }

                                @Override
                                public void onViewDetachedFromWindow(View v) {
                                    component.clean();
                                }
                            });
                        }
                    }
                });
                mLoadedComponents.put(widgetConfig.getString("id"), component);

                return component;
            }
        } catch(JSONException | ClassNotFoundException ex) {
            LOGD(TAG, "presentComponentInView", ex);
        } catch(IllegalAccessException ex) {
            LOGD(TAG, "presentComponentInView IllegalAccessException", ex);
        } catch(InstantiationException ex) {
            LOGD(TAG, "presentComponentInView InstantiationException", ex);
        }
        return null;
    }

    public void presentParentWidgets(final Context context, final ViewGroup parentView, final JSONArray widgets) {
        final ArrayList<View> parentWidgets = new ArrayList<>();
        for (int i = 0; i < widgets.length(); i++) {
            try {
                JSONObject widget = ((JSONObject) widgets.get(i));
                final JSONObject widgetConfig = widget.getJSONObject("widgetJSON");
                String className = widgetConfig.getString("class");
                final JSONObject widgetStyle = widgetConfig.optJSONObject("style");

                Class cls = Class.forName(String.format("com.altimetrik.adf.Components.%s.%s", className, className));
                if (cls != null) {
                    final ATKWidget component = (ATKWidget) cls.newInstance();
                    ((Activity) context).runOnUiThread(new Runnable() {
                        public void run() {
                            component.initWithJSON(widgetConfig, context);

                            final View componentView = component.getDisplayView();
                            //Add View to parent view
                            if (parentView != null && componentView != null) {
                                if (widgetStyle != null) {
                                    if (widgetStyle.optString("position").equals("fixed")) {
                                        ((ViewGroup) parentView.getParent()).addView(componentView);
                                    } else {
                                        parentView.addView(componentView);
                                    }
                                } else {
                                    parentView.addView(componentView);
                                }
                            }

                            //Move component and set detach listener
                            if (componentView != null) {
                                componentView.setX(component.getX());
                                componentView.setY(component.getY());
                                componentView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                                    @Override
                                    public void onViewAttachedToWindow(View v) { }

                                    @Override
                                    public void onViewDetachedFromWindow(View v) {
                                        component.clean();
                                    }
                                });
                                parentWidgets.add(componentView);
                            }


                        }
                    });
                    mLoadedComponents.put(widgetConfig.getString("id"), component);
                }
            } catch (JSONException | ClassNotFoundException | ClassCastException e) {
                LOGD(TAG, "presentScreenParentWidgets", e);
            } catch (IllegalAccessException ex) {
                LOGD(TAG, "presentComponentInView IllegalAccessException", ex);
            } catch (InstantiationException ex) {
                LOGD(TAG, "presentComponentInView InstantiationException", ex);
            } catch (NullPointerException ex) {
                LOGD(TAG, "presentComponentInView NullPointerException", ex);
            }
        }
    }

    public void showProgressHUD(final Context context, final ViewGroup parentView, final JSONObject jsonObject) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                final ATKProgressHUD progressHUD = new ATKProgressHUD(context, jsonObject);
                String componentId = jsonObject.optString("componentId");
                mCurrentProgressHUDs.put(componentId, progressHUD);

                ATKWidget component = getComponentById(componentId);
                if (component != null) {

                    RelativeLayout progressBarContainer = new RelativeLayout(context);
                    progressBarContainer.setX(component.getX());
                    progressBarContainer.setY(component.getY());
                    progressBarContainer.setLayoutParams(new RelativeLayout.LayoutParams(component.getWidth(), component.getHeight()));

                    RelativeLayout.LayoutParams progressBarParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    progressBarParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                    progressHUD.getProgressBar().setLayoutParams(progressBarParams);

                    progressBarContainer.addView(progressHUD.getProgressBar());
                    progressBarContainer.setTag(componentId);

                    progressBarContainer.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // just to prevent clicks
                        }
                    });

                    if (component instanceof ATKModal) {
                        progressBarContainer.setX(0);
                        progressBarContainer.setY(0);

                        RelativeLayout modalComponentContainer = ((ATKModal) component).getComponentContainer();
                        Rect modalComponentContainerRect = new Rect();
                        modalComponentContainer.getLocalVisibleRect(modalComponentContainerRect);

                        progressBarContainer.setLayoutParams(new RelativeLayout.LayoutParams(modalComponentContainerRect.width(), modalComponentContainerRect.height()));
                        modalComponentContainer.addView(progressBarContainer);

                    }else {
                        parentView.addView(progressBarContainer);
                    }

                    progressBarContainer.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                        @Override
                        public void onViewAttachedToWindow(View v) {
                        }

                        @Override
                        public void onViewDetachedFromWindow(View v) {
                            progressHUD.clean();
                        }
                    });
                }
            }
        });
    }

    public void hideProgressHUD(final Context context, final ViewGroup parentView, final String componentId) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {

                ATKWidget component = getComponentById(componentId);
                final View progressHUD;
                if (component != null && component instanceof ATKModal) {
                    progressHUD  = ((ATKModal) component).getComponentContainer().findViewWithTag(componentId);
                }else{
                    progressHUD = parentView == null ? null : parentView.findViewWithTag(componentId);
                }

                if (progressHUD != null) {
                    Animation fadeOut = new AlphaAnimation(1, 0);
                    fadeOut.setInterpolator(new AccelerateInterpolator());
                    fadeOut.setDuration(200);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        public void onAnimationEnd(Animation animation) {
                            ((ViewGroup) progressHUD.getParent()).removeView(progressHUD);
                        }

                        public void onAnimationRepeat(Animation animation) {
                        }

                        public void onAnimationStart(Animation animation) {
                        }
                    });
                    progressHUD.startAnimation(fadeOut);
                }
            }
        });
    }

    public void updateProgressHUD(final Context context, final String componentId, final int progress) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                ATKProgressHUD progressHUD = mCurrentProgressHUDs.get(componentId);
                if (progressHUD != null) {
                    progressHUD.setProgress(progress);
                }
            }
        });
    }

    public void removeAllProgressHUDs(final Context context, final ViewGroup parentView) {
        Iterator it = mCurrentProgressHUDs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            ATKProgressHUD progressHUD = (ATKProgressHUD) pair.getValue();
            hideProgressHUD(context, parentView, progressHUD.getComponentId());
            it.remove();
        }
        mCurrentProgressHUDs.clear();
    }

    public ATKWidget getComponentById(String id) {
        return mLoadedComponents.get(id);
    }

    public ATKSlidingComponent getSlidingComponent() {
        Iterator it = mLoadedComponents.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            ATKWidget currentWidget = (ATKWidget) pair.getValue();
            if (currentWidget instanceof ATKSlidingComponent) {
                return (ATKSlidingComponent) currentWidget;
            }
        }
        return null;
    }

    public void removeComponentByID(final Context context, final String id) {
        final ATKWidget widget = getComponentById(id);

        if (widget instanceof ATKSlidingComponent) {
            if (widget.getDisplayView() != null) {
                ((ATKSlidingComponent) widget).closeSlidingComponent();
            }
        } else {
            if (widget instanceof ATKTextField || widget instanceof ATKSearchBox) {
                ((ATKTextField) widget).setIsRemovingComponent(true);
            }

            if (widget != null) {
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        if (widget.getDisplayView() != null) {
                            ((ViewGroup) widget.getDisplayView().getParent()).removeView(widget.getDisplayView());
                        }
                    }
                });
                ATKComponentManager.getInstance().mLoadedComponents.remove(id);
            }
        }
    }

    public void removeAllComponents(final Context context, final JSONArray components) {
        try {
            for (int i = 0; i < components.length(); i++) {
                removeComponentByID(context, components.getString(i));
            }
        } catch (JSONException e) {
            LOGE(TAG, "RemoveAllComponentsJSON", e);
        }
    }

    public void initImageAssets(Context context) {
        long startGetViewTime = System.nanoTime();
        mAbsoluteAssetsPath = ATKContentManager.getAbsolutePathAssetsFolder(context);
        listAssetFiles(context.getString(R.string.images_path));
        LOGI(TAG, "initImageAssets time " + (System.nanoTime() - startGetViewTime) / 1E6);
    }

    public HashSet<String> getImageAssets() {
        return mImageAssets;
    }

    private void listAssetFiles(String path) {
        String[] list;
        try {
            File f = new File(FilenameUtils.concat(mAbsoluteAssetsPath, path));
            list = f.list();
            if (list == null) {
                LOGD(TAG, f.getAbsolutePath());
            } else {
                if (list.length > 0) {
                    // This is a folder
                    for (int i = list.length - 1; i >= 0; i--) {
                        String name = path + "/" + list[i];
                        if (list[i].contains(".")) {
                            mImageAssets.add(name);
                        } else {
                            listAssetFiles(name);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGE(TAG, "listAssetFiles", e);
        }
    }

    public void resumeBarcodeScanners() {
        Iterator it = mLoadedComponents.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            ATKWidget currentWidget = (ATKWidget) pair.getValue();
            if (currentWidget instanceof ATKBarcodeScanner) {
                ZXingScannerView barcodeScannerView = (ZXingScannerView) currentWidget.getDisplayView();
                barcodeScannerView.startCamera();
                barcodeScannerView.setResultHandler((ZXingScannerView.ResultHandler) currentWidget);
            }
        }
    }

    public void stopBarcodeScanners() {
        Iterator it = mLoadedComponents.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            ATKWidget currentWidget = (ATKWidget) pair.getValue();
            if (currentWidget instanceof ATKBarcodeScanner) {
                ZXingScannerView barcodeScannerView = (ZXingScannerView) currentWidget.getDisplayView();
                barcodeScannerView.stopCamera();
            }
        }
    }

}
