package at.ac.tuwien.touristguide;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import at.ac.tuwien.touristguide.tools.DatabaseHandler;
import at.ac.tuwien.touristguide.tools.MultiSelectionSpinner;
import at.ac.tuwien.touristguide.tools.PoiHolder;


/**
 * @author Manu Weilharter
 * Handles the user settings
 */
public class SettingsFragment extends Fragment {

    private List<String> categories = new ArrayList<>();
    private MultiSelectionSpinner spinner_categories;
    private Activity activity;
    private Toast toast;

    private boolean initDone;


    private String[] items;

    @SuppressLint("ShowToast")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = getActivity();
        items = getResources().getStringArray(R.array.sf21);

        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);

        TextView tvCategories = (TextView) rootView.findViewById(R.id.tv_settingsCategories);
        tvCategories.setText(activity.getString(R.string.sf1));

        spinner_categories = (MultiSelectionSpinner) rootView.findViewById(R.id.spinner_categories);
        spinner_categories.setContext(activity);
        initializeCategories();

        spinner_categories.setItems(categories);

        spinner_categories.setSelection(DatabaseHandler.getInstance(activity).getCategories());

        TextView tvInfoLevel = (TextView) rootView.findViewById(R.id.tv_infoLevel);
        tvInfoLevel.setText(activity.getString(R.string.sf2));

        SeekBar sbInfoLevel = (SeekBar) rootView.findViewById(R.id.seekBar1);
        sbInfoLevel.setOnSeekBarChangeListener(sbcl);
        sbInfoLevel.setProgress(DatabaseHandler.getInstance(activity).getLevel());

        TextView sbMin = (TextView) rootView.findViewById(R.id.tv_sbMin);
        sbMin.setText(activity.getString(R.string.sf3));

        TextView sbMedium = (TextView) rootView.findViewById(R.id.tv_sbMedium);
        sbMedium.setText(activity.getString(R.string.sf4));

        TextView sbMax = (TextView) rootView.findViewById(R.id.tv_sbMax);
        sbMax.setText(activity.getString(R.string.sf5));

        initSwitches(rootView);

        TextView tvReset = (TextView) rootView.findViewById(R.id.tv_settingsReset);
        tvReset.setText(activity.getString(R.string.sf10));

        Button resetBtn = (Button) rootView.findViewById(R.id.reset_btn);
        resetBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity).setCancelable(false);
                alertDialog.setMessage(getString(R.string.sf11));
                alertDialog.setNegativeButton(getString(R.string.ma4), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.setPositiveButton(getString(R.string.gf2), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        DatabaseHandler.getInstance(activity).resetVisited(0);
                        PoiHolder.resetVisited();
                        Toast.makeText(activity, activity.getString(R.string.sf12), Toast.LENGTH_SHORT).show();
                    }
                });

                alertDialog.show();

            }
        });

        toast = Toast.makeText(activity, "", Toast.LENGTH_SHORT);

        TextView tvDistance = (TextView) rootView.findViewById(R.id.tv_settingsDistance);
        tvDistance.setText(activity.getString(R.string.sf14));

        Spinner spinner_distance = (Spinner) rootView.findViewById(R.id.spinner_settingsDistance);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity, R.array.sf13, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_distance.setAdapter(adapter);
        spinner_distance.setSelection(getDistanceIndex(DatabaseHandler.getInstance(activity).getDistance()));
        spinner_distance.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                DatabaseHandler.getInstance(activity).setDistance(Integer.parseInt(parent.getItemAtPosition(position).toString()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        initDone = true;
        rootView.setBackgroundColor(Color.WHITE);
        return rootView;
    }

    private void initSwitches(View rootView) {
        TextView tvHighlight = (TextView) rootView.findViewById(R.id.tv_highlight);
        tvHighlight.setText(activity.getString(R.string.sf17));

        Switch switchBtn = (Switch) rootView.findViewById(R.id.switchBtn);
        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DatabaseHandler.getInstance(activity).setHighlight(1);
                } else {
                    DatabaseHandler.getInstance(activity).setHighlight(0);
                }
            }
        });

        TextView tvNoti = (TextView) rootView.findViewById(R.id.tv_noti);
        tvNoti.setText(activity.getString(R.string.sf18));

        Spinner spinner_noti = (Spinner) rootView.findViewById(R.id.spinner_noti);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item, items);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_noti.setAdapter(adapter);
        spinner_noti.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                DatabaseHandler.getInstance(activity).setNotify(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        TextView tvHide = (TextView) rootView.findViewById(R.id.tv_hide);
        tvHide.setText(activity.getString(R.string.sf19));

        Switch switchBtn3 = (Switch) rootView.findViewById(R.id.switchBtn3);
        switchBtn3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DatabaseHandler.getInstance(activity).setHide(1);
                } else {
                    DatabaseHandler.getInstance(activity).setHide(0);
                }
            }
        });

        TextView tvTTS = (TextView) rootView.findViewById(R.id.tv_tts);
        tvTTS.setText(activity.getString(R.string.sf20));

        Switch switchBtn4 = (Switch) rootView.findViewById(R.id.switchBtn4);
        switchBtn4.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    DatabaseHandler.getInstance(activity).setUseOwnTTS(1);
                } else {
                    DatabaseHandler.getInstance(activity).setUseOwnTTS(0);
                }
            }
        });

        if (DatabaseHandler.getInstance(activity).getHighlight() == 1) {
            switchBtn.setChecked(true);
        }
        if (DatabaseHandler.getInstance(activity).getHide() == 1) {
            switchBtn3.setChecked(true);
        }
        if (DatabaseHandler.getInstance(activity).getUseOwnTTS() == 1) {
            switchBtn4.setChecked(true);
        }
        spinner_noti.setSelection(DatabaseHandler.getInstance(activity).getNotify());
    }

    private int getDistanceIndex(int distance) {
        String[] distances = getResources().getStringArray(R.array.sf13);

        for (int i = 0; i < distances.length; i++) {
            if (Integer.parseInt(distances[i]) == distance)
                return i;
        }

        return -1;
    }

    private void initializeCategories() {
        Resources res = getResources();
        for (String category : res.getStringArray(R.array.sf6)) {
            categories.add(category);
        }
    }

    OnSeekBarChangeListener sbcl = new OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (initDone) {
                DatabaseHandler.getInstance(activity).saveLevel(progress);

                // workaround - so that when sliding from left to right on the
                // seekbar or vice versa, the toasts don't get showed in a queue
                switch (progress) {
                    case 0:
                        toast.setText(activity.getString(R.string.sf6));
                        break;
                    case 1:
                        toast.setText(activity.getString(R.string.sf7));
                        break;
                    case 2:
                        toast.setText(activity.getString(R.string.sf8));
                        break;
                    default:
                        break;
                }

                toast.show();
            }
        }
    };

}

