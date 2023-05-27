package io.gitlab.allenb1.todolist;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class TaskViewActivity extends AppCompatActivity {
    TodoTask.TodoList mTodoList;
    TodoTask mTask;
    private int mTaskPosition = -1;

    public static final String EXTRA_TASK_POS = "io.gitlab.allenb1.todolist.POSITION";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Headline);
        setSupportActionBar(toolbar);

        /* Action bar */
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        /* Init fab */
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TaskViewActivity.this, TaskEditActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.setAction(Intent.ACTION_EDIT);
                intent.putExtra(TaskEditActivity.EXTRA_TASK_POS, mTaskPosition);
                intent.putExtra(TaskEditActivity.EXTRA_DISABLE_ANIMATE_EXIT, true);
                startActivity(intent);
            }
        });

        try {
            loadData();
        } catch(Exception e) {
            e.printStackTrace();
            startActivity(TaskActivity.toTaskActivityIntent(TaskViewActivity.this, R.string.task_snackbar_deleted));
        }
    }

    private void loadData() throws Exception {
        Intent intent = getIntent();
        mTaskPosition = intent.getIntExtra(EXTRA_TASK_POS, -1);

        mTodoList = TodoTask.TodoList.getDefaultTodoList(this);
        if(mTaskPosition < 0 || mTaskPosition >= mTodoList.size())
            throw new Exception("TaskViewActivity.EXTRA_TASK_POS is < 0 or >= TodoList.size()");
        mTask = mTodoList.get(mTaskPosition);

        /* Set action bar title */
        ActionBar ab = getSupportActionBar();
        ab.setTitle(mTask.name);
        updateToolbar();

        /* Set info */
        final TextView infoView = findViewById(R.id.task_info_text);
        if(mTask.info != null)
            infoView.setText(mTask.info);
        else {
            infoView.setVisibility(View.GONE);
            findViewById(R.id.task_info_icon).setVisibility(View.GONE); }

        /* Set date */
        final TextView dateView = findViewById(R.id.task_date_text);
        if(mTask.date != null) {
            Calendar taskDate = Calendar.getInstance();
            taskDate.setTime(mTask.date);
            Calendar compDate = Calendar.getInstance();
            compDate.setTime(new Date());

            if(taskDate.get(Calendar.DAY_OF_YEAR) == compDate.get(Calendar.DAY_OF_YEAR) &&
                    taskDate.get(Calendar.YEAR) == compDate.get(Calendar.YEAR)) {
                dateView.setText(R.string.task_date_today);
            } else {
                compDate.add(Calendar.DATE, 1);
                if(taskDate.get(Calendar.DAY_OF_YEAR) == compDate.get(Calendar.DAY_OF_YEAR) &&
                        taskDate.get(Calendar.YEAR) == compDate.get(Calendar.YEAR)){
                    dateView.setText(R.string.task_date_tomorrow);
                } else {
                    DateFormat fmt = SimpleDateFormat.getDateInstance();
                    dateView.setText(fmt.format(mTask.date));
                }
            }
        } else {
            /* Hide dateView */
            dateView.setVisibility(View.GONE);
            findViewById(R.id.task_date_icon).setVisibility(View.GONE);
        }

        final View projectView = findViewById(R.id.task_project_item);
        final TextView projectTextView = findViewById(R.id.task_project_text);
        if(mTask.projects.size() == 0) {
            projectView.setVisibility(View.GONE);
        } else {
            projectTextView.setText(TextUtils.join("\n", mTask.projects));
        }
        projectView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                int size = mTask.projects.size();
                if(size == 1) {
                    Intent intent = new Intent(TaskViewActivity.this, ProjectViewActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra(ProjectViewActivity.EXTRA_PROJECT_NAME, mTask.projects.iterator().next());
                    startActivity(intent);
                } else {
                    final CharSequence[] array = mTask.projects.toArray(new CharSequence[0]);
                    new AlertDialog.Builder(TaskViewActivity.this)
                            .setTitle(R.string.dialog_projectpicker_title)
                            .setCancelable(true)
                            .setItems(array, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(TaskViewActivity.this, ProjectViewActivity.class);
                                    intent.putExtra(ProjectViewActivity.EXTRA_PROJECT_NAME, array[which]);
                                    startActivity(intent);
                                }
                            })
                            .show();
                }
            }
        });

        final TextView priorityTextView = findViewById(R.id.task_priority_text);
        final TextView priorityNumberView = findViewById(R.id.task_priority_number);
        priorityNumberView.setText(Byte.toString(mTask.priority));
        switch(mTask.priority) {
            case 1:
                priorityTextView.setText(R.string.task_priority_high);
                break;
            case 2:
            case 3:
                priorityTextView.setText(R.string.task_priority_middle);
                break;
            default:
                priorityTextView.setText(R.string.task_priority_low);
        }
    }

    private void markAsDone() {
        try {
            mTask.done = !mTask.done;
            mTodoList.set(mTaskPosition, mTask);
            mTodoList.save();

            /* Update task_done view */
            updateToolbar();

            Snackbar.make(findViewById(R.id.toolbar), mTask.done ? R.string.task_snackbar_marked_done : R.string.task_snackbar_marked_not_done, Snackbar.LENGTH_LONG).show();
        } catch (IOException e) {
            Snackbar.make(findViewById(R.id.toolbar), R.string.generic_error, Snackbar.LENGTH_LONG).show();
        }
    }

    private void updateToolbar() {
        ActionBar ab = getSupportActionBar();
        ab.setSubtitle(mTask.done ? R.string.task_done : R.string.task_not_done);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.toolbar_view_todo, menu);
        /* Hide "Mark as Done" item if the task is already done */
        if(mTask != null)
            menu.findItem(R.id.action_done).setTitle(mTask.done ? R.string.task_mark_as_not_done : R.string.task_mark_as_done);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                TaskHelper.deleteTask(this, mTodoList, mTaskPosition, new TaskHelper.DeleteCallback() {
                    @Override
                    public void ondelete(TodoTask task) {
                        startActivity(TaskActivity.toTaskActivityIntent(TaskViewActivity.this, R.string.task_snackbar_deleted));
                    }
                });
                return true;

            case R.id.action_done:
                markAsDone();
                item.setTitle(mTask.done ? R.string.task_mark_as_not_done : R.string.task_mark_as_done);
                return true;

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
