package com.obvious.notes;

import java.io.Serializable;

public class NoteObj implements Serializable {
    private int id;

    public NoteObj(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
