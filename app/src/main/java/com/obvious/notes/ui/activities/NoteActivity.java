package com.obvious.notes.ui.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.obvious.notes.Constants;
import com.obvious.notes.R;
import com.obvious.notes.data.NotesDb;
import com.obvious.notes.data.NotesDbHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NoteActivity extends AppCompatActivity {
    protected Boolean editMode;
    protected CoordinatorLayout coordinatorLayout;
    protected NotesDbHelper dbHelper;
    protected EditText titleText;
    protected EditText contentText;
    protected TextView timeText;
    private FloatingActionButton fab;
    private View toolbar;
    private View viewBottomBar;
    private boolean lightTheme;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat readableDateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
    private float radius;
    private int cx;
    private int cy;
    private int id = -1;
    private int archived = 0;
    private int notified = 0;
    private int encrypted = 0;
    private int pinned = 0;
    private int tag = 0;
    private int checklist = 0;
    private int deleted = 0;
    private String title;
    private String subtitle;
    private String content;
    private String time;
    private String createdAt;
    private String color = Constants.COLOR_NONE;
    private String reminder = Constants.REMINDER_NONE;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        SharedPreferences pref = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        lightTheme = pref.getBoolean(Constants.LIGHT_THEME, false);
        if (lightTheme)
            setTheme(R.style.NoteLight);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        titleText = findViewById(R.id.title);
        contentText = findViewById(R.id.content);
        timeText = findViewById(R.id.note_date);
        fab = findViewById(R.id.fab_note);
        toolbar = findViewById(R.id.toolbar);
        viewBottomBar = findViewById(R.id.bottom_bar);

        dbHelper = new NotesDbHelper(this);

        Bundle bundle = getIntent().getBundleExtra(Constants.NOTE_DETAILS_BUNDLE);
        if (bundle != null) {
            editMode = false;
            id = bundle.getInt(NotesDb.Note._ID);
            title = bundle.getString(NotesDb.Note.COLUMN_NAME_TITLE);
            subtitle = bundle.getString(NotesDb.Note.COLUMN_NAME_SUBTITLE);
            content = bundle.getString(NotesDb.Note.COLUMN_NAME_CONTENT);
            time = bundle.getString(NotesDb.Note.COLUMN_NAME_TIME);
            createdAt = bundle.getString(NotesDb.Note.COLUMN_NAME_CREATED_AT);
            archived = bundle.getInt(NotesDb.Note.COLUMN_NAME_ARCHIVED);
            notified = bundle.getInt(NotesDb.Note.COLUMN_NAME_NOTIFIED);
            color = bundle.getString(NotesDb.Note.COLUMN_NAME_COLOR);
            encrypted = bundle.getInt(NotesDb.Note.COLUMN_NAME_ENCRYPTED);
            pinned = bundle.getInt(NotesDb.Note.COLUMN_NAME_PINNED);
            tag = bundle.getInt(NotesDb.Note.COLUMN_NAME_TAG);
            reminder = bundle.getString(NotesDb.Note.COLUMN_NAME_REMINDER);
            checklist = bundle.getInt(NotesDb.Note.COLUMN_NAME_CHECKLIST);
            deleted = bundle.getInt(NotesDb.Note.COLUMN_NAME_DELETED);
            if (!reminder.equals(Constants.REMINDER_NONE)) {
                try {
                    Calendar c = Calendar.getInstance();
                    c.setTime(readableDateFormat.parse(reminder));
                    if (c.before(new Date(System.currentTimeMillis()))) {
                        reminder = Constants.REMINDER_NONE;
                        dbHelper.updateFlag(id, NotesDb.Note.COLUMN_NAME_REMINDER, reminder);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } else {
            editMode = true;
        }

        if (savedInstanceState != null) {
            editMode = savedInstanceState.getBoolean("editMode");
            id = savedInstanceState.getInt(NotesDb.Note._ID);
            title = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_TITLE);
            subtitle = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_SUBTITLE);
            content = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_CONTENT);
            time = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_TIME);
            createdAt = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_CREATED_AT);
            archived = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_ARCHIVED);
            notified = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_NOTIFIED);
            color = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_COLOR);
            encrypted = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_ENCRYPTED);
            pinned = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_PINNED);
            tag = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_TAG);
            reminder = savedInstanceState.getString(NotesDb.Note.COLUMN_NAME_REMINDER);
            checklist = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_CHECKLIST);
            deleted = savedInstanceState.getInt(NotesDb.Note.COLUMN_NAME_DELETED);
            viewBottomBar.setVisibility(View.VISIBLE);
            if (!reminder.equals(Constants.REMINDER_NONE)) {
                try {
                    Calendar c = Calendar.getInstance();
                    c.setTime(readableDateFormat.parse(reminder));
                    if (c.before(new Date(System.currentTimeMillis()))) {
                        reminder = Constants.REMINDER_NONE;
                        dbHelper.updateFlag(id, NotesDb.Note.COLUMN_NAME_REMINDER, reminder);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (bundle == null && savedInstanceState == null) {
            Calendar c = Calendar.getInstance();
            //get date and time, specifically in 24-hr format suitable for sorting
            createdAt = sdf.format(c.getTime());
        }

        if (!editMode) {
            if (TextUtils.isEmpty(title))
                titleText.setVisibility(View.GONE);
            else {
                titleText.setText(title);
                titleText.setEnabled(false);
                if (lightTheme)
                    titleText.setTextColor(getResources().getColor(R.color.black));
                else titleText.setTextColor(getResources().getColor(R.color.white));
            }

            contentText.setText(content);
            edit(contentText, false);
            if (lightTheme)
                contentText.setTextColor(getResources().getColor(R.color.black));
            else contentText.setTextColor(getResources().getColor(R.color.white));

            timeText.setText(time);

            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mode_edit_white_24dp));
        } else {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setupWindowAnimations();
            }
            viewBottomBar.setVisibility(View.GONE);
            timeText.setText("");
        }


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toolbar.setVisibility(View.VISIBLE);
                if (savedInstanceState == null && !editMode) revealToolbar();
            }
        }, 350);

        Log.d("Note:", "id: " + id + " createdAt: " + createdAt);
    }

    @Override
    protected void onResume() {
        View deleteBtn = findViewById(R.id.delete);
        if (!editMode) {
            deleteBtn.setVisibility(View.VISIBLE);
        } else deleteBtn.setVisibility(View.GONE);
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putBoolean("editMode", editMode);
        bundle.putInt(NotesDb.Note._ID, id);
        bundle.putString(NotesDb.Note.COLUMN_NAME_TITLE, title);
        bundle.putString(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
        bundle.putString(NotesDb.Note.COLUMN_NAME_CONTENT, content);
        bundle.putString(NotesDb.Note.COLUMN_NAME_CONTENT, time);
        bundle.putString(NotesDb.Note.COLUMN_NAME_CREATED_AT, createdAt);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_NOTIFIED, notified);
        bundle.putString(NotesDb.Note.COLUMN_NAME_COLOR, color);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_ENCRYPTED, encrypted);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_PINNED, pinned);
        bundle.putString(NotesDb.Note.COLUMN_NAME_REMINDER, reminder);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_CHECKLIST, checklist);
        bundle.putInt(NotesDb.Note.COLUMN_NAME_DELETED, deleted);

        super.onSaveInstanceState(bundle);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupWindowAnimations() {
        Slide slide = new Slide(Gravity.TOP);
        slide.addTarget(R.id.note_card);
        slide.addTarget(R.id.note_title);
        slide.addTarget(R.id.note_subtitle);
        slide.addTarget(R.id.note_content);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
        getWindow().setReenterTransition(slide);
    }

    private void revealToolbar() {
        if (Build.VERSION.SDK_INT >= 21) {
            cx = viewBottomBar.getWidth() / 2;
            cy = viewBottomBar.getHeight() / 2;
            radius = (float) Math.hypot(cx, cy);

            // create the animator for this view (the start radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(viewBottomBar, cx, cy, 0, radius);

            // make the view visible and start the animation
            viewBottomBar.setVisibility(View.VISIBLE);
            anim.start();
        } else viewBottomBar.setVisibility(View.VISIBLE);
    }

    private void hideToolbar() {
        if (Build.VERSION.SDK_INT >= 21) {
            cx = viewBottomBar.getWidth() / 2;
            cy = viewBottomBar.getHeight() / 2;
            radius = (float) Math.hypot(cx, cy);

            // create the animation (the final radius is zero)
            Animator anim = ViewAnimationUtils.createCircularReveal(viewBottomBar, cx, cy, radius, 0);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    viewBottomBar.setVisibility(View.INVISIBLE);
                }
            });

            // start the animation
            anim.start();
        } else viewBottomBar.setVisibility(View.INVISIBLE);
    }

    public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (editMode) {
            title = titleText.getText().toString().trim();
            content = contentText.getText().toString().trim();
            if (content.equals("")) {
                contentText.requestFocus();
                Snackbar.make(coordinatorLayout, R.string.incomplete, Snackbar.LENGTH_LONG).show();
            } else {
                Calendar c = Calendar.getInstance();
                //get date and time, specifically in 24-hr format suitable for sorting
                time = sdf.format(c.getTime());
                Log.d("TIME", time);
                notif(0);
                id = dbHelper.addOrUpdateNote(id, title, subtitle, content, time, createdAt, archived, notified, color, encrypted, pinned, tag, reminder, checklist, deleted);
                editMode = false;
                if (!title.equals("")) {
                    titleText.setEnabled(false);
                    if (lightTheme)
                        titleText.setTextColor(getResources().getColor(R.color.black));
                    else titleText.setTextColor(getResources().getColor(R.color.white));
                } else titleText.setVisibility(View.GONE);
                edit(contentText, false);
                if (lightTheme)
                    contentText.setTextColor(getResources().getColor(R.color.black));
                else contentText.setTextColor(getResources().getColor(R.color.white));

                fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_mode_edit_white_24dp));

                revealToolbar();
                findViewById(R.id.note_update).setVisibility(View.VISIBLE);
                timeText.setText(time);
                editMode = false;

                notif(notified);

                // Hide the keyboard
                imm.hideSoftInputFromWindow(contentText.getWindowToken(), 0);
            }
            onListChanged();
        } else {
            titleText.setEnabled(true);
            edit(contentText, true);
            contentText.setSelection(contentText.getText().length());
            titleText.setVisibility(View.VISIBLE);
            fab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_done_white_24dp));
            hideToolbar();
            findViewById(R.id.note_update).setVisibility(View.GONE);
            timeText.setText("");
            editMode = true;
            contentText.requestFocusFromTouch();
            // Show the keyboard
            imm.showSoftInput(contentText, InputMethodManager.SHOW_IMPLICIT);
        }
        onResume();

    }

    private void edit(EditText editText, boolean enabled) {
        if (!enabled) {
            Linkify.addLinks(editText, Linkify.ALL);
            editText.setMovementMethod(LinkMovementMethod.getInstance());
        } else {
            editText.setText(editText.getText().toString());
            editText.setMovementMethod(ArrowKeyMovementMethod.getInstance());
        }
        editText.setFocusable(enabled);
        editText.setFocusableInTouchMode(enabled);
        editText.setClickable(enabled);
        editText.setLongClickable(enabled);
        editText.setLinksClickable(!enabled);
    }

    public void notif(int notified) {
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notified == 0) {
            mNotifyMgr.cancel(id);
        } else {
            String info;
            if (!subtitle.equals("")) info = subtitle;
            else info = time;
            NotificationCompat.Builder notif =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentText(content)
                            .setSubText(info)
                            .setShowWhen(false)
                            .setCategory(getString(R.string.notes))
                            .setColor(Color.argb(255, 32, 128, 200));

            if (!TextUtils.isEmpty(title)) {
                notif.setContentTitle(title);
            } else {
                notif.setContentTitle(getString(R.string.note));
            }

            notif.setStyle(new NotificationCompat.BigTextStyle().bigText(content).setSummaryText(time));
            // Sets an ID for the notification
            Log.d("NOTIFICATION ID", String.valueOf(id));
            Intent resultIntent = new Intent(this, NoteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(NotesDb.Note._ID, id);
            bundle.putString(NotesDb.Note.COLUMN_NAME_TITLE, title);
            bundle.putString(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
            bundle.putString(NotesDb.Note.COLUMN_NAME_CONTENT, content);
            bundle.putString(NotesDb.Note.COLUMN_NAME_TIME, time);
            bundle.putString(NotesDb.Note.COLUMN_NAME_CREATED_AT, createdAt);
            bundle.putInt(NotesDb.Note.COLUMN_NAME_NOTIFIED, notified);
            bundle.putInt(NotesDb.Note.COLUMN_NAME_ARCHIVED, archived);
            bundle.putString(NotesDb.Note.COLUMN_NAME_COLOR, color);
            bundle.putInt(NotesDb.Note.COLUMN_NAME_ENCRYPTED, encrypted);
            bundle.putInt(NotesDb.Note.COLUMN_NAME_PINNED, pinned);
            bundle.putInt(NotesDb.Note.COLUMN_NAME_TAG, tag);
            bundle.putString(NotesDb.Note.COLUMN_NAME_REMINDER, reminder);
            bundle.putInt(NotesDb.Note.COLUMN_NAME_CHECKLIST, checklist);
            bundle.putInt(NotesDb.Note.COLUMN_NAME_DELETED, deleted);
            resultIntent.putExtra(Constants.NOTE_DETAILS_BUNDLE, bundle);
            resultIntent.setAction("ACTION_NOTE_" + id);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(NoteActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            // Gets a PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

            notif.setContentIntent(resultPendingIntent);
            notif.setOngoing(true);
            notif.setChannelId(Constants.CHANNEL_ID_NOTE);

            Log.d("Note:", "id: " + id + " createdAt: " + createdAt);
            // Builds the notification and issues it.
            mNotifyMgr.notify(id, notif.build());

        }
    }

    public void close(View v) {
        onBackPressed();
    }

    public void delete(View v) {
        notif(0);
        dbHelper.updateFlag(id, NotesDb.Note.COLUMN_NAME_ARCHIVED, 0);
        dbHelper.updateFlag(id, NotesDb.Note.COLUMN_NAME_PINNED, 0);
        dbHelper.updateFlag(id, NotesDb.Note.COLUMN_NAME_NOTIFIED, 0);
        dbHelper.updateFlag(id, NotesDb.Note.COLUMN_NAME_REMINDER, Constants.REMINDER_NONE);

        if (deleted == 1) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.permanently_delete)
                    .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dbHelper.deleteNote(createdAt);
                            onListChanged();
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else {
            Toast.makeText(this, R.string.moved_to_trash, Toast.LENGTH_SHORT).show();
            dbHelper.updateFlag(id, NotesDb.Note.COLUMN_NAME_DELETED, 1);
            onListChanged();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (editMode) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_discard)
                    .setPositiveButton(R.string.save_exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            NoteActivity.this.onClick(fab);
                            exit();
                        }
                    })
                    .setNeutralButton(R.string.cancel, null)
                    .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            exit();
                        }
                    })
                    .show();
        } else {
            exit();
        }
    }

    private void exit() {
        hideToolbar();
        if (titleText.getText().toString().isEmpty()) titleText.setVisibility(View.INVISIBLE);
        toolbar.setVisibility(View.INVISIBLE);
        super.onBackPressed();
    }

    private void onListChanged() {
        Intent intent = new Intent("note-list-changed");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}