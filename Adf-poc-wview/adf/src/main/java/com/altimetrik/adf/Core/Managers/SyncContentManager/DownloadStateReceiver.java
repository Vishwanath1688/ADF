package com.altimetrik.adf.Core.Managers.SyncContentManager;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class DownloadStateReceiver extends BroadcastReceiver {

    private IDownloadCallbacks callback;

    public DownloadStateReceiver(IDownloadCallbacks callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            final int actionCode = bundle.getInt(DownloadService.ACTION);
            switch (actionCode) {
                case DownloadService.DOWNLOAD_RESULT:
                    int resultCode = bundle.getInt(DownloadService.RESULT);
                    switch (resultCode) {
                        case Activity.RESULT_OK:
                            if (callback != null){
                                callback.OnDownloadSuccess();
                            }
                            break;
                        case Activity.RESULT_CANCELED:
                            if (callback != null){
                                callback.OnDownloadFail();
                            }
                            break;
                        default:
                            break;
                    }
                    break;
                case DownloadService.PROGRESS:
                    int progressValue = bundle.getInt(DownloadService.RESULT);
                    if (callback != null){
                        callback.OnProgressUpdate(progressValue);
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
