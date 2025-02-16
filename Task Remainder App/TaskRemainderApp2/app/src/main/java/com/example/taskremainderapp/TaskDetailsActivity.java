package com.example.taskremainderapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;

public class TaskDetailsActivity extends AppCompatActivity {

    private static final String TAG = "TaskDetailsActivity";
    private FirebaseFirestore db;
    private String taskId;
    private boolean isHighPriority;
    private LinearProgressIndicator progressIndicator;
    private MaterialCheckBox checkboxComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_details);

        db = FirebaseFirestore.getInstance();

        // Get task details from intent
        String title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String date = getIntent().getStringExtra("date");
        isHighPriority = getIntent().getBooleanExtra("isHighPriority", false);
        taskId = getIntent().getStringExtra("taskId");
        boolean completed = getIntent().getBooleanExtra("completed", false);

        // Initialize views
        TextView tvTitle = findViewById(R.id.tvTitle);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvDate = findViewById(R.id.tvDate);
        Chip chipPriority = findViewById(R.id.chipPriority);
        checkboxComplete = findViewById(R.id.checkboxComplete);
        progressIndicator = findViewById(R.id.progressIndicator);
        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnEdit = findViewById(R.id.btnEdit);
        ImageButton btnDelete = findViewById(R.id.btnDelete);

        // Set up views
        tvTitle.setText(title);
        tvDescription.setText(description);
        tvDate.setText(date);

        // Configure priority chip
        chipPriority.setText(isHighPriority ? "High Priority" : "Normal Priority");
        chipPriority.setChipBackgroundColorResource(
                isHighPriority ? R.color.priority_high : R.color.priority_normal
        );
        chipPriority.setTextColor(getColor(R.color.white));

        // Set up progress indicator
        progressIndicator.setIndicatorColor(
                getColor(isHighPriority ? R.color.priority_high : R.color.priority_normal)
        );

        // Initialize checkbox and progress
        checkboxComplete.setChecked(completed);
        updateProgressIndicator(completed);

        // Set up click listeners
        btnBack.setOnClickListener(v -> finish());

        btnEdit.setOnClickListener(v -> {
            Toast.makeText(this, "Edit functionality coming soon", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> showDeleteConfirmation());

        checkboxComplete.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTaskProgress(isChecked);
        });

        // Log initial state
        Log.d(TAG, "Initial state - Completed: " + completed);
    }

    private void updateProgressIndicator(boolean completed) {
        progressIndicator.setProgress(completed ? 100 : 0);
        Log.d(TAG, "Progress updated: " + (completed ? 100 : 0));
    }

    private void updateTaskProgress(boolean isComplete) {
        if (taskId != null) {
            // Update UI immediately for better responsiveness
            updateProgressIndicator(isComplete);

            // Update Firestore
            db.collection("tasks").document(taskId)
                    .update("completed", isComplete)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Firestore updated successfully. Completed: " + isComplete);
                        Toast.makeText(this,
                                isComplete ? "Task completed!" : "Task marked incomplete",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating Firestore", e);
                        // Revert UI changes on failure
                        updateProgressIndicator(!isComplete);
                        checkboxComplete.setChecked(!isComplete);
                        Toast.makeText(this, "Error updating task status", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "TaskId is null, cannot update Firestore");
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTask())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTask() {
        if (taskId != null) {
            db.collection("tasks").document(taskId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Task deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error deleting task", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}

