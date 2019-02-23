package at.ac.tuwien.touristguide.tools;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import at.ac.tuwien.touristguide.entities.Poi;


/**
 * @author Manu Weilharter
 */
public class RouteHelper {

    private static final String TAG = RouteHelper.class.getName();

    private GoogleMap googleMap;

    public RouteHelper(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public static String getMapsApiDirectionsUrl(Location loc, Poi poi) {
        String waypoints = "waypoints=optimize:true|"
                + poi.getLatitude() + "," + poi.getLongitude()
                + "|" + loc.getLatitude() + "," + loc.getLongitude();

        String sensor = "sensor=false";
        String mode = "mode=walking";
        String origin = "origin=" + poi.getLatitude() + "," + poi.getLongitude();
        String destination = "destination=" + loc.getLatitude() + "," + loc.getLongitude();
        String params = waypoints + "&" + sensor + "&" + mode;
        String output = "json";

        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + origin + "&" + destination + "&" + params;
    }

    /**
     * checks if the user has an active internet connection, which is required for the update
     * @param activity the Activity
     * @return true if the user has an active internet connection, false otherwise
     */
    public static boolean isOnline(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void createReadTask(String url) {
        ReadTask readTask = new ReadTask();
        readTask.execute(url);
    }

    public class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            if (routes == null) {
                return;
            }

            ArrayList<LatLng> points;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(4);
                polyLineOptions.color(Color.RED);
            }

            googleMap.addPolyline(polyLineOptions);
        }
    }

}
