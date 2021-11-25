package com.lab240.lab240.adapters;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import java.util.Set;

public class GroupHolder extends RecyclerView.ViewHolder{

    final RecyclerView devices;
    final TextView name;
    final DeviceAdapter adapter;
    String group;


    public GroupHolder(FragmentManager fm, Set<String> opened, @NonNull View itemView, Multimap<Pair<String, Out>, GroupAdapter.Updater> updaters, Map<Pair<String, Out>, Pair<String, Long>> values, @Nullable DeviceHolder.Functions tc) {
        super(itemView);
        devices = itemView.findViewById(R.id.devices);
        adapter = new DeviceAdapter(fm, updaters, values, tc);
        devices.setAdapter(adapter);
        name = itemView.findViewById(R.id.name);

        itemView.setOnClickListener(view ->{
            if(opened.contains(group)) {
                Log.i("action", "Hide GroupHolder");
                opened.remove(group);
                setVisible(false, true);
            }else {
                Log.i("action", "Unhide GroupHolder");
                opened.add(group);
                setVisible(true, true);
            }
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

    public void setVisible(boolean v, boolean animate){
        if(v){
            name.setText(group);
            devices.setVisibility(View.VISIBLE);
        }else {
            name.setText(String.format("%s(%d)", group, adapter.getItemCount()));
            devices.setVisibility(View.GONE);
        }
    }
}
