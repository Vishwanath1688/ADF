package com.altimetrik.adf.Components.ATKChartManager.ATKCharts;

import android.content.Context;
import android.graphics.Color;

import com.altimetrik.adf.Components.ATKChartManager.ATKBaseChart;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Util.MathUtils;
import com.altimetrik.adf.Util.UIUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 8/3/15.
 */
public class ATKScatterChart extends ATKBaseChart {

    private static final String TAG = makeLogTag(ATKScatterChart.class);

    private LineChart mChart;

    private HashMap<Double,String> mManualTicks;
    private ArrayList<Double> xVals;
    private ArrayList<HashMap<Double, Double>> yVals;
    private ArrayList<String> legends;
    private ArrayList<String> xValsForDataset;
    private ArrayList<LineDataSet> dataSets;
    private ArrayList<Integer> colors;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {
        super.initWithJSON(widgetDefinition, context);

        mChart = new LineChart(context);
        addChartToContainer(mChart);

        mManualTicks = new HashMap<>();
        xVals = new ArrayList<>();
        yVals = new ArrayList<>();
        legends = new ArrayList<>();
        xValsForDataset = new ArrayList<>();
        colors = new ArrayList<>();

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(0);
        xAxis.setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getAxisLeft().setStartAtZero(false);
        mChart.setDrawGridBackground(false);

        // enable touch gestures
        mChart.setTouchEnabled(false);

        loadManualTicks();

        updateChartData();

        return this;
    }

    @Override
    protected void updateChartData() {
        xVals.clear();
        yVals.clear();
        legends.clear();
        xValsForDataset.clear();
        colors.clear();

        parseChartData();
        assembleNumericXAxis();
        assembleNumericYAxis();

        LineData data = new LineData(xValsForDataset, dataSets);
        mChart.setData(data);

        updateChart(mChart);

        mChart.invalidate();
        mChart.animateXY(DEFAULT_ANIMATION_DURATION, DEFAULT_ANIMATION_DURATION);
    }

    private void loadManualTicks() {
        JSONObject manualTicks = getStyle().optJSONObject("manualTicks");
        if (manualTicks != null) {
            JSONObject xAxis = manualTicks.optJSONObject("x");
            if (xAxis != null) {
                JSONArray positions = xAxis.optJSONArray("positions");
                JSONArray labels = xAxis.optJSONArray("labels");
                if (positions != null && labels != null) {
                    for (int i = 0; i < positions.length(); i++) {
                        mManualTicks.put(positions.optDouble(i), labels.optString(i));
                    }
                }
            }
        }
    }

    @Override
    protected void parseChartData() {
        try {
            for (int i = 0; i < mItems.length(); i++) {
                HashMap<Double, Double> currentYVals = new HashMap<>();

                JSONObject currentItem = (JSONObject) mItems.get(i);
                String legend = currentItem.optString("legend");
                JSONObject value = currentItem.optJSONObject("value");
                String color = currentItem.optString("color");

                if (value != null) {
                    JSONArray xValues = value.optJSONArray("xValues");
                    JSONArray yValues = value.optJSONArray("yValues");
                    if (xValues != null && yValues != null) {
                        for (int j = 0; j < xValues.length(); j++) {
                            Double currentXValue = xValues.optDouble(j);
                            currentYVals.put(currentXValue, yValues.optDouble(j));
                            if (!xVals.contains(currentXValue)) {
                                xVals.add(currentXValue);
                            }
                        }
                    }
                }

                colors.add(UIUtils.parseColor(color));
                legends.add(legend);
                yVals.add(currentYVals);
            }
        } catch (JSONException e) {
            LOGD(TAG, "parseChartData", e);
        }
    }

    private void assembleNumericXAxis() {
        Collections.sort(xVals);

        if (!xVals.isEmpty()) {
            Double gcd = MathUtils.gcd(xVals);
            Double min = xVals.get(0);
            Double max = xVals.get(xVals.size() - 1);
            Double value = min;
            ArrayList<Double> newXVals = new ArrayList<>();
            while (value <= max) {
                newXVals.add(value);
                if (mManualTicks.containsKey(value)) {
                    xValsForDataset.add(mManualTicks.get(value));
                } else {
                    xValsForDataset.add("");
                }
                value += gcd;
            }
            xVals = newXVals;
        }

    }

    private void assembleNumericYAxis() {
        dataSets = new ArrayList<>();
        for (int i = 0; i < yVals.size(); i++) {
            HashMap<Double, Double> currentYVals = yVals.get(i);

            ArrayList<Entry> values = new ArrayList<>();
            for (int j = 0; j < xVals.size(); j++) {
                if (currentYVals.containsKey(xVals.get(j))) {
                    values.add(new Entry(currentYVals.get(xVals.get(j)).floatValue(), j));
                }
            }

            LineDataSet dataSet = new LineDataSet(values, legends.get(i));
            dataSet.setColor(colors.get(i));
            dataSets.add(dataSet);
        }
    }

    @Override
    public void clean() {
        mChart = null;
        mManualTicks = null;
        xVals = null;
        yVals = null;
        legends = null;
        xValsForDataset = null;
        colors = null;
        super.clean();
    }
}
