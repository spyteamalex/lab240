package com.lab240.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.lab240.lab240.R;

public class AlertDialog{
    LinearLayout text, buttons;
    Context context;
    android.app.AlertDialog ad;

    public AlertDialog(Activity a) {
        this.context = a;

        android.app.AlertDialog.Builder ad = new android.app.AlertDialog.Builder(a);
        ad.setCancelable(true);
        View v = a.getLayoutInflater().inflate( R.layout.inflate_alert_dialog, a.findViewById(R.id.content), false);
        text = v.findViewById(R.id.text);
        buttons = v.findViewById(R.id.buttons);
        ad.setView(v);
        this.ad = ad.create();
        this.ad.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


    public static final int DEFAULT = 0, DESTROY = 1;

    public Button addButton(String text, @Nullable Runnable runnable, int type){
        Button button = new Button(context);
        button.setText(text);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,1f));
        button.setOnClickListener(v -> {
            ad.dismiss();
            if(runnable != null) runnable.run();
        });
        if(type == DEFAULT){
            button.setTextColor(context.getResources().getColor(R.color.highlighted));
        }else if(type == DESTROY){
            button.setTextColor(Color.RED);
        }

        buttons.addView(button);

        if(buttons.getChildCount() == 2){
            buttons.setOrientation(LinearLayout.HORIZONTAL);
        }else
            buttons.setOrientation(LinearLayout.VERTICAL);
        return button;
    }

    public <T extends View> T addView(T v){
        v.setPadding(50,50,50,50);
        text.addView(v);
        return v;
    }

    public TextView addText(String text){
        TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextSize(16);
        tv.setPadding(50,50,50,50);
        tv.setGravity(Gravity.CENTER);
        this.text.addView(tv);
        return tv;
    }



    public EditText addEditText(String hint) {
        EditText editText = new EditText(context);
        editText.setBackgroundColor(0);
        editText.setHint(hint);
        editText.setTextSize(16);
        editText.setGravity(Gravity.CENTER);
        editText.setPadding(50, 50, 50, 50);
        text.addView(editText);
        return editText;
    }

    public android.app.AlertDialog create(){
        return ad;
    }
}
