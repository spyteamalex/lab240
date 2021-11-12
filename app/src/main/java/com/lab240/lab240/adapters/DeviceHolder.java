package com.lab240.lab240.adapters;

import android.content.Context;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Multimap;
import com.lab240.devices.Device;
import com.lab240.devices.Devices;
import com.lab240.devices.Out;
import com.lab240.lab240.R;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.utils.GravityArrayAdapter;
import com.lab240.utils.Lab240;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class DeviceHolder extends RecyclerView.ViewHolder{

    public interface TerminalCaller{
        void call(Device d);
    }

    final TextView name, type;
    Device item;
    final ItemAdapter adapter;
    final RecyclerView items;

    public DeviceHolder(FragmentManager fm, @NonNull View itemView, List<String> groups, Multimap<Pair<String, Out>, GroupAdapter.Updater> updaters, Map<Pair<String, Out>, String> values, @Nullable TerminalCaller tc, @Nullable Runnable update) {
        super(itemView);
        name = itemView.findViewById(R.id.name);
        type = itemView.findViewById(R.id.type);
        items = itemView.findViewById(R.id.items);
        adapter = new ItemAdapter(updaters, values);
        items.setAdapter(adapter);

        itemView.setOnClickListener(view -> {
            if(!itemView.hasWindowFocus() || !itemView.isClickable())
                return;
            itemView.setClickable(false);
            if(tc != null){
                tc.call(item);
            }
            itemView.setClickable(true);
        });

        items.setOnClickListener(v -> itemView.callOnClick());
        items.setOnLongClickListener(v -> itemView.performLongClick());

        itemView.setOnLongClickListener(view -> {
            if(!itemView.isClickable() || !itemView.hasWindowFocus())
                return false;
            itemView.setClickable(false);
            Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(25);

            AlertSheetDialog asd = new AlertSheetDialog(itemView.getContext());
            asd.addButton(itemView.getResources().getString(R.string.rename), btn->{
                AlertSheetDialog asd2 = new AlertSheetDialog(itemView.getContext());
                EditText name = asd2.addTextInput(itemView.getResources().getString(R.string.name));
                name.setSingleLine(true);
                name.setText(item.getName());
                Button doneButton = asd2.addButton(itemView.getResources().getString(R.string.rename), btn2 -> {
                    item.setName(name.getText().toString());
                    if(update != null) update.run();
                    Lab240.saveDevices(view.getContext(), Lab240.getDevices());
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
                asd2.show(fm, "");
            }, AlertSheetDialog.ButtonType.DEFAULT);
            asd.addButton(itemView.getResources().getString(R.string.move), btn->{
                AlertSheetDialog asd2 = new AlertSheetDialog(itemView.getContext());

                List<String> groups2 = new ArrayList<>(groups);
                groups2.add(itemView.getResources().getString(R.string.new_group));
                GravityArrayAdapter<String> adapter = new GravityArrayAdapter<>(itemView.getContext(), android.R.layout.simple_spinner_item, groups2);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                adapter.setGravity(Gravity.CENTER);
                Spinner spinner = asd2.addView(new Spinner(itemView.getContext()));
                spinner.setAdapter(adapter);
                spinner.setPrompt(itemView.getResources().getString(R.string.device));

                EditText group = asd2.addTextInput(itemView.getResources().getString(R.string.group_name));
                group.setSingleLine(true);
                group.setText(item.getGroup());
                Button done = asd2.addButton("", btn2-> {
                    item.setGroup(spinner.getSelectedItemPosition() == spinner.getCount()-1 ? group.getText().toString() : groups2.get(spinner.getSelectedItemPosition()));
                    if(update != null) update.run();
                    Lab240.saveDevices(view.getContext(), Lab240.getDevices());
                }, AlertSheetDialog.ButtonType.DEFAULT);


                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        group.setVisibility(spinner.getSelectedItemPosition() != spinner.getCount()-1 ? View.GONE : View.VISIBLE);
                        done.setEnabled(spinner.getSelectedItemPosition() != spinner.getCount()-1 || group.getText().length() != 0);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        done.setEnabled(spinner.getSelectedItemPosition() != spinner.getCount()-1 || group.getText().length() != 0);
                    }
                });
                spinner.setSelection(0);

                group.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        done.setEnabled(spinner.getSelectedItemPosition() != spinner.getCount()-1 || group.getText().length() != 0);
                    }
                });


                asd2.show(fm, "");
            }, AlertSheetDialog.ButtonType.DEFAULT);
            asd.addButton(itemView.getResources().getString(R.string.change_channels), btn->{
                AlertSheetDialog asd2 = new AlertSheetDialog(itemView.getContext());
                Set<Out> outs = new TreeSet<>(item.getOuts());

                LinearLayout outsLayout = asd2.addView(new LinearLayout(view.getContext()));
                outsLayout.setOrientation(LinearLayout.VERTICAL);
                outsLayout.setGravity(Gravity.CENTER);
                outsLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                outsLayout.removeAllViews();
                Devices devices = item.getType();
                for(Out o : devices.outs){
                    CheckBox cb = new CheckBox(view.getContext());
                    cb.setChecked(outs.contains(o));
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
                Button doneButton = asd2.addButton(itemView.getResources().getString(R.string.change), btn2 -> {
                    item.getOuts().clear();
                    item.getOuts().addAll(outs);
                    if(update != null) update.run();
                    Lab240.saveDevices(view.getContext(), Lab240.getDevices());
                }, AlertSheetDialog.ButtonType.DEFAULT);
                asd2.show(fm, "");
            }, AlertSheetDialog.ButtonType.DEFAULT);
            asd.addButton(itemView.getResources().getString(R.string.delete), btn->{
                Lab240.getDevices().remove(item);
                if(update != null) update.run();
                Lab240.saveDevices(view.getContext(), Lab240.getDevices());
            }, AlertSheetDialog.ButtonType.DESTROY);
            asd.setDismissAction(() -> itemView.setClickable(true));
            asd.show(fm, "");
            return false;
        });
    }
}
