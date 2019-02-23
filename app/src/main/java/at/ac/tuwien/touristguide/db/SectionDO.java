package at.ac.tuwien.touristguide.db;

public class SectionDO {

    public static final String TABLE_NAME = "section";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
    public static final String SQL_DELETE = "DELETE FROM " + TABLE_NAME;

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_POI_ID = "pois_id";
    public static final String COLUMN_HEADER = "header";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_CATEGORY = "category";


    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_POI_ID + " INTEGER references pois(id), "
            + COLUMN_HEADER + " text, "
            + COLUMN_CONTENT + " text, "
            + COLUMN_CATEGORY + " text)";

    public static final String SQL_CREATE_INDEX = "CREATE INDEX IF NOT EXISTS pois_id on " + TABLE_NAME + "(pois_id)";

}
