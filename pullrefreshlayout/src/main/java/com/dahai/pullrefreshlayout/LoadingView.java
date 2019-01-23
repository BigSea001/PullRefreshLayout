package com.dahai.pullrefreshlayout;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * 创建时间： 2018/12/19
 * 作者：大海
 * 描述：
 */
public class LoadingView extends View {
    private static final String TAG = LoadingView.class.getSimpleName();
    private String[] color;
    private int heigheRect;
    private int height;
    private int pos;
    private Rect rect;
    private Paint rectPaint;
    private int width;
    private int widthRect;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public LoadingView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        pos = 0;
        color = new String[]{"#999999", "#999999", "#999999", "#999999", "#999999", "#999999"};
        init();
    }

    private void init() {
        rectPaint = new Paint(1);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // 如果没有指定宽高，设置默认40dp
        if (widthMode ==MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED) {
            width = dpToPx(40);
        } else {
            width = MeasureSpec.getSize(widthMeasureSpec);
            height = MeasureSpec.getSize(heightMeasureSpec);
            // 为了保证圆，设置最小
            width = Math.min(width, height);
        }
        widthRect = width / 15;
        heigheRect = widthRect * 4;
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (rect == null) {
            rect = new Rect((width - widthRect) / 2, 0, (width + widthRect) / 2, heigheRect);
        }
        int i = 0;
        while (i < 12) {
            if (i - pos >= 5) {
                rectPaint.setColor(Color.parseColor(color[5]));
            } else if (i - pos >= 0 && i - pos < 5) {
                rectPaint.setColor(Color.parseColor(color[i - pos]));
            } else if (i - pos >= -7 && i - pos < 0) {
                rectPaint.setColor(Color.parseColor(color[5]));
            } else if (i - pos >= -11 && i - pos < -7) {
                rectPaint.setColor(Color.parseColor(color[(i + 12) - pos]));
            }
            canvas.drawRect(rect, rectPaint);
            canvas.rotate(30.0f, (float) (width / 2), (float) (width / 2));
            i++;
        }
        pos++;
        if (pos > 11) {
            pos = 0;
        }
//        postInvalidateDelayed(100);
    }

    private int dpToPx(int i) {
        return (int) (((float) i) * Resources.getSystem().getDisplayMetrics().density);
    }
}
