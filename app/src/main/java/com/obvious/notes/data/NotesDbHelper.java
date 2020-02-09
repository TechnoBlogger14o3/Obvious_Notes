package com.obvious.notes.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class NotesDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 7;
    private static final String DATABASE_NAME = "Notes.db";
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

    public NotesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            case 4:
                db.execSQL("ALTER TABLE " + NotesDb.Note.TABLE_NAME + " ADD COLUMN " + NotesDb.Note.COLUMN_NAME_CHECKLIST + " INTEGER DEFAULT 0;");
                db.execSQL("UPDATE " + NotesDb.Note.TABLE_NAME + " SET " + NotesDb.Note.COLUMN_NAME_CHECKLIST + " = 0");
                Log.d(getClass().getName(), "Database updated successfully to version 5 (added checklist column)");
                break;
            case 6:
                db.execSQL("ALTER TABLE " + NotesDb.Note.TABLE_NAME + " ADD COLUMN " + NotesDb.Note.COLUMN_NAME_DELETED + " INTEGER DEFAULT 0;");
                db.execSQL("UPDATE " + NotesDb.Note.TABLE_NAME + " SET " + NotesDb.Note.COLUMN_NAME_DELETED + " = 0");
                Log.d(getClass().getName(), "Database updated successfully to version 7 (added deleted column)");
                break;
            default:
                db.execSQL(SQL_DELETE_ENTRIES);
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
}