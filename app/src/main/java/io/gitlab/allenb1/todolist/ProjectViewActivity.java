package io.gitlab.allenb1.todolist;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

public class ProjectViewActivity extends AppCompatActivity implements
        TaskListFragment.TodoListKeeper {
    public final static String EXTRA_PROJECT_NAME = "io.gitlab.allenb1.todolist.PROJECT_NAME";

    private TodoTask.TodoList mTodoList;
    private TodoProject mProject;
    private TaskListFragment mFragment;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_activity_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(this, R.style.TextAppearance_AppCompat_Headline);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        try {
            loadTasks();
        } catch(Exception e) {
            Snackbar.make(toolbar, R.string.generic_error, Snackbar.LENGTH_LONG).show();
            e.printStackTrace();
        }

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProjectViewActivity.this, TaskEditActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
                intent.putExtra(TaskEditActivity.EXTRA_PROJECT_NAME, mProject.name);
                startActivity(intent);
            }
        });
    }

    public void loadTasks() throws Exception {
        mTodoList = TodoTask.TodoList.getDefaultTodoList(this);

        String name = getIntent().getStringExtra(EXTRA_PROJECT_NAME);
        if(name != null)
            mProject = new TodoProject(mTodoList, name);
        else
            throw new Exception("Project name blank");

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, mFragment = (TaskListFragment) TaskListFragment.newInstance(mProject));
        transaction.commit();
    }

    public void updateToolbar() {
        ActionBar ab = getSupportActionBar();
        ab.setTitle(mProject.name);
        ab.setSubtitle(getString(R.string.project_done, mProject.done(), mProject.size()));
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                Intent intent = NavUtils.getParentActivityIntent(this);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override public void onResume() {
        super.onResume();
        try {
            mTodoList = TodoTask.TodoList.getDefaultTodoList(this);
            mProject.parent = mTodoList;
            updateToolbar();
            mFragment.reload();
        } catch(IOException e) {
            e.printStackTrace();
            Snackbar.make(findViewById(R.id.toolbar), R.string.generic_error, Snackbar.LENGTH_LONG).show();
        }
    }

    @Override public TodoTask.TodoList getTodoList() {
        return mTodoList;
    }
}
