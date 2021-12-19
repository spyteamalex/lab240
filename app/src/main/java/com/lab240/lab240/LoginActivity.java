package com.lab240.lab240;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.common.base.Optional;
import com.lab240.devices.Device;
import com.lab240.devices.DeviceTypes;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.utils.Lab240;
import com.lab240.utils.MQTT;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class LoginActivity extends AppCompatActivity {

    EditText name, pass;
    ViewGroup loginLayout;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.i("call", "Create LoginActivity");
        name = findViewById(R.id.name);
        pass = findViewById(R.id.pass);
        loginLayout = findViewById(R.id.loginLayout);
        progressBar = findViewById(R.id.progressBar);
        Button next = findViewById(R.id.next);
        next.setOnClickListener(this::next);

        loginLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        Optional<Lab240.Config> config = Lab240.getConfig(this);
        Lab240.Config conf;
        if((conf = config.orNull()) != null){
            Log.i("info", "Autocheck in LoginActivity");
            check(conf.name, conf.pass, conf.devices, conf.hiddenGroups, conf.deviceTypes,true);
        }else{
            loginLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }


    protected void next(View v){
        Log.i("call", "Next in LoginActivity");
        check(name.getText().toString(), pass.getText().toString(), new ArrayList<>(), new TreeSet<>(), Lab240.DEFAULT_TYPES, true);
    }

    protected void check(String name, String pass, List<Device> devices, Set<String> groups, Map<Long, DeviceTypes> deviceTypes, boolean openFailDialog){
        Log.i("call", "Check in LoginActivity");
        loginLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        MQTT mqtt = new MQTT(getResources().getString(R.string.server), name, pass);
        mqtt.connect(this, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.i("info", "Successful connection in LoginActivity");
                Lab240.Config c = new Lab240.Config(name, pass, devices, groups, deviceTypes);
                Lab240.setConfig(mqtt, c);

                Lab240.saveConfig(LoginActivity.this, c);
                Intent i = new Intent(LoginActivity.this, ListActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.i("info", "Failed connection in LoginActivity");
                loginLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                if(openFailDialog) {
                    Log.i("info", "Showing dialog in check() in LoginActivity");
                    AlertSheetDialog asd = new AlertSheetDialog(LoginActivity.this);
                    asd.setCloseOnAction(false);
                    asd.addText(getResources().getString(R.string.login_fail));
                    asd.addButton(getResources().getString(R.string.try_again), btn -> check(name, pass, devices, groups, deviceTypes,false), AlertSheetDialog.ButtonType.DEFAULT);
                    asd.setCancelButtonText(getResources().getString(R.string.cancel), AlertSheetDialog.ButtonType.DESTROY);
                    asd.show(getSupportFragmentManager(), "");
                }
            }
        });
    }
}