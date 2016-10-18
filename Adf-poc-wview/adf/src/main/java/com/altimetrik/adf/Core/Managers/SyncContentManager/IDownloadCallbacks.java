package com.altimetrik.adf.Core.Managers.SyncContentManager;

/**
 * Created by gyordi on 5/19/15.
 */

public interface IDownloadCallbacks {
    void OnDownloadSuccess();

    void OnDownloadFail();

    void OnProgressUpdate(int value);
}
