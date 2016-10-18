package com.altimetrik.adf.Core.Managers.SyncContentManager;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.LOGI;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by gyordi on 5/19/15.
 */

public class DownloadService extends IntentService {

    private static final String TAG = makeLogTag(ATKSyncContentManager.class);

    public static final String NOTIFICATION = "com.altimetrik.adf.services.receiver";
    public static final String AUTHORIZATION_HEADER = "authorizationHeader";
    public static final String URL = "urlpath";
    public static final String FILENAME = "filename";
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result";
    public static final String REAL_VALUE = "realvalue";
    public static final String CANCEL = "cancel";
    public static final String ACTION = "action";
    public static final int DOWNLOAD_RESULT = 0;
    public static final int PROGRESS = 1;

    private int result = Activity.RESULT_CANCELED;

    boolean isCanceled;

    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.hasExtra(ACTION)) {
            // Set the canceling flag
            isCanceled = intent.getStringExtra(ACTION).equals(CANCEL);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // Will be called asynchronously be Android
    @Override
    protected void onHandleIntent(Intent intent) {
        // Clean up the possible queue
        if (intent.hasExtra(ACTION)) {
            boolean cancel = intent.getStringExtra(ACTION).equals(CANCEL);
            if (cancel) {
                return;
            }
        }

        BufferedInputStream input = null;
        RandomAccessFile output = null;
        File outputFileCache = null;

        try {
            String urlPath = intent.getStringExtra(DownloadService.URL);
            String fileName = intent.getStringExtra(DownloadService.FILENAME);
            String filePath = intent.getStringExtra(DownloadService.FILEPATH);
            String authorizationHeader = intent.getStringExtra(DownloadService.AUTHORIZATION_HEADER);

            outputFileCache = new File(filePath, fileName);
            /*outputFileCache.mkdirs();
            if (outputFileCache.exists()) {
                outputFileCache.delete();
            }*/

            LOGI(TAG, "Downloading " + urlPath);

            java.net.URL url = new URL(urlPath);

            HttpURLConnection connection;
            connection = (HttpURLConnection) url.openConnection();

            if (outputFileCache.exists()) {
                connection.setAllowUserInteraction(true);
                connection.setRequestProperty("Range", "bytes=" + outputFileCache.length() + "-");
            }

            if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
                connection.setRequestProperty("Authorization", authorizationHeader);
            }

            connection.setConnectTimeout(14000);
            connection.setReadTimeout(20000);
            connection.connect();
            int responseCode = connection.getResponseCode();
            switch (responseCode) {
                case 416:
                    if (outputFileCache.exists()) {
                        outputFileCache.delete();
                    }
                    connection.connect();
                case 200:
                case 202:
                case 205:
                case 206:
                    String connectionField = connection.getHeaderField("content-range");

                    long downloadedSize = 0;

                    if (connectionField != null) {
                        String[] connectionRanges = connectionField.substring("bytes=".length()).split("-");
                        downloadedSize = Long.valueOf(connectionRanges[0]);
                    }

                    if (connectionField == null && outputFileCache.exists())
                        outputFileCache.delete();

                    long fileLength = connection.getContentLength() + downloadedSize;
                    input = new BufferedInputStream(connection.getInputStream());
                    output = new RandomAccessFile(outputFileCache, "rw");
                    output.seek(downloadedSize);

                    int byteCount = 1024;
                    byte _data[] = new byte[byteCount];
                    int count;
                    int __progress = 0;

                    while ((count = input.read(_data, 0, byteCount)) != -1
                            && __progress != 100 && !isCanceled) {
                        downloadedSize += count;
                        output.write(_data, 0, count);
                        __progress = (int) ((downloadedSize * 100) / fileLength);
                        publishProgress(__progress, downloadedSize);
                    }
                    break;
                default:
                    throw new Exception("Invalid response code!");
            }
            if (isCanceled)
                result = Activity.RESULT_CANCELED;
            else
                // Sucessful finished
                result = Activity.RESULT_OK;

        } catch (Exception e) {
            result = Activity.RESULT_CANCELED;
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    LOGE(TAG, "onHandleIntent output.close()", e);
                }
            }
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGE(TAG, "onHandleIntent input.close()", e);
                }
            }
        }
        publishResults(outputFileCache.getAbsolutePath(), result);
    }

    private void publishResults(String outputPath, int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(ACTION, DOWNLOAD_RESULT);
        intent.putExtra(RESULT, result);
        intent.putExtra(FILEPATH, outputPath);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void publishProgress(int progress, long realValue) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(ACTION, PROGRESS);
        intent.putExtra(RESULT, progress);
        intent.putExtra(REAL_VALUE, realValue);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
