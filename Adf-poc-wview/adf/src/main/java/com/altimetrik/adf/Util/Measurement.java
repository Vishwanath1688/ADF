package com.altimetrik.adf.Util;

import android.content.Context;
import android.view.View;

import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;

import static com.altimetrik.adf.Util.LogUtils.makeLogTag;
import static com.altimetrik.adf.Util.Utils.getDeviceDensity;

/**
 * Created by pigounet on 3/30/15.
 */
public class Measurement {

    private static final String TAG = makeLogTag(Measurement.class);

    /**
     * Gets the different size and position values for a specific Widget
     */
    public static int[] generateMeasurement(String xPosition, String yPosition, String componentWidth, String componentHeight, Context context, View v) {
        int[] frame = new int[4];

        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        if (v != null) {
            int viewWidth = v.getLayoutParams().width;
            int viewHeight = v.getLayoutParams().height;
            if (viewWidth > 0) width = viewWidth;
            if (viewHeight > 0) height = viewHeight;
        }

        frame[0] = getIntValue(xPosition, width, "x", context, false);
        frame[1] = getIntValue(yPosition, height, "y", context, false);
        frame[2] = getIntValue(componentWidth, width , "width", context, false);
        frame[3] = getIntValue(componentHeight, height, "height", context, false);
        return frame;
    }

    /**
     * Gets the numeric value from an attribute of a Widget
     */
    public static int getIntValue(String value, int parentDimension, String valueString, Context context, boolean isNested) {
        int resultValue = 0;
        //Fix Position
        float density = getDeviceDensity(context);
        if (value.contains("calc")) {
            String equation = value.substring(5, value.length() - 1);
            String[] equationValues = equation.trim().split(" ");
            int left;
            if(equationValues[0].contains("id::")){
                left = getWidgetValue(equationValues[0].substring(4), valueString, context);
            }else {
                left = getIntValue(equationValues[0], parentDimension, valueString, context, true);
            }
            int right = getIntValue(equationValues[2], parentDimension, valueString, context, true);
            String operator = equationValues[1];
            switch (operator) {
                case "+":
                    resultValue = left + right;
                    break;
                case "-":
                    resultValue = left - right;
                    break;
            }
            if (valueString.equals("x") || valueString.equals("y")) {
                resultValue = (int) (resultValue / density);
            }
        } else if (value.contains("%")) {
            String removeCharsFromValues = value.substring(0, value.indexOf("%"));
            Double removedCharsAndCasted = Double.valueOf(removeCharsFromValues);
            if (removedCharsAndCasted != 0) {
                Double resultDouble = parentDimension * removedCharsAndCasted / 100.0f;
                if (isNested) {
                    return Math.round(resultDouble.floatValue() / density);
                }
                return Math.round(resultDouble.floatValue());
            } else {
                return 0;
            }
        } else if (value.contains("px")) {
            String removeCharsFromValues = value.substring(0, value.indexOf("p"));
            resultValue = Integer.parseInt(removeCharsFromValues);
        } else if (value.contains("pt")) {
            String removeCharsFromValues = value.substring(0, value.indexOf("p"));
            resultValue = Integer.parseInt(removeCharsFromValues);
        } else {
            if (!value.equals("")) {
                float resultFloatValue = Float.parseFloat(value);
                resultValue = (int) resultFloatValue;
            } else {
                resultValue = 0;
            }
        }
        if (isNested) {
            return resultValue;
        }
        return (int) (resultValue * density);
    }

    /**
     * Loads widget from componentMap and returns the specified value after dividing by density (Necessary to get original value)
     */
    private static int getWidgetValue(String widgetID, String valueString, Context context){
        ATKWidget widget = ATKComponentManager.getInstance().getComponentById(widgetID);
        float density = getDeviceDensity(context);
        int res = 0;
        if(widget != null){
            switch(valueString){
                case "x":
                    res = widget.getX();
                    break;
                case "y":
                    res = widget.getY();
                    break;
                case "width":
                    res = widget.getWidth();
                    break;
                case "height":
                    res = widget.getHeight();
                    break;
            }
        }
        return (int) (res / density);
    }



}
