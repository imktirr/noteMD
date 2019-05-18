package com.wmren.notemd.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;

import com.wmren.notemd.utilities.Note;
import com.wmren.notemd.utilities.NoteAdapter;
import com.wmren.notemd.utilities.NotesDB;
import com.wmren.notemd.R;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private android.support.v7.widget.SearchView mSearchView;

    private SQLiteDatabase dbRead;
    private String keyword;

    private List<Note> noteList = new ArrayList<>();
    private NoteAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        dbRead = MainActivity.notesDB.getWritableDatabase();

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar_search);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.round_arrow_back_24);
        }

        recyclerView = findViewById(R.id.search_recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NoteAdapter(noteList);
        adapter.setItemClickListener(position -> {
            Note note = noteList.get(position);
            Intent intent = new Intent(SearchActivity.this, NoteviewActivity.class);
            intent.putExtra("noteStatus", 20);
            intent.putExtra("noteTitle", note.getTitle());
            intent.putExtra("noteContent", note.getContent());
            intent.putExtra("noteId", note.getId());
            startActivity(intent);
        });

        adapter.setItemLongClickListener(position -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
            builder.setTitle("删除便签");
            builder.setMessage("确认删除便签？");
            builder.setPositiveButton("确定", (dialog, which) -> {
                Note note = noteList.get(position);
                dbRead.delete(NotesDB.TABLE_NAME_NOTES, NotesDB.COLUMN_NAME_ID + " = ?", new String[]{note.getId()});

                refreshNoteList(keyword);
                adapter.setNoteList(noteList);
                adapter.notifyDataSetChanged();
                Snackbar.make(findViewById(R.id.search_main), "已删除便签", Snackbar.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("取消", (dialog, which) -> {
                Snackbar.make(findViewById(R.id.search_main), "操作已取消", Snackbar.LENGTH_SHORT).show();
            });
            builder.show();
        });

        recyclerView.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_search, menu);

        MenuItem menuItem = menu.findItem(R.id.item_searchview);
        //通过MenuItem得到SearchView
        mSearchView = (android.support.v7.widget.SearchView) menuItem.getActionView();
        mSearchView.onActionViewExpanded();
        mSearchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                keyword = newText;
                refreshNoteList(newText);
                adapter.setNoteList(noteList);
                adapter.notifyDataSetChanged();
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //导航栏按钮选区时的监听事件
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    private void generateNoteList(String keyword) {
        Cursor cursor = dbRead.query(NotesDB.TABLE_NAME_NOTES, null,
                NotesDB.COLUMN_NAME_NOTE_TITLE + " LIKE " + "\'%" + keyword + "%\'",
                null, null, null, "_id desc");
        if (cursor.moveToFirst()) {
            do {
                String noteTitle = cursor.getString(cursor.getColumnIndex(NotesDB.COLUMN_NAME_NOTE_TITLE));
                String noteContent = cursor.getString(cursor.getColumnIndex(NotesDB.COLUMN_NAME_NOTE_CONTENT));
                String noteDate = cursor.getString(cursor.getColumnIndex(NotesDB.COLUMN_NAME_NOTE_DATE));
                String noteId = cursor.getString(cursor.getColumnIndex(NotesDB.COLUMN_NAME_ID));
                Note cNote = new Note(noteTitle, noteContent, noteDate, noteId);
                noteList.add(cNote);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void refreshNoteList(String keyword) {
        noteList.clear();
        generateNoteList(keyword);
    }
}
