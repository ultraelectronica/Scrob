package com.example.appointmentscheduler;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "appointments.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_APPOINTMENTS = "Appointments";
    public static final String TABLE_APPOINTMENT_STATUS = "AppointmentStatus";

    public static final String COLUMN_SCHEDID = "SchedID";
    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_DATE = "Date";
    public static final String COLUMN_DESCRIPTION = "Description";
    public static final String COLUMN_TIME = "Time";
    public static final String COLUMN_LINK = "Link";
    public static final String COLUMN_IS_FINISHED = "isFinished";

    private static final String CREATE_TABLE_APPOINTMENTS = "CREATE TABLE " + TABLE_APPOINTMENTS + "(" +
            COLUMN_SCHEDID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_DATE + " TEXT, " +
            COLUMN_DESCRIPTION + " TEXT, " +
            COLUMN_TIME + " TEXT, " +
            COLUMN_LINK + " TEXT);";

    private static final String CREATE_TABLE_APPOINTMENT_STATUS = "CREATE TABLE " + TABLE_APPOINTMENT_STATUS + "(" +
            COLUMN_SCHEDID + " INTEGER PRIMARY KEY, " +
            COLUMN_IS_FINISHED + " INTEGER, " +
            "FOREIGN KEY(" + COLUMN_SCHEDID + ") REFERENCES " + TABLE_APPOINTMENTS + "(" + COLUMN_SCHEDID + "));";

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_APPOINTMENTS);
        db.execSQL(CREATE_TABLE_APPOINTMENT_STATUS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            migrateV1ToV2(db);
        }
    }

    /**
     * Migrates from v1 (Users + Username columns) to v2 (local-only appointments).
     */
    private void migrateV1ToV2(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Appointments_mig (" +
                COLUMN_SCHEDID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_DESCRIPTION + " TEXT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_LINK + " TEXT)");
        db.execSQL("INSERT INTO Appointments_mig (" + COLUMN_SCHEDID + ", " + COLUMN_NAME + ", " + COLUMN_DATE + ", "
                + COLUMN_DESCRIPTION + ", " + COLUMN_TIME + ", " + COLUMN_LINK + ") " +
                "SELECT " + COLUMN_SCHEDID + ", " + COLUMN_NAME + ", " + COLUMN_DATE + ", "
                + COLUMN_DESCRIPTION + ", " + COLUMN_TIME + ", " + COLUMN_LINK + " FROM " + TABLE_APPOINTMENTS);

        db.execSQL("CREATE TABLE IF NOT EXISTS AppointmentStatus_mig (" +
                COLUMN_SCHEDID + " INTEGER PRIMARY KEY, " +
                COLUMN_IS_FINISHED + " INTEGER)");
        db.execSQL("INSERT INTO AppointmentStatus_mig (" + COLUMN_SCHEDID + ", " + COLUMN_IS_FINISHED + ") " +
                "SELECT " + COLUMN_SCHEDID + ", MAX(" + COLUMN_IS_FINISHED + ") FROM " + TABLE_APPOINTMENT_STATUS
                + " GROUP BY " + COLUMN_SCHEDID);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENT_STATUS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS Users");

        db.execSQL("ALTER TABLE Appointments_mig RENAME TO " + TABLE_APPOINTMENTS);
        db.execSQL("ALTER TABLE AppointmentStatus_mig RENAME TO " + TABLE_APPOINTMENT_STATUS);
    }

    public String getTotalAppointmentCount() {
        int schedCounts = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(" + COLUMN_SCHEDID + ") FROM " + TABLE_APPOINTMENTS, null);
        if (cursor.moveToFirst()) {
            schedCounts = cursor.getInt(0);
        }
        cursor.close();
        Log.d("DatabaseHelper", "Total appointments: " + schedCounts);
        return String.valueOf(schedCounts);
    }

    public int getFinishedScheduleCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_APPOINTMENT_STATUS +
                " WHERE " + COLUMN_IS_FINISHED + " = 1", null);
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
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(query, null);
    }

    Cursor readFinishedSchedule() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT A.* FROM " + TABLE_APPOINTMENTS + " A " +
                "JOIN " + TABLE_APPOINTMENT_STATUS + " S " +
                "ON A." + COLUMN_SCHEDID + " = S." + COLUMN_SCHEDID + " " +
                "WHERE S." + COLUMN_IS_FINISHED + " = 1 " +
                "ORDER BY A." + COLUMN_DATE + " DESC, A." + COLUMN_TIME + " DESC " +
                "LIMIT 3";
        return db.rawQuery(query, null);
    }

    Cursor readAllSchedule() {
        String query = "SELECT A.*, S." + COLUMN_IS_FINISHED + " FROM " + TABLE_APPOINTMENTS + " A " +
                "JOIN " + TABLE_APPOINTMENT_STATUS + " S ON A." + COLUMN_SCHEDID + " = S." + COLUMN_SCHEDID +
                " ORDER BY A." + COLUMN_DATE + " ASC, A." + COLUMN_TIME + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(query, null);
    }

    Cursor readUpcomingSchedules() {
        String query = "SELECT A.* FROM " + TABLE_APPOINTMENTS + " A " +
                "JOIN " + TABLE_APPOINTMENT_STATUS + " S ON A." + COLUMN_SCHEDID + " = S." + COLUMN_SCHEDID + " " +
                "WHERE S." + COLUMN_IS_FINISHED + " = 0 " +
                "ORDER BY A." + COLUMN_DATE + " ASC, A." + COLUMN_TIME + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(query, null);
    }

    /**
     * Upcoming (not finished) appointments, ordered by date/time, limited.
     */
    Cursor readUpcomingSchedulesLimit(int limit) {
        String query = "SELECT A.* FROM " + TABLE_APPOINTMENTS + " A " +
                "JOIN " + TABLE_APPOINTMENT_STATUS + " S ON A." + COLUMN_SCHEDID + " = S." + COLUMN_SCHEDID + " " +
                "WHERE S." + COLUMN_IS_FINISHED + " = 0 " +
                "ORDER BY A." + COLUMN_DATE + " ASC, A." + COLUMN_TIME + " ASC " +
                "LIMIT " + limit;
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(query, null);
    }

    /**
     * Upcoming appointments on a given calendar day ({@code Date} stored as yyyy-MM-dd).
     */
    Cursor readUpcomingAppointmentsForDate(String yyyyMmDd) {
        String query = "SELECT A.* FROM " + TABLE_APPOINTMENTS + " A " +
                "JOIN " + TABLE_APPOINTMENT_STATUS + " S ON A." + COLUMN_SCHEDID + " = S." + COLUMN_SCHEDID + " " +
                "WHERE S." + COLUMN_IS_FINISHED + " = 0 AND A." + COLUMN_DATE + " = ? " +
                "ORDER BY A." + COLUMN_TIME + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(query, new String[]{yyyyMmDd});
    }

    /**
     * Distinct dates (yyyy-MM-dd) that have at least one upcoming appointment.
     */
    public Set<String> getUpcomingAppointmentDates() {
        HashSet<String> dates = new HashSet<>();
        String query = "SELECT DISTINCT A." + COLUMN_DATE + " FROM " + TABLE_APPOINTMENTS + " A " +
                "JOIN " + TABLE_APPOINTMENT_STATUS + " S ON A." + COLUMN_SCHEDID + " = S." + COLUMN_SCHEDID + " " +
                "WHERE S." + COLUMN_IS_FINISHED + " = 0";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        try {
            while (cursor.moveToNext()) {
                dates.add(cursor.getString(0));
            }
        } finally {
            cursor.close();
        }
        return dates;
    }

    public int getPendingScheduleCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_APPOINTMENT_STATUS +
                " WHERE " + COLUMN_IS_FINISHED + " = 0", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    /**
     * Completed appointments grouped by calendar month (yyyy-MM), up to {@code limit} rows,
     * most recent months first; use {@link Cursor} column indices 0 = ym, 1 = count.
     */
    Cursor readMonthlyCompletedCounts(int limit) {
        String query = "SELECT strftime('%Y-%m', A." + COLUMN_DATE + ") AS ym, COUNT(*) AS cnt " +
                "FROM " + TABLE_APPOINTMENTS + " A " +
                "JOIN " + TABLE_APPOINTMENT_STATUS + " S ON A." + COLUMN_SCHEDID + " = S." + COLUMN_SCHEDID + " " +
                "WHERE S." + COLUMN_IS_FINISHED + " = 1 " +
                "GROUP BY ym " +
                "ORDER BY ym DESC " +
                "LIMIT " + limit;
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(query, null);
    }

    boolean addAppointment(String name, String date, String description, String time, String link) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, name);
            values.put(COLUMN_DATE, date);
            values.put(COLUMN_DESCRIPTION, description);
            values.put(COLUMN_TIME, time);
            values.put(COLUMN_LINK, link);

            long newRowId = db.insert(TABLE_APPOINTMENTS, null, values);
            if (newRowId == -1) {
                return false;
            }

            ContentValues statusValues = new ContentValues();
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

        if (result == -1) {
            Toast.makeText(context, "Error: Failed to save schedule.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Schedule updated successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateAppointmentStatus(String schedID, boolean isFinished) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCHEDID, Long.parseLong(schedID));
        values.put(COLUMN_IS_FINISHED, isFinished ? 1 : 0);

        int rows = db.update(TABLE_APPOINTMENT_STATUS, values, COLUMN_SCHEDID + "=?", new String[]{schedID});

        if (rows == 0) {
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

        if (result == -1) {
            Toast.makeText(context, "Error: Failed to delete schedule.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Schedule deleted successfully!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * All rows from {@link #TABLE_APPOINTMENTS}, ordered by date and time.
     * Prefer {@link #getAppointmentsPage(int, int)} for UI lists to avoid loading large cursors.
     */
    public Cursor getAllAppointments() {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_APPOINTMENTS, null, null, null, null, null,
                COLUMN_DATE + " ASC, " + COLUMN_TIME + " ASC");
    }

    /**
     * Paged appointments (same order as {@link #getAllAppointments()}).
     */
    public Cursor getAppointmentsPage(int limit, int offset) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM " + TABLE_APPOINTMENTS + " ORDER BY " + COLUMN_DATE + " ASC, "
                + COLUMN_TIME + " ASC LIMIT ? OFFSET ?";
        return db.rawQuery(sql, new String[]{String.valueOf(limit), String.valueOf(offset)});
    }

    public int getAppointmentTableCount() {
        SQLiteDatabase db = getReadableDatabase();
        int count = 0;
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_APPOINTMENTS, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    count = cursor.getInt(0);
                }
            } finally {
                cursor.close();
            }
        }
        return count;
    }

    /**
     * Single appointment row by {@link #COLUMN_SCHEDID}, or empty cursor if missing.
     */
    public Cursor queryAppointmentBySchedId(String schedId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_APPOINTMENTS, null, COLUMN_SCHEDID + " = ?",
                new String[]{schedId}, null, null, null);
    }
}
