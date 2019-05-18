package com.wmren.notemd.utilities;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wmren.notemd.R;

import java.util.List;
import java.util.Random;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {

    private Context mContext;
    private List<Note> mNoteList;
    private OnItemClickListener mListener;
    private OnItemLongClickListener mLongClickListener;
    private static String[] colors = {
            "#EF9A9A", "#F48FB1", "#CE93D8", "#B39DDB",
            "#9FA8DA", "#90CAF9", "#81D4FA", "#80DEEA",
            "#80CBC4", "#A5D6A7", "#C5E1A5", "#E6EE9C",
            "#FFF59D", "#FFE082", "#FFCC80", "#FFAB91"
    };

    private Random random = new Random();
    static class ViewHolder extends RecyclerView.ViewHolder {
        View noteView;
        CardView cardView;
        TextView noteTitle;
        TextView noteSummary;
        TextView noteDate;

        public ViewHolder(View view) {
            super(view);
            noteView = view;
            cardView = (CardView) view;
            noteTitle = view.findViewById(R.id.home_note_title);
            noteSummary = view.findViewById(R.id.home_note_summary);
            noteDate = view.findViewById(R.id.home_note_date);
        }
    }

    public NoteAdapter(List<Note> fruitList) {
        mNoteList = fruitList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mContext == null) {
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.note_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.noteView.setOnClickListener(v->{
            if (mListener != null) {
                mListener.onItemClick((Integer) v.getTag());
            }
        });

        holder.noteView.setOnLongClickListener(v -> {
            if (mLongClickListener != null) {
                mLongClickListener.onItemLongClick((Integer) v.getTag());
            }
            return false;
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Note note = mNoteList.get(position);
        holder.noteTitle.setText(note.getTitle());
        holder.noteSummary.setText(note.getSummary());
        holder.noteDate.setText(note.getDate());
        holder.itemView.setTag(position);


        int index = random.nextInt(colors.length);
        holder.cardView.setCardBackgroundColor(Color.parseColor(colors[index]));
    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public void setItemClickListener(OnItemClickListener itemClickListener) {
        mListener = itemClickListener;
    }

    public void setItemLongClickListener(OnItemLongClickListener itemLongClickListener) {
        mLongClickListener = itemLongClickListener;
    }

    public void setNoteList(List<Note> noteList) {
        this.mNoteList = noteList;
    }
}
