package com.example.todolistapp;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {
    private static final String TAG = "MainActivity";
    private static final String PREFS_NAME = "TodoPrefs";
    private static final String TASKS_KEY = "tasks";
    private static final String THEME_PREF = "themePref";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private EditText taskInput;
    private Button addButton, setDueDateButton;
    private RecyclerView recyclerView;
    private List<Task> tasks;
    private TaskAdapter adapter;
    private long selectedDueDate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setThemeBasedOnPref();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        initializeViews();
        setupRecyclerView();
        setupSwipeToDelete();
        setupButtonListeners();
    }

    private void setThemeBasedOnPref() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int themeMode = prefs.getInt(THEME_PREF, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.theme_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_theme) {
            Log.d(TAG, "Theme toggle button clicked"); // Add this line
            toggleTheme();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleTheme() {
        int currentMode = AppCompatDelegate.getDefaultNightMode();
        int newMode = currentMode == AppCompatDelegate.MODE_NIGHT_YES
                ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_YES;

        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt(THEME_PREF, newMode)
                .apply();

        AppCompatDelegate.setDefaultNightMode(newMode);
        recreate();
    }

    private void initializeViews() {
        taskInput = findViewById(R.id.editTextTask);
        addButton = findViewById(R.id.btnAdd);
        setDueDateButton = findViewById(R.id.btnSetDueDate);
        recyclerView = findViewById(R.id.recyclerViewTasks);
    }

    private void setupRecyclerView() {
        tasks = loadTasks();
        adapter = new TaskAdapter(tasks, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAbsoluteAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Task removedTask = tasks.remove(position);
                    adapter.notifyItemRemoved(position);
                    saveTasks();
                    showUndoSnackbar(removedTask, position);
                }
            }
        }).attachToRecyclerView(recyclerView);
    }

    private void showUndoSnackbar(Task removedTask, int position) {
        Snackbar.make(findViewById(android.R.id.content),
                        getString(R.string.task_deleted),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, v -> {
                    tasks.add(position, removedTask);
                    adapter.notifyItemInserted(position);
                    saveTasks();
                })
                .show();
    }

    private void setupButtonListeners() {
        addButton.setOnClickListener(v -> addTask());
        setDueDateButton.setOnClickListener(v -> showDatePicker(-1));
    }

    private void showDatePicker(int position) {
        Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, day) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, day);
            long timestamp = selectedDate.getTimeInMillis();

            if (position == -1) {
                selectedDueDate = timestamp;
                setDueDateButton.setText(DATE_FORMAT.format(selectedDate.getTime()));
                Toast.makeText(this, getString(R.string.set_due_date), Toast.LENGTH_SHORT).show();
            } else {
                tasks.get(position).setDueDate(timestamp);
                adapter.notifyItemChanged(position);
                saveTasks();
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void addTask() {
        String taskText = taskInput.getText().toString().trim();
        if (!TextUtils.isEmpty(taskText)) {
            Task newTask = new Task(taskText, selectedDueDate, 1);
            tasks.add(newTask);
            adapter.notifyItemInserted(tasks.size() - 1);
            taskInput.setText("");
            selectedDueDate = 0;
            setDueDateButton.setText(R.string.set_due_date);
            saveTasks();
            Toast.makeText(this, getString(R.string.task_added_success), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.empty_task_warning, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onTaskClick(int position) {
        Toast.makeText(this, getString(R.string.task_clicked, tasks.get(position).getText()), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEditClick(int position) {
        showEditDialog(position);
    }

    private void showEditDialog(int position) {
        Task task = tasks.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_task_dialog_title);

        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(50, 20, 50, 10);

        final EditText input = new EditText(this);
        input.setText(task.getText());
        container.addView(input);

        Spinner prioritySpinner = new Spinner(this);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.priority_levels, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        prioritySpinner.setAdapter(spinnerAdapter);
        prioritySpinner.setSelection(task.getPriority());
        container.addView(prioritySpinner);

        Button btnSetDueDate = new Button(this);
        btnSetDueDate.setText(task.getDueDate() > 0 ?
                DATE_FORMAT.format(task.getDueDate()) :
                getString(R.string.set_due_date));
        btnSetDueDate.setOnClickListener(v -> showDatePicker(position));
        container.addView(btnSetDueDate);

        builder.setView(container);

        builder.setPositiveButton(R.string.save_changes, (dialog, which) -> {
            String updatedText = input.getText().toString().trim();
            if (!TextUtils.isEmpty(updatedText)) {
                task.setText(updatedText);
                task.setPriority(prioritySpinner.getSelectedItemPosition());
                adapter.notifyItemChanged(position);
                saveTasks();
            }
        });

        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void saveTasks() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (Task task : tasks) {
                JSONObject jsonTask = new JSONObject();
                jsonTask.put("text", task.getText());
                jsonTask.put("dueDate", task.getDueDate());
                jsonTask.put("priority", task.getPriority());
                jsonArray.put(jsonTask);
            }
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(TASKS_KEY, jsonArray.toString())
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving tasks", e);
        }
    }

    private List<Task> loadTasks() {
        List<Task> loadedTasks = new ArrayList<>();
        try {
            String jsonString = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .getString(TASKS_KEY, "");
            if (!TextUtils.isEmpty(jsonString)) {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonTask = jsonArray.getJSONObject(i);
                    loadedTasks.add(new Task(
                            jsonTask.getString("text"),
                            jsonTask.optLong("dueDate", 0),
                            jsonTask.optInt("priority", 0)
                    ));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading tasks", e);
        }
        return loadedTasks;
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveTasks();
    }
}