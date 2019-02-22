package at.ac.tuwien.touristguide.db;

public class PoiDO {

    public static final String TABLE_NAME = "poi";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME;

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_WIKI_ID = "wiki_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LANGUAGE = "language";
    public static final String COLUMN_VISITED = "visited";

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
            + COLUMN_ID + " integer PRIMARY KEY, "
            + COLUMN_WIKI_ID + " TEXT, "
            + COLUMN_NAME + " TEXT, "
            + COLUMN_LATITUDE + " DOUBLE, "
            + COLUMN_LONGITUDE + " DOUBLE, "
            + COLUMN_LANGUAGE + " TEXT, "
            + COLUMN_VISITED + " INTEGER)";

    public static final String SQL_CREATE_INDEX = "CREATE INDEX IF NOT EXISTS lang on " + TABLE_NAME + "(language)";
}
