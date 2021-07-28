package com.lab240.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
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
    public enum ButtonType{
        DEFAULT, DESTROY;
    }
    LinearLayout layout, cancelLL;
    View v;
    Context c;
    Button cancel;

    private static final int PADDING = 20;
    public AlertSheetDialog(@NonNull Context c) {
        super(c);
        this.c = c;
        setCancelable(true);
        v = LayoutInflater.from(c).inflate( R.layout.inflate_bottom_sheet, findViewById(R.id.bottomSheetContainer), false);
        setContentView(v);
        layout = v.findViewById(R.id.buttons);
        cancelLL = v.findViewById(R.id.cancelLL);
        getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
        getBehavior().setSkipCollapsed(true);
        getBehavior().setHideable(false);
        setCancelable(cancelable);

        cancel = new Button(c);
        cancel.setText("Отмена");
        cancel.setBackgroundColor(Color.TRANSPARENT);
        cancel.setAllCaps(false);
        cancel.setTextSize(16);
        cancel.setOnClickListener(v->dismiss());
        int padding = Converter.dpToPx(c, PADDING);
        cancel.setPadding(padding,padding,padding,padding);
        cancel.setTextColor(c.getResources().getColor(R.color.highlighted));
        cancelLL.addView(cancel);
    }

    public boolean isCancelable() {
        return cancelable;
    }

    private boolean cancelable = true;

    public boolean getCloseOnAction() {
        return closeOnAction;
    }

    public void setCloseOnAction(boolean closeOnAction) {
        this.closeOnAction = closeOnAction;
    }

    private boolean closeOnAction = true;

    public void setCancelButtonText(String s, ButtonType type){
        cancel.setText(s);

        if(type == ButtonType.DEFAULT){
            cancel.setTextColor(c.getResources().getColor(R.color.highlighted));
        }else if(type == ButtonType.DESTROY){
            cancel.setTextColor(Color.RED);
        }
    }

    @Override
    public void setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        this.cancelable = cancelable;
        if(cancelLL != null) cancelLL.setVisibility(cancelable ? View.VISIBLE : View.GONE);
    }

    public void setCancelAction(Runnable r){
        cancel.setOnClickListener(v2->{
            r.run();
            dismiss();
        });
    }

    public Button addButton(String text, @Nullable Runnable runnable, ButtonType type){
        Button button = new Button(c);
        button.setText(text);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setAllCaps(false);
        button.setTextSize(16);
        int padding = Converter.dpToPx(c, PADDING);
        button.setPadding(padding,padding,padding,padding);
        button.setOnClickListener(v -> {
            if(closeOnAction) dismiss();
            if(runnable != null) runnable.run();
        });
        if(type == ButtonType.DEFAULT){
            button.setTextColor(c.getResources().getColor(R.color.highlighted));
        }else if(type == ButtonType.DESTROY){
            button.setTextColor(Color.RED);
        }

        layout.addView(button);
        return button;
    }

    public EditText addTextInput(String hint) {
        EditText editText = new EditText(c);
        editText.setBackgroundColor(0);
        editText.setHint(hint);
        editText.setTextSize(16);
        editText.setGravity(Gravity.CENTER);
        int padding = Converter.dpToPx(c, PADDING);
        editText.setPadding(padding,padding,padding,padding);
        layout.addView(editText);
        return editText;
    }

    public TextView addText(String text){
        TextView tv = new TextView(c);
        tv.setText(text);
        tv.setTextSize(16);
        int padding = Converter.dpToPx(c, PADDING);
        tv.setPadding(padding,padding,padding,padding);
        tv.setGravity(Gravity.CENTER);
        layout.addView(tv);
        return tv;
    }


    public <T extends View> T addView(T v){
        int padding = Converter.dpToPx(c, PADDING);
        v.setPadding(padding,padding,padding,padding);
        layout.addView(v);
        return v;
    }
}
