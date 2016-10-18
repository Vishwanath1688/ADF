package com.altimetrik.adf.Components.ATKDateTimePicker;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.View;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.Util.Constants;
import com.altimetrik.adf.Util.UIUtils;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 9/7/15.
 */
public class ATKDateTimePicker extends ATKComponentBase implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener, DialogInterface.OnCancelListener {

    private static final String TAG = makeLogTag(ATKDateTimePicker.class);

    //Style
    private String mPrimaryColor;

    //Properties
    private String mTitle;
    private String mType;
    private String mFormat;

    //Data
    private String mDefaultValue;

    private SimpleDateFormat mDateTimeFormatter;
    private Date mDefaultDate;

    private int mDefaultYear;
    private int mDefaultMonth;
    private int mDefaultDay;
    private int mDefaultHour;
    private int mDefaultMinute;

    private Calendar mDateTimeBuffer;

    private DialogFragment mDialog;
    private TimePickerDialog mTimePickerDialog;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        super.initWithJSON(widgetDefinition, context);

        //Get Data
        mDefaultValue = getData().optString("defaultValue");

        //Get Properties
        mTitle = getProperties().optString("title");
        mType = getProperties().optString("type", "datetime");
        mFormat = getProperties().optString("format");

        if (mFormat.isEmpty()) {
            switch (mType.toLowerCase()) {
                case Constants.ATK_DATE_TIME_PICKER_TYPE_DATE:
                    mFormat = Constants.ATK_DATE_TIME_PICKER_DATE_DEFAULT_FORMAT;
                    break;
                case Constants.ATK_DATE_TIME_PICKER_TYPE_TIME:
                    mFormat = Constants.ATK_DATE_TIME_PICKER_TIME_DEFAULT_FORMAT;
                    break;
                case Constants.ATK_DATE_TIME_PICKER_TYPE_DATETIME:
                    mFormat = Constants.ATK_DATE_TIME_PICKER_DATETIME_DEFAULT_FORMAT;
                    break;
            }
        }
        mDateTimeFormatter = new SimpleDateFormat(mFormat);

        //Get Style
        mPrimaryColor = getStyle().optString("primaryColor", "#000000");

        Calendar now = Calendar.getInstance();

        if (mDateTimeFormatter != null && !mDefaultValue.isEmpty()) {
            try {
                mDefaultDate = mDateTimeFormatter.parse(mDefaultValue);
            } catch (ParseException e) {
                LOGE(TAG, "Default Value Parse", e);
            }
        }

        if (mDefaultDate != null)
            now.setTime(mDefaultDate);

        mDefaultYear = now.get(Calendar.YEAR);
        mDefaultMonth = now.get(Calendar.MONTH);
        mDefaultDay = now.get(Calendar.DAY_OF_MONTH);
        mDefaultHour = now.get(Calendar.HOUR_OF_DAY);
        mDefaultMinute = now.get(Calendar.MINUTE);

        switch (mType.toLowerCase()) {
            case Constants.ATK_DATE_TIME_PICKER_TYPE_DATE:
                mDialog = DatePickerDialog.newInstance(
                        this,
                        mDefaultYear,
                        mDefaultMonth,
                        mDefaultDay
                );
                break;
            case Constants.ATK_DATE_TIME_PICKER_TYPE_TIME:
                mDialog = TimePickerDialog.newInstance(
                        this,
                        mDefaultHour,
                        mDefaultMinute,
                        true
                );
                break;
            case Constants.ATK_DATE_TIME_PICKER_TYPE_DATETIME:
                mDialog = DatePickerDialog.newInstance(
                        this,
                        mDefaultYear,
                        mDefaultMonth,
                        mDefaultDay
                );
                break;
        }

        if (mDialog != null) {
            if (mDialog instanceof DatePickerDialog) {
                DatePickerDialog datePickerDialog = (DatePickerDialog) mDialog;
                datePickerDialog.setAccentColor(UIUtils.parseColor(mPrimaryColor));
                datePickerDialog.vibrate(false);
                datePickerDialog.setOnCancelListener(this);
            } else if (mDialog instanceof TimePickerDialog) {
                TimePickerDialog timePickerDialog = (TimePickerDialog) mDialog;
                timePickerDialog.setAccentColor(UIUtils.parseColor(mPrimaryColor));
                timePickerDialog.vibrate(false);
                timePickerDialog.setOnCancelListener(this);
                if (!mTitle.isEmpty())
                    timePickerDialog.setTitle(mTitle);
            }
            mDialog.show(((Activity) context).getFragmentManager(), getID());
        } else {
            LOGE(TAG, "Error creating date / time picker.");
        }

        onPostInitWithJSON();

        return this;
    }

    @Override
    public View getDisplayView() {
        return null;
    }

    @Override
    public void setValue(Object attrs, Context context) {
        try {
            mDefaultDate = mDateTimeFormatter.parse((String) attrs);

            Calendar now = Calendar.getInstance();
            if (mDefaultDate != null)
                now.setTime(mDefaultDate);

            mDefaultYear = now.get(Calendar.YEAR);
            mDefaultMonth = now.get(Calendar.MONTH);
            mDefaultDay = now.get(Calendar.DAY_OF_MONTH);
            mDefaultHour = now.get(Calendar.HOUR_OF_DAY);
            mDefaultMinute = now.get(Calendar.MINUTE);
        } catch (ParseException e) {
            LOGD(TAG, "ATKDateTimePicker SetValue", e);
        }
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int year, int monthOfYear, int dayOfMonth) {
        try {

            String dateString = String.format("%04d-%02d-%02d", year, monthOfYear+1, dayOfMonth);
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dateFormatter.parse(dateString);

            if (mType.toLowerCase().equals(Constants.ATK_DATE_TIME_PICKER_TYPE_DATE)) {

                if (mDateTimeFormatter != null) {
                    String formattedDateString = mDateTimeFormatter.format(date);
                    if (getActions() != null) {
                        for (int i = 0; i < getActions().length(); i++) {
                            try {
                                JSONObject action = getActions().getJSONObject(i);
                                if (action.getString("event").compareTo(Constants.ATK_ACTION_SELECT) == 0) {
                                    JSONObject data = new JSONObject();
                                    data.put("id", getID());
                                    data.put("value", formattedDateString);
                                    ATKEventManager.excecuteComponentAction(action, data);
                                    ATKComponentManager.getInstance().removeComponentByID(mContext, getID());
                                }
                            } catch (JSONException e) {
                                LOGD(TAG, "ATKDateTimePicker ATKActionSelect onClick", e);
                            }
                        }
                    }
                }

            } else if (mType.toLowerCase().equals(Constants.ATK_DATE_TIME_PICKER_TYPE_DATETIME)) {

                mDateTimeBuffer = Calendar.getInstance();
                mDateTimeBuffer.setTime(date);

                mTimePickerDialog = TimePickerDialog.newInstance(
                        this,
                        mDefaultHour,
                        mDefaultMinute,
                        true
                );
                mTimePickerDialog.setAccentColor(UIUtils.parseColor(mPrimaryColor));
                mTimePickerDialog.vibrate(false);
                mTimePickerDialog.setOnCancelListener(this);
                if (!mTitle.isEmpty())
                    mTimePickerDialog.setTitle(mTitle);
                mTimePickerDialog.show(((Activity) mContext).getFragmentManager(), getID());

            }

        } catch (ParseException e) {
           LOGE(TAG, "Error parsing date", e);
        }
    }

    @Override
    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hourOfDay, int minute) {
        try {

            Date time = null;
            if (mType.toLowerCase().equals(Constants.ATK_DATE_TIME_PICKER_TYPE_TIME)) {
                String timeString = String.format("%02d:%02d", hourOfDay, minute);
                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");
                time = timeFormatter.parse(timeString);
            } else if (mType.toLowerCase().equals(Constants.ATK_DATE_TIME_PICKER_TYPE_DATETIME)) {
                mDateTimeBuffer.set(Calendar.HOUR, hourOfDay);
                mDateTimeBuffer.set(Calendar.MINUTE, minute);
                time = mDateTimeBuffer.getTime();
            }

            if (mDateTimeFormatter != null && time != null) {
                String formattedTimeString = mDateTimeFormatter.format(time);
                if (getActions() != null) {
                    for (int i = 0; i < getActions().length(); i++) {
                        try {
                            JSONObject action = getActions().getJSONObject(i);
                            if (action.getString("event").compareTo(Constants.ATK_ACTION_SELECT) == 0) {
                                JSONObject data = new JSONObject();
                                data.put("id", getID());
                                data.put("value", formattedTimeString);
                                ATKEventManager.excecuteComponentAction(action, data);
                                ATKComponentManager.getInstance().removeComponentByID(mContext, getID());
                            }
                        } catch (JSONException e) {
                            LOGD(TAG, "ATKDateTimePicker ATKActionSelect onClick", e);
                        }
                    }
                }
            }

        } catch (ParseException e) {
            LOGE(TAG, "Error parsing time", e);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (getActions() != null) {
            for (int i = 0; i < getActions().length(); i++) {
                try {
                    JSONObject action = getActions().getJSONObject(i);
                    if (action.getString("event").compareTo(Constants.ATK_ACTION_CANCEL) == 0) {
                        JSONObject data = new JSONObject();
                        data.put("id", getID());
                        ATKEventManager.excecuteComponentAction(action, data);
                        ATKComponentManager.getInstance().removeComponentByID(mContext, getID());
                    }
                } catch (JSONException e) {
                    LOGD(TAG, "ATKActionSheet ATKActionCancel onClick", e);
                }
            }
        }
    }

    public void rotateDialog() {
        if (mTimePickerDialog != null) {
            mTimePickerDialog.dismiss();
            mTimePickerDialog.show(((Activity) mContext).getFragmentManager(), getID());
        } else if (mDialog != null) {
            mDialog.dismiss();
            mDialog.show(((Activity) mContext).getFragmentManager(), getID());
        }
    }
}
