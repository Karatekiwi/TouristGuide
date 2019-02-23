package at.ac.tuwien.touristguide.utils;

import java.util.Locale;

public class LanguageUtils {
    public static String getLanguage() {
        if (Locale.getDefault().getLanguage().equals("de")) {
            return "de";
        } else {
            return "en";
        }
    }
}
