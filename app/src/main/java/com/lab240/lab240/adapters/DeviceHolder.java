package com.lab240.lab240.adapters;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.common.collect.Multimap;
import com.lab240.devices.Device;
import com.lab240.devices.Out;
import com.lab240.lab240.R;
import com.lab240.utils.AlertSheetDialog;

import java.util.Collection;
import java.util.Map;

public class DeviceHolder extends RecyclerView.ViewHolder{

    public interface Functions {
        void call(Device d);
        void edit(Device d);
        void delete(Device d);
        void setGroup(Collection<Device> d, String str);
        void delete(Collection<Device> d);
    }

    final TextView name, type;
    Device item;
    final ItemAdapter adapter;
    final RecyclerView items;

    public DeviceHolder(FragmentManager fm, @NonNull View itemView, Multimap<Pair<String, Out>, GroupAdapter.Updater> updaters, Map<Pair<String, Out>, Pair<String, Long>> values, @Nullable Functions tc) {
        super(itemView);
        name = itemView.findViewById(R.id.name);
        type = itemView.findViewById(R.id.type);
        items = itemView.findViewById(R.id.items);
        adapter = new ItemAdapter(updaters, values);
        items.setAdapter(adapter);

        itemView.setOnClickListener(view -> {
            Log.i("action", "Call terminal in DeviceHolder");
            if(!itemView.hasWindowFocus() || !itemView.isClickable()) {
                Log.i("info", "Cancel calling terminal in DeviceHolder");
                return;
            }
            itemView.setClickable(false);
            if(tc != null){
                tc.call(item);
            }
            itemView.setClickable(true);
        });

        items.setOnClickListener(v -> itemView.callOnClick());
        items.setOnLongClickListener(v -> itemView.performLongClick());

        itemView.setOnLongClickListener(view -> {
            Log.i("action", "Call context menu in DeviceHolder");
            if(!itemView.isClickable() || !itemView.hasWindowFocus()) {
                Log.i("info", "Cancel calling context menu in DeviceHolder");
                return false;
            }
            itemView.setClickable(false);
            Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(25);
            AlertSheetDialog asd = new AlertSheetDialog(itemView.getContext());
            asd.addButton(itemView.getResources().getString(R.string.edit), btn-> {
                Log.i("action", "Change device in DeviceHolder");
                tc.edit(item);
            }, AlertSheetDialog.ButtonType.DEFAULT);
            asd.addButton(itemView.getResources().getString(R.string.delete), btn-> {
                Log.i("action", "Delete device in DeviceHolder");
                tc.delete(item);
            }, AlertSheetDialog.ButtonType.DESTROY);
            asd.setDismissAction(()->itemView.setClickable(true));
            asd.show(fm, "");
            return false;
        });
    }
}
