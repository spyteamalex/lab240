package com.lab240.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.lab240.lab240.R;

public class AlertSheetDialog extends BottomSheetDialog {
    LinearLayout layout;
    View v;
    Context c;
    public AlertSheetDialog(@NonNull Activity a) {
        super(a);
        c = a;
        setCancelable(true);
        v = a.getLayoutInflater().inflate( R.layout.inflate_bottom_sheet, findViewById(R.id.bottomSheetContainer), false);
        setContentView(v);
        layout = v.findViewById(R.id.buttons);
        v.findViewById(R.id.cancel).setOnClickListener(v2->dismiss());
        getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        getBehavior().setSkipCollapsed(true);
    }

    public static final int DEFAULT = 0, DESTROY = 1;

    public void setCancelAction(Runnable r){
        v.findViewById(R.id.cancel).setOnClickListener(v2->{
            r.run();
            dismiss();
        });
    }

    public Button addButton(String text, @Nullable Runnable runnable, int type){
        Button button = new Button(c);
        button.setText(text);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setOnClickListener(v -> {
            dismiss();
            if(runnable != null) runnable.run();
        });
        if(type == DEFAULT){
            button.setTextColor(c.getResources().getColor(R.color.highlighted));
        }else if(type == DESTROY){
            button.setTextColor(Color.RED);
        }

        layout.addView(button);
        return button;
    }

    public EditText addEditText(String hint) {
        EditText editText = new EditText(c);
        editText.setBackgroundColor(0);
        editText.setHint(hint);
        editText.setTextSize(16);
        editText.setGravity(Gravity.CENTER);
        editText.setPadding(50, 50, 50, 50);
        layout.addView(editText);
        return editText;
    }

    public TextView addText(String text){
        TextView tv = new TextView(c);
        tv.setText(text);
        tv.setTextSize(16);
        tv.setPadding(50,50,50,50);
        tv.setGravity(Gravity.CENTER);
        layout.addView(tv);
        return tv;
    }


    public <T extends View> T addView(T v){
        v.setPadding(50,50,50,50);
        layout.addView(v);
        return v;
    }
}
