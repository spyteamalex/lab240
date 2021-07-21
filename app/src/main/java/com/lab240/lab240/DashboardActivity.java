package com.lab240.lab240;

import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.google.common.base.Optional;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.lab240.Items.Dashboard;
import com.lab240.Items.IndicatorItem;
import com.lab240.Items.Item;
import com.lab240.Items.LineChartItem;
import com.lab240.Items.RingItem;
import com.lab240.Items.TextItem;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.utils.Lab240;
import com.lab240.utils.MQTT;
import com.lab240.utils.Point;
import com.lab240.utils.ScrollLockingLayoutManager;
import com.lab240.views.RingIndicatorView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class DashboardActivity extends AppCompatActivity {

    public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (Item.Type.values()[viewType]) {
                case TEXT:
                    return new TextItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.text_item, parent, false));
                case RING:
                    return new RingItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.ring_item, parent, false));
                case LINE_CHART:
                    return new LineChartItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.line_chart_item, parent, false));
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Item item = items.get(position);
            Optional<String> value = values.containsKey(item.getId()) ? Optional.of(values.get(item.getId())) : Optional.absent();
            switch (item.getType()){
                case TEXT:
                    TextItem ti = (TextItem)item;
                    TextItemHolder tih = (TextItemHolder)holder;
                    tih.item = ti;
                    tih.name.setText(ti.getName());
                    tih.topic.setText(ti.getTopic());
                    tih.indicator.setText(value.or("—"));

                    ViewGroup.LayoutParams layoutParams = tih.itemView.getLayoutParams();
                    layoutParams.height = 300;
                    tih.itemView.setLayoutParams(layoutParams);
                    break;
                case RING:
                    RingItem ri = (RingItem)item;
                    RingItemHolder rih = (RingItemHolder)holder;
                    rih.item = ri;
                    rih.name.setText(ri.getName());
                    rih.topic.setText(ri.getTopic());
                    if(!value.isPresent()) {
                        rih.indicator.setFullness(0, true);
                        rih.indicator.setText("—");
                    }else{
                        String strValue = value.get();
                        if(strValue.matches("-?\\d*[.,]?\\d+")){
                            float v = Float.parseFloat(strValue);
                            rih.indicator.setFullness((v-ri.getMinValue())/(ri.getMaxValue()-ri.getMinValue()), true);
                            rih.indicator.setText(strValue);
                        }else {
                            rih.indicator.setFullness(1, true);
                            rih.indicator.setText("NaN");
                        }
                    }
                    layoutParams = rih.itemView.getLayoutParams();
                    layoutParams.height = 400;
                    rih.itemView.setLayoutParams(layoutParams);
                    break;
                case LINE_CHART:
                    LineChartItem lci = (LineChartItem)item;
                    LineChartItemHolder lcih = (LineChartItemHolder)holder;
                    lcih.name.setText(lci.getName());
                    lcih.topic.setText(lci.getTopic());
                    if(!value.isPresent()) {
                        //todo
                    }else{
                        String strValue = value.get();
                        if(strValue.matches("-?\\d*[.,]?\\d+")){
                            if(lcih.item != lci) {
                                List<DataPoint> dataPoints = new ArrayList<>();
                                lci.getPoints().add(new Point<>(System.currentTimeMillis(), Double.parseDouble(strValue)));
                                for(Point<Long, Double> p : lci.getPoints()) {
                                    dataPoints.add(new DataPoint(p.getX(), p.getY()));
                                }
                                lcih.series.resetData(dataPoints.toArray(new DataPoint[0]));
                            }
                            Point<Long, Double> p = new Point<>(System.currentTimeMillis(), Double.parseDouble(strValue));
                            lci.getPoints().add(p);
                            lcih.series.appendData(new DataPoint(p.getX(), p.getY()), true, 40);
                        }else {
                            //todo
                        }
                    }
                    lcih.item = lci;
                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            Item item = items.get(position);
            return item.getType().ordinal();
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
                if(i instanceof IndicatorItem) {
                    IndicatorItem i2 = (IndicatorItem)i;
                    listeners.add(Pair.create(i2.getTopic(), listener));
                    Lab240.getMqtt().addListener(i2.getTopic(), listener);
                    Lab240.getMqtt().subscribe(i2.getTopic(), 0);
                }
            }
        }

        public void dropListeners(){
            for(Pair<String, MQTT.MessageCallback> i : listeners)
                Lab240.getMqtt().removeListener(i.first, i.second);
            listeners.clear();
        }
    }

    public class TextItemHolder extends RecyclerView.ViewHolder{

        final TextView name, topic, indicator;
        TextItem item;

        public TextItemHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            topic = itemView.findViewById(R.id.topic);
            indicator = itemView.findViewById(R.id.indicator);

            itemView.setOnLongClickListener(view -> {
                Vibrator v = (Vibrator) DashboardActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(25);

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

    public class LineChartItemHolder extends RecyclerView.ViewHolder{

        final TextView name, topic;
        LineChartItem item;
        final GraphView indicator;
        final LineGraphSeries<DataPoint> series;

        public LineChartItemHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            topic = itemView.findViewById(R.id.topic);
            indicator = itemView.findViewById(R.id.indicator);
            indicator.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(DashboardActivity.this){
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    SimpleDateFormat mDateFormat = new SimpleDateFormat("dd.MM.yyyy\nHH:mm:ss",Locale.getDefault());
                    if (isValueX) {
                        return mDateFormat.format((long)value);
                    } else {
                        return super.formatLabel(value, isValueX);
                    }
                }
            });
//            indicator.getGridLabelRenderer().setHumanRounding(false);
            indicator.getGridLabelRenderer().setNumHorizontalLabels(6);
            indicator.getGridLabelRenderer().setTextSize(20);
            indicator.getViewport().setXAxisBoundsManual(true);
            indicator.getViewport().setYAxisBoundsManual(true);
            indicator.getViewport().setScalable(true);
            indicator.getViewport().setScalableY(true);
            indicator.setOnTouchListener((view, motionEvent) -> {
                System.out.println(motionEvent.getAction());
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    sllm.setScrollable(false);
                } else if(motionEvent.getAction() == MotionEvent.ACTION_UP)
                    sllm.setScrollable(true);
                return view.onTouchEvent(motionEvent);
            });
            series = new LineGraphSeries<>();
            indicator.addSeries(series);

            itemView.setOnLongClickListener(view -> {
                Vibrator v = (Vibrator) DashboardActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(25);

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

    public class RingItemHolder extends RecyclerView.ViewHolder{

        final TextView name, topic;
        RingItem item;
        final RingIndicatorView indicator;

        public RingItemHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            topic = itemView.findViewById(R.id.topic);
            indicator = itemView.findViewById(R.id.indicator);

            itemView.setOnLongClickListener(view -> {
                Vibrator v = (Vibrator) DashboardActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(25);

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
                asd.addButton("Изменить границы", ()->{
                    AlertSheetDialog asd2 = new AlertSheetDialog(DashboardActivity.this);
                    EditText minV = asd2.addEditText("Минимальное значение");
                    minV.setText(String.format(Locale.getDefault(), "%.2f", item.getMinValue()).replace(",", "."));
                    minV.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    minV.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
                    EditText maxV = asd2.addEditText("Максимальное значение");
                    maxV.setText(String.format(Locale.getDefault(), "%.2f", item.getMaxValue()).replace(",", "."));
                    maxV.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                    maxV.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
                    Button doneButton = asd2.addButton("Создать", () -> {
                        if(!minV.getText().toString().matches("-?\\d*[.,]?\\d+"))
                            minV.setText("0");
                        if(!maxV.getText().toString().matches("-?\\d*[.,]?\\d+"))
                            maxV.setText("0");
                        long id = System.currentTimeMillis();
                        item.setMinValue(Float.parseFloat(minV.getText().toString().replace(",", ".")));
                        item.setMaxValue(Float.parseFloat(maxV.getText().toString().replace(",", ".")));
                        adapter.setData(db.getItems().values());
                        Lab240.saveDashboards(DashboardActivity.this, Lab240.getDashboards());
                    }, AlertSheetDialog.DEFAULT);
                    TextWatcher tw = new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

                        @Override
                        public void afterTextChanged(Editable editable) {
                            doneButton.setEnabled(!minV.getText().toString().isEmpty() && !maxV.getText().toString().isEmpty());
                        }
                    };
                    minV.addTextChangedListener(tw);
                    maxV.addTextChangedListener(tw);
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
    ScrollLockingLayoutManager sllm;

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

        if(this.db == null)
            finish();

        items = findViewById(R.id.items);
        adapter = new ItemAdapter();
        items.setAdapter(adapter);

        sllm = new ScrollLockingLayoutManager(this);
        items.setLayoutManager(sllm);

        setTitle(this.db.getGroup()+"/"+this.db.getName());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    public void addTextItem(){
        AlertSheetDialog asd2 = new AlertSheetDialog(this);
        EditText name = asd2.addEditText("Название");
        name.setSingleLine(true);
        name.setText("Item");
        EditText topic = asd2.addEditText("Тема");
        topic.setSingleLine(true);
        Button doneButton = asd2.addButton("Создать", () -> {
            long id = System.currentTimeMillis();
            db.getItems().put(id, new TextItem(id, name.getText().toString(), topic.getText().toString()));
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

    public void addLineChartItem(){
        AlertSheetDialog asd2 = new AlertSheetDialog(this);
        EditText name = asd2.addEditText("Название");
        name.setSingleLine(true);
        name.setText("Item");
        EditText topic = asd2.addEditText("Тема");
        topic.setSingleLine(true);
        Button doneButton = asd2.addButton("Создать", () -> {
            long id = System.currentTimeMillis();
            db.getItems().put(id, new LineChartItem(id, name.getText().toString(), topic.getText().toString()));
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

    public void addRingItem(){
        AlertSheetDialog asd2 = new AlertSheetDialog(this);
        EditText name = asd2.addEditText("Название");
        name.setSingleLine(true);
        name.setText("Item");
        EditText topic = asd2.addEditText("Тема");
        topic.setSingleLine(true);
        EditText minV = asd2.addEditText("Минимальное значение");
        minV.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        minV.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
        EditText maxV = asd2.addEditText("Максимальное значение");
        maxV.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        maxV.setFilters(new InputFilter[]{new InputFilter.LengthFilter(7)});
        Button doneButton = asd2.addButton("Создать", () -> {
            if(!minV.getText().toString().matches("-?\\d*[.,]?\\d+"))
                minV.setText("0");
            if(!maxV.getText().toString().matches("-?\\d*[.,]?\\d+"))
                maxV.setText("0");
            long id = System.currentTimeMillis();
            db.getItems().put(id, new RingItem(id, name.getText().toString(), topic.getText().toString(), Float.parseFloat(minV.getText().toString().replace(",", ".")), Float.parseFloat(maxV.getText().toString().replace(",", "."))));
            adapter.setData(db.getItems().values());
            Lab240.saveDashboards(this, Lab240.getDashboards());
        }, AlertSheetDialog.DEFAULT);
        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                doneButton.setEnabled(!minV.getText().toString().isEmpty() && !maxV.getText().toString().isEmpty() && !topic.getText().toString().isEmpty());
            }
        };
        minV.addTextChangedListener(tw);
        maxV.addTextChangedListener(tw);
        topic.addTextChangedListener(tw);
        asd2.show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.text:
                addTextItem();
                break;
            case R.id.ring:
                addRingItem();
                break;
            case R.id.lineChart:
                addLineChartItem();
                break;
        }
        return super.onOptionsItemSelected(item);
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