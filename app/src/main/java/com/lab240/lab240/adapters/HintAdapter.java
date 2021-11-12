package com.lab240.lab240.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lab240.devices.Hint;
import com.lab240.lab240.R;
import com.lab240.utils.AlertSheetDialog;
import com.lab240.utils.CommandManager;

import java.util.ArrayList;
import java.util.Collection;

public class HintAdapter extends RecyclerView.Adapter<HintItem> {

    public interface ClickListener{
        void handle(String str);
    }

    @Override
    public void onBindViewHolder(final HintItem holder, final int position) {
        final Hint group = groups.get(position);
        holder.hint.setText(group.getGroup());
        if(!group.showDialog()) {
            holder.itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.handle(group.getCommands()[0]);
            });
        }else {
            holder.itemView.setOnClickListener(v -> {
                AlertSheetDialog asd = new AlertSheetDialog(holder.itemView.getContext());
                for (String h : group.getCommands()) {
                    CommandManager cm = new CommandManager(h);
                    asd.addButton(cm.getTemplate(), btn -> {
                        if (clickListener != null) clickListener.handle(h);
                    }, AlertSheetDialog.ButtonType.DEFAULT);
                }
                asd.show(fm, "");
            });
        }
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    @NonNull
    @Override
    public HintItem onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_hint, parent, false);
        return new HintItem(view);
    }

    final ArrayList<Hint> groups = new ArrayList<>();

    public HintAdapter(FragmentManager fm, @Nullable ClickListener clickListener) {
        this.clickListener = clickListener;
        this.fm = fm;
    }

    final FragmentManager fm;
    final @Nullable ClickListener clickListener;

    public void setData(Collection<Hint> items){
        this.groups.clear();
        this.groups.addAll(items);
        notifyDataSetChanged();
    }
}
