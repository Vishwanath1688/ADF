package com.altimetrik.adf.Components.ATKBarcodeScanner;

import android.content.Context;
import android.view.View;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.Util.Constants;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 9/14/15.
 */
public class ATKBarcodeScanner extends ATKComponentBase implements ZXingScannerView.ResultHandler {

    private static final String TAG = makeLogTag(ATKBarcodeScanner.class);

    private ZXingScannerView mScannerView;
    private JSONArray mSupoortedTypes;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        super.initWithJSON(widgetDefinition, context);

        mScannerView  = new ZXingScannerView(context);
        super.loadParams(mScannerView);

        mSupoortedTypes = getProperties().optJSONArray("supportedTypes");

        if (mSupoortedTypes != null) {
            List<BarcodeFormat> supportedTypes = getSupportedTypes();
            if (supportedTypes.size() > 0) {
                mScannerView.setFormats(supportedTypes);
            }
        }

        mScannerView.setResultHandler(this);
        mScannerView.startCamera();

        return this;
    }

    @Override
    public View getDisplayView() {
        return mScannerView;
    }

    @Override
    public void setValue(Object attrs, Context context) { }

    @Override
    public void handleResult(Result result) {
        JSONArray actions = getActions();
        if (actions != null) {
            for (int i = 0; i < actions.length(); i++) {
                try {
                    JSONObject action = actions.getJSONObject(i);
                    if (action.getString("event").equals(Constants.ATK_ACTION_MORE_DATA)) {
                        JSONObject data = new JSONObject();
                        data.put("id", getID());
                        data.put("value", result.getText());
                        data.put("format",  result.getBarcodeFormat().toString());
                        ATKEventManager.excecuteComponentAction(action, data);
                    }
                } catch (JSONException e) {
                    LOGD(TAG, "ATKTextField on text change", e);
                }
            }
        }
    }

    @Override
    public void clean() {
        mScannerView.stopCamera();
        mScannerView = null;
        super.clean();
    }

    private List<BarcodeFormat> getSupportedTypes() {
        List<BarcodeFormat> barcodeFormats = new ArrayList<>();

        try {
            for (int i = 0; i < mSupoortedTypes.length(); i++) {
                String barcodeFormat = mSupoortedTypes.getString(i);
                switch (barcodeFormat) {
                    case Constants.ATK_BARCODE_SCANNER_TYPE_QR:
                        barcodeFormats.add(BarcodeFormat.QR_CODE);
                        break;
                    case Constants.ATK_BARCODE_SCANNER_TYPE_UPCE:
                        barcodeFormats.add(BarcodeFormat.UPC_E);
                        break;
                    case Constants.ATK_BARCODE_SCANNER_TYPE_CODE_39:
                        barcodeFormats.add(BarcodeFormat.CODE_39);
                        break;
                    case Constants.ATK_BARCODE_SCANNER_TYPE_CODE_93:
                        barcodeFormats.add(BarcodeFormat.CODE_93);
                        break;
                    case Constants.ATK_BARCODE_SCANNER_TYPE_CODE_128:
                        barcodeFormats.add(BarcodeFormat.CODE_128);
                        break;
                    case Constants.ATK_BARCODE_SCANNER_TYPE_EAN_8:
                        barcodeFormats.add(BarcodeFormat.EAN_8);
                        break;
                    case Constants.ATK_BARCODE_SCANNER_TYPE_EAN_13:
                        barcodeFormats.add(BarcodeFormat.EAN_13);
                        break;
                    case Constants.ATK_BARCODE_SCANNER_TYPE_PDF_417:
                        barcodeFormats.add(BarcodeFormat.PDF_417);
                        break;
                    case Constants.ATK_BARCODE_SCANNER_TYPE_ITF_14:
                        barcodeFormats.add(BarcodeFormat.ITF);
                        break;
                    case Constants.ATK_BARCODE_SCANNER_TYPE_DATA_MATRIX:
                        barcodeFormats.add(BarcodeFormat.DATA_MATRIX);
                        break;
                }
            }
        } catch (JSONException e) {
            LOGE(TAG, "Unsupported scanner types", e);
        }

        return barcodeFormats;
    }
}
