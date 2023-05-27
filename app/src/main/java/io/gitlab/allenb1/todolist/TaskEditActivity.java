package io.gitlab.allenb1.todolist;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TaskEditActivity extends AppCompatActivity {

    private TodoTask.TodoList mTodoList;
    private TodoTask mTask = null;
    private int mTaskPosition = -1;

    public static final String EXTRA_TASK_POS = TaskViewActivity.EXTRA_TASK_POS;
    public static final String EXTRA_DISABLE_ANIMATE_EXIT = "io.gitlab.allenb1.todolist.ANIMATE_EXIT";
    public static final String EXTRA_PROJECT_NAME = ProjectViewActivity.EXTRA_PROJECT_NAME;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity_edit);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar bar = getSupportActionBar();
        bar.setDisplayShowTitleEnabled(false);
        bar.setDisplayHomeAsUpEnabled(true);

        try {
            mTodoList = TodoTask.TodoList.getDefaultTodoList(this);
        } catch(IOException e) {
            showError(R.string.error_cant_find_task);
        }

        /* Initialize widgets */
        initItemCallbacks();
        initDateButton();

        try {
            loadTask();
        } catch(Exception err) {
            showError(err.getMessage());
            err.printStackTrace();
        }
    }

    public void initItemCallbacks() {
        findViewById(R.id.task_info_item).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                final EditText taskInfoView = view.findViewById(R.id.task_info_text);
                taskInfoView.requestFocus();
            }
        });
        findViewById(R.id.task_priority_item).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                final EditText taskPriorityView = view.findViewById(R.id.task_priority_number);
                taskPriorityView.requestFocus();
            }
        });
        findViewById(R.id.task_project_item).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                final EditText taskProjectView = view.findViewById(R.id.task_project_text);
                taskProjectView.requestFocus();
            }
        });
    }

    /* Load data into text fields */
    private void loadTask() throws Exception {
        /* Get action */
        Intent intent = getIntent();
        String action = intent.getAction();

        if(action != null) {
            /* get views */
            final EditText taskPriorityView = findViewById(R.id.task_priority_number);
            final TextView taskDateTextView = findViewById(R.id.task_date_text);
            final TextView taskNameView = findViewById(R.id.task_name_text);
            final TextView taskInfoView = findViewById(R.id.task_info_text);
            final TextView taskProjectTextView = findViewById(R.id.task_project_text);

            mTaskPosition = intent.getIntExtra(EXTRA_TASK_POS, -1);
            switch (action) {
                case Intent.ACTION_EDIT:
                    if (mTaskPosition < 0 || mTaskPosition >= mTodoList.size())
                        throw new Exception(getResources().getString(R.string.error_cant_find_task));
                    mTask = mTodoList.get(mTaskPosition);

                    taskNameView.setText(mTask.name);
                    taskInfoView.setText(mTask.info);
                    if (mTask.date != null) {
                        taskDateTextView.setText(SimpleDateFormat.getDateInstance().format(mTask.date));
                        taskDateSet = true;
                        taskCalendar.setTime(mTask.date);
                    }

                    taskPriorityView.setText(Integer.toString(mTask.priority));
                    taskProjectTextView.setText(TextUtils.join("\n", mTask.projects));
                    break;
                case Intent.ACTION_INSERT:
                    String projectName = intent.getStringExtra(EXTRA_PROJECT_NAME);
                    if(projectName != null)
                        taskProjectTextView.setText(projectName);
                    break;
                default:
                    throw new Exception(getResources().getString(R.string.generic_error));
            }
        } else {
            throw new Exception(getResources().getString(R.string.generic_error));
        }
    }

    private Calendar taskCalendar = null;
    private boolean taskDateSet = false;
    private void initDateButton() {
        taskCalendar = Calendar.getInstance();
        final TextView taskDateTextView = findViewById(R.id.task_date_text);
        final View taskDateView = findViewById(R.id.task_date_item);

        taskDateView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        taskCalendar.set(Calendar.YEAR, year);
                        taskCalendar.set(Calendar.MONTH, month);
                        taskCalendar.set(Calendar.DAY_OF_MONTH, day);
                        taskDateSet = true;

                        taskDateTextView.setText(SimpleDateFormat.getDateInstance().format(taskCalendar.getTime()));
                    }
                },
                        taskCalendar.get(Calendar.YEAR),
                        taskCalendar.get(Calendar.MONTH),
                        taskCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    /* Show snackbar */
    protected void showError(String message) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if(toolbar != null)
            Snackbar.make(toolbar, message, Snackbar.LENGTH_LONG).show();
    }
    protected void showError(int message) {
        showError(getResources().getString(message));
    }

    /* Save task and exit */
    protected void saveAndExit() {
        /* Get text */
        final TextView taskNameView = findViewById(R.id.task_name_text);
        if(taskNameView.getText().length() == 0) {
            /* TODO: OK button on right side? */
            new AlertDialog.Builder(this)
                .setMessage(R.string.error_task_name_missing)
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_dismiss,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                .show();
            return;
        }

        /* Write to todo.txt */
        try {
            save();

            Intent intent = new Intent(this, TaskViewActivity.class);
            intent.putExtra(TaskViewActivity.EXTRA_TASK_POS, mTaskPosition);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } catch(NumberFormatException e) {
            showError(R.string.error_task_priority_not_a_number);
        } catch(Exception e) {
            showError(R.string.error_task_save);

            e.printStackTrace();
        }
    }

    private void save() throws Exception {
        final TextView taskNameView = findViewById(R.id.task_name_text);
        final TextView taskPriorityView = findViewById(R.id.task_priority_number);
        final TextView taskInfoView = findViewById(R.id.task_info_text);
        final TextView taskProjectTextView = findViewById(R.id.task_project_text);

        if(mTaskPosition < 0 || mTask == null)
            mTask = new TodoTask();

        mTask.name = taskNameView.getText().toString();
        mTask.info = taskInfoView.getText().toString();
        if(taskDateSet)
            mTask.date = taskCalendar.getTime();
        mTask.priority = Byte.parseByte(taskPriorityView.getText().toString());

        Set<String> projects = new HashSet<>();
        String[] array = TextUtils.split(taskProjectTextView.getText().toString(), "\n");
        for(String project: array) {
            String trimmed = project.trim();
            if(trimmed.length() > 0) {
                projects.add(trimmed);
            }
        }
        mTask.projects = projects;

        if(mTaskPosition >= 0)
            mTodoList.set(mTaskPosition, mTask);
        else {
            mTodoList.add(mTask);
            mTaskPosition = mTodoList.size() - 1;
        }

        mTodoList.save();
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_add_todo, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done:
                saveAndExit();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_leave_title)
                .setMessage(R.string.dialog_leave_message)
                .setPositiveButton(R.string.dialog_leave_button_leave, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        finish();
                        if (getIntent().getBooleanExtra(EXTRA_DISABLE_ANIMATE_EXIT, false))
                            overridePendingTransition(0, 0);
                    }
                })
                .setNegativeButton(R.string.dialog_leave_button_stay, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                })
                .show();
    }
}
