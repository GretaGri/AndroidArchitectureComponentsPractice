/*
* Copyright (C) 2016 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.todolist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.example.android.todolist.database.AppDataBase;
import com.example.android.todolist.database.TaskEntry;

import java.util.Date;


public class AddTaskActivity extends AppCompatActivity {

    // Extra for the task ID to be received in the intent
    public static final String EXTRA_TASK_ID = "extraTaskId";
    // Extra for the task ID to be received after rotation
    public static final String INSTANCE_TASK_ID = "instanceTaskId";
    // Constants for priority
    public static final int PRIORITY_HIGH = 1;
    public static final int PRIORITY_MEDIUM = 2;
    public static final int PRIORITY_LOW = 3;
    // Constant for default task id to be used when not in update mode
    private static final int DEFAULT_TASK_ID = -1;
    // Constant for logging
    private static final String TAG = AddTaskActivity.class.getSimpleName();
    // Fields for views
    EditText mEditText;
    RadioGroup mRadioGroup;
    Button mButton;

    private int mTaskId = DEFAULT_TASK_ID;

    //Member variable for the data base
    private AppDataBase mDb;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initViews();

        //Initialize our database instance like this:
        mDb = AppDataBase.getInstance(getApplicationContext());

        if (savedInstanceState != null && savedInstanceState.containsKey(INSTANCE_TASK_ID)) {
            mTaskId = savedInstanceState.getInt(INSTANCE_TASK_ID, DEFAULT_TASK_ID);
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_TASK_ID)) {
            mButton.setText(R.string.update_button);
            if (mTaskId == DEFAULT_TASK_ID) {
                // populate the UI
                mTaskId = getIntent().getIntExtra(EXTRA_TASK_ID, DEFAULT_TASK_ID);
               // final LiveData <TaskEntry> task = mDb.taskDao().loadTaskById(mTaskId); - we do not
                // need it here anymore, as the call to loadTaskBy Id is done in the ViewModel.
                //as this cannot be done from a thread in our disk OI executor, so we have to wrap
                // it up inside the runOnUiTread method call.
                // We will be able to simplify this one once we learn more about Android
                // architecture components.
                AddTaskViewModelFactory factory = new AddTaskViewModelFactory(mDb,mTaskId);
                final AddTaskViewModel viewModel = ViewModelProviders.of(this, factory).get(AddTaskViewModel.class);
                viewModel.getTask().observe(this, new Observer<TaskEntry>() {
                    @Override
                    public void onChanged(@Nullable TaskEntry taskEntry) {

                        viewModel.getTask().removeObserver(this); //we remove observer, because in this case we do
                        // not want to receive updates. Usually, we do not need to remove the
                        // observer as we want LiveData to reflect the state of the underlying data.
                        // In our case we are doing a one-time load, and we don’t want to listen to
                        // changes in the database.
                        Log.d(TAG, "Receiving Database update from LiveData");
                        populateUI(taskEntry);
                    }
                });

                /** Removed as LiveData is used here
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                   final LiveData <TaskEntry> task = mDb.taskDao().loadTaskById(mTaskId);
                    //as this cannot be done from a thread in our disk OI executor, so we have to wrap
                    // it up inside the runOnUiTread method call.
                    // We will be able to simplify this one once we learn more about Android
                    // architecture components.
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           populateUI(task);
                       }
                   });

                }
            });**/
            }
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(INSTANCE_TASK_ID, mTaskId);
        super.onSaveInstanceState(outState);
    }

    /**
     * initViews is called from onCreate to init the member variable views
     */
    private void initViews() {
        mEditText = findViewById(R.id.editTextTaskDescription);
        mRadioGroup = findViewById(R.id.radioGroup);

        mButton = findViewById(R.id.saveButton);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSaveButtonClicked();
            }
        });
    }

    /**
     * populateUI would be called to populate the UI when in update mode
     *
     * @param task the taskEntry to populate the UI
     */
    private void populateUI(TaskEntry task) {
    if (task == null){
        return;
    }
    mEditText.setText(task.getDescription()); // set the description text in edit text
    setPriorityInViews(task.getPriority()); // set the priority using setPriorityInViews method.
    }



    /**
     * onSaveButtonClicked is called when the "save" button is clicked.
     * It retrieves user input and inserts that new task data into the underlying database.
     */
    public void onSaveButtonClicked() {
       String description = mEditText.getText().toString();
       int priority = getPriorityFromViews();
       Date date = new Date();

       //with this we can now call the constructor of our TaskEntry class
       final TaskEntry taskEntry = new TaskEntry(description, priority, date);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                if (mTaskId == DEFAULT_TASK_ID){
                //retrieve the task dao from our Database instance
                //and call insertTask method using the taskEntry object that we just created
                //this will add our record witht he given data to our DB
                mDb.taskDao().insertTask(taskEntry);}
                else{
                    // update task
                    taskEntry.setId(mTaskId);
                    mDb.taskDao().updateTask(taskEntry);
                }

                //to return to our list once the task is saved.
                finish();
            }
        });

    }

    /**
     * getPriority is called whenever the selected priority needs to be retrieved
     */
    public int getPriorityFromViews() {
        int priority = 1;
        int checkedId = ((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId();
        switch (checkedId) {
            case R.id.radButton1:
                priority = PRIORITY_HIGH;
                break;
            case R.id.radButton2:
                priority = PRIORITY_MEDIUM;
                break;
            case R.id.radButton3:
                priority = PRIORITY_LOW;
        }
        return priority;
    }

    /**
     * setPriority is called when we receive a task from MainActivity
     *
     * @param priority the priority value
     */
    public void setPriorityInViews(int priority) {
        switch (priority) {
            case PRIORITY_HIGH:
                ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButton1);
                break;
            case PRIORITY_MEDIUM:
                ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButton2);
                break;
            case PRIORITY_LOW:
                ((RadioGroup) findViewById(R.id.radioGroup)).check(R.id.radButton3);
        }
    }
}
