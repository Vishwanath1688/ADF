package com.altimetrik.adf.Components.ATKMediaPlayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Util.Utils;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by icabanas on 7/29/15.
 */
public class ATKMediaPlayer extends ATKComponentBase {

    private static final String TAG = makeLogTag(ATKMediaPlayer.class);

    private VideoView mVideoView;
    private String mType;
    private String mSource;

    private String mAbsolutePathAssetsFolder;
    private Boolean mAutoPlay;

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        super.initWithJSON(widgetDefinition, context);

        mAbsolutePathAssetsFolder = ATKContentManager.getAbsolutePathAssetsFolder(context);

        //Load particular params
        mType = getData().optString("type");
        mSource = getData().optString("source");
        mAutoPlay = Utils.isPositiveValue(getProperties().optString("autoPlay"));

        mVideoView = new VideoView(context);
        super.loadParams(mVideoView);

        loadMediaPlayer();

        final MediaController mediaController = new MediaController(context);
        mVideoView.setMediaController(mediaController);
        mVideoView.requestFocus();

        if (mAutoPlay)
            mVideoView.start();

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaController.show();
            }
        });

        onPostInitWithJSON();

        return this;
    }

    @Override
    public View getDisplayView() {
        return mVideoView;
    }

    public void loadMediaPlayer() {
        //Set particular params
        if (mSource != null && !mSource.equals("")) {
            if (mType.equalsIgnoreCase("local")) {
                String filePath = FilenameUtils.concat(mAbsolutePathAssetsFolder, mSource);
                mVideoView.setVideoPath(filePath);
            } else if (mType.equalsIgnoreCase("remote")) {
                Uri uri = Uri.parse(mSource);
                mVideoView.setVideoURI(uri);
            }
        }
    }

    @Override
    public void loadData(Object data, Context context) {
        JSONObject updatedData;
        try {
            updatedData = ((JSONObject) data).getJSONObject("data");
            mType = updatedData.optString("type");
            mSource = updatedData.optString("source");

            mVideoView.invalidate();
            loadMediaPlayer();
        } catch (JSONException e) {
            LOGD(TAG, "loadData", e);
        }
    }

    @Override
    public void resize(int x, int y, int width, int height, Context context) {
        super.resize(x, y, width, height, context);
        mVideoView.getHolder().setSizeFromLayout();
        mVideoView.requestLayout();
        mVideoView.invalidate();
    }

    @Override
    public void setValue(Object attrs, Context context) {  }


}
