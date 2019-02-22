package at.ac.tuwien.touristguide.tools;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.TextView;
import at.ac.tuwien.touristguide.R;


/**
 * @author Manu Weilharter
 */
public class UpdateOperation extends AsyncTask<String, Void, Void> {

    private Activity activity;
    private ProgressDialog dialog;
    private TextView output;


    public UpdateOperation(Activity activity) {
        this.activity = activity;
        this.output = activity.findViewById(R.id.update_text2);

        dialog = new ProgressDialog(activity);
        dialog.setCancelable(false);
        dialog.setMessage(activity.getString(R.string.uo1));
        dialog.show();
    }

    @Override
    protected Void doInBackground(String... params) {
        activity.runOnUiThread(new Thread() {
            public void run() {
                timerDelayRemoveDialog(2000, dialog);
            }
        });

        return null;
    }

    public void timerDelayRemoveDialog(long time, final Dialog dialog) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                output.setText(activity.getString(R.string.uo7));
                dialog.cancel();
            }
        }, time);
    }

}


