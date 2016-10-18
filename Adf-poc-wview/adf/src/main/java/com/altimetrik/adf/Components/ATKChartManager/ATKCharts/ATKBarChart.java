package com.altimetrik.adf.Components.ATKChartManager.ATKCharts;

import android.content.Context;
import android.graphics.Color;

import com.altimetrik.adf.Components.ATKChartManager.ATKBaseChart;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Util.UIUtils;
import com.altimetrik.adf.Util.Utils;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 8/3/15.
 */
public class ATKBarChart extends ATKBaseChart {

    private static final String TAG = makeLogTag(ATKBarChart.class);

    private BarChart mChart;

    private boolean mHorizontal;

    private ArrayList<String> xVals;
    private ArrayList<BarEntry> yVals;
    private ArrayList<Integer> colors;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {
        super.initWithJSON(widgetDefinition, context);

        mHorizontal = Utils.isPositiveValue(getStyle().optString("horizontal"));

        if (mHorizontal)
            mChart = new HorizontalBarChart(context);
        else
            mChart = new BarChart(context);

        addChartToContainer(mChart);

        xVals = new ArrayList<>();
        yVals = new ArrayList<>();
        colors = new ArrayList<>();

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(0);
        xAxis.setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.setDrawBarShadow(false);
        mChart.setDrawGridBackground(false);
        mChart.getLegend().setEnabled(false);

        // enable touch gestures
        mChart.setTouchEnabled(false);

        updateChartData();

        return this;
    }

    @Override
    protected void updateChartData() {
        xVals.clear();
        yVals.clear();
        colors.clear();

        parseChartData();

        BarDataSet dataSet = new BarDataSet(yVals, "");
        dataSet.setColors(colors);

        ArrayList<BarDataSet> dataSets = new ArrayList<BarDataSet>();
        dataSets.add(dataSet);

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
                JSONObject currentItem = (JSONObject) mItems.get(i);

                float value = Float.parseFloat(currentItem.optString("value", "0"));
                String legend = currentItem.optString("legend");
                String color = currentItem.optString("color", "#FFFFFF");

                yVals.add(new BarEntry(value, i));
                xVals.add(legend);
                colors.add(UIUtils.parseColor(color));
            }
        } catch (JSONException e) {
            LOGD(TAG, "parseChartData", e);
        }

    }

    @Override
    public void clean() {
        mChart = null;
        xVals = null;
        yVals = null;
        colors = null;
        super.clean();
    }
}
