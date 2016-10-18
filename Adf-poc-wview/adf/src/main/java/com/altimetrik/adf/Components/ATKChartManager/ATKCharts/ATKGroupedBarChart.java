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
import java.util.Iterator;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 8/3/15.
 */
public class ATKGroupedBarChart extends ATKBaseChart {

    private static final String TAG = makeLogTag(ATKGroupedBarChart.class);

    private BarChart mChart;

    private ArrayList<BarDataSet> dataSets;
    private ArrayList<String> xVals;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {
        super.initWithJSON(widgetDefinition, context);

        mChart = new BarChart(context);
        addChartToContainer(mChart);

        dataSets = new ArrayList<>();
        xVals = new ArrayList<>();

        XAxis xAxis = mChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setSpaceBetweenLabels(0);
        xAxis.setDrawGridLines(false);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.setDrawBarShadow(false);
        mChart.setDrawGridBackground(false);

        // enable touch gestures
        mChart.setTouchEnabled(false);

        updateChartData();

        return this;
    }

    @Override
    protected void updateChartData() {
        dataSets.clear();
        xVals.clear();

        parseChartData();

        BarData data = new BarData(xVals, dataSets);
        data.setGroupSpace(80f);
        mChart.setData(data);

        updateChart(mChart);

        mChart.invalidate();
        mChart.animateY(DEFAULT_ANIMATION_DURATION);
    }

    @Override
    protected void parseChartData() {
        try {
            for (int i = 0; i < mItems.length(); i++) {
                ArrayList<BarEntry> yVals = new ArrayList<>();

                JSONObject currentItem = (JSONObject) mItems.get(i);
                String legend = currentItem.optString("legend");
                JSONObject values = currentItem.optJSONObject("values");

                if (values != null) {
                    Iterator iterator = values.keys();
                    int count = 0;
                    while(iterator.hasNext()){
                        String key = (String)iterator.next();
                        if (!xVals.contains(key))
                            xVals.add(key);

                        JSONObject value = values.getJSONObject(key);
                        String itemValue = value.optString("value", "0");
                        yVals.add(new BarEntry(Float.parseFloat(itemValue), count));
                        count++;
                    }
                }

                String color = currentItem.optString("color", "#FFFFFF");
                BarDataSet dataSet = new BarDataSet(yVals, legend);
                dataSet.setColor(UIUtils.parseColor(color));
                dataSets.add(dataSet);
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
        super.clean();
    }
}
