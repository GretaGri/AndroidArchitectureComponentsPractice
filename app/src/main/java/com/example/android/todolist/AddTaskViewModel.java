package com.example.android.todolist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.todolist.database.AppDataBase;
import com.example.android.todolist.database.TaskEntry;

/**
 * Created by Greta Grigutė on 2018-08-02.
 */
public class AddTaskViewModel extends ViewModel {
    private LiveData <TaskEntry> task;

    public AddTaskViewModel(AppDataBase dataBase, int taskId){
        task = dataBase.taskDao().loadTaskById(taskId);
    }

    public LiveData <TaskEntry> getTask(){
        return task;}
}
