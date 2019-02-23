package at.ac.tuwien.touristguide;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import at.ac.tuwien.touristguide.tools.RouteHelper;
import at.ac.tuwien.touristguide.tools.UpdateOperation;


/**
 * @author Manu Weilharter
 * <p/>
 * Retrieves the current poi list from the Webservice
 * Requires an active internet connection
 */
public class UpdateFragment extends Fragment {

    private Activity activity;
    private RouteHelper routeHelper;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        this.routeHelper = new RouteHelper(null, activity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_udpate, container, false);

        TextView textView = rootView.findViewById(R.id.update_text);
        textView.setText(activity.getString(R.string.uf1));

        Button updateBtn = rootView.findViewById(R.id.update_button);
        updateBtn.setText("Update");

        updateBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (routeHelper.isOnline(activity)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity).setCancelable(false);
                    alertDialog.setMessage(activity.getString(R.string.uf2));
                    alertDialog.setNegativeButton(activity.getString(R.string.gf3), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.setPositiveButton(activity.getString(R.string.gf2), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            new UpdateOperation(activity).execute();
                        }
                    });

                    alertDialog.show();
                } else {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                    alertDialog.setTitle(activity.getString(R.string.uf3));
                    alertDialog.setMessage(activity.getString(R.string.uf4));
                    alertDialog.setNegativeButton(activity.getString(R.string.ma4), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialog.setPositiveButton(activity.getString(R.string.ma5), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                        }
                    });

                    alertDialog.show();
                }
            }
        });

        rootView.setBackgroundColor(Color.WHITE);

        return rootView;
    }

}
