package com.lab240.lab240;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lab240.devices.Device;
import com.lab240.devices.DeviceTypes;
import com.lab240.devices.Lab240;
import com.lab240.devices.MQTT;
import com.lab240.devices.Out;
import com.lab240.devices.OutLine;
import com.lab240.lab240.adapters.DeviceHolder;
import com.lab240.lab240.adapters.GroupAdapter;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.utils.Comparator;
import com.lab240.utils.GravityArrayAdapter;
import com.lab240.utils.ShowableAdapter;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class ListActivity extends AppCompatActivity {

    GroupAdapter ga;
    ImageView status;
    public static final int KEY = 2;

    ActivityResultLauncher<Intent> consoleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if(data == null || !data.hasExtra(TerminalActivity.RESULT) || !data.hasExtra(TerminalActivity.ID)) {
                        Log.i("info", String.format("No data: (data == null: %b, no RESULT: %b, no ID: %b)", data == null,data != null && !data.hasExtra(TerminalActivity.RESULT), data != null && !data.hasExtra(TerminalActivity.ID)));
                        return;
                    }else{
                        Log.i("info", "Result in ListActivity from terminal = "+data.getDataString());
                    }
                    long id = data.getLongExtra(TerminalActivity.ID, 0);
                    Gson gson = new GsonBuilder().create();
                    List<OutLine> res = gson.fromJson(data.getStringExtra(TerminalActivity.RESULT), new TypeToken<List<OutLine>>(){}.getType());
                    for(Device d : Lab240.getDevices()) {
                        if(d.getId() == id) {
                            d.getConsoleLasts().clear();
                            d.getConsoleLasts().addAll(res);
                            break;
                        }
                    }
                    Lab240.saveDevices(this, Lab240.getDevices());
                }
            });

    protected void changeActivity(Class<?> c){
        Intent i = new Intent(this, c);
        i.putExtras(getIntent());
        i.setData(getIntent().getData());
        i.setAction(getIntent().getAction());
        startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Log.i("call", "Create ListActivity");
        if(!Lab240.isInited()) {
            finish();
            Log.i("info", "Lab240 is not inited on creating ListActivity");
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        status = findViewById(R.id.status);

        RecyclerView groups = findViewById(R.id.groups);
        ga = new GroupAdapter(getSupportFragmentManager(), new DeviceHolder.Functions() {
            @Override
            public void call(Device device) {
                Log.i("action", "Call terminal in ListActivity");
                if (!ListActivity.this.hasWindowFocus()) {
                    Log.i("info", "No focus on calling terminal in ListActivity");
                    return;
                }
                if(Lab240.getMqtt() == null || !Lab240.getMqtt().isConnected()){
                    Log.i("info", "No connection on calling terminal in ListActivity");
                    AlertSheetDialog asd = new AlertSheetDialog(ListActivity.this);
                    asd.addText(getString(R.string.no_connection));
                    asd.setCancelButtonText(getString(R.string.ok), AlertSheetDialog.ButtonType.DEFAULT);
                    asd.show(getSupportFragmentManager(), "");
                    return;
                }
                Intent i = new Intent(ListActivity.this, TerminalActivity.class);
                Gson gson = new GsonBuilder().create();
                i.putExtra(TerminalActivity.DEVICE, gson.toJson(device));
                consoleLauncher.launch(i);
            }

            @Override
            public void edit(Device d) {
                Log.i("action", "Edit device in ListActivity");
                editDevice(d);
                update();
            }

            @Override
            public void delete(Device d) {
                Log.i("action", "Delete device in ListActivity");
                Lab240.getDevices().remove(d);
                Lab240.saveDevices(ListActivity.this, Lab240.getDevices());
                update();
            }

            @Override
            public void setGroup(Collection<Device> ds, String str) {
                Log.i("action", "Edit group in ListActivity");
                for(Device d: ds)
                    d.setGroup(str);
                Lab240.saveDevices(ListActivity.this, Lab240.getDevices());
                update();
            }

            @Override
            public void delete(Collection<Device> d) {
                Log.i("action", "Delete group in ListActivity");
                Lab240.getDevices().removeAll(d);
                Lab240.saveDevices(ListActivity.this, Lab240.getDevices());
                update();
            }
        });
        groups.setAdapter(ga);
        update();
        lcc = cause -> handleNoConnection(true);

        Intent i = getIntent();
        if(Intent.ACTION_VIEW.equals(i.getAction()) && i.getData() != null){
            changeActivity(DeviceTypesActivity.class);
        }
    }

    public void update(){
        Log.i("call", "Update ListActivity");
        ga.updateData();
    }

    public void refresh(){
        Log.i("call", "Refresh ListActivity");
        ga.refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add) {
            Log.i("action", "Add device in ListActivity");
            editDevice();
        }else if(item.getItemId() == R.id.exit){
            Log.i("action", "Exit in ListActivity");
            AlertSheetDialog asd = new AlertSheetDialog(this);
            asd.addButton(getString(R.string.exit), view -> exit(), AlertSheetDialog.ButtonType.DESTROY);
            asd.show(getSupportFragmentManager(), "");
        }else if(item.getItemId() == R.id.edit){
            Log.i("action", "Edit in ListActivity");
            changeActivity(DeviceTypesActivity.class);
        }
        return super.onOptionsItemSelected(item);
    }

    public void editDevice(){
        editDevice(null);
    }

    public void editDevice(@Nullable Device device){
        Log.i("action", "Editing device (New = "+(device == null)+") in ListActivity");
        final boolean editing = device != null;
        //Создание диалога
        AlertSheetDialog asd2 = new AlertSheetDialog(this);
        asd2.show(getSupportFragmentManager(), "");

        //поле имени
        EditText name = asd2.addTextInput(getString(R.string.name));
        if(editing) name.setText(device.getName());
        name.setSingleLine(true);

        //поле идентификатора
        EditText iden = asd2.addTextInput(getString(R.string.id));
        iden.setSingleLine(true);
        if(editing) iden.setText(device.getIdentificator());

        //поле выбора группы
        Set<String> groups = new HashSet<>();
        for(Device d : Lab240.getDevices()){
            groups.add(d.getGroup());
        }
        List<String> groups2 = new ArrayList<>(groups);
        groups2.add(getString(R.string.new_group));
        int gr = editing ? groups2.indexOf(device.getGroup()) : -1;
        if(gr == -1)
            gr = groups2.size()-1;
        GravityArrayAdapter<String> groupAdapter = new GravityArrayAdapter<>(this, android.R.layout.simple_spinner_item, groups2);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupAdapter.setGravity(Gravity.CENTER);
        Spinner groupSpinner = asd2.addView(new Spinner(this));
        groupSpinner.setAdapter(groupAdapter);
        groupSpinner.setPrompt(getString(R.string.group));

        //поле добавление группы
        EditText group = asd2.addTextInput(getString(R.string.group_name));
        group.setSingleLine(true);
        if(editing) group.setText(device.getGroup());

        //выбор типа
        ArrayList<DeviceTypes> typesList = new ArrayList<>(Lab240.getDeviceTypes().values());
        ShowableAdapter<DeviceTypes> typeAdapter = new ShowableAdapter<>(this, android.R.layout.simple_spinner_item, typesList);
        typeAdapter.setGravity(Gravity.CENTER);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner type = new Spinner(this);

        //реле
        List<Out> relays = new ArrayList<>();
        LinearLayout relaysLayout = new LinearLayout(this);
        asd2.addView(relaysLayout);
        relaysLayout.setOrientation(LinearLayout.VERTICAL);
        relaysLayout.setGravity(Gravity.CENTER);
        relaysLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        relaysLayout.setPadding(
                relaysLayout.getPaddingLeft(),
               0,
                relaysLayout.getPaddingRight(),
                0);

        List<Out> customRelays = new ArrayList<>();
        if(editing) {
            customRelays.addAll(device.getRelays());
            customRelays.removeAll(Lab240.getDeviceTypes().get(device.getType()).getRelays());
        }

        //сенсоры
        List<Out> outs = new ArrayList<>();
        LinearLayout outsLayout = new LinearLayout(this);
        asd2.addView(outsLayout);
        outsLayout.setOrientation(LinearLayout.VERTICAL);
        outsLayout.setGravity(Gravity.CENTER);
        outsLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        outsLayout.setPadding(
                outsLayout.getPaddingLeft(),
                outsLayout.getPaddingTop(),
                outsLayout.getPaddingRight(),
                0);

        List<Out> customOuts = new ArrayList<>();
        if(editing) {
            customOuts.addAll(device.getOuts());
            customOuts.removeAll(Lab240.getDeviceTypes().get(device.getType()).getOuts());
        }

        //выбор типа устройства
        asd2.addView(type);
        type.setAdapter(typeAdapter);
        type.setPrompt(getString(R.string.device));

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DeviceTypes devices = (DeviceTypes) type.getSelectedItem();

                outsLayout.removeAllViews();
                relaysLayout.removeAllViews();
                outs.clear();
                relays.clear();

                List<Out> outs1 = new ArrayList<>(customOuts);
                outs1.removeAll(devices.getOuts());
                outs1.addAll(devices.getOuts());
                Collections.sort(outs1);

                List<Out> relays1 = new ArrayList<>(customRelays);
                relays1.removeAll(devices.getRelays());
                relays1.addAll(devices.getRelays());
                Collections.sort(relays1);

                for(Out o : outs1){
                    CheckBox cb = new CheckBox(view.getContext());
                    cb.setOnCheckedChangeListener((compoundButton, b) -> {
                        if(b)
                            outs.add(o);
                        else{
                            outs.remove(o);
                            customOuts.remove(o);
                            if(!devices.getOuts().contains(o)) {
                                outsLayout.removeView(cb);
                            }
                        }
                    });
                    cb.setChecked(!editing || customOuts.contains(o) || device.getOuts().contains(o) || device.getType() != devices.getId());
                    cb.setText(o.getName());
                    outsLayout.addView(cb);
                }

                for(Out o : relays1){
                    CheckBox cb = new CheckBox(view.getContext());
                    cb.setOnCheckedChangeListener((compoundButton, b) -> {
                        if(b)
                            relays.add(o);
                        else{
                            relays.remove(o);
                            customRelays.remove(o);
                            if(!devices.getRelays().contains(o)) {
                                relaysLayout.removeView(cb);
                            }
                        }
                    });
                    cb.setChecked(!editing || customRelays.contains(o) || device.getRelays().contains(o) || (device.getType() != devices.getId()));
                    cb.setText(o.getName());
                    relaysLayout.addView(cb);
                }


                //добавление нового реле
                View newRelay = getLayoutInflater().inflate(R.layout.inflate_new_out_view, relaysLayout, false);
                relaysLayout.addView(newRelay);
                newRelay.setPadding(
                        newRelay.getPaddingLeft(),
                        0,
                        newRelay.getPaddingRight(),
                        newRelay.getPaddingBottom());
                EditText newRelayText = newRelay.findViewById(R.id.name);
                newRelayText.setHint(R.string.new_relay_placeholder);
                Runnable addRelay = ()->{
                    if(newRelayText.getText().length() != 0){

                        String pth = newRelayText.getText().toString();
                        pth = pth.replaceAll("^/*", "");
                        pth = pth.replaceAll("/*$", "");
                        pth = pth.replaceAll("/+", "/");
                        ArrayList<String> path = new ArrayList<>(Arrays.asList(pth.split("/")));
                        String outName = path.get(path.size()-1);
                        path.remove(path.size()-1);
                        Out o = new Out(outName, path);
                        if(!((DeviceTypes)type.getSelectedItem()).getRelays().contains(o)) {
                            CheckBox cb = new CheckBox(newRelay.getContext());
                            cb.setOnCheckedChangeListener((compoundButton, b2) -> {
                                if (b2)
                                    relays.add(o);
                                else {
                                    relays.remove(o);
                                    customRelays.remove(o);
                                    relaysLayout.removeView(cb);
                                }
                            });
                            cb.setChecked(true);
                            cb.setText(o.getName());
                            customRelays.add(o);
                            relaysLayout.addView(cb, relaysLayout.getChildCount()-1);
                        }
                        newRelayText.setText("");
                        newRelayText.clearFocus();
                        InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(newRelayText.getWindowToken(), 0);
                    }
                };
                newRelayText.setOnFocusChangeListener((view1, b) -> {
                    if(!b){
                        addRelay.run();
                    }
                });
                newRelayText.setOnEditorActionListener((textView, i12, keyEvent) -> {
                    if (EditorInfo.IME_ACTION_DONE == i12) {
                        addRelay.run();
                    }
                    return false;
                });

                //добавление нового сенсора
                View newOut = getLayoutInflater().inflate(R.layout.inflate_new_out_view, outsLayout, false);
                outsLayout.addView(newOut);
                newOut.setPadding(
                        newOut.getPaddingLeft(),
                        0,
                        newOut.getPaddingRight(),
                        newOut.getPaddingBottom());
                EditText newOutText = newOut.findViewById(R.id.name);
                newOutText.setHint(R.string.new_out_placeholder);
                Runnable addOut = ()->{
                    if(newOutText.getText().length() != 0){
                        String pth = newOutText.getText().toString();
                        pth = pth.replaceAll("^/*", "");
                        pth = pth.replaceAll("/*$", "");
                        pth = pth.replaceAll("/+", "/");
                        ArrayList<String> path = new ArrayList<>(Arrays.asList(pth.split("/")));
                        String outName = path.get(path.size()-1);
                        path.remove(path.size()-1);
                        Out o = new Out(outName, path);
                        if(!((DeviceTypes)type.getSelectedItem()).getOuts().contains(o)) {
                            CheckBox cb = new CheckBox(newOut.getContext());
                            cb.setOnCheckedChangeListener((compoundButton, b2) -> {
                                if (b2)
                                    outs.add(o);
                                else {
                                    outs.remove(o);
                                    customOuts.remove(o);
                                    outsLayout.removeView(cb);
                                }
                            });
                            cb.setChecked(true);
                            cb.setText(o.getName());
                            customOuts.add(o);
                            outsLayout.addView(cb, outsLayout.getChildCount()-1);
                        }
                        newOutText.setText("");
                        newOutText.clearFocus();
                        InputMethodManager imm= (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(newOutText.getWindowToken(), 0);
                    }
                };
                newOutText.setOnFocusChangeListener((view1, b) -> {
                    if(!b){
                        addOut.run();
                    }
                });
                newOutText.setOnEditorActionListener((textView, i12, keyEvent) -> {
                    if (EditorInfo.IME_ACTION_DONE == i12) {
                        addOut.run();
                    }
                    return false;
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        if(device == null){
            type.setSelection(typesList.indexOf(DeviceTypes.EMPTY));
        }else{
            int res = typesList.indexOf(Lab240.getDeviceTypes().get(device.getType()));
            if(res == -1)
                res = typesList.indexOf(DeviceTypes.EMPTY);
            type.setSelection(res);
        }

        //кнопка добавления
        Button doneButton;
        if(!editing) {
            doneButton = asd2.addButton(getString(R.string.create), btn -> {
                long id = System.currentTimeMillis();
                Device d = new Device(name.getText().toString(), iden.getText().toString(),
                        groupSpinner.getSelectedItemPosition() != groupSpinner.getCount() - 1 ?
                                groups2.get(groupSpinner.getSelectedItemPosition()) :
                                group.getText().toString(),
                        id, ((DeviceTypes) type.getSelectedItem()).getId());
                d.getOuts().addAll(outs);
                d.getRelays().addAll(relays);
                Lab240.getDevices().add(d);
                update();
                Lab240.saveDevices(this, Lab240.getDevices());
            }, AlertSheetDialog.ButtonType.DEFAULT);
        }else{
            doneButton = asd2.addButton(getString(R.string.edit), btn -> {
                device.getOuts().clear();
                device.getOuts().addAll(outs);

                device.getRelays().clear();
                device.getRelays().addAll(relays);

                device.setName(name.getText().toString());
                device.setIdentificator(iden.getText().toString());
                device.setType(((DeviceTypes) type.getSelectedItem()).getId());
                device.setGroup(groupSpinner.getSelectedItemPosition() != groupSpinner.getCount()-1 ?
                        groups2.get(groupSpinner.getSelectedItemPosition()) :
                        group.getText().toString());

                update();
                Lab240.saveDevices(this, Lab240.getDevices());
            }, AlertSheetDialog.ButtonType.DEFAULT);
        }

        //проверка заполненности полей
        Runnable check = ()->doneButton.setEnabled(name.getText().length() != 0 && iden.getText().length() != 0 && (groupSpinner.getSelectedItemPosition() != groupSpinner.getCount()-1 || group.getText().length() != 0));

        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                check.run();
            }
        };
        name.addTextChangedListener(tw);
        group.addTextChangedListener(tw);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                group.setVisibility(groupSpinner.getSelectedItemPosition() != groupSpinner.getCount()-1 ? View.GONE : View.VISIBLE);
                check.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                check.run();
            }
        });
        groupSpinner.setSelection(gr);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("call", "Destroy ListActivity");
        if(Lab240.getMqtt() != null) {
            for (String topic : Lab240.getMqtt().getSubscriptions())
                Lab240.getMqtt().unsubscribe(topic, KEY);
            for (Pair<String, MQTT.MessageCallback> p : ga.callbacks)
                Lab240.getMqtt().removeListener(p.first, p.second);
        }
    }

    Timer reconnectTimer = null;
    Timer updateTimer = null;

    public final static int UPDATE_PERIOD = 1000*60*5;
    public final static int RECONNECTION_PERIOD = 5000;

    public void handleNoConnection(boolean showMsg){
        Log.i("call", "Handling no connection in ListActivity");
        status.setImageResource(R.drawable.relay_off_image);
        update();
        if(showMsg) {
            Log.i("info", "Showing alert on no connection in ListActivity");
            AlertSheetDialog asd = new AlertSheetDialog(this);
            asd.addText(getString(R.string.connection_lost));
            asd.setCancelButtonText(getString(R.string.ok), AlertSheetDialog.ButtonType.DEFAULT);
            asd.show(getSupportFragmentManager(), "");
        }
        reconnectTimer = new Timer();
        reconnectTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.i("action", "Reconnect in ListActivity");
                reconnect();
            }
        }, 0, RECONNECTION_PERIOD);
    }

    public void reconnect(){
        Log.i("call", "Reconnect in ListActivity");
        Lab240.getMqtt().connect(this, new IMqttActionListener() {
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i("info", "Successful connection in reconnect() in ListActivity");
                update();
                status.setImageResource(R.drawable.relay_on_image);
                if(reconnectTimer != null) reconnectTimer.cancel();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i("info", "Failed connection in reconnect() in ListActivity");
            }
        });
    }

    private MQTT.LostConnectionCallback lcc = null;

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("call", "Pause ListActivity");
        if(updateTimer != null)
            updateTimer.cancel();
        if(lcc != null && Lab240.getMqtt() != null)
            Lab240.getMqtt().removeOnConnectionLostCallback(lcc);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("call", "Resume ListActivity");
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.i("action", "Auto refresh in ListActivity");
                runOnUiThread(ListActivity.this::refresh);
            }
        }, UPDATE_PERIOD, UPDATE_PERIOD);
        if(lcc != null && Lab240.getMqtt() != null)
            Lab240.getMqtt().addOnConnectionLostCallback(lcc);
        if(Lab240.getMqtt() == null || !Lab240.getMqtt().isConnected())
            handleNoConnection(false);
        update();
    }

    private void exit(){
        Log.i("call", "Exit in ListActivity");
        Lab240.exit(this);
        finish();
    }
}