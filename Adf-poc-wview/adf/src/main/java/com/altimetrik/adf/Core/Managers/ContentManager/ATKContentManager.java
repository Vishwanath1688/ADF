package com.altimetrik.adf.Core.Managers.ContentManager;

import android.content.Context;
import android.os.AsyncTask;

import com.altimetrik.adf.R;
import com.altimetrik.adf.Util.FileUtils;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

import static com.altimetrik.adf.Util.LogUtils.LOGI;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

/**
 * Created by pigounet on 3/26/15.
 */
public class ATKContentManager {

    private static final String TAG = makeLogTag(ATKContentManager.class);

    public static String getAbsolutePathAssetsFolder(Context context) {
        final String appAssetsFolderName = context.getResources().getString(R.string.assets_folder_name);
        return context.getFilesDir().getAbsolutePath() + File.separator + appAssetsFolderName;
    }

    public interface ICallbacks {
        void onSuccess();

        void onError(Exception exception);
    }

    public static void copyAssetsFromAPKAssets(final Context context, final ICallbacks callbacks) {
        new AsyncTask<Void, Void, Void>() {
            long timeBefore;

            @Override
            protected Void doInBackground(Void... params) {
                timeBefore = System.currentTimeMillis();
                final String apkAssetsFolderName = context.getResources().getString(R.string.assets_folder_akp);
                FileUtils.copyAssetsFileOrDir(context, getAbsolutePathAssetsFolder(context), apkAssetsFolderName);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                LOGI(TAG, "Files successfully extracted from APK. " + (System.currentTimeMillis() - timeBefore) / 1000f + " seconds");
                if (callbacks != null) {
                    callbacks.onSuccess();
                }
            }
        }.execute();
    }

    public static void copyAssetsFromZip(final Context context, final ICallbacks callbacks) {
        new AsyncTask<Void, Void, Void>() {
            private long timeBefore;
            private Exception exception;

            @Override
            protected Void doInBackground(Void... params) {
                timeBefore = System.nanoTime();
                final String apkAssetsZipFile = context.getResources().getString(R.string.assets_zip_file);
                try {
                    String absolutePathAssetsFolder = getAbsolutePathAssetsFolder(context);
                    String destinationZip = FilenameUtils.concat(absolutePathAssetsFolder, apkAssetsZipFile);
                    File f = new File(absolutePathAssetsFolder);
                    if (!f.exists()) {
                        f.mkdirs();
                    }

                    FileUtils.copyAssetsFile(context, absolutePathAssetsFolder, apkAssetsZipFile);
                    LOGI(TAG, "copyAssetsFile finished at " + (System.nanoTime() - timeBefore) / 1E9 + " seconds");

                    long unzippedSize = FileUtils.unzip(destinationZip, absolutePathAssetsFolder + File.separator);
                    LOGI(TAG, "unzip finished at " + (System.nanoTime() - timeBefore) / 1E9 + " seconds");

                } catch (Exception e) {
                    exception = e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if (exception == null) {
                    LOGI(TAG, "Files successfully extracted from APK. " + (System.nanoTime() - timeBefore) / 1E9 + " seconds");
                    if (callbacks != null) {
                        callbacks.onSuccess();
                    }
                } else {
                    if (callbacks != null) {
                        callbacks.onError(exception);
                    }
                }
            }
        }.execute();
    }

}
