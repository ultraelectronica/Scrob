package com.example.appointmentscheduler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Minimal donut-style chart: one segment shows the completed fraction of total.
 */
public class SimplePieChartView extends View {

    private final Paint completedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint remainingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds = new RectF();
    private float completedFraction;

    public SimplePieChartView(Context context) {
        super(context);
        init(context);
    }

    public SimplePieChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SimplePieChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        completedPaint.setColor(ContextCompat.getColor(context, R.color.ios_accent));
        remainingPaint.setColor(ContextCompat.getColor(context, R.color.ios_surface_tertiary));
        completedFraction = 0f;
    }

    /**
     * @param fraction value from 0 to 1 (completed / total when total &gt; 0)
     */
    public void setCompletedFraction(float fraction) {
        if (fraction < 0f) {
            fraction = 0f;
        } else if (fraction > 1f) {
            fraction = 1f;
        }
        this.completedFraction = fraction;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int size = Math.min(getWidth(), getHeight());
        float stroke = size * 0.14f;
        float inset = stroke / 2f;
        bounds.set(inset, inset, size - inset, size - inset);

        remainingPaint.setStyle(Paint.Style.STROKE);
        remainingPaint.setStrokeWidth(stroke);
        completedPaint.setStyle(Paint.Style.STROKE);
        completedPaint.setStrokeWidth(stroke);

        canvas.drawArc(bounds, -90f, 360f, false, remainingPaint);
        float sweep = 360f * completedFraction;
        if (sweep > 0.2f) {
            canvas.drawArc(bounds, -90f, sweep, false, completedPaint);
        }
    }
}
