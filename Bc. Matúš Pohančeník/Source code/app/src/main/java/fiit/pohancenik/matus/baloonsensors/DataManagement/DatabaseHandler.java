package fiit.pohancenik.matus.baloonsensors.DataManagement;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matus on 7. 4. 2016.
 * Database Handler
 * Database is used to store Session data
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "SessionsManager";
    // Sessions table name
    private static final String TABLE_SESSIONS = "sessions";

    // Sessions Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_FILEPATH = "filepath";
    private static final String KEY_NAME = "name";
    private static final String KEY_DATE = "date";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Table
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SESSIONS_TABLE = "CREATE TABLE " + TABLE_SESSIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_FILEPATH + " TEXT,"
                + KEY_NAME + " TEXT,"
                + KEY_DATE + " TEXT" + ")";
        db.execSQL(CREATE_SESSIONS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSIONS);

        // Create tables again
        onCreate(db);
    }


    // Adding new session
    public void addSession(SessionInfo sessionInfo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FILEPATH, sessionInfo.getFilePath());
        values.put(KEY_NAME, sessionInfo.getName());
        values.put(KEY_DATE, sessionInfo.getDate());

        // Inserting Row
        db.insert(TABLE_SESSIONS, null, values);
        db.close(); // Closing database connection
    }

    // Getting All Sessions
    public List<SessionInfo> getAllSessions() {
        List<SessionInfo> sessionsList = new ArrayList<SessionInfo>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SESSIONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                SessionInfo sessionInfo = new SessionInfo();
                sessionInfo.setID(Integer.parseInt(cursor.getString(0)));
                sessionInfo.setFilePath(cursor.getString(1));
                sessionInfo.setName(cursor.getString(2));
                sessionInfo.setDate(cursor.getString(3));
                // Adding session to list
                sessionsList.add(sessionInfo);
            } while (cursor.moveToNext());
        }

        // return session list
        return sessionsList;
    }

    // Deleting single session
    public void deleteSession(SessionInfo sessionInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SESSIONS, KEY_ID + " = ?",
                new String[] { String.valueOf(sessionInfo.getID()) });
        db.close();
    }


    // Getting All Sessions
    public boolean checkSessionName(String name) {
        List<SessionInfo> sessionsList = new ArrayList<SessionInfo>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_SESSIONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                if(cursor.getString(2).equals(name)){
                    return true;
                }


            } while (cursor.moveToNext());
        }

        // return session list
        return false;
    }

}