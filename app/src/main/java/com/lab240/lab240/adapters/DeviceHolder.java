package com.lab240.lab240.adapters;

import android.content.Context;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Multimap;
import com.lab240.devices.Device;
import com.lab240.devices.Devices;
import com.lab240.devices.Out;
import com.lab240.lab240.R;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.utils.Lab240;

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

    public DeviceHolder(@NonNull View itemView, Multimap<Out, TextView> views, Map<Out, String> values, @Nullable TerminalCaller tc, @Nullable Runnable update) {
        super(itemView);
        name = itemView.findViewById(R.id.name);
        type = itemView.findViewById(R.id.type);
        items = itemView.findViewById(R.id.items);
        adapter = new ItemAdapter(views, values);
        items.setAdapter(adapter);

        itemView.setOnClickListener(view -> {
            if(tc != null){
                tc.call(item);
            }
        });

        items.setOnClickListener(v -> itemView.callOnClick());
        items.setOnLongClickListener(v -> itemView.performLongClick());

        itemView.setOnLongClickListener(view -> {
            Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(25);

            AlertSheetDialog asd = new AlertSheetDialog(view.getContext());
            asd.addButton("Переименовать", ()->{
                AlertSheetDialog asd2 = new AlertSheetDialog(view.getContext());
                EditText name = asd2.addTextInput("Название");
                name.setSingleLine(true);
                name.setText(item.getName());
                Button doneButton = asd2.addButton("Переименовать", () -> {
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
                asd2.show();
            }, AlertSheetDialog.ButtonType.DEFAULT);
            asd.addButton("Переместить в", ()->{
                AlertSheetDialog asd2 = new AlertSheetDialog(view.getContext());
                EditText group = asd2.addTextInput("Группа");
                group.setSingleLine(true);
                group.setText(item.getGroup());
                asd2.addButton("Переместить", ()-> {
                    item.setGroup(group.getText().toString());
                    if(update != null) update.run();
                    Lab240.saveDevices(view.getContext(), Lab240.getDevices());
                }, AlertSheetDialog.ButtonType.DEFAULT);
                asd2.show();
            }, AlertSheetDialog.ButtonType.DEFAULT);
            asd.addButton("Изменить каналы", ()->{
                AlertSheetDialog asd2 = new AlertSheetDialog(view.getContext());
                Set<Out> outs = new TreeSet<>(item.getOuts());

                LinearLayout outsLayout = asd2.addView(new LinearLayout(view.getContext()));
                outsLayout.setOrientation(LinearLayout.VERTICAL);
                outsLayout.setGravity(Gravity.CENTER);
                ViewGroup.LayoutParams layoutParams = outsLayout.getLayoutParams();
                layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                outsLayout.setLayoutParams(layoutParams);

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
                Button doneButton = asd2.addButton("Изменить", () -> {
                    item.getOuts().clear();
                    item.getOuts().addAll(outs);
                    if(update != null) update.run();
                    Lab240.saveDevices(view.getContext(), Lab240.getDevices());
                }, AlertSheetDialog.ButtonType.DEFAULT);
                asd2.show();
            }, AlertSheetDialog.ButtonType.DEFAULT);
            asd.addButton("Удалить", ()->{
                Lab240.getDevices().remove(item);
                if(update != null) update.run();
                Lab240.saveDevices(view.getContext(), Lab240.getDevices());
            }, AlertSheetDialog.ButtonType.DESTROY);
            asd.show();
            return false;
        });
    }
}
