package com.lab240.lab240;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.google.common.base.Optional;
import com.lab240.devices.Device;
import com.lab240.utils.Lab240;
import com.lab240.utils.MQTT;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    EditText name, pass;
    ViewGroup loginLayout;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
            check(conf.name, conf.pass, conf.devices);
        }else{
            loginLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }


    protected void next(View v){
        check(name.getText().toString(), pass.getText().toString(), Collections.emptyList());
    }

    protected void check(String name, String pass, List<Device> devices){
        loginLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        MQTT mqtt = new MQTT(getResources().getString(R.string.server), name, pass);
        mqtt.connect(this, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Lab240.setMqtt(mqtt);
                Lab240.getDevices().clear();
                Lab240.getDevices().addAll(devices);

                Lab240.saveConfig(LoginActivity.this, new Lab240.Config(name, pass, devices));
                Intent i = new Intent(LoginActivity.this, ListActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                loginLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}