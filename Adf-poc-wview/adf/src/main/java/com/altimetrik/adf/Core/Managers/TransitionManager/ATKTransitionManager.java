package com.altimetrik.adf.Core.Managers.TransitionManager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;

import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.Utils;

import org.json.JSONObject;

/**
 * Created by icabanas on 23/10/2015.
 */
public class ATKTransitionManager {

    public static void prepareForTransition(final View view, final Context context) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                ImageView existingScreenShotView = (ImageView) ((ViewGroup) view.getParent()).findViewWithTag(Constants.ATK_TRANSITION_SCREEN_SHOT);
                if (existingScreenShotView == null) {
                    Drawable drawable = new BitmapDrawable(context.getResources(), getCurrentScreenImage((View) view.getParent()));
                    ImageView printScreenView = new ImageView(context);

                    printScreenView.setTag(Constants.ATK_TRANSITION_SCREEN_SHOT);
                    printScreenView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    printScreenView.setAdjustViewBounds(true);
                    printScreenView.setScaleType(ImageView.ScaleType.FIT_XY);
                    printScreenView.setImageDrawable(drawable);

                    ((ViewGroup) view.getParent()).addView(printScreenView);
                }
            }
        });
    }

    public static void commitForTransition(final View view, final JSONObject params, final Context context) {

        final String transitionId = params.optString("transitionId");
        final String transitionDirection = params.optString("direction");
        final String transitionDuration = params.optString("duration", "0.6");
        final String transitionDelay = params.optString("wait", "0.2");

        final int duration = Utils.isEnhancedDevice(context) ? (int) (Float.parseFloat(transitionDuration) * 500) : (int) (Float.parseFloat(transitionDuration) * 1000);
        final int delay = Utils.isEnhancedDevice(context) ? (int) (Float.parseFloat(transitionDelay) * 500) : (int) (Float.parseFloat(transitionDelay) * 1000);

        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                view.bringToFront();

                if (transitionId.equals("slideOver")) {
                    if (transitionDirection.equals("left")) {
                        view.setX(-view.getWidth());
                    } else if (transitionDirection.equals("right")) {
                        view.setX(view.getWidth());
                    }
                }

                view.animate()
                        .setDuration(duration)
                        .translationX(0)
                        .setStartDelay(delay)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                ImageView screenShotView = (ImageView) ((ViewGroup) view.getParent()).findViewWithTag(Constants.ATK_TRANSITION_SCREEN_SHOT);
                                if (screenShotView != null) {
                                    screenShotView.getDrawable().setCallback(null);
                                    screenShotView.setImageDrawable(null);
                                    ((ViewGroup) view.getParent()).removeView(screenShotView);
                                }
                            }
                        });
            }
        });
    }

    public static Bitmap getCurrentScreenImage(View currentView) {
        Bitmap bitmap = Bitmap.createBitmap(currentView.getWidth(), currentView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        currentView.draw(canvas);
        return bitmap;
    }
}
