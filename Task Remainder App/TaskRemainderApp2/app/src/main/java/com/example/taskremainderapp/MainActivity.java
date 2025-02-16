package com.example.taskremainderapp;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AnticipateInterpolator;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private NotificationHelper notificationHelper;
    private FirebaseFirestore db;
    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private FloatingActionButton fabAddTask;
    private BottomNavigationView bottomNavigation;
    private ImageButton menuButton;
    private ImageButton settingsButton;
    private boolean isDataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Handle the splash screen transition
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);



        setContentView(R.layout.activity_main);

        // Keep the splash screen visible for this Activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setKeepOnScreenCondition(() -> !isDataLoaded);
        }

        notificationHelper = new NotificationHelper(this);
        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerView();
        setupListeners();
        loadTasks();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }

        // Use a ViewTreeObserver to detect when the layout is drawn
        final View content = findViewById(android.R.id.content);
        content.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        // Check if the initial data is ready.
                        if (isDataLoaded) {
                            // The content is ready; start drawing.
                            content.getViewTreeObserver().removeOnPreDrawListener(this);
                            return true;
                        } else {
                            // The content is not ready; suspend.
                            return false;
                        }
                    }
                });
    }

    private void initializeViews() {
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        fabAddTask = findViewById(R.id.fabAddTask);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        menuButton = findViewById(R.id.menuButton);
        settingsButton = findViewById(R.id.settingsButton);
    }

    private void setupRecyclerView() {
        taskAdapter = new TaskAdapter(new ArrayList<>());
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void setupListeners() {
        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTaskActivity.class);
            startActivity(intent);
        });

        bottomNavigation.setOnItemSelectedListener(item -> {
            // TODO: Handle navigation
            return true;
        });

        menuButton.setOnClickListener(v -> {
            // TODO: Handle menu button click
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show();
        });

        settingsButton.setOnClickListener(v -> openNotificationSettings());
    }

    private void loadTasks() {
        db.collection("tasks")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task> taskList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String id = document.getId();
                            String title = document.getString("title");
                            String description = document.getString("description");
                            boolean highPriority = Boolean.TRUE.equals(document.getBoolean("highPriority"));
                            boolean alert = Boolean.TRUE.equals(document.getBoolean("alert"));
                            Date createdAt = document.getDate("createdAt");
                            boolean completed = Boolean.TRUE.equals(document.getBoolean("completed"));

                            if (createdAt != null) {
                                Task taskItem = new Task(id, title, description, highPriority, alert, createdAt, completed);
                                taskList.add(taskItem);
                            } else {
                                System.out.println("Task with missing date: " + title);
                            }
                        }
                        taskAdapter.setTasks(taskList);
                        isDataLoaded = true;
                    } else {
                        Toast.makeText(MainActivity.this, "Error loading tasks", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openNotificationSettings() {
        Intent intent = new Intent(this, NotificationSettingsActivity.class);
        startActivity(intent);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        PERMISSION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasks(); // Refresh the task list when returning to the activity
    }
}

