package com.obvious.notes.ui.adapters;


import android.database.Cursor;
import android.database.DataSetObserver;
import android.util.Log;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.obvious.notes.data.NotesDb;


public abstract class RecyclerViewCursorAdapter<VH extends RecyclerView.ViewHolder> extends
        RecyclerView.Adapter<VH> {
    private Cursor mCursor;
    private boolean mDataValid;
    private int mRowIDColumn;
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            mDataValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            mDataValid = false;
            notifyDataSetChanged();
        }
    };

    public RecyclerViewCursorAdapter(Cursor cursor) {
        //setHasStableIds(true);
        swapCursor(cursor);
        mCursor = cursor;
    }

    public abstract VH onCreateViewHolder(ViewGroup parent, int viewType);

    protected abstract void onBindViewHolder(VH holder, Cursor cursor);

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (!mDataValid) {
            Log.d("RecyclerViewAdapter", "Cursor is not valid!");
        }
        if (mCursor != null && !mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Could not move cursor to position " + position);
        }
        if (mCursor != null) onBindViewHolder(holder, mCursor);
        else Log.d("RecyclerViewAdapter", "Cursor is null!");
    }

    @Override
    public int getItemViewType(int position) {
        if (!mDataValid) {
            Log.d("RecyclerViewAdapter", "Cursor is not valid!");
        }
        if ((mCursor != null && !mCursor.moveToPosition(position)) || mCursor == null) {
            throw new IllegalStateException("Could not move cursor to position " + position);
        }
        return mCursor.getInt(mCursor.getColumnIndex(NotesDb.Note.COLUMN_NAME_CHECKLIST));
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIDColumn);
        }
        return RecyclerView.NO_ID;
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        if (oldCursor != null) {
            if (mDataSetObserver != null) {
                oldCursor.unregisterDataSetObserver(mDataSetObserver);
            }
        }
        mCursor = newCursor;
        if (newCursor != null) {
            if (mDataSetObserver != null) {
                newCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIDColumn = newCursor.getColumnIndexOrThrow(NotesDb.Note._ID);
            mDataValid = true;
            notifyDataSetChanged();
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }

}