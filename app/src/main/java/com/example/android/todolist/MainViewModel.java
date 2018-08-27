package com.example.android.todolist;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.todolist.database.AppDataBase;
import com.example.android.todolist.database.TaskEntry;

import java.util.List;

/**
 * Created by Greta Grigutė on 2018-08-02.
 */
public class MainViewModel extends AndroidViewModel {
    // constant for logging.
    private static final String TAG = MainViewModel.class.getSimpleName();

    // this variable private but we will have a public getter.
    private LiveData <List<TaskEntry>> tasks;

    public MainViewModel(@NonNull Application application) {
        super(application);// constructor that gets one parameter type application.
        AppDataBase dataBase = AppDataBase.getInstance(this.getApplication());
        Log.d (TAG, "Actively retrieving the tasks from Database");
        tasks = dataBase.taskDao().loadAllTasks();
    }

    public LiveData <List<TaskEntry>> getTasks(){
        return tasks;
    }
}
