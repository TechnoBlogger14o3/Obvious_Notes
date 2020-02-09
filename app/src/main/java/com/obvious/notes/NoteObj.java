package com.obvious.notes;

import java.io.Serializable;

public class NoteObj implements Serializable {
    private int id;
    private String title;
    private String subtitle;
    private String content;
    private String time;
    private String color;
    private int tag;
    private int deleted;
    private String createdAt;
    private int archived;
    private int notified;
    private int encrypted;
    private int pinned;
    private String reminder;
    private int checklist;

    public NoteObj(int id, String title, String subtitle, String content, String time,
                   String createdAt, int archived, int notified, String color, int encrypted,
                   int pinned, int tag, String reminder, int checklist, int deleted) {
        this.id = id;
        this.title = title;
        this.subtitle = subtitle;
        this.content = content;
        this.time = time;
        this.createdAt = createdAt;
        this.archived = archived;
        this.notified = notified;
        this.color = color;
        this.encrypted = encrypted;
        this.pinned = pinned;
        this.tag = tag;
        this.reminder = reminder;
        this.checklist = checklist;
        this.deleted = deleted;
    }

    public int getArchived() {
        return archived;
    }

    public int getNotified() {
        return notified;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getEncrypted() {
        return encrypted;
    }

    public int getPinned() {
        return pinned;
    }

    public int getTag() {
        return tag;
    }

    public String getReminder() {
        return reminder;
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public int getChecklist() {
        return checklist;
    }

    public int getDeleted() {
        return deleted;
    }

}
