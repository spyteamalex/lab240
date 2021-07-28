package com.lab240.lab240.adapters;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class HintItem extends RecyclerView.ViewHolder {

    final TextView hint;

    public HintItem(final View itemView) {
        super(itemView);
        hint = (TextView) itemView;

    }
}
