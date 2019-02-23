package at.ac.tuwien.touristguide.tools;


import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.EngineInfo;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import java.util.Locale;

import at.ac.tuwien.touristguide.db.DatabaseHandler;


/**
 * @author Manu Weilharter
 * Background Service for the text-to-speech engine
 */
public class TTSHelper {

    private static TTSHelper instance;

    public TextToSpeech tts;
    private Context context;
    /**
     * initializes the text-to-speech listener
     */
    OnInitListener tts_listener = new OnInitListener() {

        @Override
        public void onInit(int status) {
            if (DatabaseHandler.getInstance(context).getUseOwnTTS() == 0) {
                for (EngineInfo ei : tts.getEngines()) {
                    if (ei.name.contains("google.android.tts")) {
                        tts.setEngineByPackageName(ei.name);
                    }
                }
            }

            if (status == TextToSpeech.SUCCESS) {
                int result;

                if (Locale.getDefault().getLanguage().equals("de")) {
                    result = tts.setLanguage(Locale.GERMAN);
                } else {
                    result = tts.setLanguage(Locale.US);
                }

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("", "This Language is not supported");
                }

            } else {
                Log.e("TTS", "Initilization Failed!");
            }

        }
    };
    private float speechRate = (float) 1.2;

    private TTSHelper(Context context) {
        this.context = context;
        initTTS();
    }

    public static TTSHelper getInstance(Context context) {
        if (TTSHelper.instance == null) {
            TTSHelper.instance = new TTSHelper(context);
        }

        return TTSHelper.instance;
    }

    public TextToSpeech getTTS() {
        return tts;
    }

    public void initTTS() {
        tts = new TextToSpeech(context, tts_listener);
        tts.setSpeechRate(speechRate);
    }
}
