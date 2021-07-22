package com.lab240.lab240;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ListActivity extends AppCompatActivity {

    GroupAdapter ga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        if(!Lab240.isInited())
            finish();

        RecyclerView rv = findViewById(R.id.groups);
        Container<Runnable> update = new Container<>(null);
        ga = new GroupAdapter(() -> update.get().run());
        update.set(()->ga.setData(Lab240.getDevices()));
        rv.setAdapter(ga);
        ga.setData(Lab240.getDevices());

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
        }
        return super.onOptionsItemSelected(item);
    }

    public void addDevice(){
        AlertSheetDialog asd2 = new AlertSheetDialog(this);
        EditText name = asd2.addEditText("Название");
        name.setSingleLine(true);
        name.setText("Device");
        EditText group = asd2.addEditText("Группа");
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
        }, AlertSheetDialog.DEFAULT);
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
}