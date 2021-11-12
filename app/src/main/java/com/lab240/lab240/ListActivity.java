package com.lab240.lab240;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lab240.devices.Device;
import com.lab240.devices.Devices;
import com.lab240.devices.Out;
import com.lab240.devices.OutLine;
import com.lab240.lab240.adapters.DeviceHolder;
import com.lab240.utils.GravityArrayAdapter;
import com.lab240.lab240.adapters.GroupAdapter;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.utils.Container;
import com.lab240.utils.Lab240;
import com.lab240.utils.MQTT;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ListActivity extends AppCompatActivity {

    GroupAdapter ga;

    public static final int KEY = 2;

    ActivityResultLauncher<Intent> consoleLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if(data == null || !data.hasExtra(TerminalActivity.RESULT) || !data.hasExtra(TerminalActivity.ID))
                        return;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        if(!Lab240.isInited())
            finish();

        RecyclerView groups = findViewById(R.id.groups);
        ga = new GroupAdapter(getSupportFragmentManager(), new DeviceHolder.Functions() {
            @Override
            public void call(Device device) {
                if (!ListActivity.this.hasWindowFocus())
                    return;
                Intent i = new Intent(ListActivity.this, TerminalActivity.class);
                i.putExtra(TerminalActivity.TYPE, device.getType().ordinal());
                i.putExtra(TerminalActivity.DEVICE, device.getIdentificator());
                i.putExtra(TerminalActivity.ID, device.getId());
                i.putExtra(TerminalActivity.OUTLINES, Lab240.serializeOutLines(device.getConsoleLasts()));
                consoleLauncher.launch(i);
            }

            @Override
            public void edit(Device d) {
                editDevice(d);
                update();
            }

            @Override
            public void delete(Device d) {
                Lab240.getDevices().remove(d);
                Lab240.saveDevices(ListActivity.this, Lab240.getDevices());
                update();
            }

            @Override
            public void setGroup(Collection<Device> ds, String str) {
                for(Device d: ds)
                    d.setGroup(str);
                Lab240.saveDevices(ListActivity.this, Lab240.getDevices());
                update();
            }

            @Override
            public void delete(Collection<Device> d) {
                Lab240.getDevices().removeAll(d);
                Lab240.saveDevices(ListActivity.this, Lab240.getDevices());
                update();
            }
        });
        groups.setAdapter(ga);
        ga.setData(Lab240.getDevices());
        lcc = cause -> handleNoConnection();
    }

    public void update(){
        ga.setData(Lab240.getDevices());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add) {
            editDevice();
        }else if(item.getItemId() == R.id.exit){
            exit();
        }
        return super.onOptionsItemSelected(item);
    }

    public void editDevice(){
        editDevice(null);
    }

    public void editDevice(@Nullable Device device){
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

        List<String> devicesString = new ArrayList<>();
        for(Devices d : Devices.values()) {
            devicesString.add(d.name);
        }

        GravityArrayAdapter<String> typeAdapter = new GravityArrayAdapter<>(this, android.R.layout.simple_spinner_item, devicesString);
        typeAdapter.setGravity(Gravity.CENTER);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        List<Out> outs = new ArrayList<>();

        LinearLayout outsLayout = asd2.addView(new LinearLayout(this));
        outsLayout.setOrientation(LinearLayout.VERTICAL);
        outsLayout.setGravity(Gravity.CENTER);
        outsLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Spinner type = asd2.addView(new Spinner(this));
        type.setAdapter(typeAdapter);
        type.setPrompt(getResources().getString(R.string.device));
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                outsLayout.removeAllViews();
                outs.clear();
                Devices devices = Devices.values()[type.getSelectedItemPosition()];
                for(Out o : devices.outs){
                    CheckBox cb = new CheckBox(view.getContext());
                    cb.setOnCheckedChangeListener((compoundButton, b) -> {
                        if(b)
                            outs.add(o);
                        else{
                            outs.remove(o);
                        }
                    });
                    cb.setChecked(!editing || device.getOuts().contains(o));
                    cb.setText(o.getName());
                    outsLayout.addView(cb);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        if(editing) type.setSelection(device.getType().ordinal());
        else type.setSelection(0);
        type.setEnabled(!editing);

        Button doneButton;
        if(!editing) {
            doneButton = asd2.addButton(getResources().getString(R.string.create), btn -> {
                long id = System.currentTimeMillis();
                Device d = new Device(name.getText().toString(), iden.getText().toString(),
                        groupSpinner.getSelectedItemPosition() != groupSpinner.getCount() - 1 ?
                                groups2.get(groupSpinner.getSelectedItemPosition()) :
                                group.getText().toString(),
                        id, Devices.values()[type.getSelectedItemPosition()]);
                d.getOuts().addAll(outs);
                Lab240.getDevices().add(d);
                ga.setData(Lab240.getDevices());
                Lab240.saveDevices(this, Lab240.getDevices());
            }, AlertSheetDialog.ButtonType.DEFAULT);
        }else{
            doneButton = asd2.addButton(getResources().getString(R.string.edit), btn -> {
                device.getOuts().clear();
                device.getOuts().addAll(outs);

                device.setName(name.getText().toString());
                device.setIdentificator(iden.getText().toString());
                device.setGroup(groupSpinner.getSelectedItemPosition() != groupSpinner.getCount()-1 ?
                        groups2.get(groupSpinner.getSelectedItemPosition()) :
                        group.getText().toString());

                ga.setData(Lab240.getDevices());
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
        for(String topic : Lab240.getMqtt().getSubscriptions())
            Lab240.getMqtt().unsubscribe(topic, KEY);
        for(Pair<String, MQTT.MessageCallback> p : ga.callbacks)
            Lab240.getMqtt().removeListener(p.first, p.second);
    }

    public void handleNoConnection(){
        AlertSheetDialog asd = new AlertSheetDialog(this);
        asd.setCancelable(false);
        asd.setCloseOnAction(false);
        asd.addText(getResources().getString(R.string.no_connection));
        asd.addButton(getResources().getString(R.string.connect), btn-> Lab240.getMqtt().connect(this, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                ga.setData(Lab240.getDevices());
                asd.dismiss();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {}
        }), AlertSheetDialog.ButtonType.DEFAULT);
        asd.addButton(getResources().getString(R.string.exit), btn->exit(), AlertSheetDialog.ButtonType.DESTROY);
        asd.show(getSupportFragmentManager(), "");
    }

    private MQTT.LostConnectionCallback lcc = null;

    @Override
    protected void onPause() {
        super.onPause();
        if(lcc != null)
            Lab240.getMqtt().removeOnConnectionLostCallback(lcc);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(lcc != null)
            Lab240.getMqtt().addOnConnectionLostCallback(lcc);
    }

    private void exit(){
        Lab240.exit(this);
        finish();
    }
}