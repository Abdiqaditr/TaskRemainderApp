package com.example.taskremainderapp;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private final SimpleDateFormat dateFormat;
    private final FirebaseFirestore db;

    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.bind(task);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), TaskDetailsActivity.class);
            intent.putExtra("title", task.getTitle());
            intent.putExtra("description", task.getDescription());
            intent.putExtra("date", task.getCreatedAt() != null ? dateFormat.format(task.getCreatedAt()) : "Date not available");
            intent.putExtra("isHighPriority", task.isHighPriority());
            intent.putExtra("taskId", task.getId());
            v.getContext().startActivity(intent);
        });

        holder.btnMore.setOnClickListener(v -> showPopupMenu(v, task, position));

        int backgroundColor = task.isHighPriority() ?
                R.color.priority_high : R.color.priority_normal;
        holder.iconBackground.setBackgroundTintList(
                ContextCompat.getColorStateList(holder.itemView.getContext(), backgroundColor)
        );
    }

    private void showPopupMenu(View view, Task task, int position) {
        PopupMenu popup = new PopupMenu(view.getContext(), view);
        popup.inflate(R.menu.task_menu);

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete) {
                deleteTask(task, position, view);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void deleteTask(Task task, int position, View view) {
        db.collection("tasks").document(task.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    tasks.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, tasks.size());
                    Toast.makeText(view.getContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(view.getContext(), "Error deleting task", Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskTitle;
        TextView taskDate;
        ImageView taskIcon;
        FrameLayout iconBackground;
        ImageButton btnMore;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDate = itemView.findViewById(R.id.taskDate);
            taskIcon = itemView.findViewById(R.id.taskIcon);
            iconBackground = itemView.findViewById(R.id.iconBackground);
            btnMore = itemView.findViewById(R.id.btnMore);
        }

        void bind(Task task) {
            taskTitle.setText(task.getTitle());
            Date createdAt = task.getCreatedAt();
            if (createdAt != null) {
                taskDate.setText(new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(createdAt));
            } else {
                taskDate.setText("Date not available");
            }
        }
    }
}