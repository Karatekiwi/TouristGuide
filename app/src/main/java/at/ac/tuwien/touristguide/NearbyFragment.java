package at.ac.tuwien.touristguide;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jsoup.Jsoup;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import at.ac.tuwien.touristguide.db.DatabaseHandler;
import at.ac.tuwien.touristguide.entities.Poi;
import at.ac.tuwien.touristguide.entities.RowItem;
import at.ac.tuwien.touristguide.tools.CustomListViewAdapterNearby;
import at.ac.tuwien.touristguide.tools.PoiHolder;
import at.ac.tuwien.touristguide.utils.LanguageUtils;


/**
 * @author Manu Weilharter
 * Shows the nearest pois
 */
public class NearbyFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String PERSISTENT_VARIABLE_BUNDLE_KEY = "persistentVariable";
    private static final int RC_ACCESS_LOCATION = 1;

    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private Activity activity;

    private Location location;

    private ListView lv_nearby;
    private TextView tv_nearby;

    private int position = -1;
    private int top = -1;
    private boolean viewDetails = false;

    private List<Poi> nearestPois;

    private FusedLocationProviderClient locationClient;
    private SwipeRefreshLayout swipeLayout;
    private CustomListViewAdapterNearby adapter;


    public NearbyFragment() {
        setArguments(new Bundle());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);
        rootView.setBackgroundColor(Color.WHITE);

        restoreFragment();

        initTextView(rootView);
        initListView(rootView);
        initSwipeLayout(rootView);
        initLocationClient();
        initLocationPermissions();

        return rootView;
    }

    private void initLocationClient() {
        locationClient = LocationServices.getFusedLocationProviderClient(activity);
    }

    private void restoreFragment() {
        Bundle mySavedInstanceState = getArguments();
        String persistentVariable = mySavedInstanceState.getString(PERSISTENT_VARIABLE_BUNDLE_KEY);

        // restore fragment
        if (persistentVariable != null) {
            position = Integer.parseInt(persistentVariable.split(",")[1]);
            top = Integer.parseInt(persistentVariable.split(",")[2]);
        }
    }

    private void initTextView(View rootView) {
        tv_nearby = rootView.findViewById(R.id.tv_nearby);
    }

    private void initSwipeLayout(View rootView) {
        swipeLayout = rootView.findViewById(R.id.srl_locations);
        swipeLayout.setOnRefreshListener(this);
        swipeLayout.setColorScheme(android.R.color.holo_orange_light,
                R.color.app_primary,
                android.R.color.holo_orange_light,
                R.color.app_primary);
    }

    private void initListView(View rootView) {
        lv_nearby = rootView.findViewById(R.id.lv_nearby);
        lv_nearby.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PoiDetailsFragment fragment = new PoiDetailsFragment();

                int index = lv_nearby.getFirstVisiblePosition();
                View childView = lv_nearby.getChildAt(0);
                int top = (childView == null) ? 0 : childView.getTop();
                savePosition(index, top);

                fragment.setPoi(nearestPois.get(position));
                fragment.setLocation(location);
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack("overview").commitAllowingStateLoss();
                viewDetails = true;

            }
        });
    }


    @SuppressLint("MissingPermission")
    private void updateLocation() {
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            setLocation(location);
                            startUp();
                        }
                    }
                });

    }

    /**
     * inits the listview by creating an async task to obtain the nearby pois
     */
    public void startUp() {
        viewDetails = false;

        String language = LanguageUtils.getLanguage();

        if (DatabaseHandler.getInstance(activity).getPoiCount(language) == 0) {
            tv_nearby.setText(activity.getString(R.string.nf1));
            return;
        }

        new CalculateNearbyPois().execute();
    }


    private void savePosition(int position, int top) {
        this.position = position;
        this.top = top;
    }


    @Override
    public void onPause() {
        super.onPause();
        if (viewDetails) {
            getArguments().putString(PERSISTENT_VARIABLE_BUNDLE_KEY, 25 + "," + position + "," + top);
        } else {
            getArguments().putString(PERSISTENT_VARIABLE_BUNDLE_KEY, 5 + "," + -1 + "," + -1);
        }
    }


    protected void getNearPois(List<Poi> nearestPois) {
        List<RowItem> rowItems = new ArrayList<>();

        if (nearestPois == null) {
            return;
        }

        tv_nearby.setVisibility(View.GONE);

        for (int index = 0; index < 25; index++) {
            try {
                RowItem item = new RowItem(R.drawable.placeholder, nearestPois.get(index).getName(),
                        Jsoup.parse(DatabaseHandler.getInstance(activity).getSectionsForPoi(nearestPois.get(index)).get(0).getContent()).text(),
                        (int) Math.round(nearestPois.get(index).getDistance()), nearestPois.get(index).getVisited());

                rowItems.add(item);
            } catch (Exception e) {
                // noop
            }
        }

        adapter = new CustomListViewAdapterNearby(activity, R.layout.listview_lines, rowItems);
        lv_nearby.setAdapter(adapter);

        if (position != -1) {
            lv_nearby.setSelectionFromTop(position, top);
        }
    }


    public void initList(List<Poi> list) {
        this.nearestPois = list;
        getNearPois(nearestPois);
    }


    private void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_ACCESS_LOCATION: {
                if (permissionGranted(grantResults)) {
                    updateLocation();
                } else {
                    tv_nearby.setText(activity.getString(R.string.nf4));
                    swipeLayout.setRefreshing(false);
                }

                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initLocationPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : PERMISSIONS) {
            int granted = ContextCompat.checkSelfPermission(activity, permission);
            if (granted != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (permissionsToRequest.isEmpty()) {
            updateLocation();
            return;
        }

        String[] toRequest = permissionsToRequest.toArray(new String[permissionsToRequest.size()]);
        requestPermissions(toRequest, RC_ACCESS_LOCATION);
    }

    private boolean permissionGranted(int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
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
        super.onDestroyView();
    }

    @Override
    public void onRefresh() {
        initLocationPermissions();
        updateLocation();
    }


    private class CalculateNearbyPois extends AsyncTask<URL, Integer, List<Poi>> {

        @Override
        protected List<Poi> doInBackground(URL... params) {
            List<Poi> nearestPois;

            if (location == null) {
                return null;
            }

            if (Locale.getDefault().getLanguage().equals("de")) {
                nearestPois = PoiHolder.retrieveNearPois(location.getLatitude(), location.getLongitude(), 25, false);
            } else {
                nearestPois = PoiHolder.retrieveNearPois(location.getLatitude(), location.getLongitude(), 25, true);
            }

            return nearestPois;
        }

        @Override
        protected void onPostExecute(List<Poi> str) {
            initList(str);
            swipeLayout.setRefreshing(false);
        }
    }

}
