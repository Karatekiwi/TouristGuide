package at.ac.tuwien.touristguide.tools;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import at.ac.tuwien.touristguide.MainActivity;
import at.ac.tuwien.touristguide.R;
import at.ac.tuwien.touristguide.service.TTSHelper;


/**
 * @author Manu Weilharter
 * display a short welcome screen
 */
public class SplashActivity extends Activity {
    private static int SPLASH_TIME_OUT = 60000;
    private Handler mHandler;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);

            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler();

        setContentView(R.layout.activity_splash);

        new PoiHolder(this).startUp(getApplicationContext());

        startService(new Intent(this, TTSHelper.class));

        mHandler.postDelayed(mRunnable, SPLASH_TIME_OUT);
    }

    public void onDone() {
        mHandler.removeCallbacks(mRunnable);
        Intent i = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(i);

        finish();
    }
}
