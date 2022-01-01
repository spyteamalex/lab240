package com.lab240.lab240;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.base.Optional;
import com.lab240.devices.Device;
import com.lab240.devices.DeviceTypes;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.devices.Lab240;
import com.lab240.devices.MQTT;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;

import java.util.ArrayList;
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


    private final static int PERMISSION_REQUEST_CODE = 101;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0){
            boolean f = true;
            for(int i : grantResults){
                f = f && i == PackageManager.PERMISSION_GRANTED;
            }
            if(f) {
                changeActivity(ListActivity.class);
            }
        }
    }

    protected void changeActivity(Class<?> c){
        Intent i = new Intent(LoginActivity.this, c);
        i.putExtras(getIntent());
        i.setData(getIntent().getData());
        i.setAction(getIntent().getAction());
        startActivity(i);
        finish();
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

                if (ContextCompat.checkSelfPermission(LoginActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(LoginActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_CODE);
                }else {
                    changeActivity(ListActivity.class);
                }
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