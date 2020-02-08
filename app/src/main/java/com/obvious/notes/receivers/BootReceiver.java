package com.obvious.notes.receivers;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.obvious.notes.Constants;
import com.obvious.notes.NoteObj;
import com.obvious.notes.R;
import com.obvious.notes.data.NotesDb;
import com.obvious.notes.data.NotesDbHelper;
import com.obvious.notes.ui.activities.NoteActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.NOTIFICATION_SERVICE;


public class BootReceiver extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        ArrayList<NoteObj> list = new NotesDbHelper(context).getNotificationsAndReminders();
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= 26) {

            NotificationChannel mChannel = new NotificationChannel(Constants.CHANNEL_ID_DEFAULT, context.getString(R.string.channel_name_default), NotificationManager.IMPORTANCE_LOW);
            mChannel.setDescription(context.getString(R.string.channel_description_default));
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            mNotifyMgr.createNotificationChannel(mChannel);

            mChannel = new NotificationChannel(Constants.CHANNEL_ID_NOTE, context.getString(R.string.channel_name_notes), NotificationManager.IMPORTANCE_LOW);
            mChannel.setDescription(context.getString(R.string.channel_description_notes));
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            mNotifyMgr.createNotificationChannel(mChannel);

            mChannel = new NotificationChannel(Constants.CHANNEL_ID_REMINDER, context.getString(R.string.channel_name_reminders), NotificationManager.IMPORTANCE_HIGH);
            mChannel.setDescription(context.getString(R.string.channel_description_reminders));
            mNotifyMgr.createNotificationChannel(mChannel);

        }

        for (int i = 0; i < list.size(); i++) {
            NoteObj note = list.get(i);
            int id = note.getId();
            String title = note.getTitle();
            String subtitle = note.getSubtitle();
            String content = note.getContent();
            String time = note.getTime();
            // Sets an ID for the notification
            Log.d("ID", String.valueOf(id));
            Bundle bundle = new Bundle();
            bundle.putInt(NotesDb.Note._ID, id);
            bundle.putString(NotesDb.Note.COLUMN_NAME_TITLE, title);
            bundle.putString(NotesDb.Note.COLUMN_NAME_SUBTITLE, subtitle);
            bundle.putString(NotesDb.Note.COLUMN_NAME_CONTENT, content);
            bundle.putString(NotesDb.Note.COLUMN_NAME_TIME, time);
            bundle.putString(NotesDb.Note.COLUMN_NAME_CREATED_AT, note.getCreated_at());
            bundle.putInt(NotesDb.Note.COLUMN_NAME_NOTIFIED, note.getNotified());
            bundle.putInt(NotesDb.Note.COLUMN_NAME_ARCHIVED, note.getArchived());
            bundle.putString(NotesDb.Note.COLUMN_NAME_COLOR, note.getColor());
            bundle.putInt(NotesDb.Note.COLUMN_NAME_ENCRYPTED, note.getEncrypted());
            bundle.putInt(NotesDb.Note.COLUMN_NAME_PINNED, note.getPinned());
            bundle.putInt(NotesDb.Note.COLUMN_NAME_TAG, note.getTag());
            bundle.putString(NotesDb.Note.COLUMN_NAME_REMINDER, note.getReminder());
            bundle.putInt(NotesDb.Note.COLUMN_NAME_CHECKLIST, note.getChecklist());
            bundle.putInt(NotesDb.Note.COLUMN_NAME_DELETED, note.getDeleted());

            if (!note.getReminder().equals(Constants.REMINDER_NONE)) {
                Intent resultIntent = new Intent(context, AlarmReceiver.class);
                bundle.putString(NotesDb.Note.COLUMN_NAME_REMINDER, Constants.REMINDER_NONE);
                resultIntent.putExtra(Constants.NOTE_DETAILS_BUNDLE, bundle);

                SimpleDateFormat readableDateFormat = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
                try {
                    Calendar c = Calendar.getInstance();
                    c.setTime(readableDateFormat.parse(note.getReminder()));
                    Log.e(getClass().getName(), "Current time: " + System.currentTimeMillis());
                    if (c.compareTo(Calendar.getInstance()) < 0) {
                        Log.e(getClass().getName(), "Missed alarm at " + note.getReminder() + " for note id " + id);
                        String info = note.getReminder();
                        note.setReminder(Constants.REMINDER_NONE);
                        NotesDbHelper dbHelper = new NotesDbHelper(context);
                        dbHelper.updateFlag(id, NotesDb.Note.COLUMN_NAME_REMINDER, Constants.REMINDER_NONE);
                        resultIntent.setAction("REMINDER_NOTE_" + id);
                        NotificationCompat.Builder notif =
                                new NotificationCompat.Builder(context)
                                        .setSmallIcon(R.drawable.ic_stat_notepal)
                                        .setContentTitle(context.getString(R.string.missed_reminder))
                                        .setContentText(content)
                                        .setSubText(info)
                                        .setShowWhen(false)
                                        .setCategory(context.getString(R.string.notes))
                                        .setColor(Color.argb(255, 32, 128, 200));

                        notif.setStyle(new NotificationCompat.BigTextStyle().bigText(content).setSummaryText(time));

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                        stackBuilder.addParentStack(NoteActivity.class);
                        // Adds the Intent to the top of the stack
                        stackBuilder.addNextIntent(resultIntent);
                        // Gets a PendingIntent containing the entire back stack
                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

                        notif.setAutoCancel(true);
                        notif.setContentIntent(resultPendingIntent);
                        notif.setOngoing(false);
                        notif.setChannelId(Constants.CHANNEL_ID_REMINDER);

                        // Builds the notification and issues it.
                        mNotifyMgr.notify(id, notif.build());

                    } else {
                        Log.d(getClass().getName(), "Settings alarm at " + note.getReminder() + " for note id " + id);
                        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, id, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), alarmIntent);
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), alarmIntent);
                        } else {
                            alarmManager.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), alarmIntent);
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }

            if (note.getNotified() == 1) {
                String info;
                if (subtitle != null && !subtitle.equals("")) info = subtitle;
                else info = time;
                NotificationCompat.Builder notif =
                        new NotificationCompat.Builder(context)
                                .setSmallIcon(R.drawable.ic_stat_notepal)
                                .setContentText(content)
                                .setSubText(info)
                                .setShowWhen(false)
                                .setCategory(context.getString(R.string.notes))
                                .setColor(Color.argb(255, 32, 128, 200));

                if (!TextUtils.isEmpty(title)) {
                    notif.setContentTitle(title);
                } else {
                    notif.setContentTitle(context.getString(R.string.note));
                }

                notif.setStyle(new NotificationCompat.BigTextStyle().bigText(content).setSummaryText(time));

                Class c;
                    c = NoteActivity.class;
                Intent resultIntent = new Intent(context, c);
                bundle.putString(NotesDb.Note.COLUMN_NAME_REMINDER, note.getReminder());
                resultIntent.putExtra(Constants.NOTE_DETAILS_BUNDLE, bundle);
                resultIntent.setAction("ACTION_NOTE_" + id);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(NoteActivity.class);
                // Adds the Intent to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                // Gets a PendingIntent containing the entire back stack
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(id, PendingIntent.FLAG_UPDATE_CURRENT);

                notif.setContentIntent(resultPendingIntent);
                notif.setOngoing(true);
                notif.setChannelId(Constants.CHANNEL_ID_NOTE);

                // Builds the notification and issues it.
                mNotifyMgr.notify(id, notif.build());
            }
        }

        if (context.getSharedPreferences(Constants.PREFS, MODE_PRIVATE).getBoolean(Constants.QUICK_NOTIFY, false)) {
            NotificationCompat.Builder notif =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_stat_notepal)
                            .setContentTitle(context.getString(R.string.new_note))
                            .setContentText(context.getString(R.string.tap_create_note))
                            .setShowWhen(false)
                            .setGroup(Constants.QUICK_NOTIFY)
                            .setPriority(NotificationCompat.PRIORITY_MIN)
                            .setColor(Color.argb(255, 32, 128, 200));
            Intent resultIntent = new Intent(context, NoteActivity.class);
            resultIntent.setAction("ACTION_NOTE_" + 0);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(NoteActivity.class);
            // Adds the Intent to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            // Gets a PendingIntent containing the entire back stack
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            notif.addAction(R.drawable.ic_note_white_24dp, context.getString(R.string.new_note), resultPendingIntent);


            notif.setContentIntent(resultPendingIntent);
            notif.setOngoing(true);
            notif.setChannelId(Constants.CHANNEL_ID_DEFAULT);


            // Builds the notification and issues it.
            mNotifyMgr.notify(0, notif.build());
        }
    }
}