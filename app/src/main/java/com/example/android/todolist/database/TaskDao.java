package com.example.android.todolist.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Greta Grigutė on 2018-07-31.
 */
@Dao
public interface TaskDao {
    @Query("SELECT * FROM task ORDER BY priority")
    LiveData<List<TaskEntry>> loadAllTasks();

    @Insert
    void insertTask(TaskEntry taskEntry);

    @Update (onConflict = OnConflictStrategy.REPLACE)
    void updateTask(TaskEntry taskEntry);

    @Delete
    void deleteTask (TaskEntry taskEntry);

    @Query("SELECT * FROM task WHERE id = :id")// if we make simple type mistake( ex. tasks) here
        // Room validates this at compile time it means we spot the mistake before app launches
        // to the device. Finding issue at compile time will save us a lot of time, instead of
    // finding issue at runtime.
    LiveData <TaskEntry> loadTaskById (int id); // our new method will return a TaskEntry object, it will take
    // an integer value called id as a parameter, this method is meant to retrieve a task fro our DB
}
