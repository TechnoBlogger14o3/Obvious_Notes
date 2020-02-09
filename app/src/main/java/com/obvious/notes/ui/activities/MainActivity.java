package com.obvious.notes.ui.activities;

import android.animation.ObjectAnimator;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityOptionsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.obvious.notes.Constants;
import com.obvious.notes.NoteObj;
import com.obvious.notes.R;
import com.obvious.notes.data.NotesDb;
import com.obvious.notes.data.NotesDbHelper;
import com.obvious.notes.data.NotesProvider;
import com.obvious.notes.ui.adapters.NotesAdapter;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private NotesAdapter mAdapter;
    protected NotesDbHelper dbHelper;
    private boolean lightTheme;
    private boolean changed;
    private ArrayList<NoteObj> noteObjArrayList;
    private RecyclerView recyclerView;
    private StaggeredGridLayoutManager layoutManager;
    private TextView blankText;
    private View fab;
    private SharedPreferences pref;
    private View addNoteView;
    private View shadow;
    private View.OnClickListener fabClickListener;
    private View.OnClickListener addNoteListener;
    private boolean fabOpen = false;
    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            changed = true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);

        lightTheme = pref.getBoolean(Constants.LIGHT_THEME, false);
        if (lightTheme)
            setTheme(R.style.AppTheme_Light_DrawerActivity);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getLoaderManager().initLoader(0, null, this);

        fab = findViewById(R.id.fab);
        addNoteView = findViewById(R.id.note_fab);
        shadow = findViewById(R.id.shadow);

        fabClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fabOpen) {
                    closeFabMenu();
                } else {
                    showFabMenu();
                }
            }
        };

        addNoteListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeFabMenu();
                Intent i = new Intent(MainActivity.this, NoteActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this);
                    startActivity(i, options.toBundle());
                } else startActivity(i);

            }
        };
        fab.setOnClickListener(fabClickListener);
        shadow.setOnClickListener(fabClickListener);
        dbHelper = new NotesDbHelper(this);
        blankText = findViewById(R.id.blankTextView);
        noteObjArrayList = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("note-list-changed"));
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    public float convertDpToPixel(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    private void showFabMenu() {
        fab.setOnClickListener(null);
        fab.animate().rotationBy(45f).setDuration(300).start();
        fabOpen = true;
        addNoteView.setVisibility(View.VISIBLE);
        addNoteView.setAlpha(0f);
        addNoteView.animate().translationYBy(convertDpToPixel(-52)).alpha(1f).setDuration(300).start();
        shadow.setVisibility(View.VISIBLE);
        shadow.setAlpha(0f);
        shadow.animate().alpha(1f).setDuration(300).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.setOnClickListener(fabClickListener);
                addNoteView.setOnClickListener(addNoteListener);
            }
        }, 300);
    }

    private void closeFabMenu() {
        fab.setOnClickListener(null);
        ObjectAnimator.ofFloat(fab, "rotation", 45f, 0f).start();
        fabOpen = false;
        addNoteView.animate().translationYBy(convertDpToPixel(52)).alpha(0f).setDuration(300).start();
        shadow.animate().alpha(0f).setDuration(300).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addNoteView.setVisibility(View.GONE);
                shadow.setVisibility(View.GONE);
                addNoteView.setOnClickListener(null);
                fab.setOnClickListener(fabClickListener);
            }
        }, 300);
    }

    @Override
    public void onResume() {

        if (changed) {
            getLoaderManager().restartLoader(0, null, this);
            changed = false;
        }

        if (lightTheme != pref.getBoolean(Constants.LIGHT_THEME, false)) {
            if (lightTheme)
                setTheme(R.style.AppTheme_NoActionBar);
            else setTheme(R.style.AppTheme_Light_NoActionBar);
            lightTheme = !lightTheme;
            recreate();
        }
        super.onResume();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.
        Uri.Builder builder = NotesProvider.BASE_URI.buildUpon().appendPath(NotesDb.Note.TABLE_NAME);
        Uri baseUri = builder.build();

        String[] projection = {
                NotesDb.Note.TABLE_NAME + "." + NotesDb.Note._ID,
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

        String sort;
        if (pref.getBoolean(Constants.OLDEST_FIRST, false))
            sort = " ASC";
        else
            sort = " DESC";

        int mode = pref.getInt(Constants.LIST_MODE, 0);
        StringBuilder selection = new StringBuilder();
        if (mode == 1) { // Notes only
            selection.append(NotesDb.Note.COLUMN_NAME_CHECKLIST + " LIKE " + 0 + " AND ");
        }
        return new CursorLoader(this, baseUri,
                projection, selection.toString(), null,
                NotesDb.Note.COLUMN_NAME_TIME + sort);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) Log.d("onLoadFinished", "Cursor is null!");
        else {
            noteObjArrayList.clear();
            if (cursor.moveToFirst()) {
                do {
                    NoteObj noteObj = new NoteObj(cursor.getInt(0));
                    noteObjArrayList.add(noteObj);
                } while (cursor.moveToNext());
            }

            Parcelable recyclerViewState = null;
            if (layoutManager != null && mAdapter != null) {
                // Save state
                recyclerViewState = layoutManager.onSaveInstanceState();
            }
            mAdapter = new NotesAdapter(this, this, cursor);
            layoutManager = new StaggeredGridLayoutManager(pref.getInt(Constants.NUM_COLUMNS, 1), StaggeredGridLayoutManager.VERTICAL);
            layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(mAdapter);
            if (recyclerViewState != null) {
                layoutManager.onRestoreInstanceState(recyclerViewState);
                recyclerView.smoothScrollToPosition(0);
            }

        }

        if (noteObjArrayList.isEmpty())
            blankText.setVisibility(View.VISIBLE);
        else
            blankText.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
