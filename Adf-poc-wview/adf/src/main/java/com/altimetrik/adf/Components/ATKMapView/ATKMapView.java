package com.altimetrik.adf.Components.ATKMapView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.view.View;
import android.widget.RelativeLayout;

import com.altimetrik.adf.Components.ATKComponentBase;
import com.altimetrik.adf.Components.ATKWidget;
import com.altimetrik.adf.Core.Managers.ComponentManager.ATKComponentManager;
import com.altimetrik.adf.Core.Managers.EventManager.ATKEventManager;
import com.altimetrik.adf.Util.UIUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.altimetrik.adf.Util.LogUtils.LOGD;
import static com.altimetrik.adf.Util.LogUtils.LOGE;
import static com.altimetrik.adf.Util.LogUtils.makeLogTag;
import static com.altimetrik.adf.Util.Utils.getDeviceDensity;

/**
 * Created by gyordi on 4/28/15.
 */
public class ATKMapView extends ATKComponentBase implements OnMapReadyCallback {

    private static final String TAG = makeLogTag(ATKMapView.class);

    private GoogleMap mGoogleMap;
    private MapView mMapView;
    private RelativeLayout mDisplayView;

    private JSONArray mAnnotations;
    private JSONObject mPopover;
    private Map<LatLng, JSONObject> mBindings;

    private Marker mSelectedMarker;

    private static Bitmap getBitmapFromAsset(Context context, String filePath) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(UIUtils.getDeviceFilePath(context, filePath));
        } catch (Exception e) {
            // handle exception
            LOGD(TAG, "getBitmapFromAsset", e);
        }

        return bitmap;
    }

    @Override
    public ATKWidget initWithJSON(JSONObject widgetDefinition, final Context context) {
        try {
            //Load base params --> super
            super.initWithJSON(widgetDefinition, context);

            mAnnotations = getData().getJSONArray("annotations");

            mPopover = widgetDefinition.optJSONObject("popover");

            mMapView = new MapView(context);
            mMapView.getMapAsync(this);
            mMapView.onCreate(null);
            mMapView.onResume();

            mDisplayView = new RelativeLayout(context);
            super.loadParams(mDisplayView);
            mDisplayView.addView(mMapView);
        } catch (JSONException e) {
            LOGE(TAG, "initWithJSON", e);
        }

        onPostInitWithJSON();

        return this;
    }

    @Override
    public void clean() {
        if (mMapView != null && mDisplayView != null) {
            mDisplayView.removeAllViews();
            mDisplayView = null;
            mMapView.onDestroy();
            mMapView = null;
        }
        super.clean();
    }

    @Override
    public View getDisplayView() {
        return mDisplayView;
    }

    @Override
    public void setValue(Object attrs, Context context) {
        //Set data
        try {
            if (mGoogleMap != null) {
                JSONObject data = (JSONObject) attrs;
                mAnnotations = data.getJSONArray("annotations");
                mGoogleMap.clear();
                loadAnnotations(data);
            } else {
                mData = (JSONObject) attrs;
            }
        } catch (JSONException e) {
            LOGD(TAG, "onMapLoadData", e);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            MapsInitializer.initialize(mContext);
        } catch (Exception e) {
            LOGD(TAG, "onMapReady MapsInitializer", e);
        }

        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setAllGesturesEnabled(true);

        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return createPopoverView(marker);
            }
        });

        JSONObject props = getProperties();
        JSONArray actions = getActions();
        try {
            // properties
            if (props != null) {
                JSONObject defaultRegion = props.getJSONObject("defaultRegion");
                if (defaultRegion != null) {
                    Double lat = defaultRegion.getDouble("lat");
                    Double lgn = defaultRegion.getDouble("long");
                    Double deltaLat = defaultRegion.getDouble("latitudeDelta");
                    Double deltaLgn = defaultRegion.getDouble("longitudeDelta");
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(new LatLng(lat + deltaLat, lgn + deltaLgn));
                    builder.include(new LatLng(lat - deltaLat, lgn + deltaLgn));
                    builder.include(new LatLng(lat + deltaLat, lgn - deltaLgn));
                    builder.include(new LatLng(lat - deltaLat, lgn - deltaLgn));
                    LatLngBounds bounds = builder.build();
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, getHeight(), getWidth(), 0));
                }

                boolean showUserLocation = props.getString("showUserLocation").equalsIgnoreCase("YES");
                if (showUserLocation) {
                    mGoogleMap.setMyLocationEnabled(true);
                    mGoogleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                        @Override
                        public void onMyLocationChange(Location location) {
                            mGoogleMap.setOnMyLocationChangeListener(null);
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                        }
                    });
                }

                mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        if (getActions() != null) {
                            for (int i = 0; i < getActions().length(); i++) {
                                try {
                                    JSONObject action = getActions().getJSONObject(i);
                                    if (action.getString("event").compareTo("ATKActionPopupSelect") == 0) {
                                        JSONObject data = new JSONObject();
                                        data.put("id", getID());
                                        ATKEventManager.excecuteComponentAction(action, data);
                                    }
                                } catch (JSONException e) {
                                    LOGD(TAG, "onInfoWindowClick onClick", e);
                                }
                            }
                        }
                    }
                });
            }

            // mAnnotations
            loadAnnotations(getData());

            if (actions != null) {
                for (int i = 0; i < actions.length(); i++) {
                    final JSONObject action = actions.getJSONObject(i);
                    if (action.getString("event").equals("ATKMapViewDidSelectAnnotation")) {
                        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                marker.showInfoWindow();
                                mSelectedMarker = marker;

                                JSONObject data = new JSONObject();
                                try {
                                    data.put("latitude", mSelectedMarker.getPosition().latitude);
                                    data.put("longitude", mSelectedMarker.getPosition().longitude);
                                    data.put("id", getID());
                                    ATKEventManager.excecuteComponentAction(action, data);
                                } catch (JSONException e) {
                                    LOGE(TAG, "put onMarkerClick", e);
                                }
                                return true;
                            }
                        });
                    } else if (action.getString("event").equals("ATKMapViewDidDeselectAnnotation")) {
                        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                if (mSelectedMarker != null) {
                                    JSONObject data = new JSONObject();
                                    try {
                                        data.put("latitude", mSelectedMarker.getPosition().latitude);
                                        data.put("longitude", mSelectedMarker.getPosition().longitude);
                                        data.put("id", getID());
                                        ATKEventManager.excecuteComponentAction(action, data);
                                    } catch (JSONException e) {
                                        LOGE(TAG, "put onMapClick", e);
                                    }
                                    mSelectedMarker = null;
                                }
                            }
                        });
                    }
                }
            }

        } catch (JSONException e) {
            LOGD(TAG, "onMapReady", e);
        }
    }

    private void loadAnnotations(JSONObject data) throws JSONException {
        final String iconName = data.optString("annotationsDefaultImage", null);
        if (mAnnotations != null && mAnnotations.length() > 0) {
            int len = mAnnotations.length();
            mBindings = new HashMap<>();
            for (int i = len; i > 0; i--) {
                JSONObject annotation = mAnnotations.getJSONObject(i - 1);
                String actualIconName = annotation.optString("image", null);
                if (actualIconName == null)
                    actualIconName = iconName;
                BitmapDescriptor icon = null;
                if (actualIconName != null && !actualIconName.equals("")) {
                    Bitmap bitmap = getBitmapFromAsset(mContext, actualIconName);
                    if (bitmap != null)
                        icon = BitmapDescriptorFactory.fromBitmap(bitmap);
                }
                if (icon == null)
                    icon = BitmapDescriptorFactory.defaultMarker();

                LatLng latLng = new LatLng(annotation.getDouble("lat"), annotation.optDouble("long"));
                MarkerOptions marker = new MarkerOptions()
                        .position(latLng)
                        .title(annotation.getString("title"))
                        .snippet(annotation.getString("subtitle"))
                        .icon(icon);
                mGoogleMap.addMarker(marker);

                if (annotation.has("popover")) {
                    JSONObject o = annotation.getJSONObject("popover");
                    mBindings.put(latLng, o);
                }
            }
        }
    }

    private View createPopoverView(Marker marker) {
        if (mPopover != null) {
            try {
                String contentSize = mPopover.get("contentSize").toString();
                if (contentSize != null) {
                    String[] tokens = contentSize.replace('{', ' ').replace('}', ' ').trim().split(",");

                    if (tokens.length > 1) {
                        float width = Float.parseFloat(tokens[0]);
                        float height = Float.parseFloat(tokens[1]);

                        RelativeLayout popoverView = new RelativeLayout(mContext);
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (width * getDeviceDensity(mContext)), (int) (height * getDeviceDensity(mContext)));
                        popoverView.setLayoutParams(params);

                        JSONArray contents = mPopover.getJSONArray("contentComponents");
                        int len = contents.length();
                        for (int i = len; i > 0; i--) {
                            JSONObject item = contents.getJSONObject(i - 1);
                            ATKWidget widget = ATKComponentManager.getInstance().presentComponentInView(mContext, popoverView, item.getJSONObject("componentJSON"));
                            widget.setValue(mBindings.get(marker.getPosition()).get(item.getString("bindKey")), mContext);
                        }
                        return popoverView;
                    }
                }
            } catch (JSONException e) {
                LOGD(TAG, "createPopoverView", e);
            }
        }
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null)
            mMapView.onResume();
    }

    @Override
    public void onPause() {
        if (mMapView != null)
            mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null)
            mMapView.onDestroy();
        super.onDestroy();
    }

    /**
     * @param data    Receives the params object containing destinationID and annotations to setvalue with
     * @param context
     */
    @Override
    public void loadData(final Object data, final Context context) {
        ((Activity) context).runOnUiThread(new Runnable() {
            public void run() {
                try {
                    JSONObject jsonData = (JSONObject) data;

                    setValue(jsonData.getJSONObject("data"), context);
                } catch (JSONException e) {
                    LOGD(TAG, "postNotification", e);
                }
            }
        });
    }

    public void dismissPopover() {
        if (mSelectedMarker != null && mSelectedMarker.isInfoWindowShown()) {
            mSelectedMarker.hideInfoWindow();
        }
    }
}
