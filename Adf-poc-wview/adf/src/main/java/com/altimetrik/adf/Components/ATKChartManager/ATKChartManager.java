package com.altimetrik.adf.Components.ATKChartManager;

import android.content.Context;
import android.view.View;

import com.altimetrik.adf.Components.ATKChartManager.ATKCharts.ATKBarChart;
import com.altimetrik.adf.Components.ATKChartManager.ATKCharts.ATKCircleChart;
import com.altimetrik.adf.Components.ATKChartManager.ATKCharts.ATKGroupedBarChart;
import com.altimetrik.adf.Components.ATKChartManager.ATKCharts.ATKPieChart;
import com.altimetrik.adf.Components.ATKChartManager.ATKCharts.ATKScatterChart;
import com.altimetrik.adf.Components.ATKChartManager.ATKCharts.ATKStackedBarChart;
import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Util.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 8/3/15.
 */
public class ATKChartManager extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKChartManager.class);

    private ATKBaseChart mChart;
    private View mFutureParent;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        try {
            String chartType = widgetDefinition.getJSONObject("properties").optString("chartType").toLowerCase();

            switch (chartType) {
                case Constants.ATK_CHART_BAR:
                    mChart = new ATKBarChart();
                    break;
                case Constants.ATK_CHART_GROUPED_BAR:
                    mChart = new ATKGroupedBarChart();
                    break;
                case Constants.ATK_CHART_SCATTER:
                    mChart = new ATKScatterChart();
                    break;
                case Constants.ATK_CHART_PIE:
                    mChart = new ATKPieChart();
                    break;
                case Constants.ATK_CHART_CIRCLE:
                    mChart = new ATKCircleChart();
                    break;
                case Constants.ATK_CHART_STACKED_BAR:
                    mChart = new ATKStackedBarChart();
                    break;
            }

            if (mFutureParent != null) {
                mChart.setFutureParent(mFutureParent);
            }

            mChart.initWithJSON(widgetDefinition, context);
        } catch (JSONException e) {
            LOGD(TAG, "initWithJSON", e);
        }
        return this;
    }

    @Override
    public int getX() {
        return mChart.getX();
    }

    @Override
    public int getY() {
        return mChart.getY();
    }

    @Override
    public int getWidth() {
        return mChart.getWidth();
    }

    @Override
    public int getHeight() {
        return mChart.getHeight();
    }

    @Override
    public String getID() {
        return mChart.getID();
    }

    @Override
    public String getBackgroundColor() {
        return mChart.getBackgroundColor();
    }

    @Override
    public Boolean getDisabled() {
        return mChart.getDisabled();
    }

    @Override
    public double getBackgroundColorOpacity() {
        return mChart.getBackgroundColorOpacity();
    }

    @Override
    public String getBorderColor() {
        return mChart.getBorderColor();
    }

    @Override
    public double getBorderWidth() {
        return mChart.getBorderWidth();
    }

    @Override
    public int getCornerRadius() {
        return mChart.getCornerRadius();
    }

    @Override
    public String getName() {
        return mChart.getName();
    }

    @Override
    public JSONObject getData() {
        return mChart.getData();
    }

    @Override
    public JSONArray getActions() {
        return mChart.getActions();
    }

    @Override
    public JSONObject getStyle() {
        return mChart.getStyle();
    }

    @Override
    public JSONObject getProperties() {
        return mChart.getProperties();
    }

    @Override
    public View getDisplayView() {
        return mChart.getDisplayView();
    }

    @Override
    public void setValue(Object attrs, Context context) {   }

    @Override
    public void loadData(Object data, Context context) {
        mChart.loadData(data, context);
    }

    @Override
    public void setFutureParent(View parent) { mFutureParent = parent;  }
}
