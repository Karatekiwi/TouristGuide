package at.ac.tuwien.touristguide;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import java.util.Locale;
import at.ac.tuwien.touristguide.service.LocationService;
import at.ac.tuwien.touristguide.tools.DatabaseHandler;


/**
 * @author Manu Weilharter
 * Automatic Tourist Guider Fragment
 */
public class GuiderFragment extends Fragment implements View.OnTouchListener {

    private Activity activity;
    private NavigationDrawerFragment navigationDrawerFragment;
    private LocationService locationService;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_guider, container, false);

        ImageView iv = (ImageView) rootView.findViewById(R.id.iv_guider);
        iv.setOnTouchListener(this);

        rootView.setBackgroundColor(Color.WHITE);

        return rootView;
    }


    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        int action = ev.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            //yellow: -1769728, white: -1, red: -65536
            int touchColor = getHotspotColor(R.id.iv_guider_overlay, (int) ev.getX(), (int) ev.getY());

            if (touchColor == -1769728) {
                startGuider();
            } else if (touchColor == -65536) {
                navigationDrawerFragment.openDrawer();
            }
        }

        return true;
    }


    // found at http://blahti.wordpress.com/2012/06/26/images-with-clickable-areas/
    public int getHotspotColor(int hotspotId, int x, int y) {
        ImageView img = (ImageView) activity.findViewById(hotspotId);
        if (img == null) {
            Log.d("ImageAreasActivity", "Hot spot image not found");
            return 0;
        } else {
            img.setDrawingCacheEnabled(true);
            Bitmap hotspots = Bitmap.createBitmap(img.getDrawingCache());

            if (hotspots == null) {
                Log.d("ImageAreasActivity", "Hot spot bitmap was not created");
                return 0;
            } else {
                img.setDrawingCacheEnabled(false);
                return hotspots.getPixel(x, y);
            }
        }
    }


    protected void startGuider() {
        String language;

        if (Locale.getDefault().getLanguage().equals("de")) {
            language = "de";
        } else {
            language = "en";
        }

        if (DatabaseHandler.getInstance(activity).getPoiCount(language) == 0) {
            showHelp(activity.getString(R.string.gdf1));
            return;
        }

        GuiderDetailsFragment fragment = new GuiderDetailsFragment();
        navigationDrawerFragment.setDetails(true);
        fragment.setLocationService(locationService);
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment).addToBackStack("overview").commitAllowingStateLoss();

        ((MainActivity) activity).setExit(false);
    }


    private void showHelp(String text) {
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(activity);
        helpBuilder.setMessage(text);
        helpBuilder.setCancelable(false);

        helpBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog helpDialog = helpBuilder.create();

        helpDialog.show();
    }


    public void setNavigationDrawerFragment(NavigationDrawerFragment navigationDrawerFragment) {
        this.navigationDrawerFragment = navigationDrawerFragment;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

}
