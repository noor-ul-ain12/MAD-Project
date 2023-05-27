package io.gitlab.allenb1.todolist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.Window;

/*
 * Defines methods shared by TaskActivity and ProjectActivity to prevent code duplication
 */

public abstract class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected DrawerLayout mDrawerLayout = null;
    protected NavigationView mDrawer = null;

    @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        switch(item.getItemId()) {
            case R.id.drawer_projects:
                if(!(this instanceof ProjectActivity)) {
                    Intent intent = new Intent(this, ProjectActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    item.setChecked(false);
                }
                break;

            case R.id.drawer_tasks:
                if(!(this instanceof TaskActivity)) {
                    Intent intent = new Intent(this, TaskActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(intent);
                    item.setChecked(false);
                }
                break;

            case R.id.drawer_about:
                AboutActivity.show(this);
                break;
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override public void onResume() {
        super.onResume();
        mDrawer.setCheckedItem(this instanceof ProjectActivity ? R.id.drawer_projects : R.id.drawer_tasks);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.root);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }
}
