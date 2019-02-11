package at.ac.tuwien.touristguide;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import at.ac.tuwien.touristguide.entities.Poi;
import at.ac.tuwien.touristguide.entities.RowItem;
import at.ac.tuwien.touristguide.service.LocationService;
import at.ac.tuwien.touristguide.tools.CustomListViewAdapterNearby;
import at.ac.tuwien.touristguide.tools.DatabaseHandler;
import at.ac.tuwien.touristguide.tools.PoiHolder;


/**
 * @author Manu Weilharter
 * Shows the nearest pois
 */
public class NearbyFragment extends Fragment {

    private static final String PERSISTENT_VARIABLE_BUNDLE_KEY = "persistentVariable";
    private Activity activity;
    private Context context;
    private Location location;
    private ListView lv_nearby;
    private TextView tv_nearby;
    private int position = -1;
    private int top = -1;
    private LinearLayout ll_header;
    private ProgressDialog pdialog;
    private List<Poi> nearestPois;
    private boolean viewDetails = false;
    private NavigationDrawerFragment navigationDrawerFragment;

    public NearbyFragment() {
        setArguments(new Bundle());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        context = activity.getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);
        pdialog = new ProgressDialog(activity);
        pdialog.setCancelable(true);
        pdialog.setMessage(activity.getString(R.string.nf4));
        pdialog.show();
        timerDelayRemoveDialog(2000);

        lv_nearby = (ListView) rootView.findViewById(R.id.lv_nearby);
        tv_nearby = (TextView) rootView.findViewById(R.id.tv_nearby);

        ll_header = (LinearLayout) rootView.findViewById(R.id.ll_listheader);
        ll_header.setVisibility(View.GONE);

        lv_nearby.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PoiDetailsFragment fragment = new PoiDetailsFragment();
                navigationDrawerFragment.setDetails(true);

                int index = lv_nearby.getFirstVisiblePosition();
                View v = lv_nearby.getChildAt(0);
                int top = (v == null) ? 0 : v.getTop();
                savePosition(index, top);

                fragment.setPoi(nearestPois.get(position));
                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).addToBackStack("overview").commitAllowingStateLoss();
                viewDetails = true;

                ((MainActivity) activity).setExit(false);
            }
        });

        Bundle mySavedInstanceState = getArguments();
        String persistentVariable = mySavedInstanceState.getString(PERSISTENT_VARIABLE_BUNDLE_KEY);

        // restore fragment
        if (persistentVariable != null) {
            position = Integer.parseInt(persistentVariable.split(",")[1]);
            top = Integer.parseInt(persistentVariable.split(",")[2]);
        }

        rootView.setBackgroundColor(Color.WHITE);

        return rootView;

    }

    /**
     * inits the listview by creating an async task to obtain the nearby pois
     */
    public void startUp() {
        viewDetails = false;

        String language;

        if (Locale.getDefault().getLanguage().equals("de")) {
            language = "de";
        } else {
            language = "en";
        }

        if (DatabaseHandler.getInstance(activity).getPoiCount(language) == 0) {
            pdialog.dismiss();
            tv_nearby.setText(activity.getString(R.string.nf1));
            ll_header.setVisibility(View.VISIBLE);
            return;
        }

        if (checkLocationServices()) {
            location = LocationService.getLoc();
            new CalculateNearbyPois().execute();
        }
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
            pdialog.dismiss();
            tv_nearby.setText(activity.getString(R.string.nf5));
            ll_header.setVisibility(View.VISIBLE);
            return;
        }

        tv_nearby.setVisibility(View.GONE);

        for (int i = 0; i < 25; i++) {
            try {
                RowItem item = new RowItem(R.drawable.placeholder, nearestPois.get(i).getName(),
                        Jsoup.parse(DatabaseHandler.getInstance(context).getSectionsForPoi(nearestPois.get(i)).get(0).getContent()).text(),
                        (int) Math.round(nearestPois.get(i).getDistance()), nearestPois.get(i).getVisited());

                rowItems.add(item);
            } catch (Exception e) {
                Log.e("NearbyFragment", e.toString());
            }
        }

        CustomListViewAdapterNearby adapter = new CustomListViewAdapterNearby(context, R.layout.listview_lines, rowItems);
        lv_nearby.setAdapter(adapter);

        pdialog.dismiss();

        if (position != -1) {
            lv_nearby.setSelectionFromTop(position, top);
        }
    }


    public void initList(List<Poi> list) {
        this.nearestPois = list;
        ll_header.setVisibility(View.GONE);
        getNearPois(nearestPois);
    }


    /**
     * checks if the location services are enabled
     * @return true if one of the location services is enabled, false otherwise
     */
    public boolean checkLocationServices() {
        if (!LocationService.isProviderEnabled()) {
            ll_header.setVisibility(View.VISIBLE);
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder
                    .setTitle(activity.getString(R.string.nf2))
                    .setMessage(activity.getString(R.string.nf3))
                    .setCancelable(false);

            builder.setNegativeButton(activity.getString(R.string.ma4), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface di, int id) {
                    pdialog.cancel();
                    tv_nearby.setText(context.getString(R.string.gdf13));
                }
            });

            builder.setPositiveButton(activity.getString(R.string.ma5), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    pdialog.cancel();
                    Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(gpsOptionsIntent);

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            tv_nearby.setText(activity.getString(R.string.nf5));
                        }
                    }, 1000);

                }
            });

            AlertDialog alert = builder.create();
            alert.show();
            return false;
        }

        return true;
    }

    public void timerDelayRemoveDialog(long time) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startUp();
            }
        }, time);
    }

    public void setNavigationDrawerFragment(NavigationDrawerFragment navigationDrawerFragment) {
        this.navigationDrawerFragment = navigationDrawerFragment;
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
        }
    }

}
