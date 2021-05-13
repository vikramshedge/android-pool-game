package com.vshedge.poolgamehelper;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Vector;

public class DrawView extends View {

    private static final int TAP_COUNT_LIMIT = 5;
    //    Paint paintRaw = new Paint();
    Paint directLinePaint = new Paint();
    Paint circlePaint = new Paint();
    Paint borderPaint = new Paint();

    private static float startX1 = 0, startY1 = 0, endX1 = 300, endY1 = 300, rBall = 24;

    private static int tapCount = 0, multiTapCount = 1;
    private static float xMin = 200, yMin = 200, xMax = 600, yMax = 900, rXY = 0;
    public static float xMinR = 400, yMinR = 200, xMaxR = 600, yMaxR = 300, rXYR = 0;

    private float iterationCount = 0;


    public static PointF vertF, sunPtDummy, targetPt;

    private PointF topLeftPt_rightVertex, sunPt;
    private PointF topLeftPt_bottomVertex;
    private PointF topRightPt_bottomVertex;
    private PointF topRightPt_leftVertex;
    private PointF bottomRightPt_topVertex;
    private PointF bottomRightPt_leftVertex;
    private PointF bottomLeftPt_topVertex;
    private PointF bottomLeftPt_RightVertex;
    private PointF topCenterPt_LeftVertex;
    private PointF topCenterPt_BottomVertex;
    private PointF topCenterPt_RightVertex;
    private PointF bottomCenterPt_LeftVertex;
    private PointF bottomCenterPt_TopVertex;
    private PointF bottomCenterPt_RightVertex;
    private PointF leftVertex;
    private PointF topVertex;
    private PointF rightVertex;
    private PointF bottomVertex;

    private static int currentVerticesTobeShown = 0;

    public DrawView(Context context) {
        super(context);
        this.customInit();
    }

    public DrawView(Context context, float startX1, float startY1, float endX1, float endY1) {
        super(context);
        this.customInit();
        DrawView.startX1 = startX1;
        DrawView.startY1 = startY1;
        DrawView.endX1 = endX1;
        DrawView.endY1 = endY1;
    }

    public static void loadPrevState(float xMin, float yMin, float xMax, float yMax, float rXY, float rBall) {
        DrawView.xMin = xMin;
        DrawView.yMin = yMin;
        DrawView.xMax = xMax;
        DrawView.yMax = yMax;
        DrawView.rXY = rXY;
        DrawView.rBall = rBall;
        DrawView.tapCount = DrawView.TAP_COUNT_LIMIT;
    }

    public static SharedPreferences.Editor saveCurrentState(SharedPreferences.Editor editor) {

        editor.putFloat("xMin", DrawView.xMin);
        editor.putFloat("yMin", DrawView.yMin);
        editor.putFloat("xMax", DrawView.xMax);
        editor.putFloat("yMax", DrawView.yMax);
        editor.putFloat("rXY", DrawView.rXY);
        editor.putFloat("rBall", DrawView.rBall);

        return editor;
    }

    private void customInit(){
        directLinePaint.setColor(Color.BLACK);
        directLinePaint.setStrokeWidth(4);

        borderPaint.setColor(Color.YELLOW);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);

        circlePaint.setColor(Color.BLACK);
        circlePaint.setStrokeWidth(3);
        circlePaint.setStyle(Paint.Style.STROKE);

        this.setOnTouchListener(new OnTouchListener() {
            long startClickTimeForUp, startClickTimeForMove, prevMultiTapTime = 0, currentMultiTapTime = 0;
            int MAX_CIRCLE_DURATION = 200, MIN_CIRCLE_DURATION_FOR_MOVE = 5, MULTITAP_DURATION = 200;
            float initialTouchX, initialTouchY;
            int prevMotion = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        startClickTimeForUp = Calendar.getInstance().getTimeInMillis();
                        startClickTimeForMove = Calendar.getInstance().getTimeInMillis();

                        initialTouchX = event.getX();
                        initialTouchY = event.getY();
                        prevMotion = event.getAction();
                        return true;

                    case MotionEvent.ACTION_UP:
                        long clickDuration = Calendar.getInstance().getTimeInMillis()-startClickTimeForUp;
                        if (clickDuration < MAX_CIRCLE_DURATION){

                                clickDuration = Calendar.getInstance().getTimeInMillis()-prevMultiTapTime;
                                if (clickDuration < MULTITAP_DURATION) {
                                    //multitap encoutered
                                    multiTapCount++;
                                } else {
                                    multiTapCount = 1;
                                    //show current time & date is text View
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (multiTapCount <= 2 ) {
                                                updateDrawView(event);
                                            }
                                        }
                                    }, MULTITAP_DURATION + 100);

                                }
                                prevMultiTapTime = Calendar.getInstance().getTimeInMillis();
                        }

                        clickDuration = Calendar.getInstance().getTimeInMillis()-startClickTimeForUp;
                        if (clickDuration > MAX_CIRCLE_DURATION) {
                            if (tapCount < TAP_COUNT_LIMIT) {
                                //this should stop setting presion
                                tapCount++;
                            }
                        }

                        prevMotion = event.getAction();
                        return true;

                    case MotionEvent.ACTION_MOVE:
//                        float targetX, targetY;
                        clickDuration = Calendar.getInstance().getTimeInMillis()-startClickTimeForMove;
                        if (clickDuration > MIN_CIRCLE_DURATION_FOR_MOVE) {
                            float currentTouchX = event.getX();
                            float currentTouchY = event.getY();

                            PointF touchedPoint = new PointF(currentTouchX, currentTouchY);
                            PointF startPt = new PointF(startX1, startY1);
                            PointF endPt = new PointF(endX1, endY1);

                            double firstDistance = Math.hypot(startX1 - currentTouchX, startY1 - currentTouchY);
                            double secondDistance = Math.hypot(endX1 - currentTouchX, endY1 - currentTouchY);

                            float dx = (currentTouchX - initialTouchX)/5.0f;
                            float dy = (currentTouchY - initialTouchY)/5.0f;

                            if (tapCount < TAP_COUNT_LIMIT) {
                                // adjust precison of each border of the table rect
                                switch (tapCount){
                                    case 0:
                                        DrawView.xMin = dx + DrawView.xMin;
                                        break;
                                    case 1:
                                        DrawView.yMin = dy + DrawView.yMin;
                                        break;
                                    case 2:
                                        DrawView.xMax = dx + DrawView.xMax;
                                        break;
                                    case 3:
                                        DrawView.yMax = dy + DrawView.yMax;
                                        break;
                                    case 4:
                                        DrawView.rXY = dy + DrawView.rXY;
                                        break;
                                }
                            } else {
                                if (multiTapCount == 1) {
                                    //only start point move
                                    startX1 = dx + startX1;
                                    startY1 = dy + startY1;
//                                    if (DrawView.currentVerticesTobeShown == 0 || DrawView.currentVerticesTobeShown == 7)
                                        sunPt = new PointF(startX1, startY1);
                                } else if (multiTapCount == 2) {
                                    //move both start and end point
                                    if (Double.compare(firstDistance, secondDistance) < 0) {
                                        //move first i.e. starting point
                                        startX1 = dx + startX1;
                                        startY1 = dy + startY1;
                                    } else {
                                        //move second i.e. ending point
                                        endX1 = dx + endX1;
                                        endY1 = dy + endY1;
                                    }
                                    sunPt = new PointF(endX1, endY1);
                                } else if (multiTapCount == 3) {
                                    // set ball radius
                                    DrawView.rBall = dy + DrawView.rBall;
                                }
                                setAllVertexPoints();
                            }
                            initialTouchX = event.getX();
                            initialTouchY = event.getY();
                            startClickTimeForMove = Calendar.getInstance().getTimeInMillis();

                            setStatics(new PointF(DrawView.xMinR, DrawView.yMinR));
                            thisInvalidate();

                        }
                        prevMotion = event.getAction();

                        return true;
                }

                return true;
            }
        });




        this.makeTranslucent(false);

    }

    public void makeTranslucent(boolean isVal) {
        if (isVal){
            this.setBackgroundColor(Color.TRANSPARENT);
            this.setAlpha(1f);
        }else{
            this.setBackgroundColor(Color.WHITE);
            this.setAlpha(0.5f);
        }
    }

    private void updateDrawView(MotionEvent event) {

        if (DrawView.tapCount < TAP_COUNT_LIMIT) {
            // set border rect for first time
            switch(DrawView.tapCount) {
                case 0:
                    xMin = event.getX();
//                    isAdjustingBorder = true;
                    break;
                case 1:
                    yMin = event.getY();
                    break;
                case 2:
                    xMax = event.getX();
                    break;
                case 3:
                    yMax = event.getY();
                    break;
            }

        } else {
            // border rect already drawn, proceed with fun of lines
            this.getNearestPocket(event);
            if (DrawView.multiTapCount == 2) {
                DrawView.endX1 = event.getX();
                DrawView.endY1 = event.getY();
            } else {
                if (DrawView.currentVerticesTobeShown == 0 || DrawView.currentVerticesTobeShown == 7) {
                    DrawView.startX1 = event.getX();
                    DrawView.startY1 = event.getY();
//                } else {
//                    this.setAllVertexPoints();
                }
            }
            this.setAllVertexPoints();
        }

        this.setStatics(new PointF(DrawView.xMinR, DrawView.yMinR));
        this.invalidate();

    }

    private void setStatics(PointF targetPt){
        DrawView.vertF = this.topLeftPt_rightVertex;
        DrawView.targetPt = targetPt;
        DrawView.sunPtDummy = sunPt;
//        xMinr: 366.50003, yminr:275.29996 ,ymaxr: 966.9, xMaxRr: 1788.8997;
//        sunPtX: 616.0, y:953.0
//        vertX: 603.5668, vertY: 966.9 calcuated vetex : lAngle = rRangle

    }

    private void setAllVertexPoints() {
        if (DrawView.multiTapCount == 2) {
            this.sunPt = new PointF(DrawView.endX1, DrawView.endY1);
        } else {
            if (DrawView.currentVerticesTobeShown == 0 || DrawView.currentVerticesTobeShown == 7)
                this.sunPt = new PointF(DrawView.startX1, DrawView.startY1);
        }

        if (DrawView.currentVerticesTobeShown == 1) {
            PointF topLeftPt = new PointF(DrawView.xMinR, DrawView.yMinR);
            topLeftPt_rightVertex = this.getVertexPoint(this.sunPt, topLeftPt, false, DrawView.xMaxR);
            topLeftPt_bottomVertex = this.getVertexPoint(this.sunPt, topLeftPt, true, DrawView.yMaxR);
        } else {
            topLeftPt_rightVertex = null;
            topLeftPt_bottomVertex = null;
        }

        if (DrawView.currentVerticesTobeShown == 2) {
            PointF topRightPt = new PointF(DrawView.xMaxR, DrawView.yMinR);
            topRightPt_bottomVertex = this.getVertexPoint(this.sunPt, topRightPt, true, DrawView.yMaxR);
            topRightPt_leftVertex = this.getVertexPoint(this.sunPt, topRightPt, false, DrawView.xMinR);
        } else {
            topRightPt_bottomVertex = null;
            topRightPt_leftVertex = null;
        }

        if (DrawView.currentVerticesTobeShown == 3) {
            PointF bottomLeftPt = new PointF(DrawView.xMinR, DrawView.yMaxR);
            bottomLeftPt_topVertex = this.getVertexPoint(this.sunPt, bottomLeftPt, true, DrawView.yMinR);
            bottomLeftPt_RightVertex = this.getVertexPoint(this.sunPt, bottomLeftPt, false, DrawView.xMaxR);
        } else {
            bottomLeftPt_topVertex = null;
            bottomLeftPt_RightVertex = null;
        }

        if (DrawView.currentVerticesTobeShown == 4) {
            PointF bottomRightPt = new PointF(DrawView.xMaxR, DrawView.yMaxR);
            bottomRightPt_topVertex = this.getVertexPoint(this.sunPt, bottomRightPt, false, DrawView.xMinR);
            bottomRightPt_leftVertex = this.getVertexPoint(this.sunPt, bottomRightPt, true, DrawView.yMinR);
        } else {
            bottomRightPt_topVertex = null;
            bottomRightPt_leftVertex = null;
        }

        if (DrawView.currentVerticesTobeShown == 5) {
            PointF topCenterPt = new PointF((DrawView.xMaxR - DrawView.xMinR) / 2 + DrawView.xMin, DrawView.yMinR);
            topCenterPt_LeftVertex = this.getVertexPoint(this.sunPt, topCenterPt, false, DrawView.xMinR);
            topCenterPt_BottomVertex = this.getVertexPoint(this.sunPt, topCenterPt, true, DrawView.yMaxR);
            topCenterPt_RightVertex = this.getVertexPoint(this.sunPt, topCenterPt, false, DrawView.xMaxR);
        } else {
            topCenterPt_LeftVertex = null;
            topCenterPt_BottomVertex = null;
            topCenterPt_RightVertex = null;
        }

        if (DrawView.currentVerticesTobeShown == 6) {
            PointF bottomCenterPt = new PointF((DrawView.xMaxR - DrawView.xMinR) / 2 + DrawView.xMin, DrawView.yMaxR);
            bottomCenterPt_LeftVertex = this.getVertexPoint(this.sunPt, bottomCenterPt, false, DrawView.xMaxR);
            bottomCenterPt_TopVertex = this.getVertexPoint(this.sunPt, bottomCenterPt, false, DrawView.xMinR);
            bottomCenterPt_RightVertex = this.getVertexPoint(this.sunPt, bottomCenterPt, true, DrawView.yMinR);
        } else {
            bottomCenterPt_LeftVertex = null;
            bottomCenterPt_TopVertex = null;
            bottomCenterPt_RightVertex = null;
        }

        if (DrawView.currentVerticesTobeShown == 7) {
            PointF startPt = new PointF(DrawView.startX1, DrawView.startY1);
            leftVertex = this.getVertexPoint(startPt, this.sunPt, false, DrawView.xMinR);
            rightVertex = this.getVertexPoint(startPt, this.sunPt, false, DrawView.xMaxR);
            topVertex = this.getVertexPoint(startPt, this.sunPt, true, DrawView.yMinR);
            bottomVertex = this.getVertexPoint(startPt, this.sunPt, true, DrawView.yMaxR);
        } else {
            leftVertex = null;
            rightVertex = null;
            topVertex = null;
            bottomVertex = null;

        }
    }

    private PointF getVertexPoint(PointF pt1, PointF pt2, boolean isHorizontal, float vertexDimVar){

        float dxLoopCounter = 10.0f;
        boolean isStartPointLowerBound;
        if (isHorizontal) {
            if (pt1.y == vertexDimVar || pt2.y == vertexDimVar)
                return null;
        } else {
            if (pt1.x == vertexDimVar || pt2.x == vertexDimVar)
                return null;
        }

        PointF startPt, endPt;
        if (isHorizontal) {
            if (pt1.x < pt2.x) {
                startPt = pt1; endPt = pt2;
                isStartPointLowerBound = true;
            }else {
                startPt = pt2; endPt = pt1;
                isStartPointLowerBound = false;
            }
        }else {
            if (pt1.y < pt2.y) {
                startPt = pt1; endPt = pt2;
                isStartPointLowerBound = true;
            }else {
                startPt = pt2; endPt = pt1;
                isStartPointLowerBound = false;
            }
        }
        
        float lowerBound = isHorizontal ? startPt.x : startPt.y;
        float upperBound = isHorizontal ? endPt.x : endPt.y;

        PointF vertexPoint;
        this.iterationCount = 0;
        vertexPoint = this.getVertexRecursion(lowerBound, upperBound, isHorizontal, vertexDimVar, startPt, endPt, dxLoopCounter, isStartPointLowerBound); // vertexDimVar is yMin for topEdge
//        vertexPoint = new PointF(vertexDimVar, (yMaxR - yMinR)/2 + yMinR);
//        Toast.makeText(getContext(), "Iteration count: " + iterationCount, Toast.LENGTH_SHORT).show();
        if (vertexPoint != null) {
            if (vertexPoint.x == pt1.x && vertexPoint.y == pt1.y)
                return null;

            if (vertexPoint.x == pt2.x && vertexPoint.y == pt2.y)
                return null;
        }

        return vertexPoint;
    }

    private PointF getVertexRecursion(float i, float upperBound, boolean isHorizontal, float vertexDimVar, PointF startPt, PointF endPt, float dxLooVar, boolean isStartPointLowerBond ) {

        while ( i < upperBound) {
            PointF vertexPt = isHorizontal ? new PointF(i, vertexDimVar) : new PointF(vertexDimVar, i); // vertexDimVar is yMinR for topEdge
            double leftAngle, rightAngle;
            if (isHorizontal) {
                leftAngle = this.angle(vertexPt, startPt, new PointF(i, startPt.y));
                rightAngle = this.angle(vertexPt, endPt, new PointF(i, endPt.y));
            } else {
                leftAngle = this.angle(vertexPt, startPt, new PointF(startPt.x, i));
                rightAngle = this.angle(vertexPt, endPt, new PointF(endPt.x, i));
            }

//            int diff = Double.compare(leftAngle, rightAngle);
            double angleRatio = Math.abs(leftAngle/rightAngle);
            double angleDeviation = isStartPointLowerBond ? 0.591711719678775 : 1.690012157512910;
            int diff = Double.compare(angleRatio, angleDeviation);
            if (diff == 0) {
                return vertexPt;
            } else if ( diff > 0) {
                if (Math.abs(dxLooVar) < 0.001) {
                    return vertexPt;
                } else {
                    return this.getVertexRecursion(i, upperBound, isHorizontal, vertexDimVar, startPt, endPt, -dxLooVar/10.0f, isStartPointLowerBond);
                }
            }
            iterationCount++;
            i = i + dxLooVar;

        }
//        this.printToast("recursion returning null : Iteration: " + this.iterationCount);
        return null;
    }

    public double angle(PointF vertexPt, PointF startPt, PointF endPt) {
        final double x = (new Float(vertexPt.x)).doubleValue();
        final double y = (double)vertexPt.y;

        final double ax = (new Float(startPt.x)).doubleValue() - x;
        final double ay = (new Float(startPt.y)).doubleValue() - y;
        final double bx = (new Float(endPt.x)).doubleValue() - x;
        final double by = (new Float(endPt.y)).doubleValue() - y;

        final double delta = (ax * bx + ay * by) / Math.sqrt(
                (ax * ax + ay * ay) * (bx * bx + by * by));

        if (delta > 1.0) {
            return 0.0;
        }
        if (delta < -1.0) {
            return 180.0;
        }

        return Math.toDegrees(Math.acos(delta));
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        DrawView.xMinR = DrawView.xMin + DrawView.rBall;
        DrawView.yMinR = DrawView.yMin + DrawView.rBall;
        DrawView.xMaxR = DrawView.xMax - DrawView.rBall;
        DrawView.yMaxR = DrawView.yMax - DrawView.rBall;

        PointF startPt = new PointF(DrawView.startX1, DrawView.startY1);
        if (DrawView.multiTapCount == 2) {
            circlePaint.setColor(Color.BLACK); directLinePaint.setColor(Color.BLACK);
            this.drawDirectLine(canvas, startPt, this.sunPt, directLinePaint, circlePaint, true, true);

            circlePaint.setColor(Color.BLACK);
            directLinePaint.setColor(Color.BLACK);
            this.drawDirectLine(canvas, startPt, this.sunPt, directLinePaint, circlePaint, false, true);
            this.drawAngel(canvas, startPt, this.sunPt, this.leftVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, startPt, this.sunPt, this.topVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, startPt, this.sunPt, this.rightVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, startPt, this.sunPt, this.bottomVertex, directLinePaint, circlePaint);

        } else {
            if (startPt != null && this.sunPt != null && (DrawView.currentVerticesTobeShown ==  0 || DrawView.currentVerticesTobeShown ==7)) {
                circlePaint.setColor(Color.BLACK);
                directLinePaint.setColor(Color.BLACK);
                this.drawDirectLine(canvas, startPt, this.sunPt, directLinePaint, circlePaint, true, true);
            }
        }
        this.drawDirectLinesToPots(canvas);


        //draw table borders
//        canvas.drawRect(DrawView.xMin, DrawView.yMin, DrawView.xMax, DrawView.yMax, borderPaint);
        canvas.drawRoundRect(DrawView.xMin, DrawView.yMin, DrawView.xMax, DrawView.yMax, DrawView.rXY, DrawView.rXY, borderPaint);

    }

    private void drawDirectLinesToPots(Canvas canvas) {

        if (this.sunPt != null) {
            PointF topLeftPt = new PointF(DrawView.xMinR, DrawView.yMinR);
            circlePaint.setColor(Color.RED);
            directLinePaint.setColor(Color.RED);
            this.drawDirectLine(canvas, this.sunPt, topLeftPt, directLinePaint, circlePaint, true, true);
            this.drawAngel(canvas, this.sunPt, topLeftPt, this.topLeftPt_rightVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, topLeftPt, this.topLeftPt_bottomVertex, directLinePaint, circlePaint);

            PointF topRightPt = new PointF(DrawView.xMaxR, DrawView.yMinR);
            circlePaint.setColor(Color.BLUE);
            directLinePaint.setColor(Color.BLUE);
            this.drawDirectLine(canvas, this.sunPt, topRightPt, directLinePaint, circlePaint, false, true);
            this.drawAngel(canvas, this.sunPt, topRightPt, this.topRightPt_bottomVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, topRightPt, this.topRightPt_leftVertex, directLinePaint, circlePaint);

            PointF bottomRightPt = new PointF(DrawView.xMaxR, DrawView.yMaxR);
            circlePaint.setColor(Color.rgb(25, 25, 112));
            directLinePaint.setColor(Color.rgb(25, 25, 112)); //dark green
            this.drawDirectLine(canvas, this.sunPt, bottomRightPt, directLinePaint, circlePaint, false, true);
            this.drawAngel(canvas, this.sunPt, bottomRightPt, this.bottomRightPt_topVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, bottomRightPt, this.bottomRightPt_leftVertex, directLinePaint, circlePaint);

            PointF bottomLeftPt = new PointF(DrawView.xMinR, DrawView.yMaxR);
            circlePaint.setColor(Color.rgb(139, 69, 19));
            directLinePaint.setColor(Color.rgb(139, 69, 19)); //saddle brown
            this.drawDirectLine(canvas, this.sunPt, bottomLeftPt, directLinePaint, circlePaint, false, true);
            this.drawAngel(canvas, this.sunPt, bottomLeftPt, this.bottomLeftPt_topVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, bottomLeftPt, this.bottomLeftPt_RightVertex, directLinePaint, circlePaint);

            PointF topCenterPt = new PointF((DrawView.xMaxR - DrawView.xMinR) / 2 + DrawView.xMinR, DrawView.yMin); //purposely kept yMin instead of yMinR, otherwise ball target will be outside the pocket not pocket
            circlePaint.setColor(Color.rgb(255, 140, 0));
            directLinePaint.setColor(Color.rgb(255, 140, 0)); //dark orange
            this.drawDirectLine(canvas, this.sunPt, topCenterPt, directLinePaint, circlePaint, false, true);
            this.drawAngel(canvas, this.sunPt, topCenterPt, this.topCenterPt_LeftVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, topCenterPt, this.topCenterPt_BottomVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, topCenterPt, this.topCenterPt_RightVertex, directLinePaint, circlePaint);

            PointF bottomCenterPt = new PointF((DrawView.xMaxR - DrawView.xMinR) / 2 + DrawView.xMinR, DrawView.yMax); //purposely kept yMax instead of yMaxR, otherwise ball target will be outside the pocket not pocket
            circlePaint.setColor(Color.BLUE);
            directLinePaint.setColor(Color.BLUE);
            this.drawDirectLine(canvas, this.sunPt, bottomCenterPt, directLinePaint, circlePaint, false, true);
            this.drawAngel(canvas, this.sunPt, bottomCenterPt, this.bottomCenterPt_LeftVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, bottomCenterPt, this.bottomCenterPt_TopVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, bottomCenterPt, this.bottomCenterPt_RightVertex, directLinePaint, circlePaint);
        }
    }

    private void drawDirectLine(Canvas canvas, PointF startPt, PointF endPt, Paint directLinePaint, Paint circlePaint, boolean shouldDrawStartCircle, boolean shouldDrawEndCircle) {

        canvas.drawLine(startPt.x, startPt.y, endPt.x, endPt.y, directLinePaint);

        if (shouldDrawStartCircle)
            canvas.drawCircle(startPt.x, startPt.y, DrawView.rBall, circlePaint);

        if (shouldDrawEndCircle)
            canvas.drawCircle(endPt.x, endPt.y, DrawView.rBall, circlePaint);

    }

    private void thisInvalidate(){
        this.invalidate();
    }

    private void printToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void drawAngel(Canvas canvas, PointF sunPt, PointF targetPt, PointF vertexPt, Paint linePaint, Paint circlePaint){

        if (vertexPt != null && sunPt != null && targetPt != null) {
            this.drawDirectLine(canvas, sunPt, vertexPt, linePaint, circlePaint, true, true);
            this.drawDirectLine(canvas, targetPt, vertexPt, linePaint, circlePaint, true, true);
        }
    }

    private void getNearestPocket(MotionEvent event) {
        int returnValue = 0;
        RectF rect = new RectF(DrawView.xMin, DrawView.yMin, DrawView.xMax, DrawView.yMax);
        
        if (!rect.contains(event.getX(), event.getY())) {
            double topLeftD = Math.abs(Math.hypot(event.getX() - DrawView.xMin, event.getY() - DrawView.yMin));
            double topRightD = Math.abs(Math.hypot(event.getX() - DrawView.xMax, event.getY() - DrawView.yMin));
            double bottomLeftD = Math.abs(Math.hypot(event.getX() - DrawView.xMin, event.getY() - DrawView.yMax));
            double bottomRightD = Math.abs(Math.hypot(event.getX() - DrawView.xMax, event.getY() - DrawView.yMax));

            double topCenterD = Math.abs(Math.hypot(event.getX() - (DrawView.xMax - DrawView.xMin)/2 + DrawView.xMin, event.getY() - DrawView.yMin));
            double bottomCenterD = Math.abs(Math.hypot(event.getX() - (DrawView.xMax - DrawView.xMin)/2 + DrawView.xMin, event.getY() - DrawView.yMax));

            double minD = topLeftD;
            returnValue = 1;
            if (Double.compare(minD, topRightD) > 0) {
                minD = topRightD;
                returnValue = 2;
            }

            if (Double.compare(minD, bottomLeftD) > 0) {
                returnValue = 3;
                minD = bottomLeftD;
            }

            if (Double.compare(minD, bottomRightD) > 0) {
                returnValue = 4;
                minD = bottomRightD;
            }

            if (Double.compare(minD, topCenterD) > 0) {
                returnValue = 5;
                minD = topCenterD;
            }

            if (Double.compare(minD, bottomCenterD) > 0)
                returnValue = 6;

        }

        if (returnValue > 0)
            multiTapCount = 1;

        DrawView.currentVerticesTobeShown = returnValue;
//        DrawView.currentVerticesTobeShown = 7;
    }
}
