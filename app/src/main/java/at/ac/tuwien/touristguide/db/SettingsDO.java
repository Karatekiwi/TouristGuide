package at.ac.tuwien.touristguide.db;

public class SettingsDO {

    public static final String TABLE_NAME = "settings";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME;

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LEVEL = "level";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_INIT = "init";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_UPDATE_ID = "update_id";
    public static final String COLUMN_HIGHLIGHT = "highlight";
    public static final String COLUMN_NOTIFY = "notify";
    public static final String COLUMN_HIDE = "hide";
    public static final String COLUMN_TTS = "tts";

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_LEVEL + " INTEGER, "
            + COLUMN_CATEGORY + " text, "
            + COLUMN_INIT + " text, "
            + COLUMN_DISTANCE + " INTEGER,"
            + COLUMN_UPDATE_ID + " TEXT, "
            + COLUMN_HIGHLIGHT + " INTEGER, "
            + COLUMN_NOTIFY + " INTEGER, "
            + COLUMN_HIDE + " INTEGER, "
            + COLUMN_TTS + " INTEGER)";

    public static final String SQL_DEFAULT_SETTINGS = "INSERT INTO " + SettingsDO.TABLE_NAME +
            " values(0, 0, '0,1,2,3', 'yes', 250, 'firstid', 1, 0, 0, 0)";

}
