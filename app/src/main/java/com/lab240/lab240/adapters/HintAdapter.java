package com.lab240.lab240.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lab240.lab240.R;

import java.util.ArrayList;
import java.util.Collection;

public class HintAdapter extends RecyclerView.Adapter<HintItem> {

    public interface ClickListener{
        void handle(String str);
    }

    @Override
    public void onBindViewHolder(final HintItem holder, final int position) {
        final String hint = hints.get(position);
        holder.hint.setText(hint);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.handle(hint);
        });
    }

    @Override
    public int getItemCount() {
        return hints.size();
    }

    @NonNull
    @Override
    public HintItem onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inflate_hint, parent, false);
        return new HintItem(view);
    }

    final ArrayList<String> hints = new ArrayList<>();

    public HintAdapter(FragmentManager fm, @Nullable ClickListener clickListener) {
        this.clickListener = clickListener;
        this.fm = fm;
    }

    final FragmentManager fm;
    final @Nullable ClickListener clickListener;

    public void setData(Collection<String> items){
        this.hints.clear();
        this.hints.addAll(items);
        notifyDataSetChanged();
    }
}
