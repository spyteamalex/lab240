package com.lab240.lab240;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.utils.Dashboard;
import com.lab240.utils.Lab240;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ListActivity extends AppCompatActivity {

    public class GroupAdapter extends RecyclerView.Adapter<GroupHolder>{

        @NonNull
        @Override
        public GroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new GroupHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_group, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull GroupHolder holder, int position) {
            String g = groups.get(position);
            holder.group = g;
            holder.adapter.setData(dashboards.get(g));
            holder.setWrapped(holder.wrapped);
        }

        @Override
        public int getItemCount() {
            return groups.size();
        }

        private final Multimap<String, Dashboard> dashboards = ArrayListMultimap.create();
        private final List<String> groups = new ArrayList<>();

        public void setData(Collection<Dashboard> data){
            groups.clear();
            dashboards.clear();
            for (Dashboard d: data) {
                dashboards.put(d.getGroup(), d);
            }
            groups.addAll(dashboards.keySet());
            Collections.sort(groups);
            notifyDataSetChanged();
        }
    }

    public class GroupHolder extends RecyclerView.ViewHolder{

        final RecyclerView dashboards;
        final TextView name;
        final DashboardAdapter adapter;
        final View divider;
        String group;
        private boolean wrapped = true;

        public void setWrapped(boolean b){
            wrapped = b;
            if(wrapped){
                divider.setVisibility(View.GONE);
                dashboards.setVisibility(View.GONE);
                name.setText(String.format(Locale.getDefault(), "▲ %s (%d)", group, adapter.getItemCount()));
            }else {
                divider.setVisibility(View.VISIBLE);
                dashboards.setVisibility(View.VISIBLE);
                name.setText(String.format(Locale.getDefault(), "▼ %s", group, adapter.getItemCount()));
            }
        }


        public GroupHolder(@NonNull View itemView) {
            super(itemView);
            dashboards = itemView.findViewById(R.id.dashboards);
            adapter = new DashboardAdapter();
            dashboards.setAdapter(adapter);

            divider = itemView.findViewById(R.id.divider);
            name = itemView.findViewById(R.id.name);

            itemView.setOnClickListener(v->{
                setWrapped(!wrapped);
            });


            itemView.setOnLongClickListener(view -> {
                AlertSheetDialog asd = new AlertSheetDialog(ListActivity.this);
                asd.addButton("Переименовать", ()->{
                    AlertSheetDialog asd2 = new AlertSheetDialog(ListActivity.this);
                    EditText gr = asd2.addEditText("Название");
                    gr.setSingleLine(true);
                    gr.setText(group);
                    asd2.addButton("Переименовать", () -> {
                        for(Dashboard db : adapter.dashboards)
                            db.setGroup(gr.getText().toString());
                        ga.setData(Lab240.getDashboards().values());
                        Lab240.saveDashboards(ListActivity.this, Lab240.getDashboards());
                    }, AlertSheetDialog.DEFAULT);
                    asd2.show();
                }, AlertSheetDialog.DEFAULT);
                asd.addButton("Удалить", ()->{
                    for(Dashboard i : adapter.dashboards)
                        Lab240.getDashboards().remove(i.getId());
                    ga.setData(Lab240.getDashboards().values());
                    Lab240.saveDashboards(ListActivity.this, Lab240.getDashboards());
                }, AlertSheetDialog.DESTROY);
                asd.show();
                return false;
            });

        }
    }

    public class DashboardAdapter extends RecyclerView.Adapter<DashboardHolder>{

        @NonNull
        @Override
        public DashboardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DashboardHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_dashboard, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DashboardHolder holder, int position) {
            Dashboard db = dashboards.get(position);
            holder.item = db;
            holder.name.setText(db.getName());
        }

        @Override
        public int getItemCount() {
            return dashboards.size();
        }

        private final List<Dashboard> dashboards = new ArrayList<>();

        public void setData(Collection<Dashboard> data){
            dashboards.clear();
            dashboards.addAll(data);
            Collections.sort(dashboards, (d1, d2)->{
                if(d1.getName().equals(d2.getName()))
                    return Long.compare(d1.getId(), d2.getId());
                return d1.getName().compareTo(d2.getName());
            });
            notifyDataSetChanged();
        }
    }

    public class DashboardHolder extends RecyclerView.ViewHolder{

        final TextView name;
        Dashboard item;

        public DashboardHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            itemView.setOnClickListener(view -> {
                Intent intent = new Intent(ListActivity.this, DashboardActivity.class);
                intent.putExtra(DashboardActivity.DASHBOARD, item.getId());
                startActivity(intent);
            });
            itemView.setOnLongClickListener(view -> {
                AlertSheetDialog asd = new AlertSheetDialog(ListActivity.this);
                asd.addButton("Переименовать", ()->{
                    AlertSheetDialog asd2 = new AlertSheetDialog(ListActivity.this);
                    EditText name = asd2.addEditText("Название");
                    name.setSingleLine(true);
                    name.setText(item.getName());
                    Button doneButton = asd2.addButton("Переименовать", () -> {
                        item.setName(name.getText().toString());
                        ga.setData(Lab240.getDashboards().values());
                        Lab240.saveDashboards(ListActivity.this, Lab240.getDashboards());
                    }, AlertSheetDialog.DEFAULT);
                    name.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                        @Override
                        public void afterTextChanged(Editable editable) {
                            doneButton.setEnabled(!editable.toString().isEmpty());
                        }
                    });
                    asd2.show();
                }, AlertSheetDialog.DEFAULT);
                asd.addButton("Переместить в", ()->{
                    AlertSheetDialog asd2 = new AlertSheetDialog(ListActivity.this);
                    EditText group = asd2.addEditText("Группа");
                    group.setSingleLine(true);
                    group.setText(item.getGroup());
                    asd2.addButton("Переместить", ()-> {
                        item.setGroup(group.getText().toString());
                        ga.setData(Lab240.getDashboards().values());
                        Lab240.saveDashboards(ListActivity.this, Lab240.getDashboards());
                    }, AlertSheetDialog.DEFAULT);
                    asd2.show();
                }, AlertSheetDialog.DEFAULT);
                asd.addButton("Удалить", ()->{
                    Lab240.getDashboards().remove(item);
                    ga.setData(Lab240.getDashboards().values());
                    Lab240.saveDashboards(ListActivity.this, Lab240.getDashboards());
                }, AlertSheetDialog.DESTROY);
                asd.show();
                return false;
            });
        }
    }

    GroupAdapter ga;
    FloatingActionButton addButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        if(!Lab240.isInited())
            finish();

        RecyclerView rv = findViewById(R.id.groups);
        ga = new GroupAdapter();
        rv.setAdapter(ga);
        ga.setData(Lab240.getDashboards().values());

        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(this::addDashboard);
    }

    public void addDashboard(View v){
        AlertSheetDialog asd2 = new AlertSheetDialog(ListActivity.this);
        EditText name = asd2.addEditText("Название");
        name.setSingleLine(true);
        name.setText("Dashboard");
        EditText group = asd2.addEditText("Группа");
        group.setSingleLine(true);
        Button doneButton = asd2.addButton("Создать", () -> {
            long id = System.currentTimeMillis();
            Lab240.getDashboards().put(id, new Dashboard(name.getText().toString(), id, group.getText().toString()));
            ga.setData(Lab240.getDashboards().values());
            Lab240.saveDashboards(ListActivity.this, Lab240.getDashboards());
        }, AlertSheetDialog.DEFAULT);
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                doneButton.setEnabled(!editable.toString().isEmpty());
            }
        });
        asd2.show();
    }
}