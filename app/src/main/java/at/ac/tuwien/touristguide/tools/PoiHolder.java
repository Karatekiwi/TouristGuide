package at.ac.tuwien.touristguide.tools;

import android.content.Context;
import android.os.AsyncTask;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import at.ac.tuwien.touristguide.db.DatabaseHandler;
import at.ac.tuwien.touristguide.entities.Poi;
import at.ac.tuwien.touristguide.utils.LanguageUtils;


/**
 * @author Manu Weilharter
 */
public class PoiHolder {

    public static List<Poi> allPois_de;
    public static List<Poi> allPois_en;
    private static Context context;

    private SplashActivity splashActivity;

    public PoiHolder(SplashActivity sa) {
        this.splashActivity = sa;
    }

    public static List<Poi> retrieveNearPois(double latitude, double longitude, int numPois, boolean english) {
        int hide = DatabaseHandler.getInstance(context).getHide();
        List<Poi> nearPois;

        if (english) {
            nearPois = allPois_en;
        } else {
            nearPois = allPois_de;
        }

        List<Poi> result = new ArrayList<>();

        for (Poi poi : nearPois) {

            Poi newPoi = new Poi(poi.getId(), poi.getWikiId(), poi.getName(), poi.getLatitude(), poi.getLongitude(), poi.getLanguage(),
                    poi.getVisited(), distFrom(latitude, longitude, poi.getLatitude(), poi.getLongitude()));

            newPoi.setSections(poi.getSections());

            if (hide == 0) {
                result.add(newPoi);
            }

            if ((hide == 1) && (poi.getVisited() == 0)) {
                result.add(newPoi);
            }
        }

        Collections.sort(result, new Comparator<Poi>() {
            public int compare(Poi p1, Poi p2) {
                return Double.compare(p1.getDistance(), p2.getDistance());
            }
        });

        if (numPois == -1) {
            return result;
        } else if (result.size() >= numPois) {
            return result.subList(0, numPois);
        } else {
            return result;
        }
    }

    /**
     * Calculates the distance between two POIs (Vincenty approximation)
     * Got it from http://stackoverflow.com/questions/120283/how-can-i-measure-distance-and-create-a-bounding-box-based-on-two-latitudelongi
     *
     * @param lat1 latitude point 1
     * @param lng1 longitude point 1
     * @param lat2 latitude point 2
     * @param lng2 longitude point 2
     * @return distance
     */
    private static double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double a = 6378137, b = 6356752.314245, f = 1 / 298.257223563; // WGS-84 ellipsoid params
        double L = Math.toRadians(lng2 - lng1);
        double U1 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat1)));
        double U2 = Math.atan((1 - f) * Math.tan(Math.toRadians(lat2)));
        double sinU1 = Math.sin(U1), cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2), cosU2 = Math.cos(U2);

        double sinLambda, cosLambda, sinSigma, cosSigma, sigma, sinAlpha, cosSqAlpha, cos2SigmaM;
        double lambda = L, lambdaP, iterLimit = 100;

        do {
            sinLambda = Math.sin(lambda);
            cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2 * sinLambda) * (cosU2 * sinLambda)
                    + (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda) * (cosU1 * sinU2 - sinU1 * cosU2 * cosLambda));

            if (sinSigma == 0) {
                return 0; // co-incident points
            }

            cosSigma = sinU1 * sinU2 + cosU1 * cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha * sinAlpha;
            cos2SigmaM = cosSigma - 2 * sinU1 * sinU2 / cosSqAlpha;
            if (Double.isNaN(cos2SigmaM)) {
                cos2SigmaM = 0; // equatorial line: cosSqAlpha=0 (6)
            }

            double C = f / 16 * cosSqAlpha * (4 + f * (4 - 3 * cosSqAlpha));
            lambdaP = lambda;
            lambda = L + (1 - C) * f * sinAlpha
                    * (sigma + C * sinSigma * (cos2SigmaM + C * cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM)));

         } while (Math.abs(lambda - lambdaP) > 1e-12 && --iterLimit > 0);

        if (iterLimit == 0) {
            return Double.NaN; // formula failed to converge
        }

        double uSq = cosSqAlpha * (a * a - b * b) / (b * b);
        double A = 1 + uSq / 16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq / 1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double deltaSigma = B
                * sinSigma
                * (cos2SigmaM + B
                / 4
                * (cosSigma * (-1 + 2 * cos2SigmaM * cos2SigmaM) - B / 6 * cos2SigmaM
                * (-3 + 4 * sinSigma * sinSigma) * (-3 + 4 * cos2SigmaM * cos2SigmaM)));

        return b * A * (sigma - deltaSigma);
    }

    public static void setVisited(String wikiId, int status) {
        if (allPois_de != null) {
            for (Poi poi : allPois_de) {
                if (poi.getWikiId().equals(wikiId)) {
                    poi.setVisited(status);
                    return;
                }
            }
        }

        if (allPois_en != null) {
            for (Poi poi : allPois_en) {
                if (poi.getWikiId().equals(wikiId)) {
                    poi.setVisited(status);
                    return;
                }
            }
        }
    }

    public static List<Poi> getPois(Locale locale) {
        int hide = DatabaseHandler.getInstance(context).getHide();

        List<Poi> result = new ArrayList<>();
        List<Poi> allPois;
        if (locale == Locale.GERMAN) {
            allPois = allPois_de;
        } else {
            allPois = allPois_en;
        }

        for (Poi poi : allPois) {
            if (hide == 0) {
                result.add(poi);
            }

            if ((hide == 1) && (poi.getVisited() == 0)) {
                result.add(poi);
            }
        }

        return result;
    }

    public static void resetVisited() {
        if (allPois_en != null) {
            for (Poi poi : allPois_en) {
                poi.setVisited(0);
            }
        }

        if (allPois_de != null) {
            for (Poi poi : allPois_de) {
                poi.setVisited(0);
            }
        }

    }

    public void startUp(Context context) {
        PoiHolder.context = context;

        new CalculatePois().execute();
    }

    private class CalculatePois extends AsyncTask<URL, Integer, Void> {

        @Override
        protected Void doInBackground(URL... params) {
            if (LanguageUtils.getLanguage().equals("de")) {
                allPois_de = DatabaseHandler.getInstance(context).getAllPois(false);
            } else {
                allPois_en = DatabaseHandler.getInstance(context).getAllPois(true);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            splashActivity.onDone();
        }
    }

}
