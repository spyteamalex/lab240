package com.lab240.lab240;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lab240.devices.Device;
import com.lab240.devices.DeviceTypes;
import com.lab240.devices.Lab240;
import com.lab240.devices.Out;
import com.lab240.lab240.adapters.DeviceTypesAdapter;
import com.lab240.lab240.adapters.DeviceTypesHolder;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.utils.GravityArrayAdapter;
import com.lab240.utils.ShowableAdapter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DeviceTypesActivity extends AppCompatActivity {

    DeviceTypesAdapter adapter;

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
        adapter = new DeviceTypesAdapter(getSupportFragmentManager(), new DeviceTypesHolder.Functions() {
            @Override
            public void delete(DeviceTypes dt) {
                Lab240.getDeviceTypes().remove(dt.getId());
                for(Device d : Lab240.getDevices()){
                    if(d.getType() == dt.getId())
                        d.setType(DeviceTypes.EMPTY.getId());
                }
                Lab240.saveDevices(DeviceTypesActivity.this, Lab240.getDevices());
                Lab240.saveDeviceTypes(DeviceTypesActivity.this, Lab240.getDeviceTypes());
                update();
            }

            @Override
            public void edit(DeviceTypes dt) {
                editDeviceType(dt);
            }
        });
        types.setAdapter(adapter);

        adapter.setData(Lab240.getDeviceTypes().values());
    }

    public void update(){
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_types_activity_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add) {
            Log.i("action", "Add device in DeviceTypesActivity");
            editDeviceType();
        }else if(item.getItemId() == R.id.export){
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

    public void editDeviceType(){
        editDeviceType(null);
    }

    public void editDeviceType(@Nullable DeviceTypes deviceType){
        Log.i("action", "Editing deviceType (New = "+(deviceType == null)+") in DeviceTypesActivity");
        final boolean editing = deviceType != null;
        AlertSheetDialog asd2 = new AlertSheetDialog(this);
        asd2.show(getSupportFragmentManager(), "");
        EditText name = asd2.addTextInput(getString(R.string.name));
        if(editing) name.setText(deviceType.getName());
        name.setSingleLine(true);

        List<Out> relays = new ArrayList<>();
        if(editing)
            relays.addAll(deviceType.getRelays());
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

        List<Out> outs = new ArrayList<>();
        if(editing)
            outs.addAll(deviceType.getOuts());
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

        if(editing)
            for(Out o : outs){
                CheckBox cb = new CheckBox(this);
                cb.setChecked(true);
                cb.setText(o.getName());
                cb.setOnCheckedChangeListener((compoundButton, b) -> {
                    if(!b) {
                        outs.remove(o);
                        outsLayout.removeView(cb);
                    }
                });
                outsLayout.addView(cb);
            }

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

                CheckBox cb = new CheckBox(this);
                cb.setChecked(true);
                cb.setText(o.getName());
                cb.setOnCheckedChangeListener((compoundButton, b) -> {
                    if(!b) {
                        outs.remove(o);
                        outsLayout.removeView(cb);
                    }
                });
                outsLayout.addView(cb, outsLayout.getChildCount()-1);

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

        if(editing)
            for(Out o : relays){
                CheckBox cb = new CheckBox(this);
                cb.setChecked(true);
                cb.setText(o.getName());
                cb.setOnCheckedChangeListener((compoundButton, b) -> {
                    if(!b) {
                        relays.remove(o);
                        relaysLayout.removeView(cb);
                    }
                });
                relaysLayout.addView(cb);
            }

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

                CheckBox cb = new CheckBox(this);
                cb.setChecked(true);
                cb.setText(o.getName());
                cb.setOnCheckedChangeListener((compoundButton, b) -> {
                    if(!b) {
                        relays.remove(o);
                        relaysLayout.removeView(cb);
                    }
                });
                relaysLayout.addView(cb, relaysLayout.getChildCount()-1);

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

        Button doneButton;
        if(!editing) {
            long id = 0;
            for(; Lab240.getDeviceTypes().containsKey(id); id++);
            long id2 = id;
            doneButton = asd2.addButton(getString(R.string.create), btn -> {
                DeviceTypes dt = new DeviceTypes(name.getText().toString(), id2, new TreeSet<>(relays), new TreeSet<>(outs), new ArrayList<>(), new ArrayList<>());
                Lab240.getDeviceTypes().put(id2, dt);
                update();
                Lab240.saveDevices(this, Lab240.getDevices());
                Lab240.saveDeviceTypes(this, Lab240.getDeviceTypes());
            }, AlertSheetDialog.ButtonType.DEFAULT);
        }else{
            doneButton = asd2.addButton(getString(R.string.edit), btn -> {
                deviceType.setName(name.getText().toString());
                deviceType.getOuts().clear();
                deviceType.getOuts().addAll(outs);

                deviceType.getRelays().clear();
                deviceType.getRelays().addAll(relays);

                update();
                Lab240.saveDevices(this, Lab240.getDevices());
                Lab240.saveDeviceTypes(this, Lab240.getDeviceTypes());
            }, AlertSheetDialog.ButtonType.DEFAULT);
        }


        Runnable check = ()->doneButton.setEnabled(name.getText().length() != 0);

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
    }
}