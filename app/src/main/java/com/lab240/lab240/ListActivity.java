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

    ActivityResultLauncher<Intent> filePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getData() != null) {
                    Log.i("info", "File is picked");
                    openFile(result.getData().getData());
                }else{
                    Log.i("info", "File is not picked");
                }
            });

    protected void openFile(Uri uri){
        StringBuilder sb = new StringBuilder();
        try (BufferedReader inputStream = new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(uri)))){
            int c;
            while ((c = inputStream.read()) != -1) {
                sb.append((char) c);
            }

        } catch (IOException e) {
            Toast.makeText(this, R.string.import_error, Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        Pair<List<Device>, Map<Long, DeviceTypes>> res = Lab240.fromDeviceConfig(sb.toString());
        Lab240.getDeviceTypes().putAll(res.second);
        Lab240.getDevices().addAll(res.first);
        Lab240.saveDevices(ListActivity.this, Lab240.getDevices());
        Lab240.saveDeviceTypes(ListActivity.this, Lab240.getDeviceTypes());
        update();
        Toast.makeText(this, R.string.import_ok, Toast.LENGTH_LONG).show();
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
                    asd.addText(getResources().getString(R.string.no_connection));
                    asd.setCancelButtonText(getResources().getString(R.string.ok), AlertSheetDialog.ButtonType.DEFAULT);
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
            Log.i("action", "Opening file from intent");
            openFile(i.getData());
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
            exit();
        }else if(item.getItemId() == R.id.export){
            Log.i("action", "Export in ListActivity");

            File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Config.lab240");
            for (int i = 1; f.exists(); i++){
                f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Config("+i+").lab240");
            }
            try {
                f.createNewFile();
                try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)))) {
                    out.write(Lab240.toDeviceConfig(Lab240.getDevices(), Lab240.getDeviceTypes()));
                }
                ((DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE)).addCompletedDownload(f.getName(),f.getName(),true,"application/json", f.getAbsolutePath(), f.length(),true);
                Toast.makeText(this, getString(R.string.export_ok) + " " + f.getName(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.export_error, Toast.LENGTH_LONG).show();
            }

        }else if(item.getItemId() == R.id.imprt){
            Log.i("action", "Import in ListActivity");

            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFile.setType("*/*");
            Intent intent = Intent.createChooser(chooseFile, getString(R.string.choose_config));
            filePicker.launch(intent);
        }
        return super.onOptionsItemSelected(item);
    }
    public void editDevice(){
        editDevice(null);
    }

    //todo подчистить, добавить комментарии
    public void editDevice(@Nullable Device device){
        Log.i("action", "Editing device (New = "+(device == null)+") in ListActivity");
        final boolean editing = device != null;
        AlertSheetDialog asd2 = new AlertSheetDialog(this);
        asd2.show(getSupportFragmentManager(), "");
        EditText name = asd2.addTextInput(getResources().getString(R.string.name));
        if(editing) name.setText(device.getName());
        name.setSingleLine(true);
        EditText iden = asd2.addTextInput(getResources().getString(R.string.id));
        iden.setSingleLine(true);
        if(editing) iden.setText(device.getIdentificator());

        Set<String> groups = new HashSet<>();
        for(Device d : Lab240.getDevices()){
            groups.add(d.getGroup());
        }
        List<String> groups2 = new ArrayList<>(groups);
        groups2.add(getResources().getString(R.string.new_group));
        int gr = editing ? groups2.indexOf(device.getGroup()) : -1;
        if(gr == -1)
            gr = groups2.size()-1;
        GravityArrayAdapter<String> groupAdapter = new GravityArrayAdapter<>(this, android.R.layout.simple_spinner_item, groups2);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupAdapter.setGravity(Gravity.CENTER);
        Spinner groupSpinner = asd2.addView(new Spinner(this));
        groupSpinner.setAdapter(groupAdapter);
        groupSpinner.setPrompt(getResources().getString(R.string.group));

        EditText group = asd2.addTextInput(getResources().getString(R.string.group_name));
        group.setSingleLine(true);
        if(editing) group.setText(device.getGroup());

        ArrayList<DeviceTypes> typesList = new ArrayList<>(Lab240.getDeviceTypes().values());
        ShowableAdapter<DeviceTypes> typeAdapter = new ShowableAdapter<>(this, android.R.layout.simple_spinner_item, typesList);
        typeAdapter.setGravity(Gravity.CENTER);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        List<Out> relays = new ArrayList<>();
        LinearLayout relaysLayout = new LinearLayout(this);
        asd2.addView(relaysLayout);
        relaysLayout.setOrientation(LinearLayout.VERTICAL);
        relaysLayout.setGravity(Gravity.CENTER);
        relaysLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        relaysLayout.setPadding(
                relaysLayout.getPaddingLeft(),
                relaysLayout.getPaddingTop(),
                relaysLayout.getPaddingRight(),
                0);

        List<Out> customRelays = new ArrayList<>();
        if(editing) {
            customRelays.addAll(device.getRelays());
            customRelays.removeAll(Lab240.getDeviceTypes().get(device.getType()).relays);
        }

        Spinner type = new Spinner(this);

        View newRelay = getLayoutInflater().inflate(R.layout.inflate_new_out_view, relaysLayout, false);
        asd2.addView(newRelay);
        newRelay.setPadding(
                newRelay.getPaddingLeft(),
                0,
                newRelay.getPaddingRight(),
                newRelay.getPaddingBottom());
        EditText newRelayText = newRelay.findViewById(R.id.name);
        newRelayText.setHint(R.string.new_relay_placeholder);
        Runnable addRelay = ()->{
            if(newRelayText.getText().length() != 0){
                ArrayList<String> path = new ArrayList<>(Arrays.asList(newRelayText.getText().toString().split("/")));
                String outName = path.get(path.size()-1);
                path.remove(path.size()-1);
                Out o = new Out(outName, path);
                if(!((DeviceTypes)type.getSelectedItem()).relays.contains(o)) {
                    CheckBox cb = new CheckBox(newRelay.getContext());
                    cb.setOnCheckedChangeListener((compoundButton, b2) -> {
                        if (b2)
                            relays.add(o);
                        else {
                            relays.remove(o);
                            customRelays.remove(o);
                            relaysLayout.removeView(cb);
                            relaysLayout.setVisibility(relaysLayout.getChildCount() != 0 ? View.VISIBLE : View.GONE);
                        }
                    });
                    cb.setChecked(true);
                    cb.setText(o.getName());
                    relaysLayout.setVisibility(View.VISIBLE);
                    customRelays.add(o);
                    relaysLayout.addView(cb);
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
            customOuts.removeAll(Lab240.getDeviceTypes().get(device.getType()).outs);
        }

        View newOut = getLayoutInflater().inflate(R.layout.inflate_new_out_view, outsLayout, false);
        asd2.addView(newOut);
        newOut.setPadding(
                newOut.getPaddingLeft(),
                0,
                newOut.getPaddingRight(),
                newOut.getPaddingBottom());
        EditText newOutText = newOut.findViewById(R.id.name);
        newOutText.setHint(R.string.new_out_placeholder);
        Runnable addOut = ()->{
            if(newOutText.getText().length() != 0){
                ArrayList<String> path = new ArrayList<>(Arrays.asList(newOutText.getText().toString().split("/")));
                String outName = path.get(path.size()-1);
                path.remove(path.size()-1);
                Out o = new Out(outName, path);
                if(!((DeviceTypes)type.getSelectedItem()).outs.contains(o)) {
                    CheckBox cb = new CheckBox(newOut.getContext());
                    cb.setOnCheckedChangeListener((compoundButton, b2) -> {
                        if (b2)
                            outs.add(o);
                        else {
                            outs.remove(o);
                            customOuts.remove(o);
                            outsLayout.removeView(cb);
                            outsLayout.setVisibility(outsLayout.getChildCount() != 0 ? View.VISIBLE : View.GONE);
                        }
                    });
                    cb.setChecked(true);
                    cb.setText(o.getName());
                    outsLayout.setVisibility(View.VISIBLE);
                    customOuts.add(o);
                    outsLayout.addView(cb);
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

        asd2.addView(type);
        type.setAdapter(typeAdapter);
        type.setPrompt(getResources().getString(R.string.device));

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                DeviceTypes devices = (DeviceTypes) type.getSelectedItem();

                outsLayout.removeAllViews();
                relaysLayout.removeAllViews();
                outs.clear();
                relays.clear();

                List<Out> outs1 = new ArrayList<>(customOuts);
                outs1.removeAll(devices.outs);
                outs1.addAll(devices.outs);
                Collections.sort(outs1);

                List<Out> relays1 = new ArrayList<>(customRelays);
                relays1.removeAll(devices.relays);
                relays1.addAll(devices.relays);
                Collections.sort(relays1);

                for(Out o : outs1){
                    CheckBox cb = new CheckBox(view.getContext());
                    cb.setOnCheckedChangeListener((compoundButton, b) -> {
                        if(b)
                            outs.add(o);
                        else{
                            outs.remove(o);
                            customOuts.remove(o);
                            if(!devices.outs.contains(o)) {
                                outsLayout.removeView(cb);
                                outsLayout.setVisibility(outsLayout.getChildCount() != 0 ? View.VISIBLE : View.GONE);
                            }
                        }
                    });
                    cb.setChecked(!editing || customOuts.contains(o) || device.getOuts().contains(o) || device.getType() != devices.id);
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
                            if(!devices.relays.contains(o)) {
                                relaysLayout.removeView(cb);
                                relaysLayout.setVisibility(relaysLayout.getChildCount() != 0 ? View.VISIBLE : View.GONE);
                            }
                        }
                    });
                    cb.setChecked(!editing || customRelays.contains(o) || device.getRelays().contains(o) || (device.getType() != devices.id));
                    cb.setText(o.getName());
                    relaysLayout.addView(cb);
                }

                outsLayout.setVisibility(outsLayout.getChildCount() != 0 ? View.VISIBLE : View.GONE);
                relaysLayout.setVisibility(relaysLayout.getChildCount() != 0 ? View.VISIBLE : View.GONE);
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

        Button doneButton;
        if(!editing) {
            doneButton = asd2.addButton(getResources().getString(R.string.create), btn -> {
                long id = System.currentTimeMillis();
                Device d = new Device(name.getText().toString(), iden.getText().toString(),
                        groupSpinner.getSelectedItemPosition() != groupSpinner.getCount() - 1 ?
                                groups2.get(groupSpinner.getSelectedItemPosition()) :
                                group.getText().toString(),
                        id, ((DeviceTypes) type.getSelectedItem()).id);
                d.getOuts().addAll(outs);
                d.getRelays().addAll(relays);
                Lab240.getDevices().add(d);
                update();
                Lab240.saveDevices(this, Lab240.getDevices());
            }, AlertSheetDialog.ButtonType.DEFAULT);
        }else{
            doneButton = asd2.addButton(getResources().getString(R.string.edit), btn -> {
                device.getOuts().clear();
                device.getOuts().addAll(outs);

                device.getRelays().clear();
                device.getRelays().addAll(relays);

                device.setName(name.getText().toString());
                device.setIdentificator(iden.getText().toString());
                device.setType(((DeviceTypes) type.getSelectedItem()).id);
                device.setGroup(groupSpinner.getSelectedItemPosition() != groupSpinner.getCount()-1 ?
                        groups2.get(groupSpinner.getSelectedItemPosition()) :
                        group.getText().toString());

                update();
                Lab240.saveDevices(this, Lab240.getDevices());
            }, AlertSheetDialog.ButtonType.DEFAULT);
        }


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
            asd.addText(getResources().getString(R.string.connection_lost));
            asd.setCancelButtonText(getResources().getString(R.string.ok), AlertSheetDialog.ButtonType.DEFAULT);
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
    }

    private void exit(){
        Log.i("call", "Exit in ListActivity");
        Lab240.exit(this);
        finish();
    }
}