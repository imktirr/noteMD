package com.wmren.notemd.utilities;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wmren.notemd.staticfile.NoteExample;
import com.wmren.notemd.utilities.Note;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NotesDB extends SQLiteOpenHelper {

    public static final String TABLE_NAME_NOTES = "note";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_NOTE_TITLE = "title";
    public static final String COLUMN_NAME_NOTE_CONTENT = "content";
    public static final String COLUMN_NAME_NOTE_DATE = "date";

    //private Context mContext;

    public NotesDB(Context context, String name,
                   SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        //mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
        String dateNum = sdf.format(date);
        Note noteDefault = new Note(NoteExample.defaultTitle, NoteExample.defaultContent,  dateNum, "1");
        Note noteExample = new Note(NoteExample.noteTitle, NoteExample.noteContent,  dateNum, "2");
        String sql = "CREATE TABLE " + TABLE_NAME_NOTES + "(" + COLUMN_NAME_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME_NOTE_TITLE + " TEXT NOT NULL DEFAULT\"\","
                + COLUMN_NAME_NOTE_CONTENT + " TEXT NOT NULL DEFAULT\"\","
                + COLUMN_NAME_NOTE_DATE + " TEXT NOT NULL DEFAULT\"\"" + ")";
        //Log.d("SQL", sql);
        db.execSQL(sql);

        db.execSQL("insert into " + TABLE_NAME_NOTES + "(" + COLUMN_NAME_ID + ", " + COLUMN_NAME_NOTE_TITLE + ", "
        + COLUMN_NAME_NOTE_CONTENT + ", " + COLUMN_NAME_NOTE_DATE + ") values(?, ?, ?, ?)"
        , new String[] {"1", noteDefault.getTitle(), noteDefault.getContent(), noteDefault.getDate()});

        db.execSQL("insert into " + TABLE_NAME_NOTES + "(" + COLUMN_NAME_ID + ", " + COLUMN_NAME_NOTE_TITLE + ", "
                        + COLUMN_NAME_NOTE_CONTENT + ", " + COLUMN_NAME_NOTE_DATE + ") values(?, ?, ?, ?)"
                , new String[] {"2", noteExample.getTitle(), noteExample.getContent(), noteExample.getDate()});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
