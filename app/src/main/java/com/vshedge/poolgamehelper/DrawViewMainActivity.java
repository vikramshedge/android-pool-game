package com.vshedge.poolgamehelper;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.View;

public class DrawViewMainActivity extends View {

    public static PointF startPt;
    public static PointF endPt;
    public static PointF vertexPt;
    public static PointF deviatedEndPt;
    private float offsetY = -200;
    private float offsetX = -50;

    public DrawViewMainActivity(Context context) {
        super(context);
        Utilities.retrievePrevPrefValues(getContext(), 1);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.YELLOW);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
        RectF borderRect = new RectF(Utilities.xMin + offsetX, Utilities.yMin + offsetY, Utilities.xMax + offsetX, Utilities.yMax + offsetY);
        canvas.drawRect(borderRect, borderPaint);

        if (startPt != null && endPt != null && vertexPt != null && deviatedEndPt != null) {
            Paint linePaint = new Paint();
            linePaint.setStrokeWidth(4);

            linePaint.setColor(Color.BLACK);
            canvas.drawLine(startPt.x, startPt.y, vertexPt.x, vertexPt.y, linePaint);

            linePaint.setColor(Color.BLACK);
            canvas.drawLine(endPt.x, endPt.y, vertexPt.x, vertexPt.y, linePaint);

            linePaint.setColor(Color.RED);
//            linePaint.setStyle(Paint.Style.STROKE);
            canvas.drawLine(deviatedEndPt.x, deviatedEndPt.y, vertexPt.x, vertexPt.y, linePaint);

            linePaint.setColor(Color.BLACK);
            canvas.drawLine(startPt.x, startPt.y, vertexPt.x, vertexPt.y, linePaint);

        }

    }

    public void drawTestCords() {

        startPt.offset(offsetX, offsetY);
        endPt.offset(offsetX, offsetY);
        vertexPt.offset(offsetX, offsetY);
        deviatedEndPt.offset(offsetX, offsetY);

        this.invalidate();
    }
}
