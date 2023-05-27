package io.gitlab.allenb1.todolist;

import android.support.v7.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView website = findViewById(R.id.website);
        website.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/AllenB1/taskeeper"));
                startActivity(intent);
            }
        });

        TextView feedback = findViewById(R.id.send_feedback);
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://gitlab.com/AllenB1/taskeeper/issues/new"));
                startActivity(intent);
            }
        });

        TextView version = findViewById(R.id.app_version);
        version.setText(BuildConfig.VERSION_NAME);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    static public void show(final Context ctx) {
        Intent intent = new Intent(ctx, AboutActivity.class);
        ctx.startActivity(intent);
    }
}
