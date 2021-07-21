package com.lab240.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import com.lab240.lab240.R;
import com.lab240.utils.CanvasUtil;

public class RingIndicatorView extends View {
    public RingIndicatorView(Context context) {
        super(context);
        init(context, null);
    }

    public RingIndicatorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RingIndicatorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs){
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.RingIndicatorView, 0, 0);
        try {
            setFullness(ta.getFloat(R.styleable.RingIndicatorView_fullness, 1), false);
            String text = ta.getString(R.styleable.RingIndicatorView_text);
            setColor(ta.getColor(R.styleable.RingIndicatorView_color, Color.BLACK));
            setText(text != null ? text : (100* getFullness())+"%");
        } finally {
            ta.recycle();
        }
    }

    private final static int minAngle = -35;
    private final static int maxAngle = 180-minAngle;
    private final static float innerRadiusToSize = 0.35f;
    private final static float outerRadiusToSize = 0.5f;
    private final static float textToSize = 0.5f*2f*innerRadiusToSize;

    public String getText() {
        return text;
    }

    public RingIndicatorView setText(String text) {
        this.text = text;
        invalidate();
        return this;
    }

    private String text = "100%";

    public float getFullness() {
        return targetFullness;
    }

    public RingIndicatorView setDrawnFullness(float drawnFullness) {
        this.drawnFullness = Math.min(1, Math.max(drawnFullness, 0));
        invalidate();
        return this;
    }

    public RingIndicatorView setFullness(float fullness, boolean animate, @Nullable ValueAnimator.AnimatorUpdateListener listener){
        float old = targetFullness;
        this.targetFullness = Math.min(1, Math.max(fullness, 0));
        if(animate){
            ObjectAnimator animation = ObjectAnimator.ofFloat(this, "drawnFullness", old, targetFullness);
            animation.setInterpolator(new AccelerateDecelerateInterpolator());
            if(listener != null)
                animation.addUpdateListener(listener);
            animation.setDuration(200);
            animation.start();
        }else {
            setDrawnFullness(targetFullness);
        }
        return this;
    }

    public RingIndicatorView setFullness(float fullness, boolean animate){
        return setFullness(fullness, animate, null);
    }

    private float drawnFullness = 0;
    private float targetFullness = 0;

    public int getColor() {
        return color;
    }

    public RingIndicatorView setColor(int color) {
        this.color = color;
        invalidate();
        return this;
    }

    private int color;
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w,h);
        int cw = w/2, ch = h/2;
        float innerRadius = size*innerRadiusToSize,
                outerRadius = size*outerRadiusToSize;
        Path path = new Path();

        int currentMinAngle = (int)(maxAngle-(maxAngle-minAngle)* drawnFullness);

        path.moveTo((int)(cw+innerRadius*Math.cos(currentMinAngle*Math.PI/180)),(int)(ch-innerRadius*Math.sin(currentMinAngle*Math.PI/180)));

        for(int phi = currentMinAngle; phi <= maxAngle; phi++) {
            path.lineTo((int) (cw + innerRadius * Math.cos(phi * Math.PI / 180)), (int) (ch - innerRadius * Math.sin(phi * Math.PI / 180)));
        }

        for(int phi = maxAngle; phi >= currentMinAngle; phi--) {
            path.lineTo((int) (cw + outerRadius * Math.cos(phi * Math.PI / 180)), (int) (ch - outerRadius * Math.sin(phi * Math.PI / 180)));
        }
        path.close();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        canvas.drawPath(path, paint);


        Paint textPaint = new Paint();
        textPaint.setTextSize(CanvasUtil.getTextSizeForBounds(textPaint, size*textToSize, size*textToSize, text.length()));
        textPaint.setTextAlign(Paint.Align.CENTER);

        int xPos = cw;
        int yPos = (int) (ch - ((textPaint.descent() + textPaint.ascent()) / 2)) ;
        canvas.drawText(text, xPos, yPos, textPaint);
    }
}
