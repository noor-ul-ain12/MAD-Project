package io.gitlab.allenb1.todolist;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TaskListFragment extends Fragment {
    public static Fragment newInstance(TodoProject p) {
        TaskListFragment f = new TaskListFragment();
        Bundle args = new Bundle();
        args.putString("project", p.toString());
        f.setArguments(args);

        return f;
    }

    private TodoSet.TodoFilter mSetFilter = null;

    public interface TodoListKeeper {
        public TodoTask.TodoList getTodoList();
    }

    public interface TaskUpdateListener {
        public void onTaskChecked(TodoTask task);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.task_fragment_list, parent, false);

        if(getArguments() != null) {
            String projectName = getArguments().getString("project");
            if(projectName != null)
                mSetFilter = new TodoProject.TodoProjectFilter(projectName);
        }

        final ListView listView = rootView.findViewById(R.id.list);
        listView.setEmptyView(rootView.findViewById(R.id.no_text));
        listView.setAdapter(new TodoTaskAdapter2(getContext(), new ArrayList<Map.Entry<Integer, TodoTask>>()));
        loadData(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> adapterView, final View view, int i, long l) {
                Intent intent = new Intent(getContext(), TaskViewActivity.class);
                intent.putExtra(TaskViewActivity.EXTRA_TASK_POS, (Integer)view.getTag());
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override public boolean onItemLongClick(AdapterView<?> adapterView, final View view, int i, long l) {
                PopupMenu menu = new PopupMenu(getContext(), view, Gravity.TOP | Gravity.START);
                menu.inflate(R.menu.task_popup);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_delete:
                                TaskHelper.deleteTask(getActivity(), getTodoList(), (Integer)view.getTag(), new TaskHelper.DeleteCallback() {
                                    @Override
                                    public void ondelete(TodoTask task) {
                                        Snackbar.make(view, R.string.task_snackbar_deleted, Snackbar.LENGTH_LONG).show();
                                        view.setVisibility(View.GONE);
                                    }
                                });
                                break;
                            case R.id.action_edit:
                                Intent intent = new Intent(getContext(), TaskEditActivity.class);
                                intent.setAction(Intent.ACTION_EDIT);
                                intent.putExtra(TaskEditActivity.EXTRA_TASK_POS, (Integer)view.getTag());
                                startActivity(intent);
                        }
                        return true;
                    }
                });
                menu.show();
                return true;
            }
        });

        return rootView;
    }

    @Override public void onResume() {
        super.onResume();
        reload();
    }

    protected void loadData(final ListView listView) {
        TodoTask.TodoList todoList = getTodoList();
        if(todoList == null) {
            return;
        }

        final TodoSet set = new TodoSet(todoList, mSetFilter);

        Iterator<TodoTask> it = set.iterator();

        TodoTaskAdapter2 adapter = (TodoTaskAdapter2)listView.getAdapter();
        adapter.set(set);
        adapter.notifyDataSetChanged();
    }

    public void reload() {
        if(getView() != null)
            loadData((ListView)getView().findViewById(R.id.list));
    }

    private TodoTask.TodoList getTodoList() {
        Activity activity = this.getActivity();
        if(activity instanceof TodoListKeeper) {
            return ((TodoListKeeper)activity).getTodoList();
        } else {
            Log.e("getTodoList", "parent activity not instance of TodoListKeeper");
            return null;
        }
    }
}

class TodoTaskAdapter2 extends ArrayAdapter<Map.Entry<Integer, TodoTask>> {
    public TodoTaskAdapter2(Context ctx, List<Map.Entry<Integer,TodoTask>> list) {
        super(ctx, 0, list);
    }

    public TodoTaskAdapter2(Context ctx) {
        this(ctx, new ArrayList<Map.Entry<Integer, TodoTask>>());
    }

    public void set(TodoSet list) {
        clear();
        for(TodoSet.TodoIterator it = (TodoSet.TodoIterator)list.iterator(); it.hasNext(); ) {
            final TodoTask task = it.next();
            add(new AbstractMap.SimpleEntry<Integer,TodoTask>(it.position(), task));
        }
    }

    @Override @NonNull public View getView(int position, View view, @NonNull ViewGroup parent) {
        if(view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        }

        Map.Entry<Integer,TodoTask> item = getItem(position);
        if(item == null)
            return view;
        final TodoTask task = item.getValue();
        view.setTag(item.getKey());

        final TextView text1 = view.findViewById(android.R.id.text1);
        text1.setText(task.name);
        if(task.isOverdue() && !task.done) {
            text1.setTextColor(getContext().getResources().getColor(R.color.colorError));
        }

        final ImageView done = view.findViewById(android.R.id.icon);
        done.setVisibility(task.done ? View.VISIBLE : View.INVISIBLE);

        return view;
    }
}