package at.ac.tuwien.touristguide;


import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import at.ac.tuwien.touristguide.service.LocationService;
import at.ac.tuwien.touristguide.service.LocationService.LocalBinder;
import at.ac.tuwien.touristguide.service.TTSHelper;
import at.ac.tuwien.touristguide.tools.DatabaseHandler;


/**
 * @author Manu Weilharter
 * MainActivity - starting point for the application
 */
public class MainActivity extends AppCompatActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment navigationDrawerFragment;
    private CharSequence mTitle;
    private LocationService locationService;
    private boolean bound = false;
    private boolean exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, LocationService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        setContentView(R.layout.activity_main);

        navigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        ActionBar actionBar = getSupportActionBar();

        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#769AC9")));

        exit = true;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        try {
            String action = intent.getAction();
            if (action.contains("noti")) {
                GuiderDetailsFragment fragment = new GuiderDetailsFragment();
                fragment.setLocationService(locationService);
                fragment.setSpecificPoi(DatabaseHandler.getInstance(this).getPoiByWikiId(action.split(" ")[1]));
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment).addToBackStack("overview").commitAllowingStateLoss();
            }
        } catch (Exception e) {
            Log.e("MainActivity", e.toString());
        }

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment currentFragment = null;
        exit = true;

        switch (position) {
            case 0:
                if (navigationDrawerFragment != null) {
                    navigationDrawerFragment.setDetails(false);
                }
                setLocationUpates(false);

                mTitle = getResources().getStringArray(R.array.nav_drawer_items)[0];
                GuiderFragment gf = new GuiderFragment();
                gf.setNavigationDrawerFragment(navigationDrawerFragment);
                gf.setLocationService(locationService);
                currentFragment = gf;
                break;
            case 1:
                setLocationUpates(true);
                if (navigationDrawerFragment != null) {
                    navigationDrawerFragment.setDetails(false);
                }
                mTitle = getResources().getStringArray(R.array.nav_drawer_items)[1];
                NearbyFragment nbf = new NearbyFragment();
                nbf.setNavigationDrawerFragment(navigationDrawerFragment);
                currentFragment = nbf;
                break;
            case 2:
                setLocationUpates(true);
                mTitle = getResources().getStringArray(R.array.nav_drawer_items)[2];
                currentFragment = new GoogleMapsFragment();
                break;
            case 3:
                setLocationUpates(false);
                mTitle = getResources().getStringArray(R.array.nav_drawer_items)[3];
                currentFragment = new SettingsFragment();
                break;
            case 4:
                setLocationUpates(false);
                mTitle = getResources().getStringArray(R.array.nav_drawer_items)[4];
                currentFragment = new UpdateFragment();
                break;
            case 5:
                setLocationUpates(false);
                mTitle = getResources().getStringArray(R.array.nav_drawer_items)[5];
                currentFragment = new AboutFragment();
                break;

        }

        if (currentFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, currentFragment, "currentFragment").commitAllowingStateLoss();
        } else {
            Log.e(this.toString(), "Error in creating fragment");
        }
    }

    private void setLocationUpates(boolean start) {
        if (bound) {
            if (start) {
                locationService.startLocationUpdates();
            } else {
                locationService.stopLocationUpdates();
            }
        }

    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(Html.fromHtml("<font color=\"white\" size=\"16\" >" + mTitle + "</font>"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            navigationDrawerFragment.openDrawer();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (exit) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this).setCancelable(false);
            alertDialog.setMessage(getString(R.string.gf1));

            alertDialog.setNegativeButton(getString(R.string.gf3), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            final Context context = this;

            alertDialog.setPositiveButton(getString(R.string.gf2), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    stopService(new Intent(context, TTSHelper.class));
                    stopService(new Intent(context, LocationService.class));
                    System.exit(0);
                }
            });

            alertDialog.show();
        } else {
            navigationDrawerFragment.setDetails(false);

            exit = true;

            try {
                super.onBackPressed();
            } catch (Exception e) {
                Log.e("MainActivity", e.toString());
            }
        }
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            LocalBinder binder = (LocalBinder) service;
            locationService = binder.getService();
            onNavigationDrawerItemSelected(0);
            bound = true;
            setLocationUpates(false);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
