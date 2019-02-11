package at.ac.tuwien.touristguide;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * @author Manu Weilharter
 * Requires an active internet connection
 */
public class AboutFragment extends Fragment {

    private Activity activity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        TextView textView = rootView.findViewById(R.id.about_text);
        String about = getAboutText();
        textView.setText(about);

        rootView.setBackgroundColor(Color.WHITE);

        return rootView;
    }

    private String getAboutText() {
        String version = activity.getString(R.string.af_version) + " " + BuildConfig.VERSION_NAME;
        String developer = activity.getString(R.string.af_developer);

        return version + "\n\n" + developer;
    }


}
