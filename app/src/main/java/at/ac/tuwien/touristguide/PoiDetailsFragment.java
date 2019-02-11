package at.ac.tuwien.touristguide;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
 */
@SuppressWarnings("deprecation")
public class PoiDetailsFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = PoiDetailsFragment.class.getName();

    private Button play_btn;
    private Button stop_btn;
    private Button back_btn;
    private Button fwd_btn;
    private Button pause_btn;

    private TextToSpeech tts;
    private String[] splitspeech;
    private int counter = 0;
    private EditText edittext_content;

    private Activity activity;

    private Poi poi;
    private NLPHelper styler;
    private List<Section> sections;

    private GoogleMap googleMap;
    private Marker lastOpenned;
    private SupportMapFragment mapFrag;
    private ScrollView scrollView;

    private HashMap<String, String> myHash;
    private boolean playing;
    /**
     * when the tts engine is done with a line, onDone() of the oupl gets called
     */
    UtteranceProgressListener oupl = new UtteranceProgressListener() {

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
                    String sentence = styler.replacementsForTTS(splitspeech[counter]);
                    tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, myHash);

                    highlightText();
                }

                // the article is read completely
                else {
                    activity.runOnUiThread(new Thread() {
                        public void run() {
                            try {
                                setButtonStates(true, false, false, false, false);
                                highlightText();
                            } catch (Exception e) {
                                Log.e(TAG, e.toString());
                            }
                        }
                    });
                }
            }
        }
    };

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_poidetails, container, false);
        styler = new NLPHelper(activity);

        myHash = new HashMap<>();
        myHash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "done");

        int infoLevel = DatabaseHandler.getInstance(activity).getLevel();

        scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);
        ImageView transparentImage = (ImageView) rootView.findViewById(R.id.transparent_image);

        scrollView.requestDisallowInterceptTouchEvent(true);

        transparentImage.setOnTouchListener(new OnTouchListener() {

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
        });

        TextView tv_poidetails_header = (TextView) rootView.findViewById(R.id.tv_poidetails_header);
        edittext_content = (EditText) rootView.findViewById(R.id.edittext_content);
        edittext_content.setKeyListener(null);
        edittext_content.setHighlightColor(Color.parseColor("#E0F0FF"));
        edittext_content.setPressed(true);
        edittext_content.setMinHeight(new HeightHelper(activity).getHeight());

        tv_poidetails_header.setText(Html.fromHtml("<font color=\"#769AC9\">" + poi.getName() + "<font>"));

        if (infoLevel == 1) {
            sections = styler.summarizeSections(DatabaseHandler.getInstance(activity).getSectionsForPoi(poi));
        } else if (infoLevel == 2) {
            sections = new ArrayList<>();
            sections.add(DatabaseHandler.getInstance(activity).getSectionsForPoi(poi).get(0));
        } else
            sections = DatabaseHandler.getInstance(activity).getSectionsForPoi(poi);

        splitspeech = styler.structureTextForTTS(poi, sections, false, infoLevel);

        edittext_content.setBackgroundColor(Color.WHITE);

        String text = styler.structureTextForView(sections, infoLevel);

        edittext_content.setText(Html.fromHtml(text));

        edittext_content.refreshDrawableState();
        edittext_content.setMovementMethod(new ScrollingMovementMethod());

        play_btn = (Button) rootView.findViewById(R.id.details_play_button);
        stop_btn = (Button) rootView.findViewById(R.id.details_stop_button);
        back_btn = (Button) rootView.findViewById(R.id.details_back_button);
        fwd_btn = (Button) rootView.findViewById(R.id.details_fwd_button);
        pause_btn = (Button) rootView.findViewById(R.id.details_pause_button);

        playing = false;

        setButtonListerners();
        setButtonStates(true, false, false, false, false);
        initTTS();

        if (poi != null) {
            DatabaseHandler.getInstance(activity).setVisited(poi.getWikiId(), 1);
            PoiHolder.setVisited(poi.getWikiId(), 1);
        }

        rootView.setBackgroundColor(Color.WHITE);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentManager fm = getChildFragmentManager();
        mapFrag = (SupportMapFragment) fm.findFragmentById(R.id.map);

        if (mapFrag == null) {
            mapFrag = SupportMapFragment.newInstance();
            fm.beginTransaction().replace(R.id.route_map, mapFrag).commit();
        }
    }

    private void setupMap() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        googleMap.setMyLocationEnabled(true);

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


        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(poi.getLatLng(), 15));
        googleMap.addMarker(new MarkerOptions()
                .position(poi.getLatLng())
                .title(poi.getName()));


        Location loc = LocationService.getLoc();

        if (loc != null) {
            if (RouteHelper.isOnline(activity)) {
                String url = RouteHelper.getMapsApiDirectionsUrl(loc, poi);
                new RouteHelper(googleMap).createReadTask(url);
            }
        }
    }

    private void initTTS() {
        tts = TTSHelper.tts;

        if (Locale.getDefault().getLanguage().equals("de"))
            tts.setLanguage(Locale.GERMAN);
        else
            tts.setLanguage(Locale.US);

        tts.setOnUtteranceProgressListener(oupl);
    }

    /**
     * Button Listeners for TTS control buttons
     */
    private void setButtonListerners() {
        play_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String sentence = styler.replacementsForTTS(splitspeech[counter]);
                tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, myHash);

                setButtonStates(false, true, true, true, true);
                highlightText();

                playing = true;
            }
        });

        pause_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                playing = false;
                tts.stop();

                setButtonStates(true, false, false, false, false);
                highlightText();
            }
        });

        stop_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                playing = false;
                tts.stop();

                counter = 0;

                setButtonStates(true, false, false, false, false);
                highlightText();
            }
        });

        back_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int index = styler.getSectionBeginning(sections, counter, splitspeech, true);

                if (index == -1) {
                    Toast.makeText(getActivity(), activity.getString(R.string.pdf2), Toast.LENGTH_SHORT).show();
                } else {
                    playing = false;
                    tts.stop();

                    counter = index;

                    shortPause();

                    String sentence = styler.replacementsForTTS(splitspeech[counter]);
                    tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, myHash);

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
                    Toast.makeText(getActivity(), activity.getString(R.string.pdf1), Toast.LENGTH_SHORT).show();
                } else {
                    playing = false;
                    tts.stop();

                    counter = index;

                    shortPause();

                    String sentence = styler.replacementsForTTS(splitspeech[counter]);
                    tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, myHash);

                    highlightText();

                    playing = true;

                }
            }
        });

    }


    private void highlightText() {
        if (DatabaseHandler.getInstance(activity).getHighlight() == 1) {
            if (counter >= splitspeech.length) {
                edittext_content.setSelection(0, 0);
                edittext_content.setPressed(true);
                scrollView.smoothScrollTo(0, 0);
            } else if (splitspeech[counter].equals(poi.getName())) {
                edittext_content.setSelection(0, 0);
                edittext_content.setPressed(true);
                scrollView.smoothScrollTo(0, 0);
            } else {
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
    }

    /**
     * sets the button visibility according to the status of the tts engine (paused,...)
     */
    private void setButtonStates(boolean play, boolean stop, boolean pause, boolean fwd, boolean back) {
        play_btn.setEnabled(play);
        stop_btn.setEnabled(stop);
        pause_btn.setEnabled(pause);
        fwd_btn.setEnabled(fwd);
        back_btn.setEnabled(back);
    }


    /**
     * introduces a short pause, fix for tts problem (sometimes after calling tts.stop() still another sentence got read)
     */
    protected void shortPause() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Log.e(TAG, e.toString());
        }
    }


    @Override
    public void onDestroyView() {
        if (tts != null) {
            playing = false;
            tts.stop();

            counter = 0;
            setButtonStates(true, false, false, false, false);
        }
        Fragment mapFragment = getFragmentManager().findFragmentById(R.id.map);

        if (mapFragment != null)
            getFragmentManager().beginTransaction().remove(mapFragment).commit();

        googleMap = null;

        super.onDestroyView();
    }


    public void setPoi(Poi poi) {
        this.poi = poi;
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapFrag.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        setupMap();
    }
}
