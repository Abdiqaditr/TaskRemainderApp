package com.example.taskremainderapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AddTaskActivity extends AppCompatActivity {

    private TextInputEditText etTaskTitle;
    private TextInputEditText etTaskDescription;
    private RadioGroup rgPriority;
    private SwitchMaterial switchAlert;
    private MaterialButton btnCreateTask;
    private ImageButton menuButton;
    private ImageButton btnSettings;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        rgPriority = findViewById(R.id.rgPriority);
        switchAlert = findViewById(R.id.switchAlert);
        btnCreateTask = findViewById(R.id.btnCreateTask);
        menuButton = findViewById(R.id.menuButton);
        btnSettings = findViewById(R.id.btnSettings);
    }

    private void setupListeners() {
        btnCreateTask.setOnClickListener(v -> createTask());
        menuButton.setOnClickListener(v -> finish());
        btnSettings.setOnClickListener(v -> openNotificationSettings());
    }

    private void createTask() {
        String title = etTaskTitle.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();
        boolean isHighPriority = rgPriority.getCheckedRadioButtonId() == R.id.rbHigh;
        boolean isAlertOn = switchAlert.isChecked();

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please enter both title and description", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> task = new HashMap<>();
        task.put("title", title);
        task.put("description", description);
        task.put("highPriority", isHighPriority);
        task.put("alert", isAlertOn);
        task.put("createdAt", new Date());
        task.put("completed", false); // Add this line

        db.collection("tasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddTaskActivity.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                    if (isAlertOn) {
                        sendNotification(title, description, isHighPriority, documentReference.getId());
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddTaskActivity.this, "Error adding task", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendNotification(String title, String description, boolean isHighPriority, String taskId) {
        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.sendNotification(title, description, isHighPriority, taskId);
    }

    private void openNotificationSettings() {
        Intent intent = new Intent(this, NotificationSettingsActivity.class);
        startActivity(intent);
    }
}

