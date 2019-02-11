package at.ac.tuwien.touristguide;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import at.ac.tuwien.touristguide.entities.Poi;
import at.ac.tuwien.touristguide.entities.PoiMarker;
import at.ac.tuwien.touristguide.service.LocationService;
import at.ac.tuwien.touristguide.tools.PoiHolder;
import at.ac.tuwien.touristguide.tools.PoiRenderer;


/**
 * @author Manu Weilharter
 * Map View of all pois
 */
public class GoogleMapsFragment extends Fragment implements OnInfoWindowClickListener, OnMarkerClickListener, OnMapReadyCallback {

    private GoogleMap googleMap;
    private SupportMapFragment mapFrag;
    private Context context;
    private Activity activity;
    private ProgressDialog dialog;

    private List<Poi> allPois;

    private boolean firstsetup = true;
    private ClusterManager<PoiMarker> mClusterManager;
    private Poi clickedPoi;
    private float zoomFactor = 15;

    private LatLngBounds bounds;
    private Marker lastOpened = null;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        context = activity.getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_gmaps, container, false);

        if (firstsetup) {
            dialog = new ProgressDialog(activity);
            dialog.setCancelable(false);
            dialog.setMessage(activity.getString(R.string.gmf2));
            dialog.show();
            timerDelayRemoveDialog(4500);
        }

        rootView.setBackgroundColor(Color.WHITE);

        return rootView;
    }


    public void timerDelayRemoveDialog(long time) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startUp();
            }
        }, time);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentManager fm = getChildFragmentManager();
        mapFrag = (SupportMapFragment) fm.findFragmentById(R.id.map);
        mapFrag = SupportMapFragment.newInstance();
        fm.beginTransaction().replace(R.id.map, mapFrag).commit();

    }


    public void startUp() {
        if (!checkLocationServices()) {
            Builder builder = new Builder(activity);

            builder.setNegativeButton(activity.getString(R.string.ma4), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();

                }
            });
            builder.setPositiveButton(activity.getString(R.string.ma5), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });

            builder.setTitle(activity.getString(R.string.nf2));
            builder.setMessage(activity.getString(R.string.gmf3));

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }


        if (Locale.getDefault().getLanguage().equals("de")) {
            allPois = PoiHolder.getPois_de();
        } else {
            allPois = PoiHolder.getPois_en();
        }

        mapFrag.getMapAsync(this);
    }

    private void setLocationEnabled(boolean enabled) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(enabled);
    }


    /**
     * sets up the google map, this includes positioning of the camera and enabling the current location
     */
    private void setUpMap() {
        Location currentLoc = LocationService.getLoc();
        Location mapLoc = googleMap.getMyLocation();

        if (firstsetup) {
            if (currentLoc != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLoc.getLatitude(), currentLoc.getLongitude()), zoomFactor));
            } else if (mapLoc != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mapLoc.getLatitude(), mapLoc.getLongitude()), zoomFactor));
            } else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(48.209943, 16.370257), zoomFactor));
            }

            dialog.dismiss();
        } else if (clickedPoi != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(clickedPoi.getLatitude(), clickedPoi.getLongitude()), zoomFactor));
        }

        firstsetup = false;
    }


    /**
     * adds all pois to the map as markers
     */
    private void addMarkers() {
        List<PoiMarker> markers = new ArrayList<>();

        mClusterManager.clearItems();

        for (Poi poi : allPois) {
            if (bounds.contains(poi.getLatLng())) {
                markers.add(new PoiMarker(poi));
            }
        }

        mClusterManager.addItems(markers);
        mClusterManager.cluster();

        if (lastOpened != null) {
            lastOpened.showInfoWindow();
        }
    }


    /**
     * checks if the location services are enabled
     *
     * @return true if one of the location services is enabled, false otherwise
     */
    public boolean checkLocationServices() {
        LocationManager lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        return (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }


    /**
     * implements the behaviour when the user clicks on the markers info window
     * -> the corresponding poi details view will be openend
     */
    @Override
    public void onInfoWindowClick(Marker marker) {
        clickedPoi = PoiRenderer.markerMap.get(marker).getPoi();

        zoomFactor = googleMap.getCameraPosition().zoom;

        if (clickedPoi != null) {
            PoiDetailsFragment fragment = new PoiDetailsFragment();
            fragment.setPoi(clickedPoi);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment).addToBackStack("overview").commitAllowingStateLoss();

            ((MainActivity) activity).setExit(false);
        }

    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!firstsetup) {
            startUp();
        }
    }


    @Override
    public void onDestroyView() {
        Fragment mapFragment = getFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null) {
            getFragmentManager().beginTransaction().remove(mapFragment).commit();
        }

        googleMap = null;

        super.onDestroyView();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        if (lastOpened != null) {
            lastOpened.hideInfoWindow();

            if (lastOpened.equals(marker)) {
                lastOpened = null;
                return true;
            }
        }

        marker.showInfoWindow();
        lastOpened = marker;

        return true;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        mClusterManager = new ClusterManager<>(context, googleMap);
        mClusterManager.setRenderer(new PoiRenderer(context, googleMap, mClusterManager));
        googleMap.setOnCameraIdleListener(mClusterManager);
        googleMap.setOnMarkerClickListener(this);
        googleMap.setOnInfoWindowClickListener(this);
        googleMap.clear();
        setLocationEnabled(true);

        googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                addMarkers();
            }
        });

        setUpMap();
    }
}
