package com.lab240.lab240.adapters;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
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
import com.lab240.devices.Lab240;

import java.util.Locale;
import java.util.Map;

public class GroupHolder extends RecyclerView.ViewHolder{

    final RecyclerView devices;
    final TextView name;
    final DeviceAdapter adapter;
    String group;


    public GroupHolder(FragmentManager fm, @NonNull View itemView, Multimap<Pair<String, Out>, ItemHolder.Updater> updaters, Map<Pair<String, Out>, Pair<String, Long>> values, @Nullable DeviceHolder.Functions tc) {
        super(itemView);
        devices = itemView.findViewById(R.id.devices);
        adapter = new DeviceAdapter(fm, updaters, values, tc);
        devices.setAdapter(adapter);
        name = itemView.findViewById(R.id.name);

        itemView.setOnClickListener(view ->{
            if(!Lab240.getHiddenGroups().contains(group)) {
                Log.i("action", "Hide GroupHolder");
                Lab240.getHiddenGroups().add(group);
                setVisible(false);
            }else {
                Log.i("action", "Unhide GroupHolder");
                Lab240.getHiddenGroups().remove(group);
                setVisible(true);
            }
            Lab240.saveHiddenGroups(devices.getContext(), Lab240.getHiddenGroups());
        });
        itemView.setOnLongClickListener(view -> {
            Log.i("action", "Call context menu in GroupHolder");
            Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(25);
            AlertSheetDialog asd = new AlertSheetDialog(itemView.getContext());
            asd.addButton(itemView.getResources().getString(R.string.rename), btn->{
                Log.i("action", "Rename group in GroupHolder");
                AlertSheetDialog asd2 = new AlertSheetDialog(itemView.getContext());
                EditText gr = asd2.addTextInput(itemView.getResources().getString(R.string.name));
                gr.setSingleLine(true);
                gr.setText(group);
                asd2.addButton(itemView.getResources().getString(R.string.rename), btn2 -> tc.setGroup(adapter.devices, gr.getText().toString()), AlertSheetDialog.ButtonType.DEFAULT);
                asd2.show(fm, "");
            }, AlertSheetDialog.ButtonType.DEFAULT);
            asd.addButton(itemView.getResources().getString(R.string.delete), btn-> {
                Log.i("action", "Delete group in GroupHolder");
                tc.delete(adapter.devices);
            }, AlertSheetDialog.ButtonType.DESTROY);
            asd.show(fm, "");
            return false;
        });
    }

    public void setVisible(boolean v){
        if(v){
            name.setText(group);
            devices.setVisibility(View.VISIBLE);
        }else {
            name.setText(String.format(Locale.getDefault(), "%s(%d)", group, adapter.getItemCount()));
            devices.setVisibility(View.GONE);
        }
    }
}
