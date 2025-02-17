package com.example.taskremainderapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationSettingsActivity extends AppCompatActivity {

    private NotificationHelper notificationHelper;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_settings);

        notificationHelper = new NotificationHelper(this);
        sharedPreferences = getSharedPreferences("NotificationSettings", MODE_PRIVATE);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        Switch switchVibration = findViewById(R.id.switchVibration);

        // Load the current vibration setting
        boolean vibrationEnabled = sharedPreferences.getBoolean("vibration_enabled", true);
        switchVibration.setChecked(vibrationEnabled);

        switchVibration.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the new vibration setting
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("vibration_enabled", isChecked);
            editor.apply();

            notificationHelper.updateNotificationSettings(isChecked);
        });
    }
}