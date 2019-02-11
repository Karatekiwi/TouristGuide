package at.ac.tuwien.touristguide.tests;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import com.robotium.solo.Solo;

import java.lang.reflect.Method;
import java.util.Locale;

import at.ac.tuwien.touristguide.MainActivity;


/**
 * @author Manu Weilharter
 */
public class TestGUI extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private boolean english;

    public TestGUI() {
        super(MainActivity.class);
    }

    protected void setUp() throws Exception {
        super.setUp();

        Intent i = new Intent();
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setActivityIntent(i);

        english = Locale.getDefault().getLanguage().equals("en");

        solo = new Solo(getInstrumentation(), getActivity());
    }

    protected void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }


    @SmallTest
    public void testUpdateShouldShowDialogIfNoInternetConnAvailable() {
        switchInternetState(false);

        solo.clickOnMenuItem("Update");
        solo.clickOnButton("Update");

        if (english)
            assertTrue(solo.searchText("No internet connection available!"));

        else
            assertTrue(solo.searchText("Keine Internetverbindung vorhanden!"));

        solo.goBack();
    }


    @SmallTest
    public void testUpdateShouldPerformUpdate() {
        switchInternetState(true);

        solo.clickOnMenuItem("Update");
        solo.clickOnButton("Update");

        if (english)
            assertTrue(solo.searchText("Are you sure you want to update the application?"));

        else
            assertTrue(solo.searchText("Sind Sie sicher, dass Sie die Anwendung aktualisieren moechten?"));

        solo.goBack();
    }


    protected void switchInternetState(boolean enabled) {

        WifiManager wifiman = (WifiManager) solo.getCurrentActivity().getSystemService(Context.WIFI_SERVICE);
        wifiman.setWifiEnabled(false);

        ConnectivityManager dataManager = (ConnectivityManager) solo.getCurrentActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        Method dataMtd = null;
        ;
        try {
            dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
            dataMtd.setAccessible(true);
            dataMtd.invoke(dataManager, enabled);
        } catch (Exception e) {
        }

    }


}
