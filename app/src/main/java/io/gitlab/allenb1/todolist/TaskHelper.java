package io.gitlab.allenb1.todolist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.Window;

import java.io.IOException;
import java.util.function.Function;

public class TaskHelper {

    public static interface DeleteCallback {
        public void ondelete(TodoTask task);
    }

    public static void deleteTask(final Activity ctx, final TodoTask.TodoList todoList, final int position, final DeleteCallback ondelete) {
        final TodoTask task = todoList.get(position);
        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_message)
                .setPositiveButton(R.string.dialog_delete_button_delete, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int i) {
                        try {
                            todoList.remove(position);
                            todoList.save();

                            if(ondelete != null)
                                ondelete.ondelete(task);
                        } catch(IOException e) {
                            Snackbar.make(ctx.findViewById(android.R.id.content), R.string.generic_error, Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(R.string.dialog_button_cancel, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                })
                .create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }
}

