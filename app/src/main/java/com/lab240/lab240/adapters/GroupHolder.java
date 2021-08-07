package com.lab240.lab240.adapters;

import android.content.Context;
import android.os.Vibrator;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Multimap;
import com.lab240.devices.Device;
import com.lab240.devices.Out;
import com.lab240.lab240.R;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.utils.Lab240;

import java.util.List;
import java.util.Map;

public class GroupHolder extends RecyclerView.ViewHolder{

    final RecyclerView devices;
    final TextView name;
    final DeviceAdapter adapter;
    String group;


    public GroupHolder(@NonNull View itemView, List<String> groups, Multimap<Pair<String, Out>, GroupAdapter.Updater> updaters, Map<Pair<String, Out>, String> values, @Nullable DeviceHolder.TerminalCaller tc, @Nullable Runnable update) {
        super(itemView);
        devices = itemView.findViewById(R.id.devices);
        adapter = new DeviceAdapter(groups, updaters, values, tc, update);
        devices.setAdapter(adapter);
        name = itemView.findViewById(R.id.name);

        itemView.setOnLongClickListener(view -> {
            Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(25);
            AlertSheetDialog asd = new AlertSheetDialog(view.getContext());
            asd.addButton("Переименовать", ()->{
                AlertSheetDialog asd2 = new AlertSheetDialog(view.getContext());
                EditText gr = asd2.addTextInput("Название");
                gr.setSingleLine(true);
                gr.setText(group);
                asd2.addButton("Переименовать", () -> {
                    for(Device d: adapter.devices)
                        d.setGroup(gr.getText().toString());
                    if(update != null) update.run();
                    Lab240.saveDevices(view.getContext(), Lab240.getDevices());
                }, AlertSheetDialog.ButtonType.DEFAULT);
                asd2.show();
            }, AlertSheetDialog.ButtonType.DEFAULT);
            asd.addButton("Удалить", ()->{
                for(Device i : adapter.devices)
                    Lab240.getDevices().remove(i);
                if(update != null) update.run();
                Lab240.saveDevices(view.getContext(), Lab240.getDevices());
            }, AlertSheetDialog.ButtonType.DESTROY);
            asd.show();
            return false;
        });

    }
}
