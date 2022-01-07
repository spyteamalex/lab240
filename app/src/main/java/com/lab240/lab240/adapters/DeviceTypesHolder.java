package com.lab240.lab240.adapters;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lab240.devices.DeviceTypes;
import com.lab240.devices.Out;
import com.lab240.lab240.R;
import com.lab240.utils.AlertSheetDialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DeviceTypesHolder extends RecyclerView.ViewHolder{

    TextView name;
    TextView relaysLabel;
    TextView outsLabel;
    RecyclerView relays;
    RecyclerView outs;
    DeviceTypes dt;
    OutAdapter relaysAdapter;
    OutAdapter outsAdapter;

    public DeviceTypesHolder(@NonNull View itemView, FragmentManager fm, @Nullable Functions functions) {
        super(itemView);
        name = itemView.findViewById(R.id.name);
        relays = itemView.findViewById(R.id.relays);
        outs = itemView.findViewById(R.id.outs);
        relaysLabel = itemView.findViewById(R.id.relayLabel);
        outsLabel = itemView.findViewById(R.id.outsLabel);

        relaysAdapter = new OutAdapter();
        relays.setAdapter(relaysAdapter);
        relays.setOnLongClickListener(v->itemView.performLongClick());

        outsAdapter = new OutAdapter();
        outs.setAdapter(outsAdapter);
        outs.setOnLongClickListener(v->itemView.performLongClick());

        itemView.setOnLongClickListener(view -> {
            Log.i("action", "Call context menu in DeviceTypesHolder");

            Vibrator v = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(25);
            if(!DeviceTypes.EMPTY.equals(dt)) {
                AlertSheetDialog asd = new AlertSheetDialog(itemView.getContext());
                asd.addButton(itemView.getResources().getString(R.string.edit), btn -> {
                    Log.i("action", "Edit type in GroupHolder");
                    if (functions != null) functions.edit(dt);
                }, AlertSheetDialog.ButtonType.DEFAULT);
                asd.addButton(itemView.getResources().getString(R.string.delete), btn -> {
                    Log.i("action", "Delete type in DeviceTypesHolder");
                    if (functions != null) functions.delete(dt);
                }, AlertSheetDialog.ButtonType.DESTROY);
                asd.setDismissAction(() -> itemView.setClickable(true));
                asd.show(fm, "");
            }else{
                Toast.makeText(itemView.getContext(), R.string.cant_edit_empty, Toast.LENGTH_LONG).show();
            }
            return false;
        });
    }


    public static class OutAdapter extends RecyclerView.Adapter<OutHolder>{

        @NonNull
        @Override
        public OutHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            OutHolder outHolder = new OutHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_device_type_out, parent, false));
            outHolder.itemView.setOnLongClickListener(view -> {
                Log.i("action", "Long click in OutAdapter");
                return parent.performLongClick();
            });
            return outHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull OutHolder holder, int position) {
            holder.view.setText(outs.get(position).getName());
        }

        @Override
        public int getItemCount() {
            return outs.size();
        }

        private final List<Out> outs = new ArrayList<>();

        public void setData(Collection<Out> data){
            outs.clear();
            outs.addAll(data);
            notifyDataSetChanged();
        }
    }

    public static class OutHolder extends RecyclerView.ViewHolder {
        TextView view;
        public OutHolder(@NonNull View itemView) {
            super(itemView);
            view = (TextView)itemView;
        }
    }

    public interface Functions{
        void delete(DeviceTypes dt);
        void edit(DeviceTypes dt);
    }
}
