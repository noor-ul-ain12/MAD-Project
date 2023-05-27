package io.gitlab.allenb1.todolist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/* Tasks are specific things that you can do right now. Nonspecific tasks and goals should be put in Projects.
 */

public class TodoTask implements Comparable<TodoTask> {
    public String name = null;
    public String info = null;
    public boolean done = false;
    public Date date = null;
    public byte priority = 0;
    @NonNull public Set<String> projects = new HashSet<>();

    public TodoTask(){}

    public boolean isValid() {
        return this.name != null && this.name.length() > 0;
    }

    public boolean isOverdue() {
        Calendar today = Calendar.getInstance();
        if(date == null)
            return false;
        Calendar taskdate = (Calendar)today.clone();
        taskdate.setTime(date);
        return date.getTime() < today.getTimeInMillis() &&
                taskdate.get(Calendar.DAY_OF_YEAR) != today.get(Calendar.DAY_OF_YEAR);
    }

    @Override public boolean equals(Object other) {
        return other instanceof TodoTask && toString().equals(other.toString());
    }

    @Override public int compareTo(@NonNull TodoTask other) {
        return this.priority - other.priority;
    }

    public TodoTask(String line) {
        line = line.trim();

        if(line.indexOf("x ") == 0) {
            done = true;
            line = line.substring(2).trim();
        }

        String[] parts = line.split(" ");

        StringBuilder nameBuilder = new StringBuilder();
        for(String part: parts) {
            String[] strings = part.split(":", 2);
            // assert strings.length > 0;
            if(strings.length == 1) {
                if(strings[0].length() == 0)
                    continue;
                if(strings[0].charAt(0) == '+') {
                    projects.add(strings[0].substring(1));
                } else if(priority == 0 && strings[0].charAt(0) == '(' && strings[0].endsWith(")")) {
                    priority = (byte)(strings[0].charAt(1) - 'A' + 1);
                } else {
                    nameBuilder.append(TextUtils.join(" ", strings));
                    nameBuilder.append(" ");
                }
                continue;
            }
            switch(strings[0]) {
                case "info":
                case "notes":
                    info = strings[1].replace('_', ' ').trim();
                    break;
                case "date":
                case "due":
                    SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
                    try {
                        date = fmt.parse(strings[1]);
                    } catch(ParseException e) {}
            }
        }
        name = nameBuilder.toString().trim();
    }

    public String toString() {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        if(this.isValid()) {
            return (done ? "x " : "") +
                    (priority != 0 ? "(" + (char)(priority - 1 + 'A') +  ") " : "") + name +
                    (projects.size() == 0 ? "" : " +" + TextUtils.join(" +", projects)) +
                    (info != null && info.length() > 0 ? " notes:" + info.replace("\n", "_").replace(" ", "_") + " " : "") +
                    (date != null ? " date:" + fmt.format(date) + " " : "");
        }
        return null;
    }

    /* actual, whole todolist */
    public static class TodoList extends ArrayList<TodoTask> {
        private File file = null;

        public static File getDefaultFile(Context ctx) {
            return new File(ctx.getFilesDir(), "todo.txt");
        }

        public static TodoList getDefaultTodoList(Context ctx) throws IOException {
            File file = getDefaultFile(ctx);
            file.createNewFile();
            return new TodoList(file);
        }

        public TodoList() {
            super(); }

        public TodoList(String text) {
            super();
            String[] lines = text.split("\n");
            for(String line: lines) {
                TodoTask task = new TodoTask(line);
                if(task.isValid())
                    add(task);
            }
        }

        public TodoList(File file) throws IOException {
            super();

            load(file);
        }

        /* reload */
        public void load() throws IOException {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while(line != null) {
                TodoTask task = new TodoTask(line);
                if(task.isValid())
                    add(task);

                line = reader.readLine();
            }
            reader.close();
        }

        public void load(File file) throws IOException {
            this.file = file;
            load();
        }

        @Override public String toString() {
            StringBuilder b = new StringBuilder();
            for(TodoTask task: this) {
                b.append(task.toString());
                b.append("\n");
            }
            return b.toString();
        }

        public void setDone(int pos, boolean done) {
            this.get(pos).done = done;
        }

        public void save() throws IOException {
            if(file != null)
                saveTo(file);
        }

        public void saveTo(File file) throws IOException {
            FileOutputStream o = new FileOutputStream(file);
            o.write(this.toString().getBytes());
            o.close();
        }
    }
}
