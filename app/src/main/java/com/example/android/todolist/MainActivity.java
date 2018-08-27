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
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import com.example.android.todolist.database.AppDataBase;
import com.example.android.todolist.database.TaskEntry;

import java.util.List;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;


public class MainActivity extends AppCompatActivity implements TaskAdapter.ItemClickListener {

    // Constant for logging
    private static final String TAG = MainActivity.class.getSimpleName();
    // Member variables for the adapter and RecyclerView
    private RecyclerView mRecyclerView;
    private TaskAdapter mAdapter;

    //add member variable for the data base
    private AppDataBase mDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set the RecyclerView to its corresponding view
        mRecyclerView = findViewById(R.id.recyclerViewTasks);

        // Set the layout for the RecyclerView to be a linear layout, which measures and
        // positions items within a RecyclerView into a linear list
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter and attach it to the RecyclerView
        mAdapter = new TaskAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);

        DividerItemDecoration decoration = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        mRecyclerView.addItemDecoration(decoration);

        /*
         Add a touch helper to the RecyclerView to recognize when a user swipes to delete an item.
         An ItemTouchHelper enables touch behavior (like swipe and move) on each ViewHolder,
         and uses callbacks to signal when a user is performing these actions.
         */
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                // Here is where you'll implement swipe to delete
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        int position = viewHolder.getAdapterPosition();
                        List<TaskEntry> tasks = mAdapter.getTasks();
                        mDb.taskDao().deleteTask(tasks.get(position)); // at this point we have deleted
                        // swiped task from our DB, but we need to update UI too.
                       // retrieveTasks(); // updates the UI. Do not need this after adding LiveData
                    }
                });
            }
        }).attachToRecyclerView(mRecyclerView);

        /*
         Set the Floating Action Button (FAB) to its corresponding View.
         Attach an OnClickListener to it, so that when it's clicked, a new intent will be created
         to launch the AddTaskActivity.
         */
        FloatingActionButton fabButton = findViewById(R.id.fab);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a new intent to start an AddTaskActivity
                Intent addTaskIntent = new Intent(MainActivity.this, AddTaskActivity.class);
                startActivity(addTaskIntent);
            }
        });

        //initialize DB
        mDb = AppDataBase.getInstance(getApplicationContext());
        //we could query our DB in OnCreate, but then our list won't be refreshed until our activity
        //is recreated, the alternative ir query the DB in onResume
        retrieveTasks(); // as LiveData does some work for us.
    }

    //This method is called whenever our activity has been paused or restarted.
    // This re-queries the DB data for any changes. Changed to using this call in onCreate
    // when Live Data is used.

    //@Override
   // protected void onResume() {
    //    super.onResume();
     //   retrieveTasks(); // select the repetitive method and choose refactor -> Extract -> Method
   // }

    //renamed method after adding view model (before it "retrieveTasks")
    private void retrieveTasks() {
      //  Log.d(TAG, "Actively retrieving the tasks from the Database"); - removed after view model implementation.
        //calling loadAllTasks method from our taskDao and in this way when we return to activity
        // and it is resumed we get updated list
        //the result in list is passed to the adapter using its setTasks method.

        //LiveData will run outside main thread and because of that we can avoid using the
        // executor.
     //   final LiveData<List<TaskEntry>> tasks = mDb.taskDao().loadAllTasks(); - removed after view model implementation.

        //To get a view model we need to call ViewModel providers of this activity and pass the
        // MainViewModel class.
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        //we can call on LiveData observer method, which requires two parameters:
        //a lifecycle owner (in our case it is activity) and Observer (we can just create a new one).
        //it was called on tasks, now we are retrieving tasks from our viewModel.
        viewModel.getTasks().observe(this, new Observer<List<TaskEntry>>() {
            @Override
            public void onChanged(@Nullable List<TaskEntry> taskEntries) {
                Log.d(TAG, "Updating list of tasks from LiveData in ViewModel");
                mAdapter.setTasks(taskEntries);
            }
        });
        // AppExecutors.getInstance().diskIO().execute(new Runnable() {
        //     @Override
        //     public void run() {

        //as this cannot be done from a thread in our disk OI executor, so we have to wrap
        // it up inside the runOnUiTread method call.
        // We will be able to simplify this one once we learn more about Android
        // architecture components.
        //  runOnUiThread(new Runnable() {
        //      @Override
        //       public void run() {
        //          mAdapter.setTasks(tasks);
        //      }
        // });

        //        }
        //   });
    }

    @Override
    public void onItemClickListener(int itemId) {
        // Launch AddTaskActivity adding the itemId as an extra in the intent
        Intent intent = new Intent(MainActivity.this, AddTaskActivity.class);
        intent.putExtra(AddTaskActivity.EXTRA_TASK_ID, itemId);
        startActivity(intent);
    }
}
