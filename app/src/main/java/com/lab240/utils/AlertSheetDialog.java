package com.lab240.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.lab240.lab240.R;

import java.util.ArrayList;
import java.util.List;

public class AlertSheetDialog extends BottomSheetDialogFragment {
    public enum ButtonType{
        DEFAULT, DESTROY;
    }
    LinearLayout layout, cancelLL;
    View v;
    Button cancel;
    String cancelText = null;
    ButtonType cancelType = ButtonType.DESTROY;
    Context c;
    private final List<View> views = new ArrayList<>();
    private Runnable onShow = null;
    View.OnClickListener cancelAction = v->{};
    Runnable dismissAction = ()->{};

    public AlertSheetDialog(Context c) {
        super();
        this.c = c;
    }

    private static final int PADDING = 20;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.inflate_bottom_sheet, container, false);
        layout = v.findViewById(R.id.buttons);
        cancelLL = v.findViewById(R.id.cancelLL);
        Context c = inflater.getContext();
        cancel = new Button(c);
        cancel.setBackgroundColor(Color.TRANSPARENT);
        cancel.setAllCaps(false);
        cancel.setTextSize(16);
        cancel.setOnClickListener(v->dismiss());
        int padding = Converter.dpToPx(c, PADDING);
        cancel.setPadding(padding,padding,padding,padding);
        if(cancelText == null)
            cancelText = getResources().getString(R.string.cancel);
        setCancelButtonText(cancelText, cancelType);
        cancelLL.addView(cancel);
        getDialog().setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            bottomSheetDialog.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            bottomSheetDialog.getBehavior().setSkipCollapsed(true);
            bottomSheetDialog.getBehavior().setHideable(false);
            bottomSheetDialog.getBehavior().setDraggable(false);
            if(onShow != null)
                onShow.run();
        });
        push();
        setCancelable(cancelable);
        setCancelAction(cancelAction);
        setDismissAction(dismissAction);
        return v;
    }

    public void show(@NonNull FragmentManager manager, @Nullable String tag, Runnable onShow) {
        super.show(manager, tag);
        this.onShow = onShow;
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
        cancelText = s;
        cancelType = type;
        if(cancel != null) {
            cancel.setText(s);

            if(type == ButtonType.DEFAULT){
                cancel.setTextColor(AppCompatResources.getColorStateList(c,R.color.alert_sheet_dialog_button_default));
            }else if(type == ButtonType.DESTROY){
                cancel.setTextColor(AppCompatResources.getColorStateList(c,R.color.alert_sheet_dialog_button_destroy));
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        dismissAction.run();
    }

    @Override
    public void setCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        this.cancelable = cancelable;
        if(cancelLL != null) cancelLL.setVisibility(cancelable ? View.VISIBLE : View.GONE);
    }

    public void setCancelAction(View.OnClickListener r){
        cancelAction = r;
        if(cancel != null) cancel.setOnClickListener(v2->{
            if(r != null) r.onClick(v2);
            dismiss();
        });
    }

    public void setDismissAction(Runnable r){
        dismissAction = r;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        cancelAction.onClick(cancel);
        dismissAction.run();
    }

    public Button addButton(String text, @Nullable Button.OnClickListener runnable, ButtonType type){
        Button button = new Button(c);
        button.setText(text);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setAllCaps(false);
        button.setTextSize(16);
        int padding = Converter.dpToPx(c, PADDING);
        button.setPadding(padding,padding,padding,padding);
        button.setOnClickListener(v -> {
            if(closeOnAction) dismiss();
            if(runnable != null) runnable.onClick(button);
        });
        if(type == ButtonType.DEFAULT){
            button.setTextColor(AppCompatResources.getColorStateList(c,R.color.alert_sheet_dialog_button_default));
        }else if(type == ButtonType.DESTROY){
            button.setTextColor(AppCompatResources.getColorStateList(c,R.color.alert_sheet_dialog_button_destroy));
        }

        views.add(button);
        push();
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
        views.add(editText);
        push();
        return editText;
    }

    public TextView addText(String text){
        TextView tv = new TextView(c);
        tv.setText(text);
        tv.setTextSize(16);
        int padding = Converter.dpToPx(c, PADDING);
        tv.setPadding(padding,padding,padding,padding);
        tv.setGravity(Gravity.CENTER);
        views.add(tv);
        push();
        return tv;
    }


    public <T extends View> T addView(T v){
        int padding = Converter.dpToPx(c, PADDING);
        v.setPadding(padding,padding,padding,padding);
        views.add(v);
        push();
        return v;
    }

    private void push() {
        if(layout != null){
            for(View v : views){
                layout.addView(v);
            }
            views.clear();
        }
    }
}
