package io.gitlab.allenb1.todolist;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.Toolbar;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ProjectActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.project_activity_main);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(R.drawable.ic_menu_white);

        mDrawerLayout = findViewById(R.id.root);

        mDrawer = findViewById(R.id.drawer);
        mDrawer.setCheckedItem(R.id.drawer_projects);
        mDrawer.setNavigationItemSelectedListener(this);

        final ListView listView = findViewById(R.id.list);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1));
        listView.setEmptyView(findViewById(R.id.no_text));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(ProjectActivity.this, ProjectViewActivity.class);
                intent.putExtra(ProjectViewActivity.EXTRA_PROJECT_NAME, adapterView.getItemAtPosition(i).toString());
                startActivity(intent);
            }
        });
    }

    @Override public void onResume() {
        super.onResume();

        final ListView listView = findViewById(R.id.list);
        ArrayAdapter<String> adapter = (ArrayAdapter<String>)listView.getAdapter();
        adapter.clear();

        try {
            final TodoTask.TodoList list = TodoTask.TodoList.getDefaultTodoList(this);

            Set<String> set = new HashSet<>();
            for (TodoTask item : list) {
                set.addAll(item.projects);
            }

            adapter.addAll(set);
            adapter.notifyDataSetChanged();
        } catch(IOException e) {
            Snackbar.make(listView, R.string.generic_error, Snackbar.LENGTH_LONG).show();
        }
    }
}