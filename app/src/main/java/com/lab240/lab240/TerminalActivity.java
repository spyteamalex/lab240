package com.lab240.lab240;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.lab240.devices.Device;
import com.lab240.devices.DeviceTypes;
import com.lab240.devices.Hint;
import com.lab240.devices.Out;
import com.lab240.devices.OutLine;
import com.lab240.lab240.adapters.HintAdapter;
import com.lab240.lab240.adapters.ItemHolder;
import com.lab240.lab240.adapters.TerminalAdapter;
import com.lab240.lab240.adapters.TerminalItemAdapter;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.devices.Lab240;
import com.lab240.devices.MQTT;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class TerminalActivity extends AppCompatActivity {

    public static final String RESULT = "Result";

    public static final String DEVICE = "Device";
    public static final String ID = "Id";
    public static final String IN_DATE_FORMAT = "'IN'(dd.MM HH:mm):  ";
    public static final String OUT_DATE_FORMAT = "'OUT'(dd.MM HH:mm):  ";
    public static final String LOG_DATE_FORMAT = "'LOG'(dd.MM HH:mm):  ";
    Device device;
    String in;
    String out;
    String log;
    TerminalAdapter ta;

    static final int KEY = 1;

    MQTT.MessageCallback inCallback, outCallback, logCallback;

    List<OutLine> outlines = new ArrayList<>();
    RecyclerView outsView, relays;

    Map<Pair<String, Out>, Pair<String, Long>> values = new HashMap<>();
    Multimap<Pair<String, Out>, ItemHolder.Updater> updaters = ArrayListMultimap.create();
    public Set<Pair<String, MQTT.MessageCallback>> callbacks = new HashSet<>();
    final LinkedList<Hint> hints = new LinkedList<>();
    LinearLayout bars;
    Timer updateTimer;
    TerminalItemAdapter ia;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);
        Log.i("call", "Create TerminalActivity");
        Bundle extras = getIntent().getExtras();
        if(!extras.containsKey(DEVICE)) {
            setResult(RESULT_CANCELED);
            Log.i("info", "no data for TerminalActivity");
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();

        Gson gson = new GsonBuilder().create();

        bars = findViewById(R.id.bars);
        setBarsVisible(false);

        device = gson.fromJson(intent.getStringExtra(DEVICE), Device.class);
        in = Lab240.getOutPath(device, DeviceTypes.mainIn);
        out = Lab240.getOutPath(device, DeviceTypes.mainOut);
        log = Lab240.getOutPath(device, DeviceTypes.log);

        for (Out o : device.getOuts()) {
            String path = Lab240.getOutPath(device, o);
            Lab240.getMqtt().subscribe(path, 0, ListActivity.KEY, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
            MQTT.MessageCallback mc = (topic, msg) -> {
                values.put(Pair.create(device.getIdentificator(), o), Pair.create(msg.toString(), System.currentTimeMillis()));
                updateValues(device.getIdentificator(), o);
            };
            Lab240.getMqtt().addListener(path, mc);
            callbacks.add(Pair.create(path, mc));
        }
        for (Out o : device.getRelays()) {
            String path = Lab240.getOutPath(device, o);
            Lab240.getMqtt().subscribe(path, 0, ListActivity.KEY, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                }
            });
            MQTT.MessageCallback mc = (topic, msg) -> {
                values.put(Pair.create(device.getIdentificator(), o), Pair.create(msg.toString(), System.currentTimeMillis()));
                updateValues(device.getIdentificator(), o);
            };
            Lab240.getMqtt().addListener(path, mc);
            callbacks.add(Pair.create(path, mc));
        }
        ia = new TerminalItemAdapter(updaters, values);
        this.relays = findViewById(R.id.relays);
        this.relays.setAdapter(ia);
        ia.setData(device.getIdentificator(),device.getRelays(), device.getOuts());

        outlines.addAll(device.getConsoleLasts());


        outsView = findViewById(R.id.outlines);
        RecyclerView setterHints = findViewById(R.id.setters), getterHints = findViewById(R.id.getters), historyHints = findViewById(R.id.history);
        Button send = findViewById(R.id.send);
        EditText cmd = findViewById(R.id.cmd);

        ta = new TerminalAdapter();
        outsView.setAdapter(ta);


        HintAdapter historyHintAdapter = new HintAdapter(getSupportFragmentManager(), str -> {
            Log.i("action", "history message \""+str+"\" selected in TerminalActivity");
            cmd.setText(cmd.getText()+(cmd.getText().length() == 0 || cmd.getText().charAt(cmd.getText().length()-1) == ' ' ? "" : " ") + str);
            cmd.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(cmd, InputMethodManager.SHOW_IMPLICIT);
            cmd.setSelection(cmd.getText().length());
        });
        historyHints.setAdapter(historyHintAdapter);

        send.setOnClickListener(view -> {
            String c = cmd.getText().toString();
            if(c.matches("^\\s*$")) {
                cmd.setText("");
                return;
            }
            Log.i("action", "\""+c+"\" sended in TerminalActivity");
            cmd.setText("");
            send(c);
            hints.removeAll(Collections.singletonList(new Hint(c)));
            hints.addFirst(new Hint(c));
            historyHintAdapter.setData(hints);
        });

        HintAdapter setterHintAdapter = new HintAdapter(getSupportFragmentManager(), str -> {
            Log.i("action", "setter \""+str+"\" selected in TerminalActivity");
            cmd.setText(cmd.getText()+(cmd.getText().length() == 0 || cmd.getText().charAt(cmd.getText().length()-1) == ' ' ? "" : " ") + str);
            cmd.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(cmd, InputMethodManager.SHOW_IMPLICIT);
            cmd.setSelection(cmd.getText().length());
        });
        setterHintAdapter.setData(Lab240.getDeviceTypes().get(device.getType()).getSetterHints());
        setterHints.setAdapter(setterHintAdapter);

        HintAdapter getterHintAdapter = new HintAdapter(getSupportFragmentManager(), str -> {
            Log.i("action", "getter \""+str+"\" selected in TerminalActivity");
            cmd.setText(cmd.getText()+(cmd.getText().length() == 0 || cmd.getText().charAt(cmd.getText().length()-1) == ' ' ? "" : " ") + str);
            cmd.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(cmd, InputMethodManager.SHOW_IMPLICIT);
            cmd.setSelection(cmd.getText().length());
        });
        getterHintAdapter.setData(Lab240.getDeviceTypes().get(device.getType()).getGetterHints());
        getterHints.setAdapter(getterHintAdapter);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(device.getName());
        }

        lcc = cause -> handleNoConnection();
        prepareTopics();

        update(outlines);

    }

    public synchronized void updateValues(String device, Out out) {
        Pair<String, Out> p = Pair.create(device, out);
        String str = values.containsKey(p) ? values.get(p).first : null;
        for (ItemHolder.Updater i : updaters.get(p)) {
            i.update(str);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("call", "Destroy TerminalActivity");
        Lab240.getMqtt().removeListener(out, outCallback);
        Lab240.getMqtt().unsubscribe(out, KEY);
        Lab240.getMqtt().removeListener(in, inCallback);
        Lab240.getMqtt().unsubscribe(in, KEY);
        Lab240.getMqtt().removeListener(log, logCallback);
        Lab240.getMqtt().unsubscribe(log, KEY);
    }

    public void send(String value){
        Log.i("call", "send in TerminalActivity");
        if(Lab240.getMqtt().isConnected())
            Lab240.getMqtt().send(in, value, 0);
        else {
            handleNoConnection();
            return;
        }
        update(this.outlines);
    }

    private static String clearMsg(String str){
        return str.replaceAll("^\\s+", "").replaceAll("\\s+$", "");
    }

    public void handleNoConnection(){
        Log.i("call", "Handling no connection in TerminalActivity");
        AlertSheetDialog asd = new AlertSheetDialog(this);
        asd.addText(getString(R.string.connection_lost));
        asd.setCancelButtonText(getString(R.string.ok), AlertSheetDialog.ButtonType.DEFAULT);
        asd.setCancelAction(v->finish());
        asd.show(getSupportFragmentManager(), "");
    }

    private MQTT.LostConnectionCallback lcc = null;
    public static final long MAX_NO_MSG_TIME = 1000 * 60 * 5;

    public void checkOutValues(){
        for(Map.Entry<Pair<String, Out>, Pair<String, Long>> v : new HashSet<>(values.entrySet())){
            if(System.currentTimeMillis()-v.getValue().second >= MAX_NO_MSG_TIME){
                values.remove(v.getKey());
                updateValues(device.getIdentificator(), v.getKey().second);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("call", "Pause ListActivity");
        if(lcc != null)
            Lab240.getMqtt().removeOnConnectionLostCallback(lcc);
        if(updateTimer != null)
            updateTimer.cancel();
    }

    public final static int UPDATE_PERIOD = 1000*60*5;

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("call", "Resume ListActivity");
        if(lcc != null)
            Lab240.getMqtt().addOnConnectionLostCallback(lcc);
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.i("action", "Autocheck in TerminalActivity");
                runOnUiThread(TerminalActivity.this::checkOutValues);
            }
        }, UPDATE_PERIOD, UPDATE_PERIOD);
    }

    public void setResult(List<OutLine> outs){
        List<OutLine> outLines = outs.subList(Math.max(0, outs.size() - Device.SAVE_COUNT), outs.size());
        String result = Lab240.serializeOutLines(outLines);
        Intent intent = new Intent();
        intent.putExtra(RESULT, result);
        intent.putExtra(ID, device.getId());
        setResult(RESULT_OK, intent);
    }

    public void prepareTopics(){
        Log.i("call", "prepareTopics in ListActivity");
        if(!Lab240.getMqtt().isConnected()){
            return;
        }
        Lab240.getMqtt().subscribe(out, 0, KEY);
        outCallback = (topic, msg) -> {
            SimpleDateFormat sdf = new SimpleDateFormat(OUT_DATE_FORMAT, Locale.getDefault());
            this.outlines.add(new OutLine(sdf.format(new Date()) + clearMsg(msg.toString()), OutLine.Type.OUT));
            update(this.outlines);
            outsView.smoothScrollToPosition(this.outlines.size()-1);
        };
        Lab240.getMqtt().addListener(out, outCallback);

        Lab240.getMqtt().subscribe(in, 0, KEY);
        inCallback = (topic, msg) -> {
            SimpleDateFormat sdf = new SimpleDateFormat(IN_DATE_FORMAT, Locale.getDefault());
            this.outlines.add(new OutLine(sdf.format(new Date()) + clearMsg(msg.toString()), OutLine.Type.IN));
            update(this.outlines);
            outsView.smoothScrollToPosition(this.outlines.size()-1);
        };
        Lab240.getMqtt().addListener(in, inCallback);

        Lab240.getMqtt().subscribe(log, 0, KEY);
        logCallback = (topic, msg) -> {
            SimpleDateFormat sdf = new SimpleDateFormat(LOG_DATE_FORMAT, Locale.getDefault());
            this.outlines.add(new OutLine(sdf.format(new Date()) + clearMsg(msg.toString()), OutLine.Type.LOG));
            update(this.outlines);
            outsView.smoothScrollToPosition(this.outlines.size()-1);
        };
        Lab240.getMqtt().addListener(log, inCallback);
    }

    public void update(List<OutLine> data){
        Log.i("call", "update in ListActivity");
        ta.setData(data);
        setResult(data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.terminal_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            Log.i("action", "return from ListActivity");
            onBackPressed();
            return true;
        }else if(item.getItemId() == R.id.showBars) {
            Log.i("action", (areBarsVisible ? "hide" : "show") + " bars ListActivity");
            setBarsVisible(!areBarsVisible);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    boolean areBarsVisible = false;
    public void setBarsVisible(boolean b){
        areBarsVisible = b;
        bars.setVisibility(b ? View.VISIBLE : View.GONE);
    }

}