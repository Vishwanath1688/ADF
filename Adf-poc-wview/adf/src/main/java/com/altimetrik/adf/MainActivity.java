package com.altimetrik.adf;

import android.Manifest;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RelativeLayout;

import com.altimetrik.adf.Core.Managers.ActivityManager.ATKActivityManager;
import com.altimetrik.adf.Core.Managers.BridgeManager.ATKBridgeManager;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.Util.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.altimetrik.adf.Util.LogUtils.LOGI;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = makeLogTag(MainActivity.class);

    private RelativeLayout mainView;

    private final List<ATKBridgeManager.IPermissionObserver> contactPermissionObserverList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainView = (RelativeLayout) findViewById(R.id.mainLayout);
        ATKActivityManager.initApp(mainView, MainActivity.this);
    }

    @Override
    public void onBackPressed() {
        ATKActivityManager.handleBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ATKActivityManager.handleNativeDeviceRotation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ATKActivityManager.resumeServices();
        ATKEventManager.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ATKActivityManager.pauseServices();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_LOCATION:
                // We have requested multiple permissions for location, so all of them need to be
                // checked.
                if (PermissionUtils.verifyPermissions(grantResults)) {
                    // All required permissions have been granted.
                    Snackbar.make(mainView, R.string.permission_available_location,
                            Snackbar.LENGTH_SHORT)
                            .show();
                    //useLocationImpl();
                    // we don't have a callback, we have to say "retry"
                } else {
                    Snackbar.make(mainView, R.string.permissions_not_granted,
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
                break;
            case PermissionUtils.REQUEST_CONTACTS:
                synchronized(contactPermissionObserverList) {
                    for (ATKBridgeManager.IPermissionObserver permissionObserver : contactPermissionObserverList) {
                        permissionObserver.OnPermissionGranted();
                    }
                    contactPermissionObserverList.clear();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            LOGI(TAG, "Displaying location permission rationale to provide additional context.");

            // Display a SnackBar with an explanation and a button to trigger the request.
            Snackbar.make(mainView, R.string.permission_location_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat
                                    .requestPermissions(MainActivity.this, PermissionUtils.PERMISSIONS_LOCATION,
                                            PermissionUtils.REQUEST_LOCATION);
                        }
                    })
                    .show();
        } else {
            // Location permissions have not been granted yet.
            showMessageOKCancel(getString(R.string.get_location_request_message),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    PermissionUtils.PERMISSIONS_LOCATION,
                                    PermissionUtils.REQUEST_LOCATION);
                        }
                    });
        }
    }

    public void requestContactPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_CONTACTS)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            LOGI(TAG, "Displaying contacts permission rationale to provide additional context.");

            // Display a SnackBar with an explanation and a button to trigger the request.
            Snackbar.make(mainView, R.string.permission_contacts_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat
                                    .requestPermissions(MainActivity.this, PermissionUtils.PERMISSIONS_CONTACT,
                                            PermissionUtils.REQUEST_CONTACTS);
                        }
                    })
                    .show();
        } else {
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(MainActivity.this,
                    PermissionUtils.PERMISSIONS_CONTACT,
                    PermissionUtils.REQUEST_CONTACTS);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, okListener)
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    public void addContactPermissionObserver(ATKBridgeManager.IPermissionObserver permissionObserver) {
        synchronized(contactPermissionObserverList) {
            contactPermissionObserverList.add(permissionObserver);
        }
    }
}
