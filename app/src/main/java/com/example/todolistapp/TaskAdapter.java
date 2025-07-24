package com.example.todolistapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private static final String TAG = "TaskAdapter";
    private final List<Task> tasks;
    private final OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(int position);
        void onEditClick(int position);
    }

    public TaskAdapter(@NonNull List<Task> tasks, @NonNull OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        try {
            Task task = tasks.get(position);
            Log.d(TAG, "Binding task - Priority: " + task.getPriority());
            holder.bind(task);
        } catch (Exception e) {
            Log.e(TAG, "Error binding task at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private final TextView taskText;
        private final TextView dueDateText;
        private final ImageButton editButton;
        private final ImageView priorityIndicator;
        private final Context context;

        TaskViewHolder(@NonNull View itemView, @NonNull OnTaskClickListener listener) {
            super(itemView);
            context = itemView.getContext();
            taskText = itemView.findViewById(R.id.taskTitle);
            dueDateText = itemView.findViewById(R.id.taskDetails);
            editButton = itemView.findViewById(R.id.editTaskButton);
            priorityIndicator = itemView.findViewById(R.id.priorityIndicator);

            itemView.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTaskClick(position);
                }
            });

            editButton.setOnClickListener(v -> {
                int position = getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(position);
                }
            });
        }

        void bind(@NonNull Task task) {
            // Set task text
            taskText.setText(task.getText());

            // Set priority indicator - FORCE VISIBLE with colors
            priorityIndicator.setVisibility(View.VISIBLE);
            switch (task.getPriority()) {
                case 1: // Low
                    priorityIndicator.setImageResource(R.drawable.ic_priority_low);
                    priorityIndicator.setColorFilter(ContextCompat.getColor(context, R.color.priority_low));
                    priorityIndicator.setContentDescription(context.getString(R.string.priority_low_desc));
                    break;
                case 2: // Medium
                    priorityIndicator.setImageResource(R.drawable.ic_priority_medium);
                    priorityIndicator.setColorFilter(ContextCompat.getColor(context, R.color.priority_medium));
                    priorityIndicator.setContentDescription(context.getString(R.string.priority_medium_desc));
                    break;
                case 3: // High
                    priorityIndicator.setImageResource(R.drawable.ic_priority_high);
                    priorityIndicator.setColorFilter(ContextCompat.getColor(context, R.color.priority_high));
                    priorityIndicator.setContentDescription(context.getString(R.string.priority_high_desc));
                    break;
                default: // None
                    priorityIndicator.setVisibility(View.GONE);
            }


            // Set due date
            if (task.getDueDate() > 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                dueDateText.setText(sdf.format(new Date(task.getDueDate())));
                dueDateText.setVisibility(View.VISIBLE);
            } else {
                dueDateText.setVisibility(View.GONE);
            }
        }
    }
}