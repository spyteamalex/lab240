package com.lab240.lab240;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemAnimator;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.utils.Dashboard;
import com.lab240.utils.Item;
import com.lab240.utils.Lab240;
import com.lab240.utils.MQTT;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DashboardActivity extends AppCompatActivity {

    public class ItemAdapter extends RecyclerView.Adapter<ItemHolder>{

        @NonNull
        @Override
        public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.text_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
            Item item = items.get(position);
            holder.item = item;
            holder.name.setText(item.getName());
            holder.topic.setText(item.getTopic());
            holder.indicator.setText(values.containsKey(item.getId()) ? values.get(item.getId()) :  "—");

            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            layoutParams.height = 300;
            holder.itemView.setLayoutParams(layoutParams);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private final List<Item> items = new ArrayList<>();
        private final Map<Long, String> values = new TreeMap<>();

        Set<Pair<String, MQTT.MessageCallback>> listeners = new HashSet<>();

        public void setData(Collection<Item> data){
            items.clear();
            items.addAll(data);
            notifyDataSetChanged();

            dropListeners();

            for(Item i : data) {
                MQTT.MessageCallback listener = (topic1, msg) -> {
                    values.put(i.getId(), msg.toString());
                    notifyDataSetChanged();
                };
                listeners.add(Pair.create(i.getTopic(), listener));
                Lab240.getMqtt().addListener(i.getTopic(), listener);
                Lab240.getMqtt().subscribe(i.getTopic(), 0);
            }
        }

        public void dropListeners(){
            for(Pair<String, MQTT.MessageCallback> i : listeners)
                Lab240.getMqtt().removeListener(i.first, i.second);
            listeners.clear();
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder{

        final TextView name, topic, indicator;
        Item item;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            topic = itemView.findViewById(R.id.topic);
            indicator = itemView.findViewById(R.id.indicator);

            itemView.setOnLongClickListener(view -> {
                AlertSheetDialog asd = new AlertSheetDialog(DashboardActivity.this);
                asd.addButton("Переименовать", ()->{
                    AlertSheetDialog asd2 = new AlertSheetDialog(DashboardActivity.this);
                    EditText name = asd2.addEditText("Название");
                    name.setSingleLine(true);
                    name.setText(item.getName());
                    Button doneButton = asd2.addButton("Переименовать", () -> {
                        item.setName(name.getText().toString());
                        adapter.setData(db.getItems().values());
                        Lab240.saveDashboards(DashboardActivity.this, Lab240.getDashboards());
                    }, AlertSheetDialog.DEFAULT);
                    asd2.show();
                }, AlertSheetDialog.DEFAULT);
                asd.addButton("Сменить тему", ()->{
                    AlertSheetDialog asd2 = new AlertSheetDialog(DashboardActivity.this);
                    EditText topic = asd2.addEditText("Тема");
                    topic.setSingleLine(true);
                    topic.setText(item.getTopic());
                    Button doneButton = asd2.addButton("Сменить тему", () -> {
                        item.setTopic(topic.getText().toString());
                        adapter.setData(db.getItems().values());
                        Lab240.saveDashboards(DashboardActivity.this, Lab240.getDashboards());
                    }, AlertSheetDialog.DEFAULT);
                    topic.addTextChangedListener(new TextWatcher() {
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
                asd.addButton("Удалить", ()->{
                    db.getItems().remove(item.getId());
                    adapter.setData(db.getItems().values());
                    Lab240.saveDashboards(DashboardActivity.this, Lab240.getDashboards());
                }, AlertSheetDialog.DESTROY);
                asd.show();
                return false;
            });
        }
    }

    public static String DASHBOARD = "Dashboard";

    RecyclerView items;
    Dashboard db;
    ItemAdapter adapter;
    FloatingActionButton addButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        Bundle extras = getIntent().getExtras();
        if(!extras.containsKey(DASHBOARD))
            finish();

        Long db = extras.getLong(DASHBOARD);

        if(!Lab240.getDashboards().containsKey(db))
            finish();

        this.db = Lab240.getDashboards().get(db);

        items = findViewById(R.id.items);
        adapter = new ItemAdapter();
        items.setAdapter(adapter);

        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(this::addItem);
    }

    public void addItem(View v){
        AlertSheetDialog asd2 = new AlertSheetDialog(this);
        EditText name = asd2.addEditText("Название");
        name.setSingleLine(true);
        name.setText("Item");
        EditText topic = asd2.addEditText("Тема");
        topic.setSingleLine(true);
        Button doneButton = asd2.addButton("Создать", () -> {
            long id = System.currentTimeMillis();
            db.getItems().put(id, new Item(id, name.getText().toString(), topic.getText().toString()));
            adapter.setData(db.getItems().values());
            Lab240.saveDashboards(this, Lab240.getDashboards());
        }, AlertSheetDialog.DEFAULT);
        topic.addTextChangedListener(new TextWatcher() {
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

    @Override
    protected void onPause() {
        super.onPause();
        adapter.dropListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.setData(db.getItems().values());
    }
}