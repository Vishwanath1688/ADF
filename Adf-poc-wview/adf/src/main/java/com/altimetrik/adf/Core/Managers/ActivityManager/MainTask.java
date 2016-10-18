package com.altimetrik.adf.Core.Managers.ActivityManager;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;

import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 7/27/15.
 */
public class MainTask extends AsyncTask<Void,Void,Void> {

    private static final String TAG = makeLogTag(MainTask.class);

    private Context mContext;
    private ViewGroup mMainContainer;
    private View mSplashScreen;
    private ATKWidget mMainWidget;

    public MainTask(Context context, ViewGroup mainContainer, View splashScreen) {
        mContext = context;
        mMainContainer = mainContainer;
        mSplashScreen = splashScreen;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String absolutePathAssetsFolder = ATKContentManager.getAbsolutePathAssetsFolder(mContext);
        String webViewDefinition =
                "{ " +
                        "'id':'webView1'," +
                        "'class': 'ATKWebView'," +
                        "'name': 'ATKWebView'," +
                        "'style': {"+"'x': 0," +
                        "'y': 0," +
                        "'width': '100%'," +
                        "'height': '100%'" +
                        "}, 'properties':{}, 'data':{'source':'file:///android_res/raw/index.html', 'type': 'local'}" +
                        //"}, 'properties':{}, 'data':{'source':'file://"+ absolutePathAssetsFolder +"/dist/index.html', 'type': 'local'}" +
                        ", 'actions':[]" +
                        "}";
        try {
            JSONObject widgetJSON = new JSONObject(webViewDefinition);
            ATKComponentManager.getInstance().initImageAssets(mContext);
            mMainWidget = ATKComponentManager.getInstance().presentComponentInView(mContext, mMainContainer, widgetJSON);
        } catch (JSONException e) {
            LOGE(TAG, "mainTask", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        mMainWidget.getDisplayView().setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        mSplashScreen.bringToFront();

        mSplashScreen = null;
        mContext = null;
        mMainContainer = null;
        mMainWidget = null;
    }

}
