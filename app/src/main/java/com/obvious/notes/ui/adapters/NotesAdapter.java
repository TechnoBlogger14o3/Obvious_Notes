package com.obvious.notes.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.obvious.notes.Constants;
import com.obvious.notes.R;
import com.obvious.notes.data.NotesDb;
import com.obvious.notes.ui.activities.NoteActivity;

public class NotesAdapter extends RecyclerViewCursorAdapter<RecyclerView.ViewHolder> {

    private Cursor cursor;
    private Context context;
    private Activity activity;

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotesAdapter(Context c, Activity a, Cursor cursor) {
        super(cursor);
        this.cursor = cursor;
        context = c;
        activity = a;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.note_layout, parent, false);
            return new NoteViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.checklist_item_layout, parent, false);
            return new ChecklistViewHolder(v);
        }
    }

    @Override
    protected void onBindViewHolder(RecyclerView.ViewHolder vh, Cursor cursor) {
        final int id = cursor.getInt(cursor.getColumnIndex(NotesDb.Note._ID));
        final String title = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_TITLE));
        final String subtitle = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_SUBTITLE));
        final String content = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_CONTENT));
        final String time = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_TIME));
        final String created_at = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_CREATED_AT));
        final int archived = cursor.getInt(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_ARCHIVED));
        final int notified = cursor.getInt(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_NOTIFIED));
        final String color = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_COLOR));
        final int encrypted = cursor.getInt(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_ENCRYPTED));
        final int pinned = cursor.getInt(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_PINNED));
        final int tag = cursor.getInt(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_TAG));
        final String reminder = cursor.getString(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_REMINDER));
        final int checklist = cursor.getInt(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_CHECKLIST));
        final int deleted = cursor.getInt(cursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_DELETED));

        if (vh.getItemViewType() == 0) {
            // note ViewHolder
            final NoteViewHolder holder = (NoteViewHolder) vh;
            if (TextUtils.isEmpty(title)) {
                holder.title.setVisibility(View.GONE);
            } else holder.title.setText(title);
            if (TextUtils.isEmpty(subtitle)) {
                holder.subtitle.setVisibility(View.GONE);
            } else holder.subtitle.setText(subtitle);
            holder.content.setText(content);
            holder.date.setText(time);
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, NoteActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putInt(NotesDb.Note._ID, id);
                    bundle.putString(NotesDb.Note.COLUMN_NAME_TITLE, title);
                    bundle.putString(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
                    bundle.putString(NotesDb.Note.COLUMN_NAME_CONTENT, content);
                    bundle.putString(NotesDb.Note.COLUMN_NAME_TIME, time);
                    bundle.putString(NotesDb.Note.COLUMN_NAME_CREATED_AT, created_at);
                    bundle.putInt(NotesDb.Note.COLUMN_NAME_NOTIFIED, notified);
                    bundle.putInt(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
                    bundle.putString(NotesDb.Note.COLUMN_NAME_COLOR, color);
                    bundle.putInt(NotesDb.Note.COLUMN_NAME_ENCRYPTED, encrypted);
                    bundle.putInt(NotesDb.Note.COLUMN_NAME_PINNED, pinned);
                    bundle.putInt(NotesDb.Note.COLUMN_NAME_TAG, tag);
                    bundle.putString(NotesDb.Note.COLUMN_NAME_REMINDER, reminder);
                    bundle.putInt(NotesDb.Note.COLUMN_NAME_CHECKLIST, checklist);
                    bundle.putInt(NotesDb.Note.COLUMN_NAME_DELETED, deleted);
                    i.putExtra(Constants.NOTE_DETAILS_BUNDLE, bundle);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.card.setTransitionName("card");
                        holder.title.setTransitionName("title");
                        holder.subtitle.setTransitionName("subtitle");
                        holder.date.setTransitionName("time");
                        holder.content.setTransitionName("content");
                        Pair<View, String> p1 = Pair.create(holder.card, "card");
                        Pair<View, String> p2 = Pair.create((View) holder.title, "title");
                        Pair<View, String> p3 = Pair.create((View) holder.subtitle, "subtitle");
                        Pair<View, String> p4 = Pair.create((View) holder.content, "content");
                        Pair<View, String> p5 = Pair.create((View) holder.date, "time");
                        ActivityOptionsCompat options;
                        if (!TextUtils.isEmpty(subtitle)) {
                            if (!TextUtils.isEmpty(title))
                                options = ActivityOptionsCompat.
                                        makeSceneTransitionAnimation(activity, p1, p2, p3, p4, p5);
                            else options = ActivityOptionsCompat.
                                    makeSceneTransitionAnimation(activity, p1, p3, p4, p5);
                        } else {
                            if (!TextUtils.isEmpty(title))
                                options = ActivityOptionsCompat.
                                        makeSceneTransitionAnimation(activity, p1, p2, p4, p5);
                            else options = ActivityOptionsCompat.
                                    makeSceneTransitionAnimation(activity, p1, p4, p5);
                        }
                        context.startActivity(i, options.toBundle());
                    } else
                        context.startActivity(i);
                }
            };
            
            holder.card.setOnClickListener(onClickListener);
            holder.content.setOnClickListener(onClickListener);
        }
    }

    // Return the size of your data set (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        TextView title, subtitle, content, date;
        View card;

        public NoteViewHolder(View v) {
            super(v);
            mView = v;
            title = v.findViewById(R.id.note_title);
            subtitle = v.findViewById(R.id.note_subtitle);
            content = v.findViewById(R.id.note_content);
            date = v.findViewById(R.id.note_date);
            card = v.findViewById(R.id.note_card);
        }

    }

    public static class ChecklistViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        TextView title, subtitle, date, checklist1, checklist2, checklist3, more;
        CheckBox checkBox1, checkBox2, checkBox3;
        View card, contentView;

        public ChecklistViewHolder(View v) {
            super(v);
            mView = v;
            title = v.findViewById(R.id.note_title);
            subtitle = v.findViewById(R.id.note_subtitle);
            contentView = v.findViewById(R.id.checklist_items);
            checkBox1 = v.findViewById(R.id.checkbox1);
            checkBox2 = v.findViewById(R.id.checkbox2);
            checkBox3 = v.findViewById(R.id.checkbox3);
            checklist1 = v.findViewById(R.id.checklist_item1);
            checklist2 = v.findViewById(R.id.checklist_item2);
            checklist3 = v.findViewById(R.id.checklist_item3);
            more = v.findViewById(R.id.overflow_text);
            date = v.findViewById(R.id.note_date);
            card = v.findViewById(R.id.note_card);
        }

    }

}
