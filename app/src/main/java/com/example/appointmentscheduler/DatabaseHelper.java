package com.example.appointmentscheduler;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "appointments.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USERS = "Users";
    public static final String TABLE_APPOINTMENTS = "Appointments";
    public static final String TABLE_APPOINTMENT_STATUS = "AppointmentStatus";

    // Columns for Users table
    public static final String COLUMN_USERNAME = "Username";

    // Columns for Appointments table
    public static final String COLUMN_SCHEDID = "SchedID";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_DATE = "Date";
    public static final String COLUMN_DESCRIPTION = "Description";
    public static final String COLUMN_TIME = "Time";
    public static final String COLUMN_LINK = "Link";
    public static final String COLUMN_USER_FK = "Username";

    // Columns for AppointmentStatus table
    public static final String COLUMN_IS_FINISHED = "isFinished";

    // Create table statements
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "(" +
            COLUMN_USERNAME + " TEXT PRIMARY KEY);";

    private static final String CREATE_TABLE_APPOINTMENTS = "CREATE TABLE " + TABLE_APPOINTMENTS + "(" +
            COLUMN_SCHEDID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_DATE + " TEXT, " +
            COLUMN_DESCRIPTION + " TEXT, " +
            COLUMN_TIME + " TEXT, " +
            COLUMN_LINK + " TEXT, " +
            COLUMN_USER_FK + " TEXT, " +
            "FOREIGN KEY(" + COLUMN_USER_FK + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USERNAME + "));";

    private static final String CREATE_TABLE_APPOINTMENT_STATUS = "CREATE TABLE " + TABLE_APPOINTMENT_STATUS + "(" +
            COLUMN_USERNAME + " TEXT, " +
            COLUMN_SCHEDID + " INTEGER, " +
            COLUMN_IS_FINISHED + " INTEGER, " +
            "FOREIGN KEY(" + COLUMN_USERNAME + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USERNAME + "), " +
            "FOREIGN KEY(" + COLUMN_SCHEDID + ") REFERENCES " + TABLE_APPOINTMENTS + "(" + COLUMN_SCHEDID + "), " +
            "PRIMARY KEY(" + COLUMN_USERNAME + ", " + COLUMN_SCHEDID + "));";
    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_APPOINTMENTS);
        db.execSQL(CREATE_TABLE_APPOINTMENT_STATUS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENT_STATUS);
        onCreate(db);
    }

//    public Cursor getLatestSchedule() {
//        SQLiteDatabase db = getReadableDatabase();
//        Cursor cursor = null;
//        try {
//            // Get the current date
//            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//            String currentDate = dateFormat.format(new Date());
//
//            // Query to get the latest schedule
//            String query = "SELECT * FROM " + TABLE_APPOINTMENTS +
//                    " WHERE " + COLUMN_DATE + " >= ?" +
//                    " ORDER BY " + COLUMN_DATE + " ASC, " + COLUMN_TIME + " ASC LIMIT 1";
//
//            cursor = db.rawQuery(query, new String[]{currentDate});
//
//            // Move the cursor to the first row
//            if (cursor != null) {
//                cursor.moveToFirst();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return cursor;
//    }


    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    @SuppressLint("Range")
    public String getCurrentUsername() {
        String username = null;
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null) {
            Cursor cursor = db.rawQuery("SELECT " + COLUMN_USERNAME + " FROM " + TABLE_USERS, null);
            if (cursor.moveToFirst()) {
                username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
            }
            cursor.close();
        }
        Log.d("DatabaseHelper", "Current username: " + username);
        return username;
    }

    public boolean updateUserName(String newUsername, String oldUsername) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Update the username in the Users table
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_USERNAME, newUsername);
            int rowsAffected = db.update(DatabaseHelper.TABLE_USERS, values, DatabaseHelper.COLUMN_USERNAME + " = ?", new String[]{oldUsername});

            // Update the username in the Appointments table
            values.clear();
            values.put(DatabaseHelper.COLUMN_USER_FK, newUsername);
            rowsAffected += db.update(DatabaseHelper.TABLE_APPOINTMENTS, values, DatabaseHelper.COLUMN_USER_FK + " = ?", new String[]{oldUsername});

            // Update the username in the AppointmentStatus table
            values.clear();
            values.put(DatabaseHelper.COLUMN_USERNAME, newUsername);
            rowsAffected += db.update(DatabaseHelper.TABLE_APPOINTMENT_STATUS, values, DatabaseHelper.COLUMN_USERNAME + " = ?", new String[]{oldUsername});

            db.setTransactionSuccessful();
            return rowsAffected > 0;
        } finally {
            db.endTransaction();
        }
    }


    public String getCurrentSchedCount(String username) {
        int schedCounts = 0;

        SQLiteDatabase db = getReadableDatabase();

        if (db != null) {
            Cursor cursor = db.rawQuery("SELECT COUNT(" + COLUMN_SCHEDID + ") FROM " + TABLE_APPOINTMENTS +
                    " WHERE " + COLUMN_USER_FK + "=?", new String[]{username});
            if (cursor.moveToFirst()) {
                schedCounts = cursor.getInt(0);
            }
            cursor.close();
        }
        Log.d("DatabaseHelper", "Schedule count for user " + username + ": " + schedCounts);
        return String.valueOf(schedCounts);
    }

    public int getFinishedScheduleCount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_APPOINTMENT_STATUS +
                        " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_IS_FINISHED + " = 1",
                new String[]{username});

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }

        return count;
    }

    Cursor readLatestSchedule() {
        String query = "SELECT a.* " +
                "FROM " + TABLE_APPOINTMENTS + " a " +
                "JOIN " + TABLE_APPOINTMENT_STATUS + " s " +
                "ON a." + COLUMN_SCHEDID + " = s." + COLUMN_SCHEDID + " " +
                "WHERE s." + COLUMN_IS_FINISHED + " = 0 " +
                "ORDER BY a." + COLUMN_DATE + " ASC, a." + COLUMN_TIME + " ASC " +
                "LIMIT 1";
//        String query = "SELECT * FROM " + TABLE_APPOINTMENTS + " ORDER BY " + COLUMN_DATE + " ASC, " + COLUMN_TIME + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    Cursor readFinishedSchedule(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT A.* FROM " + TABLE_APPOINTMENTS + " A " +
                "JOIN " + TABLE_APPOINTMENT_STATUS + " S " +
                "ON A." + COLUMN_SCHEDID + " = S." + COLUMN_SCHEDID + " " +
                "WHERE S." + COLUMN_USERNAME + " = ? AND S." + COLUMN_IS_FINISHED + " = 1 " +
                "ORDER BY A." + COLUMN_DATE + " DESC, A." + COLUMN_TIME + " DESC " +
                "LIMIT 3";

        Cursor cursor = null;
        if(db != null) {
            cursor = db.rawQuery(query, new String[]{username});
        }
        return cursor;
    }


    Cursor readAllSchedule() {
        String query = "SELECT A.*, S." + COLUMN_IS_FINISHED + " FROM " + TABLE_APPOINTMENTS + " A " +
                "JOIN " + TABLE_APPOINTMENT_STATUS + " S ON A." + COLUMN_SCHEDID + " = S." + COLUMN_SCHEDID +
                " ORDER BY A." + COLUMN_DATE + " ASC, A." + COLUMN_TIME + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(query, null);
    }

    Cursor readUpcomingScheduleByUser(String username) {
        String query = "SELECT A.* FROM " + TABLE_APPOINTMENTS + " A " +
                "JOIN " + TABLE_APPOINTMENT_STATUS + " S ON A." + COLUMN_SCHEDID + " = S." + COLUMN_SCHEDID + " " +
                "WHERE S." + COLUMN_USERNAME + " = ? AND S." + COLUMN_IS_FINISHED + " = 0 " +
                "ORDER BY A." + COLUMN_DATE + " ASC, A." + COLUMN_TIME + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(query, new String[]{username});
    }

    boolean addAppointment(String name, String date, String description, String time, String link, String username) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_DATE, date);
            values.put(COLUMN_DESCRIPTION, description);
            values.put(COLUMN_TIME, time);
            values.put(COLUMN_LINK, link);
            values.put(COLUMN_USER_FK, username);

            long newRowId = db.insert(TABLE_APPOINTMENTS, null, values);
            if (newRowId == -1) {
                return false;
            }

            ContentValues statusValues = new ContentValues();
            statusValues.put(COLUMN_USERNAME, username);
            statusValues.put(COLUMN_SCHEDID, newRowId);
            statusValues.put(COLUMN_IS_FINISHED, 0);

            long statusRowId = db.insert(TABLE_APPOINTMENT_STATUS, null, statusValues);
            if (statusRowId == -1) {
                return false;
            }

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }


    void updateSchedule(String row_id, String name, String date, String time, String desc, String link) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COLUMN_NAME, name);
        cv.put(COLUMN_DATE, date);
        cv.put(COLUMN_TIME, time);
        cv.put(COLUMN_DESCRIPTION, desc);
        cv.put(COLUMN_LINK, link);

        long result = db.update(TABLE_APPOINTMENTS, cv, "SchedID=? ", new String[]{row_id});

        if(result==-1) {

            Toast.makeText(context, "Error: Failed to save schedule.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Schedule updated successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateAppointmentStatus (String schedID, boolean isFinished) {
        SQLiteDatabase db = this.getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCHEDID, Long.parseLong(schedID));
        values.put(COLUMN_IS_FINISHED, isFinished ? 1 : 0);

        int rows = db.update(TABLE_APPOINTMENT_STATUS, values, COLUMN_SCHEDID + "=?", new String[]{schedID});

        if (rows == 0) {
            values.put(COLUMN_USERNAME, getCurrentUsername());
            db.insert(TABLE_APPOINTMENT_STATUS, null, values);
        }
    }

    @SuppressLint("Range")
    public boolean isAppointmentFinished(String schedID) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_APPOINTMENT_STATUS, new String[]{COLUMN_IS_FINISHED}, COLUMN_SCHEDID + "=?", new String[]{schedID}, null, null, null);
        boolean isFinished = false;
        if (cursor != null && cursor.moveToFirst()) {
            isFinished = cursor.getInt(cursor.getColumnIndex(COLUMN_IS_FINISHED)) == 1;
            cursor.close();
        }
        return isFinished;
    }

    public void deleteAppointmentStatus(String schedID) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_APPOINTMENT_STATUS, COLUMN_SCHEDID + "=?", new String[]{schedID});
    }

    void deleteRowSchedule(String row_id) {
        SQLiteDatabase db = getWritableDatabase();
        long result = db.delete(TABLE_APPOINTMENTS, "SchedID=? ", new String[]{row_id});

        if(result==-1) {
            Toast.makeText(context, "Error: Failed to delete schedule.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Schedule deleted successfully!", Toast.LENGTH_SHORT).show();
        }

    }
}
