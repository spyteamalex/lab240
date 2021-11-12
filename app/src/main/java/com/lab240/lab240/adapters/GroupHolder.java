package com.lab240.lab240.adapters;

import android.content.Context;
import android.os.Vibrator;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Multimap;
import com.lab240.devices.Out;
import com.lab240.lab240.R;
import com.lab240.utils.AlertSheetDialog;

import java.util.Map;

public class GroupHolder extends RecyclerView.ViewHolder{

    final RecyclerView devices;
    final TextView name;
    final DeviceAdapter adapter;
    String group;


    public GroupHolder(FragmentManager fm, @NonNull View itemView, Multimap<Pair<String, Out>, GroupAdapter.Updater> updaters, Map<Pair<String, Out>, String> values, @Nullable DeviceHolder.Functions tc) {
        super(itemView);
        devices = itemView.findViewById(R.id.devices);
        adapter = new DeviceAdapter(fm, updaters, values, tc);
        devices.setAdapter(adapter);
        name = itemView.findViewById(R.id.name);

        itemView.setOnLongClickListener(view -> {
            Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(25);
            AlertSheetDialog asd = new AlertSheetDialog(itemView.getContext());
            asd.addButton(itemView.getResources().getString(R.string.rename), btn->{
                AlertSheetDialog asd2 = new AlertSheetDialog(itemView.getContext());
                EditText gr = asd2.addTextInput(itemView.getResources().getString(R.string.name));
                gr.setSingleLine(true);
                gr.setText(group);
                asd2.addButton(itemView.getResources().getString(R.string.rename), btn2 -> tc.setGroup(adapter.devices, gr.getText().toString()), AlertSheetDialog.ButtonType.DEFAULT);
                asd2.show(fm, "");
            }, AlertSheetDialog.ButtonType.DEFAULT);
            asd.addButton(itemView.getResources().getString(R.string.delete), btn-> tc.delete(adapter.devices), AlertSheetDialog.ButtonType.DESTROY);
            asd.show(fm, "");
            return false;
        });

    }
}
