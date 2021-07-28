package com.lab240.lab240;

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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.lab240.devices.Device;
import com.lab240.devices.Devices;
import com.lab240.devices.Out;
import com.lab240.lab240.adapters.GroupAdapter;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.utils.Container;
import com.lab240.utils.Lab240;
import com.lab240.utils.MQTT;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    GroupAdapter ga;

    public static final int KEY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        if(!Lab240.isInited())
            finish();

        RecyclerView rv = findViewById(R.id.groups);
        Container<Runnable> update = new Container<>(null);
        ga = new GroupAdapter((device)->{
            Intent i = new Intent(this, TerminalActivity.class);
            i.putExtra(TerminalActivity.HINTS, device.getType().hints);
            String prefix = "/"+Lab240.getMqtt().getName()+"/"+device.getName()+"/";
            i.putExtra(TerminalActivity.IN, prefix+device.getType().mainIn);
            i.putExtra(TerminalActivity.OUT, prefix+device.getType().mainOut);
            i.putExtra(TerminalActivity.LOG, prefix+device.getType().log);
            i.putExtra(TerminalActivity.DEVICE, device.getName());
            startActivity(i);
        },() -> update.get().run());
        update.set(()->ga.setData(Lab240.getDevices()));
        rv.setAdapter(ga);
        ga.setData(Lab240.getDevices());

        lcc = cause -> {
            handleNoConnection();
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.list_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.add:
                addDevice();
                break;
            case R.id.exit:
                exit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void addDevice(){
        AlertSheetDialog asd2 = new AlertSheetDialog(this);
        EditText name = asd2.addTextInput("Название");
        name.setSingleLine(true);
        EditText group = asd2.addTextInput("Группа");
        group.setSingleLine(true);

        List<String> devicesString = new ArrayList<>();
        for(Devices d : Devices.values()) {
            devicesString.add(d.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, devicesString){
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View dropDownView = super.getDropDownView(position, convertView, parent);
                ((TextView)dropDownView).setGravity(Gravity.CENTER);
                return dropDownView;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((TextView)view).setGravity(Gravity.CENTER);
                return view;
            }
        };
        List<Out> outs = new ArrayList<>();

        LinearLayout outsLayout = asd2.addView(new LinearLayout(this));
        outsLayout.setOrientation(LinearLayout.VERTICAL);
        outsLayout.setGravity(Gravity.CENTER);
        ViewGroup.LayoutParams layoutParams = outsLayout.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        outsLayout.setLayoutParams(layoutParams);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = asd2.addView(new Spinner(this));
        spinner.setAdapter(adapter);
        spinner.setPrompt("Устройство");
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                outsLayout.removeAllViews();
                outs.clear();
                Devices devices = Devices.values()[spinner.getSelectedItemPosition()];
                for(Out o : devices.outs){
                    CheckBox cb = new CheckBox(view.getContext());
                    cb.setOnCheckedChangeListener((compoundButton, b) -> {
                        if(b)
                            outs.add(o);
                        else{
                            outs.remove(o);
                        }
                    });
                    cb.setText(o.getName());
                    outsLayout.addView(cb);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        Button doneButton = asd2.addButton("Создать", () -> {
            long id = System.currentTimeMillis();
            Device d = new Device(name.getText().toString(), group.getText().toString(), id, Devices.values()[spinner.getSelectedItemPosition()]);
            d.getOuts().addAll(outs);
            Lab240.getDevices().add(d);
            ga.setData(Lab240.getDevices());
            Lab240.saveDevices(this, Lab240.getDevices());
        }, AlertSheetDialog.ButtonType.DEFAULT);
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
        asd.addButton("Подключиться", ()->{
            Lab240.getMqtt().connect(this, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    ga.setData(Lab240.getDevices());
                    asd.dismiss();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });
        }, AlertSheetDialog.ButtonType.DEFAULT);
        asd.addButton("Выйти", this::exit, AlertSheetDialog.ButtonType.DESTROY);
        asd.show();
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
        System.exit(0);
    }
}