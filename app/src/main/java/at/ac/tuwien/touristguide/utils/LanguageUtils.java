package at.ac.tuwien.touristguide.utils;

import java.util.Locale;

public class LanguageUtils {

    public static String getLanguage() {
        if (Locale.getDefault().getLanguage().equals("de")) {
            return "de";
        }

        return "en";
    }

    public static Locale getLocale() {
        if (Locale.getDefault().getLanguage().equals("de")) {
            return Locale.GERMAN;
        } else {
            return Locale.US;
        }
    }

}
