package com.altimetrik.adf.Components.ATKPDFReader;

import android.content.Context;
import android.view.View;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.joanzapata.pdfview.PDFView;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

import java.io.File;

import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 7/30/15.
 */
public class ATKPDFReader extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKPDFReader.class);

    private PDFView mPDFView;
    private String mSource;
    private String mAbsolutePathAssetsFolder;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        super.initWithJSON(widgetDefinition, context);

        mAbsolutePathAssetsFolder = ATKContentManager.getAbsolutePathAssetsFolder(context);

        mSource = getData().optString("source", null);

        mPDFView = new PDFView(context, null);
        super.loadParams(mPDFView);

        File pdfFile = new File(FilenameUtils.concat(mAbsolutePathAssetsFolder, mSource));

        if (pdfFile.exists())
            mPDFView.fromFile(pdfFile)
                    .swipeVertical(true)
                    .load();

        onPostInitWithJSON();

        return this;
    }

    @Override
    public View getDisplayView() {
        return mPDFView;
    }

    @Override
    public void setValue(Object attrs, Context context) {
    }

    @Override
    public void clean() {
        mPDFView.recycle();
        mPDFView = null;
        super.clean();
    }
}
