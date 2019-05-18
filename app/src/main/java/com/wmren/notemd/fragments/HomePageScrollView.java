package com.wmren.notemd.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wmren.notemd.activities.MainActivity;
import com.wmren.notemd.utilities.Note;
import com.wmren.notemd.utilities.NoteAdapter;
import com.wmren.notemd.utilities.NotesDB;
import com.wmren.notemd.activities.NoteviewActivity;
import com.wmren.notemd.R;

import java.util.ArrayList;
import java.util.List;



public class HomePageScrollView extends Fragment {

    private static final String TAG = "HomePageScrollView";

    private SQLiteDatabase dbRead;

    private List<Note> noteList = new ArrayList<>();
    private NoteAdapter adapter;
    private RecyclerView recyclerView;

    public HomePageScrollView() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home_page_scroll_view, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");

        dbRead = MainActivity.notesDB.getWritableDatabase();
        refreshNoteList();
        View rootView = getView();
        recyclerView = rootView.findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new NoteAdapter(noteList);
        adapter.setItemClickListener(position -> {
            Note note = noteList.get(position);
            Intent intent = new Intent(getContext(), NoteviewActivity.class);
            intent.putExtra("noteStatus", 20);
            intent.putExtra("noteTitle", note.getTitle());
            intent.putExtra("noteContent", note.getContent());
            intent.putExtra("noteId", note.getId());
            startActivity(intent);
        });

        adapter.setItemLongClickListener(position -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("删除便签");
            builder.setMessage("确认删除便签？");
            builder.setPositiveButton("确定", (dialog, which) -> {
                Note note = noteList.get(position);
                dbRead.delete(NotesDB.TABLE_NAME_NOTES, NotesDB.COLUMN_NAME_ID + " = ?", new String[]{note.getId()});

                refreshNoteList();
                adapter.setNoteList(noteList);
                adapter.notifyDataSetChanged();
                Snackbar.make(getActivity().findViewById(R.id.main), "已删除便签", Snackbar.LENGTH_SHORT).show();
            });
            builder.setNegativeButton("取消", (dialog, which) -> {
                Snackbar.make(getActivity().findViewById(R.id.main), "操作已取消", Snackbar.LENGTH_SHORT).show();
            });
            builder.show();
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshNoteList();
        adapter.setNoteList(noteList);
        adapter.notifyDataSetChanged();

        Log.d(TAG, "onResume");
    }

    private void generateNoteList() {
        Cursor cursor = dbRead.query(NotesDB.TABLE_NAME_NOTES, null, null,
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

    private void refreshNoteList() {
        noteList.clear();
        generateNoteList();
    }
}
