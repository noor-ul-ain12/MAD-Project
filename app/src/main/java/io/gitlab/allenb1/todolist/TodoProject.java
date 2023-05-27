package io.gitlab.allenb1.todolist;
import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TodoProject extends TodoSet implements Set<TodoTask> {
    final public String name;
    public static class TodoProjectFilter implements TodoSet.TodoFilter {
        final private String name;
        public TodoProjectFilter(String name) {
            this.name = name;
        }
        public boolean contains(TodoTask task) {
            return task.projects.contains(name);
        }
    }
    public TodoProject(TodoTask.TodoList parent, String name) {
        super(parent, new TodoProjectFilter(name));
        this.name = name;
    }

    @Override public boolean add(TodoTask task) {
        if(task == null)
            return false;
        return task.projects.add(name);
    }

    @Override public boolean remove(Object o) {
        return remove((TodoTask)o);
    }

    public boolean remove(TodoTask task) {
        if(task == null)
            return false;
        return task.projects.remove(name);
    }

    @Override public String toString() {
        return name;
    }
}
