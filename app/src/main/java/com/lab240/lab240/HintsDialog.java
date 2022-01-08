package com.lab240.lab240;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.lab240.devices.Hint;

import java.util.Collection;

public class HintsDialog {
    private final AlertDialog ad;
    private final FlexboxLayout getters, setters;
    private final TextView gettersLabel, settersLabel;

    interface Handler{
        void handle(String s);
    }

    public HintsDialog(Activity a) {
        android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(a);
        adb.setCancelable(true);

        View v = a.getLayoutInflater().inflate( R.layout.inflate_hints_dialog, a.findViewById(R.id.content), false);
        adb.setView(v);
        getters = v.findViewById(R.id.getters);
        setters = v.findViewById(R.id.setters);

        gettersLabel = v.findViewById(R.id.getterLabel);
        settersLabel = v.findViewById(R.id.setterLabel);

        ad = adb.create();
        ad.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    public android.app.AlertDialog create(){
        return ad;
    }

    public void setData(Collection<Hint> gettersData, Collection<Hint> settersData, Handler onClick){
        getters.removeAllViews();
        for(Hint g : gettersData){
            TextView v = (TextView) LayoutInflater.from(getters.getContext()).inflate(R.layout.inflate_hint, getters, false);
            v.setText(g.getHint());
            v.setOnClickListener(b->{
                onClick.handle(g.getCmd());
                ad.dismiss();
            });
            getters.addView(v);
        }
        getters.setVisibility(gettersData.isEmpty() ? View.GONE : View.VISIBLE);
        gettersLabel.setVisibility(gettersData.isEmpty() ? View.GONE : View.VISIBLE);

        setters.removeAllViews();
        for(Hint s : settersData){
            TextView v = (TextView) LayoutInflater.from(setters.getContext()).inflate(R.layout.inflate_hint, getters, false);
            v.setText(s.getHint());
            v.setOnClickListener(b->{
                onClick.handle(s.getCmd());
                ad.dismiss();
            });
            setters.addView(v);
        }
        setters.setVisibility(settersData.isEmpty() ? View.GONE : View.VISIBLE);
        settersLabel.setVisibility(settersData.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
