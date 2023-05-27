package io.gitlab.allenb1.todolist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskActivity extends BaseActivity
        implements TaskListFragment.TodoListKeeper {
    final public static String EXTRA_SNACKBAR_MESAGE = "io.gitlab.allenb1.taskeeper.MESSAGE";

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_activity_main);
        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_white);
        ab.setTitle(R.string.task_activity_title);

        mDrawerLayout = findViewById(R.id.root);

        mDrawer = findViewById(R.id.drawer);
        mDrawer.setCheckedItem(R.id.drawer_tasks);
        mDrawer.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent intent = new Intent(TaskActivity.this, TaskEditActivity.class);
                intent.setAction(Intent.ACTION_INSERT);
                startActivity(intent);
            }
        });
    }

    @Override public void onNewIntent(Intent intent) {
        String message = intent.getStringExtra(EXTRA_SNACKBAR_MESAGE);
        if(message != null)
            Snackbar.make(findViewById(R.id.fab), message, Snackbar.LENGTH_LONG).show();
    }

    /* Static methods */
    public static Intent toTaskActivityIntent(Context ctx, String message) {
        Intent intent = new Intent(ctx, TaskActivity.class);
        intent.putExtra(TaskActivity.EXTRA_SNACKBAR_MESAGE, message);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    public static Intent toTaskActivityIntent(Context ctx, int resource)  {
        return toTaskActivityIntent(ctx, ctx.getResources().getString(resource));
    }

    @Override public void onResume() {
        super.onResume();
        initTodoList();
    }

    private TodoTask.TodoList mTodoList = null;
    private void initTodoList() {
        try {
            mTodoList = TodoTask.TodoList.getDefaultTodoList(this);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    @Override public TodoTask.TodoList getTodoList() {
        return mTodoList;
    }
}