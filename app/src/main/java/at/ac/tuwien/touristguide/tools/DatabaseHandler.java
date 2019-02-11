package at.ac.tuwien.touristguide.tools;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;
import java.util.ArrayList;
import java.util.List;
import at.ac.tuwien.touristguide.R;
import at.ac.tuwien.touristguide.entities.Poi;
import at.ac.tuwien.touristguide.entities.Section;


/**
 * @author Manu Weilharter
 */
public class DatabaseHandler extends SQLiteAssetHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String TAG = DatabaseHandler.class.getName();
    private static final String DATABASE_NAME = "touristguide.db";
    private static final String TABLE_POIS = "poi";
    private static final String TABLE_SECTIONS = "section";
    private static final String TABLE_SETTINGS = "settings";
    private static DatabaseHandler instance;
    private Context context;


    private DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
        this.context = context;
    }

    public static DatabaseHandler getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHandler(context.getApplicationContext());
        }

        return instance;
    }

    /**
     * Creates all necessary databases
     * @param db the SQLiteDatabase
     */
    public void createTables(SQLiteDatabase db) {
        String create_poisTable = "CREATE TABLE IF NOT EXISTS " + TABLE_POIS + "("
                + "id integer PRIMARY KEY, "
                + "wiki_id TEXT, "
                + "name TEXT, "
                + "latitude DOUBLE, "
                + "longitude DOUBLE, "
                + "language TEXT, "
                + "visited INTEGER)";

        String create_sectionsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_SECTIONS + " ("
                + "id integer primary key, "
                + "pois_id INTEGER references pois(id), "
                + "header text, "
                + "content text, "
                + "category text)";

        String create_settingsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_SETTINGS + "("
                + "id INTEGER,"
                + "level INTEGER,"
                + "category TEXT,"
                + "init TEXT, "
                + "distance INTEGER, "
                + "update_id TEXT, "
                + "highlight INTEGER, "
                + "notify INTEGER, "
                + "hide INTEGER, "
                + "tts INTEGER)";

        String index = "CREATE INDEX IF NOT EXISTS lang on " + TABLE_POIS + "(language)";
        String index2 = "CREATE INDEX IF NOT EXISTS pois_id on " + TABLE_SECTIONS + "(pois_id)";

        try {
            db.execSQL(create_poisTable);
            db.execSQL(create_sectionsTable);
            db.execSQL(create_settingsTable);

            db.execSQL(index);
            db.execSQL(index2);

            /** Sets the default values for the settings table */
            db.execSQL("INSERT INTO " + TABLE_SETTINGS + " values(0, 0, '0,1,2,3', 'yes', 250, 'firstid', 1, 0, 0, 0)");

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    /**
     * Drops all databases
     */
    public void dropTables() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_POIS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SECTIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            db.close();
        }
    }

    /**
     * Returns a list of all points of interest
     * @param english true if all english pois should be returned, false if all german pois are requested
     * @return a list of all pois in the given language
     */
    public List<Poi> getAllPois(boolean english) {
        List<Poi> poiList = new ArrayList<>();
        String selectQuery;

        if (english) {
            selectQuery = "SELECT * FROM poi WHERE language LIKE 'en'";
        } else {
            selectQuery = "SELECT * FROM poi WHERE language LIKE 'de'";
        }

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        db.beginTransaction();

        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor.moveToFirst()) {
                do {
                    Poi poi = new Poi();
                    poi.setId(cursor.getInt(0));
                    poi.setWikiId(cursor.getString(1));
                    poi.setName(cursor.getString(2));
                    poi.setLatitude(cursor.getDouble(3));
                    poi.setLongitude(cursor.getDouble(4));
                    poi.setLanguage(cursor.getString(5));
                    poi.setVisited(cursor.getInt(6));

                    poiList.add(poi);

                } while (cursor.moveToNext());
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            db.endTransaction();

            if (db.isOpen()) {
                db.close();
            }
        }

        return poiList;
    }


    public List<Section> getSectionsForPoi(Poi poi) {
        String selectQuery = "SELECT  * FROM " + TABLE_SECTIONS + " WHERE pois_id=" + poi.getId();
        List<Section> sections = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    Section sec = new Section(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));

                    if (isCategoryInSelectedCategories(cursor.getString(4))) {
                        sections.add(sec);
                    }

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        return sections;
    }

    /**
     * Saves the categories chosen from the user to the database
     * @param categories the chosen categories
     */
    public void saveCategories(String categories) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("category", categories);

            db.update(TABLE_SETTINGS, values, "id=0", null);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            db.close();
        }
    }

    /**
     * Returns the categories chosen from the user
     * @return an int[] representation of the category indices
     */
    public int[] getCategories() {
        String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String categoryString = "";

        try {
            cursor = db.rawQuery(selectQuery, null);

            while (cursor.moveToNext()) {
                categoryString = cursor.getString(2);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        categoryString = categoryString.replace(" ", "");

        String[] numberStrs = categoryString.split(",");

        int[] numbers = new int[numberStrs.length];

        for (int i = 0; i < numberStrs.length; i++) {
            numbers[i] = Integer.parseInt(numberStrs[i]);
        }

        return numbers;
    }

    /**
     * Saves the information level chosen from the user to the database
     * 0 - simple, 1 - detailed, 2 - fun
     * @param level: the chosen information level
     */
    public void saveLevel(int level) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("level", level);

            db.update(TABLE_SETTINGS, values, "id=0", null);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            db.close();
        }
    }

    /**
     * Returns the information level chosen from the user
     * @return the information level
     */
    public int getLevel() {
        String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int level = -1;

        try {
            cursor = db.rawQuery(selectQuery, null);

            while (cursor.moveToNext()) {
                level = cursor.getInt(1);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        return level;
    }

    /**
     * sets the init value to no, which means that the app has been used before
     */
    public void setInitFalse() {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("init", "no");

            db.update(TABLE_SETTINGS, values, "id=0", null);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            db.close();
        }
    }

    /**
     * Retrieves the information if the app has been used before
     * @return true if the app has been used before, false otherwise
     */
    public boolean getInit() {
        String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String init = "";

        try {
            cursor = db.rawQuery(selectQuery, null);

            while (cursor.moveToNext()) {
                init = cursor.getString(3);
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        return init.equals("yes");
    }

    /**
     * Saves the visited value for a poi to the database
     * @param wiki_id the poi's Wikipedia id
     * @param visited 0 for unvisited, 1 for visited
     */
    public void setVisited(String wiki_id, int visited) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("visited", visited);
            db.update(TABLE_POIS, values, "wiki_id=" + wiki_id, null);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            db.close();
        }
    }

    /**
     * Resets the visited values in the database
     * @param visited 0 for unvisited, 1 for visited
     */
    public void resetVisited(int visited) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("visited", visited);
            db.update(TABLE_POIS, values, null, null);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            db.close();
        }
    }

    /**
     * Returns the user-chosen distance value within which the pois should be presented (standard: 250m)
     * @return the distance
     */
    public int getDistance() {
        String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int distance = -1;

        try {
            cursor = db.rawQuery(selectQuery, null);

            while (cursor.moveToNext()) {
                distance = cursor.getInt(4);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        return distance;
    }

    /**
     * Saves the user-chosen distance value to the database
     * @param distance the maximum distance for which pois will be read to the user
     */
    public void setDistance(int distance) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("distance", distance);

            db.update(TABLE_SETTINGS, values, "id=0", null);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            db.close();
        }
    }

    /**
     * Returns the setting for the highlight option
     * @return 1 for enabled, 0 for disabled
     */
    public int getHighlight() {
        String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int highlight = -1;

        try {
            cursor = db.rawQuery(selectQuery, null);

            while (cursor.moveToNext()) {
                highlight = cursor.getInt(6);
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        return highlight;
    }

    /**
     * Sets the settings for the highlight option
     * @param status 1 for enabled, 0 for disabled
     */
    public void setHighlight(int status) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("highlight", status);

            db.update(TABLE_SETTINGS, values, "id=0", null);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            db.close();
        }
    }

    /**
     * Returns the setting for the notify option
     * @return 1 for enabled, 0 for disabled
     */
    public int getNotify() {
        String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int notify = -1;

        try {
            cursor = db.rawQuery(selectQuery, null);

            while (cursor.moveToNext()) {
                notify = cursor.getInt(7);
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        return notify;
    }

    /**
     * Sets the settings for the notify option
     * @param status 1 for enabled, 0 for disabled
     */
    public void setNotify(int status) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("notify", status);

            db.update(TABLE_SETTINGS, values, "id=0", null);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            db.close();
        }
    }

    /**
     * Returns the setting for the hide option
     * @return 1 for enabled, 0 for disabled
     */
    public int getHide() {
        String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int hide = -1;

        try {
            cursor = db.rawQuery(selectQuery, null);

            while (cursor.moveToNext()) {
                hide = cursor.getInt(8);
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        return hide;
    }

    /**
     * Sets the settings for the hide option
     * @param status 1 for enabled, 0 for disabled
     */
    public void setHide(int status) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("hide", status);

            db.update(TABLE_SETTINGS, values, "id=0", null);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            db.close();
        }
    }

    /**
     * Returns the setting for the tts usage option
     * @return 1 for enabled, 0 for disabled
     */
    public int getUseOwnTTS() {
        String selectQuery = "SELECT  * FROM " + TABLE_SETTINGS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int tts = -1;

        try {
            cursor = db.rawQuery(selectQuery, null);

            while (cursor.moveToNext()) {
                tts = cursor.getInt(9);
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        return tts;
    }

    /**
     * Sets the settings for the tts usage option
     * @param status 1 for enabled, 0 for disabled
     */
    public void setUseOwnTTS(int status) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put("tts", status);

            db.update(TABLE_SETTINGS, values, "id=0", null);

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            db.close();
        }
    }

    /**
     * Returns the number of POIs in the database
     *
     * @return numPois
     */
    public int getPoiCount(String language) {
        String selectQuery = "SELECT  count(id) FROM " + TABLE_POIS + " where language LIKE '" + language + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = -1;

        try {
            cursor = db.rawQuery(selectQuery, null);

            while (cursor.moveToNext()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        return count;
    }

    /**
     * adds pois to the database
     * @param pois the pois to add
     */
    public void addPois(List<Poi> pois) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();

        try {
            for (Poi poi : pois) {
                ContentValues values = new ContentValues();
                values.put("id", poi.getId());
                values.put("wiki_id", poi.getWikiId());
                values.put("name", poi.getName());
                values.put("latitude", poi.getLatitude());
                values.put("longitude", poi.getLongitude());
                values.put("language", poi.getLanguage());
                values.put("visited", 0);

                db.insert(TABLE_POIS, null, values);

                if (poi.getSections() != null) {
                    for (Section sec : poi.getSections()) {
                        ContentValues values2 = new ContentValues();
                        values2.put("id", sec.getId());
                        values2.put("pois_id", sec.getPois_id());
                        values2.put("header", sec.getHeader());
                        values2.put("content", sec.getContent());
                        values2.put("category", sec.getCategory());

                        db.insert(TABLE_SECTIONS, null, values2);
                    }
                }
            }

            db.setTransactionSuccessful();

            Log.i(TAG, "Added " + pois.size() + " pois to the database.");

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            db.endTransaction();
            db.close();
        }
    }


    private boolean isCategoryInSelectedCategories(String category) {
        if (category.equals(""))
            return true;

        String[] availableCategories = context.getResources().getStringArray(R.array.sf6);
        int[] categoryIndices = getCategories();

        // user has chosen all categories
        if (categoryIndices.length == availableCategories.length) {
            return true;
        }

        List<String> availableCategoriesAL = new ArrayList<>();

        for (String cat : availableCategories) {
            availableCategoriesAL.add(cat);
        }

        List<String> selectedCategories = new ArrayList<>();

        for (int cat : categoryIndices) {
            selectedCategories.add(availableCategoriesAL.get(cat));
        }

        return selectedCategories.contains(category);
    }


    /**
     * returns the POI for the given Wikipedia id
     * @param wikipediaId the id of the Wikipedia page
     * @return the corresponding POI
     */
    public Poi getPoiByWikiId(String wikipediaId) {
        String selectQuery = "SELECT * FROM poi WHERE wiki_id LIKE '" + wikipediaId + "'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Cursor cursor2;

        Poi poi = new Poi();

        try {
            cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    poi.setId(cursor.getInt(0));
                    poi.setWikiId(cursor.getString(1));
                    poi.setName(cursor.getString(2));
                    poi.setLatitude(cursor.getDouble(3));
                    poi.setLongitude(cursor.getDouble(4));
                    poi.setLanguage(cursor.getString(5));
                    poi.setVisited(cursor.getInt(6));

                    cursor2 = db.rawQuery("SELECT * FROM sections WHERE pois_id = " + poi.getId(), null);

                    List<Section> sections = new ArrayList<>();

                    if (cursor2.moveToFirst()) {
                        do {
                            Section sec = new Section(cursor.getInt(0), cursor.getInt(1), cursor2.getString(2), cursor2.getString(3), cursor2.getString(4));
                            sections.add(sec);
                        } while (cursor2.moveToNext());
                    }

                    poi.setSections(sections);
                    cursor2.close();

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (cursor != null) {
                cursor.close();
            }

            db.close();
        }

        return poi;
    }

}
