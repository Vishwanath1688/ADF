package com.altimetrik.adf.Components;

import android.support.annotation.NonNull;

/**
 * Created by gyordi on 5/7/15.
 */
public interface IATKInput {

    @NonNull
    String getDataString();
    @NonNull
    String getInputId();
    void setValue(String value);

}
