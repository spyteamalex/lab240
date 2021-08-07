package com.lab240.lab240.adapters;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.lab240.devices.OutLine;
import com.lab240.lab240.R;
import com.lab240.utils.Converter;

import java.util.ArrayList;
import java.util.List;

public class TerminalAdapter extends RecyclerView.Adapter<TerminalItem> {

    @Override
    public void onBindViewHolder(final TerminalItem holder, final int position) {
        final OutLine item = items.get(position);
        holder.titleView.setText(item.value);
        int padding = Converter.dpToPx(holder.itemView.getContext(), 5);
        holder.titleView.setPadding(padding, padding, padding, padding);
        Typeface tf = ResourcesCompat.getFont(holder.titleView.getContext(), R.font.allerta);
        switch (item.type) {
            case IN:
                holder.titleView.setTypeface(tf, Typeface.BOLD);
                holder.titleView.setTextColor(Color.BLACK);
                break;
            case OUT:
                holder.titleView.setTypeface(tf, Typeface.NORMAL);
                holder.titleView.setTextColor(Color.BLACK);
                break;
            case LOG:
                holder.titleView.setTypeface(tf, Typeface.BOLD);
                holder.titleView.setTextColor(Color.RED);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @NonNull
    @Override
    public TerminalItem onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = new TextView(parent.getContext());
        return new TerminalItem(view);
    }

    final List<OutLine> items = new ArrayList<>();

    public void setData(List<OutLine> items){
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }
}
