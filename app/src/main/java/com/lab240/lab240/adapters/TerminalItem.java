package com.lab240.lab240.adapters;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class TerminalItem extends RecyclerView.ViewHolder {
    
    final TextView titleView;

    public TerminalItem(final View itemView) {
        super(itemView);
        titleView = (TextView) itemView;
        titleView.setTextColor(Color.BLACK);
    }
}
