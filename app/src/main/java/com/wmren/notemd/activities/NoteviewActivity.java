package com.wmren.notemd.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.wmren.notemd.fragments.EditNoteFragment;
import com.wmren.notemd.utilities.NotesDB;
import com.wmren.notemd.R;
import com.wmren.notemd.fragments.PreviewNoteFragment;

import java.text.SimpleDateFormat;
import java.util.Date;

public class NoteviewActivity extends AppCompatActivity implements EditNoteFragment.EditFragmentInteraction {

    private static final String TAG = "NoteviewActivity";
    private static final int NEW_NOTE = 10;
    private static final int EXIST_NOTE = 20;

    private int noteStatus;

    private String noteTitle = "";
    private String noteContent = "";
    private String noteId;

    private int EDIT = 1;
    private int PREVIEW = 2;

    private int currentState = EDIT;

    private EditNoteFragment editNoteFragment = new EditNoteFragment();
    private PreviewNoteFragment previewNoteFragment = new PreviewNoteFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noteview);

        Intent intent = getIntent();
        noteStatus = intent.getIntExtra("noteStatus", NEW_NOTE);
        noteTitle = intent.getStringExtra("noteTitle");
        noteContent = intent.getStringExtra("noteContent");
        noteId = intent.getStringExtra("noteId");

        Log.d(TAG, "note id = " + noteId);
        //将原生actionbar替换为toolbar
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.view_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.round_arrow_back_24);
        }

        //如果是打开已经存在的便签，则需要拷贝内容
        if (noteStatus == EXIST_NOTE) {
            Bundle currentNoteInfo;
            currentNoteInfo = new Bundle();
            currentNoteInfo.putString(EditNoteFragment.NOTE_TITLE, noteTitle);
            currentNoteInfo.putString(EditNoteFragment.NOTE_CONTENT, noteContent);
            editNoteFragment.setArguments(currentNoteInfo);
        }
        addFragment(editNoteFragment, R.id.fragment_field);
    }

    //切换碎片所用的函数
    private void replaceFragment(Fragment fragment, int fragmentId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(fragmentId, fragment);
        transaction.commit();
    }

    //添加新碎片所用的函数
    private void addFragment(Fragment fragment, int fragmentId) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(fragmentId, fragment);
        transaction.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //用于在主活动中创建导航栏
        getMenuInflater().inflate(R.menu.toolbar_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //导航栏按钮选区时的监听事件
        switch (item.getItemId()) {
            case android.R.id.home:
                saveNote(noteStatus);
                finish();
                break;
            case R.id.edit: //切换显示类型
                currentState = EDIT;
                replaceFragment(editNoteFragment, R.id.fragment_field);
                break;
            case R.id.preview:
                currentState = PREVIEW;
                Bundle noteInfo = new Bundle();
                if (noteTitle == null) {
                    noteInfo.putString(PreviewNoteFragment.NOTE_TITLE, "");
                } else {
                    noteInfo.putString(PreviewNoteFragment.NOTE_TITLE, noteTitle);
                }
                if (noteContent == null) {
                    noteInfo.putString(PreviewNoteFragment.NOTE_CONTENT, "");
                } else {
                    noteInfo.putString(PreviewNoteFragment.NOTE_CONTENT, noteContent);
                }
                previewNoteFragment.setArguments(noteInfo);
                replaceFragment(previewNoteFragment, R.id.fragment_field);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //TODO: 保存便签内容
        if (currentState == EDIT) {
            super.onBackPressed();
            saveNote(noteStatus);
        } else if (currentState == PREVIEW) {
            currentState = EDIT;
            replaceFragment(editNoteFragment, R.id.fragment_field);
        }
    }

    @Override
    public void onTitleChanged(String title) {
        noteTitle = title;
    }

    @Override
    public void onContentChanged(String content) {
        noteContent = content;
    }

    private void saveNote(int type) {
        SQLiteDatabase dbRead;
        dbRead = MainActivity.notesDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (noteTitle == null)
                noteTitle = "";
        values.put(NotesDB.COLUMN_NAME_NOTE_TITLE, noteTitle);
        if (noteContent == null)
                noteContent = "";
        values.put(NotesDB.COLUMN_NAME_NOTE_CONTENT, noteContent);
        if (type == NEW_NOTE) {
            if (!noteTitle.equals("") || !noteContent.equals("")) {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
                String dateNum = sdf.format(date);
//            values.put(NotesDB.COLUMN_NAME_ID, noteId);
                values.put(NotesDB.COLUMN_NAME_NOTE_DATE, dateNum);
                dbRead.insert(NotesDB.TABLE_NAME_NOTES, null, values);
                Toast.makeText(NoteviewActivity.this, "便签已保存", Toast.LENGTH_SHORT).show();
            }
        } else if (type == EXIST_NOTE) {
            dbRead.update(NotesDB.TABLE_NAME_NOTES, values, NotesDB.COLUMN_NAME_ID + " = ?",
                    new String[]{noteId});
            Toast.makeText(NoteviewActivity.this, "便签已保存", Toast.LENGTH_SHORT).show();
        }
    }
}
