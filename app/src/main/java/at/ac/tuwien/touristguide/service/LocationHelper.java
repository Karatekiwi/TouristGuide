package at.ac.tuwien.touristguide.service;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


/**
 * @author Manu Weilharter
 */
public class LocationHelper {

    private static final int RC_PERMISSION_ACCESS_LOCATION = 1;
    private static Location location;
    private FusedLocationProviderClient locationClient;

    public LocationHelper(Activity activity) {
        locationClient = LocationServices.getFusedLocationProviderClient(activity);

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // request permission
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    RC_PERMISSION_ACCESS_LOCATION);
        }

        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            setLocation(location);
                        }
                    }
                });
    }

    private void setLocation(Location location) {
        this.location = location;
    }

}


