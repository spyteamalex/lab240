package com.lab240.lab240;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.lab240.devices.Device;
import com.lab240.devices.DeviceTypes;
import com.lab240.devices.Lab240;
import com.lab240.lab240.adapters.DeviceTypesAdapter;
import com.lab240.lab240.adapters.DeviceTypesHolder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

public class DeviceTypesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_types);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        Intent i = getIntent();
        if(Intent.ACTION_VIEW.equals(i.getAction()) && i.getData() != null){
            Log.i("action", "Opening file from intent");
            openFile(i.getData());
        }

        RecyclerView types = findViewById(R.id.types);
        DeviceTypesAdapter adapter = new DeviceTypesAdapter(getSupportFragmentManager(), new DeviceTypesHolder.Functions() {
            @Override
            public void delete(DeviceTypes dt) {
                Lab240.getDeviceTypes().remove(dt.id);
                for(Device d : Lab240.getDevices()){
                    if(d.getType() == dt.id)
                        d.setType(DeviceTypes.EMPTY.id);
                }
                Lab240.saveDevices(DeviceTypesActivity.this, Lab240.getDevices());
                Lab240.saveDeviceTypes(DeviceTypesActivity.this, Lab240.getDeviceTypes());
            }

            @Override
            public void edit(DeviceTypes dt) {
                //todo
            }
        });
        types.setAdapter(adapter);

        adapter.setData(Lab240.getDeviceTypes().values());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    ActivityResultLauncher<Intent> filePicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getData() != null) {
                    Log.i("info", "File is picked in DeviceTypesActivity");
                    openFile(result.getData().getData());
                }else{
                    Log.i("info", "File is not picked in DeviceTypesActivity");
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
        Lab240.saveDevices(DeviceTypesActivity.this, Lab240.getDevices());
        Lab240.saveDeviceTypes(DeviceTypesActivity.this, Lab240.getDeviceTypes());
        update();
        Toast.makeText(this, R.string.import_ok, Toast.LENGTH_LONG).show();
    }

    private void update() {
        //todo
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_types_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.export){
            Log.i("action", "Export in DeviceTypesActivity");

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
            Log.i("action", "Import in DeviceTypesActivity");

            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
            chooseFile.setType("*/*");
            Intent intent = Intent.createChooser(chooseFile, getString(R.string.choose_config));
            filePicker.launch(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}