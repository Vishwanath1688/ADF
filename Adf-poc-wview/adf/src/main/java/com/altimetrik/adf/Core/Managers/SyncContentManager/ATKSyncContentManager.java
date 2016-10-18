package com.altimetrik.adf.Core.Managers.SyncContentManager;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.altimetrik.adf.Core.Managers.ContentManager.ATKContentManager;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.R;
import com.altimetrik.adf.Util.FileUtils;

import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGW;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by gyordi on 5/20/15.
 */
public class ATKSyncContentManager {

    private static final String TAG = makeLogTag(ATKSyncContentManager.class);

    private static boolean syncRunning = false;

    private static DownloadStateReceiver mDownloadStateReceiver;

    public static synchronized void sync(final Context context, String urlString, String authorizationHeader, final String callbackFunction, final String senderId) {
        if (syncRunning) {
            LOGW(TAG, "Synchronization already running");
            return;
        }

        final String fileName = context.getResources().getString(R.string.sync_update_file_name);

        final String appAssetsFolder = ATKContentManager.getAbsolutePathAssetsFolder(context) + File.separator;

        final String downloadFolder = context.getFilesDir().getAbsolutePath();

        final String destinationFolder = downloadFolder + File.separator + "temp" + File.separator;

        File tempDir = new File(destinationFolder);

        FileUtils.deleteRecursive(tempDir);

        mDownloadStateReceiver = new DownloadStateReceiver(
                new IDownloadCallbacks() {
                    @Override
                    public void OnDownloadSuccess() {

                        new AsyncTask<Void, Void, Void>() {
                            private Exception exception;

                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    String updateFileName = FilenameUtils.concat(downloadFolder, fileName);
                                    long unzippedSize = FileUtils.unzip(updateFileName, destinationFolder);
                                    Log.i("doInBackground", unzippedSize + " unzippedSize");
                                    Log.i("doInBackground", FileUtils.bytesAvailableInternalSpace() + " bytesAvailable");
                                    if (unzippedSize < FileUtils.bytesAvailableInternalSpace()) {
                                        FileUtils.moveRecursive(new File(destinationFolder), new File(appAssetsFolder));
                                    } else {
                                        exception = new RuntimeException("Not enough free space");
                                    }
                                    File toDelete = new File(updateFileName);
                                    if (!toDelete.delete()) {
                                        LOGE(TAG, "Unable to delete file " + updateFileName);
                                    }
                                } catch (Exception e) {
                                    exception = e;
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                onSyncFinish(context);
                                notifyResult(exception, callbackFunction, senderId);
                            }
                        }.execute();
                    }

                    @Override
                    public void OnDownloadFail() {
                        onSyncFinish(context);
                        notifyResult(new RuntimeException("Unable to download updates"), callbackFunction, senderId);
                    }

                    @Override
                    public void OnProgressUpdate(int value) {
                        //progressBar.setProgress(value);
                    }
                }
        );

        IntentFilter mStatusIntentFilter = new IntentFilter();
        mStatusIntentFilter.addAction(DownloadService.NOTIFICATION);
        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(context).registerReceiver(
                mDownloadStateReceiver,
                mStatusIntentFilter);

        // start download service
        Intent intent = new Intent(context, DownloadService.class);

        intent.putExtra(DownloadService.FILEPATH, downloadFolder);
        intent.putExtra(DownloadService.FILENAME, fileName);
        intent.putExtra(DownloadService.URL, urlString);
        intent.putExtra(DownloadService.AUTHORIZATION_HEADER, authorizationHeader);

        context.startService(intent);

        syncRunning = true;
    }

    private static void onSyncFinish(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mDownloadStateReceiver);
        syncRunning = false;
    }

    private static void notifyResult(Exception exception, String callbackFunction, String senderId) {
        JSONObject action = new JSONObject();

        JSONObject data = new JSONObject();

        JSONObject params = new JSONObject();
        try {
            data.put("success", exception == null);
            data.put("error", exception != null ? exception.getMessage() : "");
            data.put("id", "");

            params.put("webId", senderId);
            params.put("callbackFunction", callbackFunction);

            action.put("data", data);
            action.put("type", "javascript");
            action.put("params", params);

            ATKEventManager.excecuteComponentAction(action, data);
        } catch (JSONException e) {
            LOGE(TAG, "OnDownloadSuccess onPostExecute", e);
        }
    }
}
