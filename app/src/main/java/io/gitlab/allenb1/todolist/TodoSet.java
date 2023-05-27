package io.gitlab.allenb1.todolist;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.text.DateFormat;
import java.util.AbstractSet;
import java.util.Calendar;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;

public class TodoSet extends AbstractSet<TodoTask> implements Set<TodoTask> {
    static interface TodoFilter {
        public boolean contains(TodoTask task);
    }
    public static class TodoEmptyFilter implements TodoFilter {
        public boolean contains(TodoTask task) {
            return false;
        }
        public String toString() {
            return "TodoEmptyFilter";
        }
    }
    /* Returns true if task either 1) is on given day 2) is overdue or 3) has no date */
    public static class TodoDateFilter implements TodoFilter {
        final Calendar date;

        public TodoDateFilter(Calendar day) {
            date = day;
        }

        public boolean contains(TodoTask task) {
            Calendar todayCal = Calendar.getInstance();
            Calendar taskCal = (Calendar)todayCal.clone();
            if(task.date != null)
                taskCal.setTime(task.date);
            if(task.date == null ||
                    (taskCal.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR) &&
                            taskCal.get(Calendar.YEAR) == date.get(Calendar.YEAR))  ||
                    (task.isOverdue() && !task.done && (
                            todayCal.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR) &&
                            todayCal.get(Calendar.YEAR) == date.get(Calendar.YEAR)
                            ))) {
                return true;
            }
            return false;
        }

        public String toString() {
            return "TodoDateFilter: " + DateFormat.getDateInstance().format(date.getTime());
        }
    }

    public static class TodoAllFilter implements TodoFilter {
        @Override public boolean contains(TodoTask task) {
            return true;
        }

        public String toString() {
            return "TodoAllFilter";
        }
    }

    public static class TodoIterator implements Iterator<TodoTask> {
        /* -1 represents start of list, -2 and below is invalid */
        private ListIterator<TodoTask> it = null;
        private TodoSet set;

        public TodoIterator(TodoSet set) {
            this.set = set;
            it = set.parent.listIterator();
        }

        private Iterator nextIt() {
            while(it.hasNext()) {
                TodoTask task = it.next();
                if(set.filter.contains(task)) {
                    return it;
                }
            }
            return null;
        }

        public TodoTask next() {
            while(it.hasNext()) {
                TodoTask task = it.next();
                if(set.filter.contains(task)) {
                    return task;
                }
            }
            return null;
        }

        public boolean hasNext() {
            while(it.hasNext()) {
                TodoTask task = it.next();
                if(set.filter.contains(task)) {
                    it.previous();
                    return true;
                }
            }
            return false;
        }

        public void remove() {
            it.remove(); }

        public void set(TodoTask task) {
            it.set(task);
        }

        public int position() {
            return it.nextIndex() - 1;
        }
    }

    public TodoTask get(int index) {
        return this.toArray(new TodoTask[0])[index];
    }

    public TodoIterator iteratorAt(int index) {
        int count = 0;
        for(TodoIterator it = (TodoIterator)iterator(); it.hasNext(); it.next()) {
            if(count >= index)
                return it;
            count++;
        }
        return null;
    }

    public int size() {
        int size = 0;
        for(TodoTask task: this) {
            size++;
        }
        return size;
    }

    protected TodoFilter filter = new TodoEmptyFilter();
    protected TodoTask.TodoList parent = null;

    @NonNull public Iterator<TodoTask> iterator() {
        return new TodoIterator(this);
    }

    public boolean contains(TodoTask o) {
        return parent != null && filter.contains(o) && parent.contains(o);
    }

    public void save() throws IOException {
        parent.save();
    }

    public TodoSet(TodoTask.TodoList list, TodoFilter filter) {
        super();
        this.filter = filter == null ? new TodoAllFilter() : filter;
        this.parent = list;
    }

    public TodoSet(TodoTask.TodoList list) {
        this(list, null);
    }

    @Override public String toString() {
        return TextUtils.join("\n", this);
    }

    /* Returns number of tasks done */
    public int done() {
        int count = 0;
        for(TodoTask task: this) {
            if(task.done)
                count++;
        }
        return count;
    }
}