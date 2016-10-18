package com.altimetrik.adf.Components.ATKChartManager.ATKCharts;

import android.content.Context;
import android.graphics.Color;

import com.altimetrik.adf.Components.ATKChartManager.ATKBaseChart;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Util.UIUtils;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 8/3/15.
 */
public class ATKPieChart extends ATKBaseChart {

    private static final String TAG = makeLogTag(ATKPieChart.class);

    private PieChart mChart;

    private ArrayList<String> xVals;
    private ArrayList<Entry> yVals;
    private ArrayList<Integer> colors;
    private float mTotal;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {
        super.initWithJSON(widgetDefinition, context);

        mChart = new PieChart(context);
        addChartToContainer(mChart);

        xVals = new ArrayList<>();
        yVals = new ArrayList<>();
        colors = new ArrayList<>();

        mChart.setDrawHoleEnabled(false);
        mChart.setDrawCenterText(false);
        mChart.setDrawSliceText(false);

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

        PieDataSet dataSet = new PieDataSet(yVals, "");
        dataSet.setValueFormatter(new CustomPieChartFormatter(mTotal));
        dataSet.setColors(colors);

        PieData data = new PieData(xVals, dataSet);
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        mChart.setData(data);

        updateChart(mChart);

        mChart.invalidate();
        mChart.animateX(DEFAULT_ANIMATION_DURATION);
    }

    @Override
    protected void parseChartData() {
        float total = 0f;
        try {
            for (int i = 0; i < mItems.length(); i++) {
                JSONObject currentItem = (JSONObject) mItems.get(i);
                float value = Float.parseFloat(currentItem.optString("value", "0"));
                String legend = currentItem.optString("legend");
                String color = currentItem.optString("color", "#FFFFFF");

                total += value;
                yVals.add(new Entry(value, i));
                xVals.add(legend);
                colors.add(UIUtils.parseColor(color));
            }
        } catch (JSONException e) {
            LOGD(TAG, "parseChartData", e);
        }

        mTotal = total;
    }

    public class CustomPieChartFormatter implements ValueFormatter {

        private float mTotal;

        public CustomPieChartFormatter(float total) {
            mTotal = total;
        }

        @Override
        public String getFormattedValue(float value) {
            return value + " (" + String.format("%.1f", value * 100 / mTotal) + "%" + ")";
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
