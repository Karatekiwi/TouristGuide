package at.ac.tuwien.touristguide;


import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


/**
 * @author Manu Weilharter
 * MainActivity - starting point for the application
 */
public class MainActivity extends AppCompatActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    private NavigationDrawerFragment navigationDrawerFragment;
    private CharSequence title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        navigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        title = getTitle();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String action = intent.getAction();
        if (action.contains("noti")) {
           /* GuiderDetailsFragment fragment = new GuiderDetailsFragment();
            fragment.setSpecificPoi(DatabaseHandler.getInstance(this).getPoiByWikiId(action.split(" ")[1]));
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment).addToBackStack("overview").commitAllowingStateLoss();*/
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

        switch (position) {
            case 0:
                title = getResources().getStringArray(R.array.nav_drawer_items)[0];
                currentFragment = new NearbyFragment();
                break;
            case 1:
                title = getResources().getStringArray(R.array.nav_drawer_items)[1];
                currentFragment = new GoogleMapsFragment();
                break;
            case 2:
                title = getResources().getStringArray(R.array.nav_drawer_items)[2];
                currentFragment = new SettingsFragment();
                break;
            case 3:
                title = getResources().getStringArray(R.array.nav_drawer_items)[3];
                currentFragment = new UpdateFragment();
                break;
            case 4:
                title = getResources().getStringArray(R.array.nav_drawer_items)[4];
                currentFragment = new AboutFragment();
                break;

        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, currentFragment, "currentFragment").commitAllowingStateLoss();

    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
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
    protected void onDestroy() {
        super.onDestroy();
    }

}
