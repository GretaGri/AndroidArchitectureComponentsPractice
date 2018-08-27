package com.example.android.todolist.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

// TODO (2) Annotate the class with Entity. Use "task" for the table name
@Entity (tableName = "task")
public class TaskEntry {

    // TODO (3) Annotate the id as PrimaryKey. Set autoGenerate to true.
    //those are values that constructs the table
    //we need primary key and for this we will use int id, so just add @PrimaryKey annotation to it
    //with this the id field of each entry in our table will be unique
    //to auto generate the unique values we just add (autoGenerate = true)
    // and with that database will handle this for us
    @PrimaryKey (autoGenerate = true)
    private int id;
    private String description;
    private int priority;
    @ColumnInfo(name = "updated_at")
    private Date updatedAt;
    // if we want to add here something that is not inside the table, we need to use
    // annotation @Ignore

    // TODO (4) Use the Ignore annotation so Room knows that it has to use the other constructor instead
    //Room can only use one constructor so we need to add @Ignore annotation to the first constructor.
    @Ignore
    public TaskEntry(String description, int priority, Date updatedAt) {
        this.description = description;
        this.priority = priority;
        this.updatedAt = updatedAt;
    }

    public TaskEntry(int id, String description, int priority, Date updatedAt) {
        this.id = id;
        this.description = description;
        this.priority = priority;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
