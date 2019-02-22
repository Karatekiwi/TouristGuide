package at.ac.tuwien.touristguide;


import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


/**
 * @author Manu Weilharter
 * Requires an active internet connection
 */
public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        TextView textView = rootView.findViewById(R.id.about_text);
        String about = getAboutText();
        textView.setText(about);

        rootView.setBackgroundColor(Color.WHITE);

        return rootView;
    }

    private String getAboutText() {
        String version = getActivity().getString(R.string.af_version) + " " + BuildConfig.VERSION_NAME;
        String developer = getActivity().getString(R.string.af_developer);

        return version + "\n\n" + developer;
    }


}
