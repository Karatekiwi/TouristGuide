package at.ac.tuwien.touristguide;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.Html;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import at.ac.tuwien.touristguide.entities.Poi;
import at.ac.tuwien.touristguide.entities.Section;
import at.ac.tuwien.touristguide.service.LocationService;
import at.ac.tuwien.touristguide.service.TTSHelper;
import at.ac.tuwien.touristguide.tools.DatabaseHandler;
import at.ac.tuwien.touristguide.tools.HeightHelper;
import at.ac.tuwien.touristguide.tools.NLPHelper;
import at.ac.tuwien.touristguide.tools.PoiHolder;
import at.ac.tuwien.touristguide.tools.RouteHelper;


/**
 * @author Manu Weilharter
 * GuiderDetails: positioning is included in this frame, for it to be able to adapt to the user's movements
 */
public class GuiderDetailsFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = GuiderDetailsFragment.class.getName();

    private Button play_btn;
    private Button stop_btn;
    private Button back_btn;
    private Button fwd_btn;
    private Button pause_btn;
    private Poi poi;
    private int distance;
    private TextToSpeech tts;
    private String[] splitspeech;
    private int counter = 0;
    private EditText edittext_content;
    private TextView tv_guider_header;
    private TextView tv_error;
    private Button error_btn;
    private NLPHelper styler;
    private boolean playing;
    private boolean paused;
    private boolean stopped;
    private Poi specificPoi;
    private Poi poiOnMap;
    private List<Section> sections;
    private Context context;
    private Activity activity;
    private int numNotifications;
    private long startDate;
    private HashMap<String, String> myHash;
    private boolean repeat = false;
    private boolean firstPoi = true;
    private boolean screenWasLocked;
    private GoogleMap googleMap;
    private Marker lastOpenned;
    private SupportMapFragment mapFrag;
    private ScrollView scrollView;
    private ImageView transparentImage;
    private boolean helpShowing = false;
    private ProgressDialog pdialog;
    private ProgressDialog pdialog2;
    private LocationService locationService;
    private List<Poi> poisAlreadyNotified;
    private Poi currentPoi;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        myHash = new HashMap<>();
        myHash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "done");

        this.activity = activity;
        this.context = activity.getApplicationContext();

        numNotifications = 0;
        startDate = System.currentTimeMillis();

        distance = DatabaseHandler.getInstance(activity).getDistance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_guiderdetails, container, false);

        pdialog = new ProgressDialog(activity);
        pdialog.setCancelable(true);
        pdialog.setMessage(activity.getString(R.string.gdf2));
        pdialog.show();

        pdialog2 = new ProgressDialog(activity);
        pdialog2.setCancelable(true);
        pdialog2.setMessage(activity.getString(R.string.gdf15));

        timerDelayRemoveDialog(5000, pdialog);

        styler = new NLPHelper(activity);

        scrollView = rootView.findViewById(R.id.scrollView);
        transparentImage = rootView.findViewById(R.id.transparent_image);
        tv_guider_header = rootView.findViewById(R.id.tv_guiderdetails_header);
        tv_error = rootView.findViewById(R.id.tv_error);
        error_btn = rootView.findViewById(R.id.btn_error);
        edittext_content = rootView.findViewById(R.id.edittext_content);
        edittext_content.setKeyListener(null);
        edittext_content.setHighlightColor(Color.parseColor("#E0F0FF"));
        edittext_content.setPressed(true);
        edittext_content.setMovementMethod(new ScrollingMovementMethod());

        edittext_content.setMinHeight(new HeightHelper(activity).getHeight());

        scrollView.requestDisallowInterceptTouchEvent(true);

        transparentImage.setOnTouchListener(otl);

        play_btn = rootView.findViewById(R.id.guider_play_button);
        stop_btn = rootView.findViewById(R.id.guider_stop_button);
        back_btn = rootView.findViewById(R.id.guider_back_button);
        fwd_btn = rootView.findViewById(R.id.guider_fwd_button);
        pause_btn = rootView.findViewById(R.id.guider_pause_button);

        setButtonListerners();
        setButtonVisibility(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE);

        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver, new IntentFilter("better-loc"));
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver, new IntentFilter("location-change"));
        LocalBroadcastManager.getInstance(context).registerReceiver(mMessageReceiver, new IntentFilter("gps-off"));

        if (locationService != null) {
            locationService.startLocationUpdates();
        }

        poisAlreadyNotified = new ArrayList<>();
        rootView.setBackgroundColor(Color.WHITE);

        return rootView;
    }

    private void showHelp(String text) {
        AlertDialog.Builder helpBuilder = new AlertDialog.Builder(activity);
        helpBuilder.setMessage(text);
        helpBuilder.setCancelable(false);

        helpBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                helpShowing = false;
            }
        });

        AlertDialog helpDialog = helpBuilder.create();

        if (!helpShowing) {
            helpShowing = true;
            helpDialog.show();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentManager fm = getChildFragmentManager();
        mapFrag = (SupportMapFragment) fm.findFragmentById(R.id.map);

        transparentImage.setBackgroundColor(Color.WHITE);

        if (mapFrag == null) {
            mapFrag = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.route_map, mapFrag).commit();
        }
    }

    /**
     * sets the listeners for all buttons in the tts controll panel
     */
    private void setButtonListerners() {
        play_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setButtonEnabled(false, true, true, true, true);

                if (stopped) {
                    readPoi(LocationService.getLoc());
                } else {
                    tts.speak(splitspeech[counter], TextToSpeech.QUEUE_FLUSH, myHash);
                    highlightText();
                }

                playing = true;
                paused = false;
                stopped = false;
            }
        });

        stop_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                playing = false;
                paused = true;
                stopped = true;

                Toast.makeText(activity, activity.getString(R.string.gdf18), Toast.LENGTH_SHORT).show();

                tts.stop();

                counter = 0;
                setButtonEnabled(true, false, false, false, false);

                edittext_content.setSelection(0, 0);
                edittext_content.setPressed(true);
                scrollView.smoothScrollTo(0, 0);
            }
        });

        pause_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                playing = false;
                paused = true;

                tts.stop();

                setButtonEnabled(true, false, false, false, false);

                highlightText();
            }
        });

        back_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int index = styler.getSectionBeginning(sections, counter, splitspeech, true);

                if (index == -1) {
                    Toast.makeText(activity, activity.getString(R.string.pdf2), Toast.LENGTH_SHORT).show();
                } else {
                    playing = false;
                    tts.stop();

                    counter = index;

                    shortPause();

                    tts.speak(splitspeech[counter], TextToSpeech.QUEUE_FLUSH, myHash);
                    highlightText();

                    playing = true;
                }
            }
        });

        fwd_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int index = styler.getSectionBeginning(sections, counter, splitspeech, false);

                if (index == -1) {
                    Toast.makeText(activity, activity.getString(R.string.pdf1), Toast.LENGTH_SHORT).show();
                } else {
                    playing = false;
                    tts.stop();

                    counter = index;

                    shortPause();

                    tts.speak(splitspeech[counter], TextToSpeech.QUEUE_FLUSH, myHash);
                    highlightText();

                    playing = true;

                }
            }
        });


        error_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (checkLocationServices()) {
                    pdialog2.show();
                    timerDelayRemoveDialog(5000, pdialog2);
                }
            }
        });
    }


    /**
     * checks if there are other pois nearby and invokes the user notification
     *
     * @param loc: the current location of the user
     */
    protected void searchForNearPois(Location loc) {
        int dist = 75;

        List<Poi> nearPois;

        if (Locale.getDefault().getLanguage().equals("de"))
            nearPois = PoiHolder.retrieveNearPois(loc.getLatitude(), loc.getLongitude(), 25, false);
        else
            nearPois = PoiHolder.retrieveNearPois(loc.getLatitude(), loc.getLongitude(), 25, true);

        List<Poi> poisWithinDistance = new ArrayList<>();

        try {
            for (Poi poi : nearPois) {
                if (poi.getDistance() <= dist) {
                    if (!poi.getName().equals(this.poi.getName()) && !poisAlreadyNotified.contains(poi) && poi.getVisited() != 1)
                        poisWithinDistance.add(poi);
                } else
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        poisWithinDistance.remove(poi);

        if (poisWithinDistance.size() > 0) {
            createNotification(poisWithinDistance.get(0));
        }
    }


    /**
     * Notification if user passes by near an attraction while guider is active
     *
     * @param nearPoi the new poi which is near
     */
    private void createNotification(Poi nearPoi) {
        if (sendNotification()) {
            NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            Intent myIntent = new Intent(activity, MainActivity.class);
            myIntent.setAction("noti " + nearPoi.getWikiId());
            PendingIntent myPendingIntent = PendingIntent.getActivity(activity, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            Notification notification = new Notification.Builder(activity)
                    .setContentTitle(activity.getResources().getString(R.string.gdf16))
                    .setContentText(nearPoi.getName() + " " + activity.getResources().getString(R.string.gdf17))
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setSound(alarmSound)
                    .setContentIntent(myPendingIntent)
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), AudioManager.STREAM_MUSIC)
                    .getNotification();

            nm.notify(1, notification);
            poisAlreadyNotified.add(nearPoi);
            numNotifications++;
        }
    }


    /**
     * 0: Unlimited (= max 4 per minute)
     * 1: 1 per Minute
     * 2: 1 per 1/2 Hour
     * 3: 1 per Hour
     * 4: None
     *
     * @return true if a notification should be sent, false otherwise
     */
    private boolean sendNotification() {
        long endDate = System.currentTimeMillis();
        long timebetween = endDate - startDate;

        switch (DatabaseHandler.getInstance(activity).getNotify()) {

            case 0:
                return true;
            case 1:
                if (numNotifications == 0) {
                    return true;
                }

                if (timebetween >= 60000) {
                    startDate = System.currentTimeMillis();
                    return true;
                }

            case 2:
                if (numNotifications == 0) {
                    return true;
                }

                if (timebetween >= 1800000) {
                    startDate = System.currentTimeMillis();
                    return true;
                }

            case 3:
                if (numNotifications == 0) {
                    return true;
                }

                if (timebetween >= 3600000) {
                    startDate = System.currentTimeMillis();
                    return true;
                }

            case 4:
                return false;
        }

        return false;
    }


    /**
     * checks if the location services are enabled
     *
     * @return true if one of the location services is enabled, false otherwise
     */
    public boolean checkLocationServices() {
        if (!LocationService.isProviderEnabled()) {
            pdialog.dismiss();

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder
                    .setTitle(activity.getString(R.string.nf2))
                    .setMessage(activity.getString(R.string.nf3))
                    .setCancelable(false);

            builder.setNegativeButton(activity.getString(R.string.ma4), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface di, int id) {
                    showHelp(activity.getString(R.string.gdf12));
                    activity.onBackPressed();
                }
            });

            builder.setPositiveButton(activity.getString(R.string.ma5), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(gpsOptionsIntent);

                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            tv_error.setText(activity.getString(R.string.gdf14));
                            setButtonVisibility(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE);
                        }
                    }, 1000);

                }
            });


            AlertDialog alert = builder.create();
            alert.show();
        }

        return true;
    }


    /**
     * initializes the text-to-speech engine
     */
    private void initTTS() {
        tts = TTSHelper.tts;

        if (Locale.getDefault().getLanguage().equals("de"))
            tts.setLanguage(Locale.GERMAN);
        else
            tts.setLanguage(Locale.US);

        tts.setOnUtteranceProgressListener(upl);
    }


    /**
     * reads the nearest poi for the given location
     *
     * @param location the current location of the user
     */
    private void readPoi(Location location) {
        if (location == null) {
            tv_error.setText(activity.getString(R.string.gdf14));
            setButtonVisibility(View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.VISIBLE);
            pdialog.dismiss();
            pdialog2.dismiss();
            return;
        }

        setButtonVisibility(View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE, View.VISIBLE, View.GONE);
        tv_error.setVisibility(View.GONE);

        List<Poi> nearPois;

        if (Locale.getDefault().getLanguage().equals("de"))
            nearPois = PoiHolder.retrieveNearPois(location.getLatitude(), location.getLongitude(), -1, false);
        else
            nearPois = PoiHolder.retrieveNearPois(location.getLatitude(), location.getLongitude(), -1, true);


        if (nearPois.size() > 0) {
            List<Poi> poisWithinDistance = new ArrayList<>();
            List<Poi> poisUnvisited = new ArrayList<>();

            for (Poi poi : nearPois) {
                if (poi.getDistance() <= distance && poi.getVisited() == 0)
                    poisWithinDistance.add(poi);
                else if (poi.getVisited() == 0)
                    poisUnvisited.add(poi);

            }

            // FOUND an unvisited poi within the given distance
            if (poisWithinDistance.size() != 0 || specificPoi != null) {
                if (specificPoi != null) {
                    specificPoi.setDistance(PoiHolder.distFrom(specificPoi.getLatitude(), specificPoi.getLongitude(),
                            location.getLatitude(), location.getLongitude()));

                    poi = specificPoi;
                    specificPoi = null;
                } else
                    poi = poisWithinDistance.get(0);

                // get the sections for the information level
                int infoLevel = DatabaseHandler.getInstance(context).getLevel();

                if (infoLevel == 1) {
                    sections = styler.summarizeSections(DatabaseHandler.getInstance(activity).getSectionsForPoi(poi));
                } else if (infoLevel == 2) {
                    sections = new ArrayList<>();
                    sections.add(DatabaseHandler.getInstance(activity).getSectionsForPoi(poi).get(0));
                } else
                    sections = DatabaseHandler.getInstance(activity).getSectionsForPoi(poi);

                tv_guider_header.setText(Html.fromHtml("<font color=\"#769AC9\">" + poi.getName() + "<font>"));
                edittext_content.setBackgroundColor(Color.WHITE);

                String text = styler.structureTextForView(sections, infoLevel);
                edittext_content.setText(Html.fromHtml(text));
                edittext_content.refreshDrawableState();
                edittext_content.scrollTo(0, 0);

                initMap(poi);
                poiOnMap = poi;

                if (!firstPoi) {
                    try {
                        // a short break between reading two pois
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Log.e(TAG, e.toString());
                    }
                }

                splitspeech = styler.structureTextForTTS(poi, sections, true, infoLevel);

                repeat = false;
                firstPoi = false;
            }

            // DID NOT FIND an unvisited poi withinin the given distance
            else {
                String text;

                if (poisUnvisited.size() < 1) {
                    text = context.getString(R.string.gdf6);
                } else {
                    text = context.getString(R.string.gdf3) + " " + distance + " " + context.getString(R.string.gdf4) + " " +
                            poisUnvisited.get(0).getName() + " (" + (int) poisUnvisited.get(0).getDistance() + " " +
                            context.getString(R.string.gdf5);

                    initMap(poisUnvisited.get(0));
                    poiOnMap = poisUnvisited.get(0);
                }

                splitspeech = text.split("\\.");

                tv_guider_header.setText(Html.fromHtml("<font color=\"#769AC9\">" + "Information" + "<font>"));
                edittext_content.setBackgroundColor(Color.WHITE);
                edittext_content.setText(text);
                edittext_content.refreshDrawableState();
                edittext_content.scrollTo(0, 0);

                if (repeat) {
                    return;
                }

                poi = null;
                repeat = true;
            }

            if (tts != null) {
                if (tts.getLanguage() == null) {
                    setButtonEnabled(true, false, false, false, false);
                } else {
                    tts.speak(splitspeech[counter], TextToSpeech.QUEUE_FLUSH, myHash);

                    //mark poi visited
                    if (poi != null) {
                        DatabaseHandler.getInstance(activity).setVisited(poi.getWikiId(), 1);
                        PoiHolder.setVisited(poi.getWikiId(), 1);
                    }

                    highlightText();
                    setButtonEnabled(false, true, true, true, true);
                    playing = true;
                }
            }

        }

        pdialog.dismiss();
        pdialog2.dismiss();
    }


    /**
     * highlights the current sentence and moves the edittext view to center this sentence
     * this feature can be disabled in the app settings
     */
    private void highlightText() {
        try {
            // no need to highlight if the screen is locked
            if (!isScreenLocked()) {
                if (DatabaseHandler.getInstance(activity).getHighlight() == 1) {
                    final int start = edittext_content.getText().toString().indexOf(splitspeech[counter]);

                    if (start != -1) {
                        activity.runOnUiThread(new Thread() {
                            public void run() {
                                edittext_content.setSelection(start, start + splitspeech[counter].length());
                                edittext_content.setPressed(true);

                                Layout layout = edittext_content.getLayout();
                                int line = layout.getLineForOffset(start);

                                scrollView.smoothScrollTo(0, layout.getLineTop(line) - 200);
                            }
                        });
                    }

                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }


    /**
     * checks if the screen is locked - used to disable highlighting on locked screen
     */
    private boolean isScreenLocked() {
        KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        if (myKM.inKeyguardRestrictedInputMode()) {
            screenWasLocked = true;
            return true;
        } else {
            screenWasLocked = false;
            return false;
        }
    }


    /**
     * initializes the google map - draws the marker and the route (if online)
     */
    private void initMap(Poi currentPoi) {
        this.currentPoi = currentPoi;
        if (googleMap == null) {
            mapFrag.getMapAsync(this);
        }
    }


    private void setLocationEnabled(boolean enabled) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(enabled);
    }


    /**
     * sets the buttons enabled state according to the status of the tts engine (paused,...)
     */
    private void setButtonEnabled(boolean play, boolean stop, boolean pause, boolean fwd, boolean back) {
        play_btn.setEnabled(play);
        stop_btn.setEnabled(stop);
        pause_btn.setEnabled(pause);
        fwd_btn.setEnabled(fwd);
        back_btn.setEnabled(back);
    }


    /**
     * sets the buttons visibility
     */
    private void setButtonVisibility(int play, int stop, int pause, int fwd, int back, int error) {
        play_btn.setVisibility(play);
        stop_btn.setVisibility(stop);
        pause_btn.setVisibility(pause);
        fwd_btn.setVisibility(fwd);
        back_btn.setVisibility(back);
        error_btn.setVisibility(error);
    }


    public void timerDelayRemoveDialog(long time, final Dialog d) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (d.isShowing()) {
                    d.dismiss();
                    readPoi(LocationService.getLoc());
                }
            }
        }, time);
    }


    /**
     * adds a short pause for the tts engine to react
     */
    protected void shortPause() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e(TAG, e.toString());
        }
    }


    // also gets called if screen locks
    @Override
    public void onStop() {
        super.onStop();
    }


    // also gets called after screen lock
    @Override
    public void onStart() {
        super.onStart();
        initTTS();
        checkLocationServices();
    }


    @Override
    public void onDestroyView() {
        if (tts != null) {
            playing = false;
            paused = true;
            tts.stop();

            counter = 0;
            setButtonEnabled(true, false, false, false, false);
        }

        LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);

        super.onDestroyView();
    }


    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }


    public void setSpecificPoi(Poi poi) {
        this.specificPoi = poi;
    }


    /**
     * when the TTS engine is done with a line, onDone() of the upl gets called
     */
    UtteranceProgressListener upl = new UtteranceProgressListener() {

        @Override
        public void onStart(String utteranceId) {
        }

        @Override
        public void onError(String utteranceId) {
        }

        @Override
        // gets called if sentence is finished or tts.stop() is called
        public void onDone(String utteranceId) {
            if (playing) {
                counter++;

                if (counter < splitspeech.length) {
                    tts.speak(splitspeech[counter], TextToSpeech.QUEUE_FLUSH, myHash);
                    highlightText();
                }

                // the article is read completely
                else {
                    counter = 0;

                    activity.runOnUiThread(new Thread() {
                        public void run() {
                            try {
                                setButtonEnabled(true, false, false, false, false);
                                playing = false;

                                readPoi(LocationService.getLoc());

                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                    });
                }
            }
        }
    };

    /**
     * BroadcastReceiver to get Location Updates from the Location Service
     */
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("better-loc")) {
                Location loc = new Location("");
                loc.setLatitude(intent.getExtras().getDouble("Latitude"));
                loc.setLongitude(intent.getExtras().getDouble("Longitude"));

                if (poiOnMap != null)
                    initMap(poiOnMap);

                if (playing) {
                    searchForNearPois(loc);
                }

                if (!playing && !paused) {
                    readPoi(loc);
                }
            }
        }
    };

    /**
     * implemented for the combination scrollview + googlemaps, so that the map part can
     * be scrolled independently
     */
    OnTouchListener otl = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Disallow ScrollView to intercept touch events.
                    scrollView.requestDisallowInterceptTouchEvent(true);
                    // Disable touch on transparent view
                    return false;

                case MotionEvent.ACTION_UP:
                    // Allow ScrollView to intercept touch events.
                    scrollView.requestDisallowInterceptTouchEvent(false);
                    return true;

                case MotionEvent.ACTION_MOVE:
                    scrollView.requestDisallowInterceptTouchEvent(true);
                    return false;

                default:
                    return true;
            }
        }
    };


    @Override
    public void onResume() {
        super.onResume();
        if (screenWasLocked) {
            highlightText();
            screenWasLocked = false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        setLocationEnabled(true);
        googleMap.clear();

        if (!repeat) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPoi.getLatLng(), 15));
        }

        googleMap.addMarker(new MarkerOptions()
                .position(currentPoi.getLatLng())
                .title(currentPoi.getName()));

        Location loc = LocationService.getLoc();

        if (loc != null) {
            try {
                if (!isScreenLocked()) {
                    if (RouteHelper.isOnline(activity)) {
                        String url = RouteHelper.getMapsApiDirectionsUrl(loc, currentPoi);
                        new RouteHelper(googleMap).createReadTask(url);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        }
        transparentImage.setBackgroundColor(Color.TRANSPARENT);

        // custom listener, so that view doesn't get centered when clicking on the marker (if centered - only half of the info window is visible)
        googleMap.setOnMarkerClickListener(new OnMarkerClickListener() {
            public boolean onMarkerClick(Marker marker) {
                if (lastOpenned != null) {
                    lastOpenned.hideInfoWindow();

                    if (lastOpenned.equals(marker)) {
                        lastOpenned = null;
                        return true;
                    }
                }

                marker.showInfoWindow();
                lastOpenned = marker;

                return true;
            }
        });
    }
}
