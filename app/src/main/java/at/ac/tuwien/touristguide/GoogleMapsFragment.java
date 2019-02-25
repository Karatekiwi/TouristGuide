package at.ac.tuwien.touristguide;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import at.ac.tuwien.touristguide.entities.Poi;
import at.ac.tuwien.touristguide.entities.PoiMarker;
import at.ac.tuwien.touristguide.tools.PoiHolder;
import at.ac.tuwien.touristguide.tools.PoiRenderer;
import at.ac.tuwien.touristguide.utils.LanguageUtils;

import static androidx.core.content.PermissionChecker.checkSelfPermission;


/**
 * @author Manu Weilharter
 * Map View of all pois
 */
public class GoogleMapsFragment extends Fragment implements OnInfoWindowClickListener, OnMarkerClickListener, OnMapReadyCallback {

    private static final int PERMISSIONS_ACCESS_LOCATION = 25;
    private GoogleMap googleMap;
    private SupportMapFragment mapFrag;
    private Context context;
    private Activity activity;

    private List<Poi> allPois;

    private ClusterManager<PoiMarker> mClusterManager;
    private Poi clickedPoi;
    private float zoomFactor = 14;

    private LatLngBounds bounds;
    private Marker lastOpened = null;

    private FusedLocationProviderClient locationClient;
    private Location location;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        context = activity.getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        locationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        View rootView = inflater.inflate(R.layout.fragment_gmaps, container, false);
        rootView.setBackgroundColor(Color.WHITE);

        FragmentManager fm = getChildFragmentManager();
        mapFrag = (SupportMapFragment) fm.findFragmentById(R.id.map);
        mapFrag = SupportMapFragment.newInstance();
        fm.beginTransaction().replace(R.id.map, mapFrag).commit();

        startUp();

        return rootView;
    }

    public void startUp() {
        allPois = PoiHolder.getPois(LanguageUtils.getLocale());
        mapFrag.getMapAsync(this);
    }

    private void requestPermission() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ActivityCompat.requestPermissions(getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_ACCESS_LOCATION);

    }


    private void setUpMap() {
        if (checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            setLocation(location);
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoomFactor));
                            googleMap.setMyLocationEnabled(true);
                        }
                    }
                });
    }

    private void setLocation(Location location) {
         this.location = location;
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
            fragment.setLocation(location);

            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment).addToBackStack("overview").commitAllowingStateLoss();

        }

    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
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
        requestPermission();

        googleMap.setOnCameraChangeListener(new OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                addMarkers();
            }
        });

        setUpMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_ACCESS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(true);
                    return;
                }
            }
        }
    }

}
