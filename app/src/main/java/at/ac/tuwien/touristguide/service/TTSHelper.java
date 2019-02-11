package at.ac.tuwien.touristguide.service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.EngineInfo;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import java.util.Locale;
import at.ac.tuwien.touristguide.tools.DatabaseHandler;


/**
 * @author Manu Weilharter
 * Background Service for the text-to-speech engine
 */
@SuppressWarnings("deprecation")
public class TTSHelper extends Service {

    public static TextToSpeech tts;
    private float speechRate = (float) 1.2;

    public TTSHelper() {
        super();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initTTS();
        return Service.START_STICKY;
    }

    public void initTTS() {
        tts = new TextToSpeech(getApplicationContext(), tts_listener);
        tts.setSpeechRate(speechRate);
    }


    /**
     * initializes the text-to-speech listener
     */
    OnInitListener tts_listener = new OnInitListener() {

        @Override
        public void onInit(int status) {
            if (DatabaseHandler.getInstance(getApplicationContext()).getUseOwnTTS() == 0) {
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
