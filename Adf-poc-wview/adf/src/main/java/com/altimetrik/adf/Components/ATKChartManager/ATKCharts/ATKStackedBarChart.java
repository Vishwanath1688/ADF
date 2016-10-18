package com.altimetrik.adf.Components.ATKChartManager.ATKCharts;

import android.content.Context;
import android.graphics.Color;

import com.altimetrik.adf.Components.ATKChartManager.ATKBaseChart;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Util.UIUtils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 8/3/15.
 */
public class ATKStackedBarChart extends ATKBaseChart {

    private static final String TAG = makeLogTag(ATKStackedBarChart.class);

    private BarChart mChart;

    private ArrayList<BarDataSet> dataSets;
    private ArrayList<String> xVals;
    private ArrayList<HashMap<String, Float>> yValsMaps;
    private ArrayList<String> labels;
    private ArrayList<Integer> colors;
    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {
        super.initWithJSON(widgetDefinition, context);

        mChart = new BarChart(context);
        addChartToContainer(mChart);

        dataSets = new ArrayList<>();
        xVals = new ArrayList<>();
        yValsMaps = new ArrayList<>();
        labels = new ArrayList<>();
        colors = new ArrayList<>();

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(0);
        xAxis.setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.setDrawBarShadow(false);
        mChart.setDrawGridBackground(false);
        mChart.setDrawValueAboveBar(false);

        // enable touch gestures
        mChart.setTouchEnabled(false);

        updateChartData();

        return this;
    }

    @Override
    protected void updateChartData() {

        dataSets.clear();
        xVals.clear();
        yValsMaps.clear();
        labels.clear();
        colors.clear();

        parseChartData();

        ArrayList<BarEntry> yVals = new ArrayList<>();

        for (int i = 0; i < xVals.size(); i++) {
            ArrayList<Float> currentStackedValues = new ArrayList<>();

            for (int j = 0; j < yValsMaps.size(); j++) {
                HashMap<String, Float> currentYValsMap = yValsMaps.get(j);
                if (currentYValsMap.containsKey(xVals.get(i)))
                    currentStackedValues.add(currentYValsMap.get(xVals.get(i)));
            }

            float[] floatArray = new float[currentStackedValues.size()];
            int k = 0;
            for (Float f : currentStackedValues) {
                floatArray[k] = (f != null ? f : Float.NaN);
                k++;
            }

            yVals.add(new BarEntry(floatArray, i));
        }

        BarDataSet set = new BarDataSet(yVals, "");
        set.setColors(colors);

        String[] labelsArr = new String[labels.size()];
        labelsArr = labels.toArray(labelsArr);
        set.setStackLabels(labelsArr);

        ArrayList<BarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);

        BarData data = new BarData(xVals, dataSets);
        mChart.setData(data);

        updateChart(mChart);

        mChart.invalidate();
        mChart.animateY(DEFAULT_ANIMATION_DURATION);
    }

    @Override
    protected void parseChartData() {
        try {
            for (int i = 0; i < mItems.length(); i++) {
                HashMap<String, Float> yValsMap = new HashMap<>();

                JSONObject currentItem = (JSONObject) mItems.get(i);
                String legend = currentItem.optString("legend");
                JSONObject values = currentItem.optJSONObject("values");
                String color = currentItem.optString("color", "#FFFFFF");

                labels.add(legend);
                colors.add(UIUtils.parseColor(color));

                if (values != null) {
                    Iterator iterator = values.keys();
                    while(iterator.hasNext()){
                        String key = (String)iterator.next();
                        if (!xVals.contains(key))
                            xVals.add(key);
                        JSONObject value = values.getJSONObject(key);
                        String itemValue = value.optString("value", "0");
                        yValsMap.put(key, Float.parseFloat(itemValue));
                    }
                }

                yValsMaps.add(yValsMap);
            }
        } catch (JSONException e) {
            LOGD(TAG, "parseChartData", e);
        }
    }

    @Override
    public void clean() {
        mChart = null;
        dataSets = null;
        xVals = null;
        yValsMaps = null;
        labels = null;
        colors = null;
        super.clean();
    }

}
