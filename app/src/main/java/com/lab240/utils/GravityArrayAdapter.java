package com.lab240.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class GravityArrayAdapter<T> extends ArrayAdapter<T> {
    public GravityArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public GravityArrayAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public GravityArrayAdapter(@NonNull Context context, int resource, @NonNull T[] objects) {
        super(context, resource, objects);
    }

    public GravityArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull T[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public GravityArrayAdapter(@NonNull Context context, int resource, @NonNull List<T> objects) {
        super(context, resource, objects);
    }

    public GravityArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<T> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public int getGravity() {
        return gravity;
    }

    public GravityArrayAdapter<T> setGravity(int gravity) {
        this.gravity = gravity;
        notifyDataSetChanged();
        return this;
    }

    private int gravity = Gravity.CENTER;


    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View dropDownView = super.getDropDownView(position, convertView, parent);
        ((TextView) dropDownView).setGravity(gravity);
        return dropDownView;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        ((TextView) view).setGravity(gravity);
        return view;
    }
}
