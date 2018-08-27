package com.example.android.todolist;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;

import com.example.android.todolist.database.AppDataBase;

/**
 * Created by Greta Grigutė on 2018-08-02.
 */
public class AddTaskViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDataBase mDb;
    private final int mTaskId;

    public AddTaskViewModelFactory (AppDataBase dataBase, int taskId){
        mDb = dataBase;
        mTaskId = taskId;
    }
    @Override
    public <T extends ViewModel> T create(Class <T> modelClass){
    //noinspection unchecked
       return (T) new AddTaskViewModel(mDb,mTaskId);

    }
}
