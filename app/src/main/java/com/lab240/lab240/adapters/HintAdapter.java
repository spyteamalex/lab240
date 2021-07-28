package com.lab240.lab240.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.lab240.lab240.R;
import com.lab240.utils.CommandManager;

import java.util.ArrayList;
import java.util.Collection;

public class HintAdapter extends RecyclerView.Adapter<HintItem> {

    public interface ClickListener{
        void handle(String str);
    }

    @Override
    public void onBindViewHolder(final HintItem holder, final int position) {
        final String item = items.get(position);
        CommandManager cm = new CommandManager(item);
        holder.hint.setText(cm.getTemplate());
        holder.itemView.setOnClickListener(v-> {
            if (clickListener != null) clickListener.handle(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public HintItem onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_hint, parent, false);
        return new HintItem(view);
    }

    final ArrayList<String> items = new ArrayList<>();

    public HintAdapter(@Nullable ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    final @Nullable ClickListener clickListener;

    public void setData(Collection<String> items){
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }
}
