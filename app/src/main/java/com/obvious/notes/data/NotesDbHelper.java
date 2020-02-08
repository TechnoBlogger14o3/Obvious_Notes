package com.obvious.notes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.obvious.notes.Constants;
import com.obvious.notes.NoteObj;

import java.util.ArrayList;


public class NotesDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "Notes.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + NotesDb.Note.TABLE_NAME + " (" +
                    NotesDb.Note._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_TITLE + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_SUBTITLE + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_CONTENT + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_CREATED_AT + TEXT_TYPE + " UNIQUE" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_ARCHIVED + " INTEGER" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_NOTIFIED + " INTEGER" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_COLOR + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_ENCRYPTED + " INTEGER" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_PINNED + " INTEGER" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_TAG + " INTEGER" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_REMINDER + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_CHECKLIST + " INTEGER" + COMMA_SEP +
                    NotesDb.Note.COLUMN_NAME_DELETED + " INTEGER" + " ) ";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + NotesDb.Note.TABLE_NAME;

    private static final String SQL_CREATE_ENTRIES_CHECKLIST =
            "CREATE TABLE " + NotesDb.Checklist.TABLE_NAME + " (" +
                    NotesDb.Checklist._ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
                    NotesDb.Checklist.COLUMN_NAME_NOTE_ID + " INTEGER " + COMMA_SEP +
                    NotesDb.Checklist.COLUMN_NAME_ITEM + TEXT_TYPE + COMMA_SEP +
                    NotesDb.Checklist.COLUMN_NAME_CHECKED + " INTEGER ) ";
    private static final String SQL_DELETE_ENTRIES_CHECKLIST =
            "DROP TABLE IF EXISTS " + NotesDb.Checklist.TABLE_NAME;

    public NotesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES_CHECKLIST);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 4:
                db.execSQL("ALTER TABLE " + NotesDb.Note.TABLE_NAME + " ADD COLUMN " + NotesDb.Note.COLUMN_NAME_CHECKLIST + " INTEGER DEFAULT 0;");
                db.execSQL("UPDATE " + NotesDb.Note.TABLE_NAME + " SET " + NotesDb.Note.COLUMN_NAME_CHECKLIST + " = 0");
                Log.d(getClass().getName(), "Database updated successfully to version 5 (added checklist column)");
            case 5:
                db.execSQL(SQL_CREATE_ENTRIES_CHECKLIST);
                Log.d(getClass().getName(), "Database updated successfully to version 6 (created checklist table)");
            case 6:
                db.execSQL("ALTER TABLE " + NotesDb.Note.TABLE_NAME + " ADD COLUMN " + NotesDb.Note.COLUMN_NAME_DELETED + " INTEGER DEFAULT 0;");
                db.execSQL("UPDATE " + NotesDb.Note.TABLE_NAME + " SET " + NotesDb.Note.COLUMN_NAME_DELETED + " = 0");
                Log.d(getClass().getName(), "Database updated successfully to version 7 (added deleted column)");
                break;
            default:
                db.execSQL(SQL_DELETE_ENTRIES);
                db.execSQL(SQL_DELETE_ENTRIES_CHECKLIST);
                onCreate(db);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public int addOrUpdateNote(int id, String title, String subtitle, String content, String time, String created_at, int archived, int notified, String color, int encrypted, int pinned, int tag, String reminder, int checklist, int deleted) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NotesDb.Note.COLUMN_NAME_TITLE, title);
        values.put(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
        values.put(NotesDb.Note.COLUMN_NAME_CONTENT, content);
        values.put(NotesDb.Note.COLUMN_NAME_TIME, time);
        values.put(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
        values.put(NotesDb.Note.COLUMN_NAME_NOTIFIED, notified);
        values.put(NotesDb.Note.COLUMN_NAME_COLOR, color);
        values.put(NotesDb.Note.COLUMN_NAME_ENCRYPTED, encrypted);
        values.put(NotesDb.Note.COLUMN_NAME_PINNED, pinned);
        values.put(NotesDb.Note.COLUMN_NAME_TAG, tag);
        values.put(NotesDb.Note.COLUMN_NAME_REMINDER, reminder);
        values.put(NotesDb.Note.COLUMN_NAME_CHECKLIST, checklist);
        values.put(NotesDb.Note.COLUMN_NAME_DELETED, deleted);

        int i = db.update(NotesDb.Note.TABLE_NAME, values,
                NotesDb.Note.COLUMN_NAME_CREATED_AT + " = ? ",
                new String[]{created_at});
        if (i == 0) {
            values.put(NotesDb.Note.COLUMN_NAME_CREATED_AT, created_at);
            i = (int) db.insertWithOnConflict(NotesDb.Note.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            Log.d("DB", "Added");
        } else {
            Log.d("DB", "Updated");
            i = id;
        }
        db.close();
        return i;
    }

    public int updateFlag(int id, String field, int value) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(field, value);

        int i = db.update(NotesDb.Note.TABLE_NAME, values,
                NotesDb.Note._ID + " = ? ",
                new String[]{String.valueOf(id)});
        Log.d("Updated field", field + value + " for note id: " + i);
        db.close();
        return i;
    }

    public int updateFlag(int id, String field, String value) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(field, value);

        int i = db.update(NotesDb.Note.TABLE_NAME, values,
                NotesDb.Note._ID + " = ? ",
                new String[]{String.valueOf(id)});
        Log.d("Updated field", field + value + " for note id: " + i);
        db.close();
        return i;
    }

    public void deleteNote(String created_at) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(NotesDb.Note.TABLE_NAME,
                NotesDb.Note.COLUMN_NAME_CREATED_AT + " = ? ",
                new String[]{created_at});
        Log.d("DB", "Deleted");
        db.close();
    }

    public NoteObj getNote(int id) {
        ArrayList<NoteObj> mList = new ArrayList<NoteObj>();
        String[] projection = {
                NotesDb.Note._ID,
                NotesDb.Note.COLUMN_NAME_TITLE,
                NotesDb.Note.COLUMN_NAME_SUBTITLE,
                NotesDb.Note.COLUMN_NAME_CONTENT,
                NotesDb.Note.COLUMN_NAME_TIME,
                NotesDb.Note.COLUMN_NAME_CREATED_AT,
                NotesDb.Note.COLUMN_NAME_ARCHIVED,
                NotesDb.Note.COLUMN_NAME_NOTIFIED,
                NotesDb.Note.COLUMN_NAME_COLOR,
                NotesDb.Note.COLUMN_NAME_ENCRYPTED,
                NotesDb.Note.COLUMN_NAME_PINNED,
                NotesDb.Note.COLUMN_NAME_TAG,
                NotesDb.Note.COLUMN_NAME_REMINDER,
                NotesDb.Note.COLUMN_NAME_CHECKLIST,
                NotesDb.Note.COLUMN_NAME_DELETED
        };

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(NotesDb.Note.TABLE_NAME, projection, NotesDb.Note._ID + " = " + id, null, null, null, NotesDb.Note.COLUMN_NAME_TIME + " DESC");


        if (cursor.moveToFirst()) {
            NoteObj noteObj = new NoteObj(cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getInt(6),
                    cursor.getInt(7),
                    cursor.getString(8),
                    cursor.getInt(9),
                    cursor.getInt(10),
                    cursor.getInt(11),
                    cursor.getString(12),
                    cursor.getInt(13),
                    cursor.getInt(14));

            cursor.close();
            db.close();
            return noteObj;
        }

        cursor.close();
        db.close();
        return null;
    }

    public ArrayList<NoteObj> getAllNotes(int archive, int deleted) {
        ArrayList<NoteObj> mList = new ArrayList<NoteObj>();
        String[] projection = {
                NotesDb.Note._ID,
                NotesDb.Note.COLUMN_NAME_TITLE,
                NotesDb.Note.COLUMN_NAME_SUBTITLE,
                NotesDb.Note.COLUMN_NAME_CONTENT,
                NotesDb.Note.COLUMN_NAME_TIME,
                NotesDb.Note.COLUMN_NAME_CREATED_AT,
                NotesDb.Note.COLUMN_NAME_ARCHIVED,
                NotesDb.Note.COLUMN_NAME_NOTIFIED,
                NotesDb.Note.COLUMN_NAME_COLOR,
                NotesDb.Note.COLUMN_NAME_ENCRYPTED,
                NotesDb.Note.COLUMN_NAME_PINNED,
                NotesDb.Note.COLUMN_NAME_TAG,
                NotesDb.Note.COLUMN_NAME_REMINDER,
                NotesDb.Note.COLUMN_NAME_CHECKLIST,
                NotesDb.Note.COLUMN_NAME_DELETED
        };

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(NotesDb.Note.TABLE_NAME, projection, NotesDb.Note.COLUMN_NAME_ARCHIVED + " LIKE " + archive + " AND " + NotesDb.Note.COLUMN_NAME_DELETED + " LIKE " + deleted, null, null, null, NotesDb.Note.COLUMN_NAME_TIME + " DESC");

        if (cursor.moveToFirst()) {
            do {
                NoteObj noteObj = new NoteObj(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getString(8),
                        cursor.getInt(9),
                        cursor.getInt(10),
                        cursor.getInt(11),
                        cursor.getString(12),
                        cursor.getInt(13),
                        cursor.getInt(14));
                mList.add(noteObj);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return mList;
    }

    public ArrayList<NoteObj> getNotificationsAndReminders() {
        ArrayList<NoteObj> mList = new ArrayList<NoteObj>();
        String[] projection = {
                NotesDb.Note._ID,
                NotesDb.Note.COLUMN_NAME_TITLE,
                NotesDb.Note.COLUMN_NAME_SUBTITLE,
                NotesDb.Note.COLUMN_NAME_CONTENT,
                NotesDb.Note.COLUMN_NAME_TIME,
                NotesDb.Note.COLUMN_NAME_CREATED_AT,
                NotesDb.Note.COLUMN_NAME_ARCHIVED,
                NotesDb.Note.COLUMN_NAME_NOTIFIED,
                NotesDb.Note.COLUMN_NAME_COLOR,
                NotesDb.Note.COLUMN_NAME_ENCRYPTED,
                NotesDb.Note.COLUMN_NAME_PINNED,
                NotesDb.Note.COLUMN_NAME_TAG,
                NotesDb.Note.COLUMN_NAME_REMINDER,
                NotesDb.Note.COLUMN_NAME_CHECKLIST,
                NotesDb.Note.COLUMN_NAME_DELETED
        };

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(NotesDb.Note.TABLE_NAME, projection, NotesDb.Note.COLUMN_NAME_NOTIFIED + " LIKE 1 OR " + NotesDb.Note.COLUMN_NAME_REMINDER + " NOT LIKE '" + Constants.REMINDER_NONE + "'",
                null, null, null, NotesDb.Note.COLUMN_NAME_TIME + " ASC");

        if (cursor.moveToFirst()) {
            do {
                NoteObj noteObj = new NoteObj(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getInt(6),
                        cursor.getInt(7),
                        cursor.getString(8),
                        cursor.getInt(9),
                        cursor.getInt(10),
                        cursor.getInt(11),
                        cursor.getString(12),
                        cursor.getInt(13),
                        cursor.getInt(14));
                mList.add(noteObj);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return mList;
    }

}