package at.ac.tuwien.touristguide;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import at.ac.tuwien.touristguide.db.DatabaseHandler;
import at.ac.tuwien.touristguide.service.TTSHelper;


/**
 * @author Manu Weilharter
 * MainActivity - starting point for the application
 */
public class MainActivity extends AppCompatActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment navigationDrawerFragment;
    private CharSequence mTitle;
    private boolean exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        navigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        exit = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        try {
            String action = intent.getAction();
            if (action.contains("noti")) {
                GuiderDetailsFragment fragment = new GuiderDetailsFragment();
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

                mTitle = getResources().getStringArray(R.array.nav_drawer_items)[0];
                GuiderFragment gf = new GuiderFragment();
                gf.setNavigationDrawerFragment(navigationDrawerFragment);
                currentFragment = gf;
                break;
            case 1:
                if (navigationDrawerFragment != null) {
                    navigationDrawerFragment.setDetails(false);
                }
                mTitle = getResources().getStringArray(R.array.nav_drawer_items)[1];
                NearbyFragment nbf = new NearbyFragment();
                nbf.setNavFragment(navigationDrawerFragment);
                currentFragment = nbf;
                break;
            case 2:
                mTitle = getResources().getStringArray(R.array.nav_drawer_items)[2];
                currentFragment = new GoogleMapsFragment();
                break;
            case 3:
                mTitle = getResources().getStringArray(R.array.nav_drawer_items)[3];
                currentFragment = new SettingsFragment();
                break;
            case 4:
                mTitle = getResources().getStringArray(R.array.nav_drawer_items)[4];
                currentFragment = new UpdateFragment();
                break;
            case 5:
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
