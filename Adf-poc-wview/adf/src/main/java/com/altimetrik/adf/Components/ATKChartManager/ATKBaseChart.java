package com.altimetrik.adf.Components.ATKChartManager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.UIUtils;
import com.altimetrik.adf.Util.Utils;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 8/3/15.
 */
public class ATKBaseChart extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKBaseChart.class);
    protected static final int DEFAULT_ANIMATION_DURATION = 1200;

    private boolean mLegendIsHidden;
    private String mLegendAnchor;
    private int mLegendDisplacementX;
    private int mLegendDisplacementY;
    private String mLegendFillColor;
    private int mLegendBorderWidth;
    private String mLegendBorderColor;
    private String mLegendFontName;
    private String mLegendFontSize;
    private String mLegendFontColor;
    private int mLegendNumberOfColumns;

    private String mTitle;
    private String mTitleFontName;
    private int mTitleFontSize;
    private String mTitleFontColor;
    private String mTitleAnchor;
    private int mTitleDisplacementX;
    private int mTitleDisplacementY;

    private String mDataLabelFontName;
    private int mDataLabelFontSize;
    private String mDataLabelFontColor;

    protected JSONArray mItems;
    protected String mType;

    protected LinearLayout mContainer;
    protected TextView mTitleLabel;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, Context context) {
        super.initWithJSON(widgetDefinition, context);

        mContainer = new LinearLayout(context);
        mContainer.setOrientation(LinearLayout.VERTICAL);
        loadParams(mContainer);

        JSONObject legend = getStyle().optJSONObject("legend");
        if (legend != null) {
            mLegendIsHidden = Utils.isPositiveValue(legend.optString("legendIsHidden"));
            mLegendAnchor = legend.optString("legendAnchor");
            mLegendDisplacementX = legend.optInt("legendDisplacementX");
            mLegendDisplacementY = legend.optInt("legendDisplacementY");
            mLegendFillColor = legend.optString("legendFillColor");
            mLegendBorderWidth = legend.optInt("legendBorderWidth");
            mLegendBorderColor = legend.optString("legendBorderColor");
            mLegendFontName = legend.optString("legendFontName");
            mLegendFontSize = legend.optString("legendFontSize");
            mLegendFontColor = legend.optString("legendFontColor");
            mLegendNumberOfColumns = legend.optInt("legendNumberOfColumns");
        }

        mTitle = getStyle().optString("title");
        mTitleFontName = getStyle().optString("titleFontName");
        mTitleFontSize = getStyle().optInt("titleFontSize");
        mTitleFontColor = getStyle().optString("titleFontColor");
        mTitleAnchor = getStyle().optString("titleAnchor");
        mTitleDisplacementX = getStyle().optInt("titleDisplacementX");
        mTitleDisplacementY = getStyle().optInt("titleDisplacementY");

        mDataLabelFontName = getStyle().optString("dataLabelFontName");
        mDataLabelFontSize = getStyle().optInt("dataLabelFontSize");
        mDataLabelFontColor = getStyle().optString("dataLabelFontColor");

        readData();

        onPostInitWithJSON();

        return this;
    }

    protected void readData() {
        mType = getData().optString("type");
        if (mType.equals("local")) {
            mItems = getData().optJSONArray("source");
        }
    }

    protected void addChartToContainer(View chart) {
        LinearLayout.LayoutParams chartParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        chartParams.weight = 1;
        chart.setLayoutParams(chartParams);
        mContainer.addView(chart);
    }

    protected void updateChart(Chart chart) {
        chart.setDescription("");
        updateTitle();
        updateData(chart);
        updateLegend(chart);
    }

    private void updateData(Chart chart) {
        if (chart != null && chart.getData() != null) {
            if (!mDataLabelFontColor.isEmpty())
                chart.getData().setValueTextColor(UIUtils.parseColor(mDataLabelFontColor));
            if (mDataLabelFontSize > 0)
                chart.getData().setValueTextSize(mDataLabelFontSize);
            if (!mDataLabelFontName.isEmpty()) {
                String fontPath = String.format(Constants.ATK_FONT_DIR, mDataLabelFontName);
                try {
                    Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(mContext), fontPath));
                    chart.getData().setValueTypeface(font);
                } catch (Exception e) {
                    LOGW(TAG, String.format("Font %s not found.", fontPath));
                }
            }
        }
    }

    private void updateTitle() {
        if (!mTitle.isEmpty() && mTitleLabel == null) {
            mTitleLabel = new TextView(mContext);
            mTitleLabel.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            mTitleLabel.setGravity(Gravity.CENTER);
            mTitleLabel.setText(mTitle);

            if (!mTitleFontColor.isEmpty())
                mTitleLabel.setTextColor(UIUtils.parseColor(mTitleFontColor));

            if (mTitleFontSize > 0)
                mTitleLabel.setTextSize(mTitleFontSize);

            if (!mTitleFontName.isEmpty()) {
                String fontPath = String.format(Constants.ATK_FONT_DIR, mTitleFontName);
                try {
                    Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(mContext), fontPath));
                    mTitleLabel.setTypeface(font);
                } catch (Exception e) {
                    LOGW(TAG, String.format("Font %s not found.", fontPath));
                }
            }

            switch (mTitleAnchor.toLowerCase()) {
                case "top":
                    mContainer.addView(mTitleLabel, 0);
                    break;
                case "bottom":
                    mContainer.addView(mTitleLabel);
                    break;
                default:
                    mContainer.addView(mTitleLabel, 0);
                    break;
            }
        }
    }

    private void updateLegend(Chart chart) {

        if (chart != null) {
            Legend legend = chart.getLegend();
            if (mLegendIsHidden) {
                legend.setEnabled(false);
            } else {

                if (!mLegendFontColor.isEmpty())
                    legend.setTextColor(UIUtils.parseColor(mLegendFontColor));

                if (!mLegendFontSize.isEmpty())
                    legend.setTextSize(Float.parseFloat(mLegendFontSize));

                if (!mLegendFontName.isEmpty()) {
                    String fontPath = String.format(Constants.ATK_FONT_DIR, mLegendFontName);
                    try {
                        Typeface font = Typeface.createFromFile(FilenameUtils.concat(ATKContentManager.getAbsolutePathAssetsFolder(mContext), fontPath));
                        legend.setTypeface(font);
                    } catch (Exception e) {
                        LOGW(TAG, String.format("Font %s not found.", fontPath));
                    }
                }

                switch (mLegendAnchor.toLowerCase()) {
                    case "topright":
                        legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
                        break;
                    case "right":
                        legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_CENTER);
                        break;
                    case "bottomright":
                        legend.setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
                        break;
                    case "topleft":
                        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART);
                        break;
                    case "left":
                        legend.setPosition(Legend.LegendPosition.LEFT_OF_CHART_CENTER);
                        break;
                    case "bottomleft":
                        legend.setPosition(Legend.LegendPosition.BELOW_CHART_LEFT);
                        break;
                }
            }
        }

    }

    protected void parseChartData() {}

    protected void updateChartData() {}

    @Override
    public void loadData(Object data, Context context) {
        JSONObject chartData = ((JSONObject) data).optJSONObject("data");

        if (chartData != null) {
            mType = chartData.optString("type");
            if (mType.equals("local")) {
                mItems = chartData.optJSONArray("source");
            }
        }

        updateChartData();
    }


    @Override
    public View getDisplayView() {
        return mContainer;
    }

    @Override
    public void setValue(Object attrs, Context context) { }

    @Override
    public void clean() {
        mContainer.removeAllViews();
        mContainer = null;
        mTitleLabel = null;
        super.clean();
    }
}
