package com.vshedge.poolgamehelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.vshedge.poolgamehelper.utilities.CacheClass;
import com.vshedge.poolgamehelper.utilities.PotHole;

import java.util.Calendar;

public class DrawView extends View {

    private static final int TAP_COUNT_LIMIT = 5;
    //    Paint paintRaw = new Paint();
    Paint directLinePaint = new Paint();
    Paint circlePaint = new Paint();
    Paint borderPaint = new Paint();

    private static float rBall = 24;
    public static CustomPointF startXPt = new CustomPointF(0, 0);
    public static CustomPointF endXPt = new CustomPointF(300, 300);

    private static int tapCount = 0;
    private static int multiTapCount = 1;
    private static float xMin = 200, yMin = 200, xMax = 600, yMax = 900, rXY = 0;
    public static float xMinR = 400, yMinR = 200, xMaxR = 600, yMaxR = 300, rXYR = 0;

    private float iterationCount = 0;


    public static PointF vertF, sunPtDummy, targetPt;

    private PointF topLeftPt;
    private PointF topRightPt;
    private PointF bottomRightPt;
    private PointF bottomLeftPt;
    private PointF topCenterPt;
    private PointF bottomCenterPt;

    private CustomPointF sunPt;
    private PointF topLeftPt_rightVertex;
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

    private static boolean IS_DEVIATED_REFLECTION = false;
    public static PotHole currentVerticesTobeShown = PotHole.NONE;
    public static PotHole selectedSatelite = PotHole.NONE;

    public DrawView(Context context) {
        super(context);
        this.customInit();
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

            @SuppressLint("ClickableViewAccessibility")
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
                                            if (multiTapCount <= 3 ) {
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
                            PointF startPt = startXPt;
                            PointF endPt = endXPt;

                            double firstDistance = Math.hypot(startXPt.getX() - currentTouchX, startXPt.getY() - currentTouchY);
                            double secondDistance = Math.hypot(endXPt.getX() - currentTouchX, endXPt.getY() - currentTouchY);

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
                                        Log.i(CacheClass.APP_NAME, "Ball radius : " + DrawView.rXY);
                                        break;
                                }
                            } else {
                                selectedSatelite = PotHole.NONE;
                                if (multiTapCount == 1) {
                                    //only start point move
                                    startXPt = new CustomPointF(dx + startXPt.getX(), dy + startXPt.getY());
//                                    if (DrawView.currentVerticesTobeShown == 0 || DrawView.currentVerticesTobeShown == 7)
                                        sunPt = startXPt.duplicate();
                                    printLogs();

                                } else if (multiTapCount == 2) {
                                    //move both start and end point
                                    if (Double.compare(firstDistance, secondDistance) < 0) {
                                        //move first i.e. starting point
                                        startXPt = new CustomPointF(dx + startXPt.getX(), dy + startXPt.getY());
                                    } else {
                                        //move second i.e. ending point
                                        endXPt = new CustomPointF(dx + endXPt.getX(), dy + endXPt.getY());
                                    }
                                    sunPt = endXPt.duplicate();
                                    printLogs();
                                } else if (multiTapCount == 3) {
                                    // set ball radius
                                    // commenting out, this has to be included in the separate menu button with group of dimension settings
//                                    DrawView.rBall = dy + DrawView.rBall;
//                                    selectedSatelite = getNearestPocket(event);
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
            if (!isInsideRect(event)) {
                DrawView.currentVerticesTobeShown = this.getNearestPocket(event);
                DrawView.multiTapCount = 1;
            }

            if (DrawView.multiTapCount == 2) {
                endXPt = new CustomPointF(event.getX(), event.getY());
                printLogs();
            } else if (DrawView.multiTapCount == 1) {
                if (DrawView.currentVerticesTobeShown == PotHole.NONE || DrawView.currentVerticesTobeShown == PotHole.CENTER) {
                    startXPt = new CustomPointF(event.getX(), event.getY());
                    printLogs();
//                } else {
//                    this.setAllVertexPoints();
                }
            } else if (DrawView.multiTapCount == 3) {
                selectedSatelite = getNearestPocket(event);
            }
            this.setAllVertexPoints();
        }

        this.setStatics(null);
        this.invalidate();

    }

    private void setStatics(PointF targetPt){
        DrawView.vertF = this.topLeftPt_rightVertex;
        DrawView.targetPt = startXPt;
        DrawView.sunPtDummy = sunPt;
//        xMinr: 366.50003, yminr:275.29996 ,ymaxr: 966.9, xMaxRr: 1788.8997;
//        sunPtX: 616.0, y:953.0
//        vertX: 603.5668, vertY: 966.9 calcuated vetex : lAngle = rRangle

    }

    private void setAllVertexPoints() {
        if (DrawView.multiTapCount == 2) {
            this.sunPt = endXPt.duplicate();
            printLogs();
        } else {
            if (DrawView.currentVerticesTobeShown == PotHole.NONE || DrawView.currentVerticesTobeShown == PotHole.CENTER) {
                this.sunPt = startXPt.duplicate();
                printLogs();
            }
        }

        if (DrawView.currentVerticesTobeShown == PotHole.TOP_LEFT) {
            PointF topLeftPt = new PointF(DrawView.xMinR, DrawView.yMinR);
            topLeftPt_rightVertex = this.getVertexPoint(this.sunPt, topLeftPt, false, DrawView.xMaxR, IS_DEVIATED_REFLECTION);
            topLeftPt_bottomVertex = this.getVertexPoint(this.sunPt, topLeftPt, true, DrawView.yMaxR, IS_DEVIATED_REFLECTION);
        } else {
            topLeftPt_rightVertex = null;
            topLeftPt_bottomVertex = null;
        }

        if (DrawView.currentVerticesTobeShown == PotHole.TOP_RIGHT) {
            PointF topRightPt = new PointF(DrawView.xMaxR, DrawView.yMinR);
            topRightPt_bottomVertex = this.getVertexPoint(this.sunPt, topRightPt, true, DrawView.yMaxR, IS_DEVIATED_REFLECTION);
            topRightPt_leftVertex = this.getVertexPoint(this.sunPt, topRightPt, false, DrawView.xMinR, IS_DEVIATED_REFLECTION);
        } else {
            topRightPt_bottomVertex = null;
            topRightPt_leftVertex = null;
        }

        if (DrawView.currentVerticesTobeShown == PotHole.BOTTOM_LEFT) {
            PointF bottomLeftPt = new PointF(DrawView.xMinR, DrawView.yMaxR);
            bottomLeftPt_topVertex = this.getVertexPoint(this.sunPt, bottomLeftPt, true, DrawView.yMinR, IS_DEVIATED_REFLECTION);
            bottomLeftPt_RightVertex = this.getVertexPoint(this.sunPt, bottomLeftPt, false, DrawView.xMaxR, IS_DEVIATED_REFLECTION);
        } else {
            bottomLeftPt_topVertex = null;
            bottomLeftPt_RightVertex = null;
        }

        if (DrawView.currentVerticesTobeShown == PotHole.BOTTOM_RIGHT) {
            PointF bottomRightPt = new PointF(DrawView.xMaxR, DrawView.yMaxR);
            bottomRightPt_topVertex = this.getVertexPoint(this.sunPt, bottomRightPt, false, DrawView.xMinR, IS_DEVIATED_REFLECTION);
            bottomRightPt_leftVertex = this.getVertexPoint(this.sunPt, bottomRightPt, true, DrawView.yMinR, IS_DEVIATED_REFLECTION);
        } else {
            bottomRightPt_topVertex = null;
            bottomRightPt_leftVertex = null;
        }

        if (DrawView.currentVerticesTobeShown == PotHole.TOP_CENTER) {
            PointF topCenterPt = new PointF((DrawView.xMaxR - DrawView.xMinR) / 2 + DrawView.xMin, DrawView.yMinR);
            topCenterPt_LeftVertex = this.getVertexPoint(this.sunPt, topCenterPt, false, DrawView.xMinR, IS_DEVIATED_REFLECTION);
            topCenterPt_BottomVertex = this.getVertexPoint(this.sunPt, topCenterPt, true, DrawView.yMaxR, IS_DEVIATED_REFLECTION);
            topCenterPt_RightVertex = this.getVertexPoint(this.sunPt, topCenterPt, false, DrawView.xMaxR, IS_DEVIATED_REFLECTION);
        } else {
            topCenterPt_LeftVertex = null;
            topCenterPt_BottomVertex = null;
            topCenterPt_RightVertex = null;
        }

        if (DrawView.currentVerticesTobeShown == PotHole.BOTTOM_CENTER) {
            PointF bottomCenterPt = new PointF((DrawView.xMaxR - DrawView.xMinR) / 2 + DrawView.xMin, DrawView.yMaxR);
            bottomCenterPt_LeftVertex = this.getVertexPoint(this.sunPt, bottomCenterPt, false, DrawView.xMaxR, IS_DEVIATED_REFLECTION);
            bottomCenterPt_TopVertex = this.getVertexPoint(this.sunPt, bottomCenterPt, false, DrawView.xMinR, IS_DEVIATED_REFLECTION);
            bottomCenterPt_RightVertex = this.getVertexPoint(this.sunPt, bottomCenterPt, true, DrawView.yMinR, IS_DEVIATED_REFLECTION);
        } else {
            bottomCenterPt_LeftVertex = null;
            bottomCenterPt_TopVertex = null;
            bottomCenterPt_RightVertex = null;
        }

        if (DrawView.currentVerticesTobeShown == PotHole.CENTER) {
            leftVertex = this.getVertexPoint(startXPt, this.sunPt, false, DrawView.xMinR, IS_DEVIATED_REFLECTION);
            rightVertex = this.getVertexPoint(startXPt, this.sunPt, false, DrawView.xMaxR, IS_DEVIATED_REFLECTION);
            topVertex = this.getVertexPoint(startXPt, this.sunPt, true, DrawView.yMinR, IS_DEVIATED_REFLECTION);
            bottomVertex = this.getVertexPoint(startXPt, this.sunPt, true, DrawView.yMaxR, IS_DEVIATED_REFLECTION);
        } else {
            leftVertex = null;
            rightVertex = null;
            topVertex = null;
            bottomVertex = null;

        }
    }

    private PointF getVertexPoint(PointF pt1, PointF pt2, boolean isHorizontal, float vertexDimVar, boolean isDeviation){

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
        vertexPoint = this.getVertexRecursion(lowerBound, upperBound, isHorizontal, vertexDimVar, startPt, endPt, dxLoopCounter, isStartPointLowerBound, isDeviation); // vertexDimVar is yMin for topEdge
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

    private PointF getVertexRecursion(float i, float upperBound, boolean isHorizontal, float vertexDimVar, PointF startPt, PointF endPt, float dxLooVar, boolean isStartPointLowerBond, boolean isDeviation) {

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

            double angleRatio = Math.abs(leftAngle/rightAngle);
            double angleDeviation = isStartPointLowerBond ? 0.6666666666666666666666666666 : 1.3333333333333333333333;
            int diff = isDeviation ? Double.compare(angleRatio, angleDeviation) :  Double.compare(leftAngle, rightAngle);
            if (diff == 0) {
                return vertexPt;
            } else if ( diff > 0) {
                if (Math.abs(dxLooVar) < 0.0001) {
                    return vertexPt;
                } else {
                    return this.getVertexRecursion(i, upperBound, isHorizontal, vertexDimVar, startPt, endPt, -dxLooVar/10.0f, isStartPointLowerBond, isDeviation);
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

        if (DrawView.multiTapCount == 2) {
            circlePaint.setColor(Color.BLACK); directLinePaint.setColor(Color.BLACK);
            if (DrawView.selectedSatelite == PotHole.NONE)
                this.drawDirectLine(canvas, startXPt,  this.sunPt, directLinePaint, circlePaint, true, true);
            else
                this.drawDirectLine(canvas, startXPt,  this.getSateliteCenter(sunPt, DrawView.selectedSatelite), directLinePaint, circlePaint, true, true);

            circlePaint.setColor(Color.BLACK);
            directLinePaint.setColor(Color.BLACK);
            this.drawDirectLine(canvas, startXPt, this.sunPt, directLinePaint, circlePaint, false, true);
            this.drawAngel(canvas, startXPt, this.sunPt, this.leftVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, startXPt, this.sunPt, this.topVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, startXPt, this.sunPt, this.rightVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, startXPt, this.sunPt, this.bottomVertex, directLinePaint, circlePaint);

        } else {
            if (startXPt != null && this.sunPt != null && (DrawView.currentVerticesTobeShown ==  PotHole.NONE || DrawView.currentVerticesTobeShown == PotHole.CENTER)) {
                circlePaint.setColor(Color.BLACK);
                directLinePaint.setColor(Color.BLACK);
                this.drawDirectLine(canvas, startXPt, this.sunPt, directLinePaint, circlePaint, true, true);
            }
        }
        this.drawDirectLinesToPots(canvas);
        this.defineSatelites(canvas);


        //draw table borders
//        canvas.drawRect(DrawView.xMin, DrawView.yMin, DrawView.xMax, DrawView.yMax, borderPaint);
        canvas.drawRoundRect(DrawView.xMin, DrawView.yMin, DrawView.xMax, DrawView.yMax, DrawView.rXY, DrawView.rXY, borderPaint);

    }

    private void drawDirectLinesToPots(Canvas canvas) {

        if (this.sunPt != null) {
            topLeftPt = new PointF(DrawView.xMinR, DrawView.yMinR);
            circlePaint.setColor(Color.RED);
            directLinePaint.setColor(Color.RED);
            this.drawDirectLine(canvas, this.sunPt, topLeftPt, directLinePaint, circlePaint, true, true);
            this.drawAngel(canvas, this.sunPt, topLeftPt, this.topLeftPt_rightVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, topLeftPt, this.topLeftPt_bottomVertex, directLinePaint, circlePaint);

            topRightPt = new PointF(DrawView.xMaxR, DrawView.yMinR);
            circlePaint.setColor(Color.BLUE);
            directLinePaint.setColor(Color.BLUE);
            this.drawDirectLine(canvas, this.sunPt, topRightPt, directLinePaint, circlePaint, false, true);
            this.drawAngel(canvas, this.sunPt, topRightPt, this.topRightPt_bottomVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, topRightPt, this.topRightPt_leftVertex, directLinePaint, circlePaint);

            bottomRightPt = new PointF(DrawView.xMaxR, DrawView.yMaxR);
            circlePaint.setColor(Color.rgb(25, 25, 112));
            directLinePaint.setColor(Color.rgb(25, 25, 112)); //dark green
            this.drawDirectLine(canvas, this.sunPt, bottomRightPt, directLinePaint, circlePaint, false, true);
            this.drawAngel(canvas, this.sunPt, bottomRightPt, this.bottomRightPt_topVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, bottomRightPt, this.bottomRightPt_leftVertex, directLinePaint, circlePaint);

            bottomLeftPt = new PointF(DrawView.xMinR, DrawView.yMaxR);
            circlePaint.setColor(Color.rgb(139, 69, 19));
            directLinePaint.setColor(Color.rgb(139, 69, 19)); //saddle brown
            this.drawDirectLine(canvas, this.sunPt, bottomLeftPt, directLinePaint, circlePaint, false, true);
            this.drawAngel(canvas, this.sunPt, bottomLeftPt, this.bottomLeftPt_topVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, bottomLeftPt, this.bottomLeftPt_RightVertex, directLinePaint, circlePaint);

            topCenterPt = new PointF((DrawView.xMaxR - DrawView.xMinR) / 2 + DrawView.xMinR, DrawView.yMin); //purposely kept yMin instead of yMinR, otherwise ball target will be outside the pocket not pocket
            circlePaint.setColor(Color.rgb(255, 140, 0));
            directLinePaint.setColor(Color.rgb(255, 140, 0)); //dark orange
            this.drawDirectLine(canvas, this.sunPt, topCenterPt, directLinePaint, circlePaint, false, true);
            this.drawAngel(canvas, this.sunPt, topCenterPt, this.topCenterPt_LeftVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, topCenterPt, this.topCenterPt_BottomVertex, directLinePaint, circlePaint);
            this.drawAngel(canvas, this.sunPt, topCenterPt, this.topCenterPt_RightVertex, directLinePaint, circlePaint);

            bottomCenterPt = new PointF((DrawView.xMaxR - DrawView.xMinR) / 2 + DrawView.xMinR, DrawView.yMax); //purposely kept yMax instead of yMaxR, otherwise ball target will be outside the pocket not pocket
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

    private void drawAngel(Canvas canvas, PointF sunPt, PointF targetPt, PointF vertexPt, Paint linePaint, Paint circlePaint){

        if (vertexPt != null && sunPt != null && targetPt != null) {
            this.drawDirectLine(canvas, sunPt, vertexPt, linePaint, circlePaint, true, true);
            this.drawDirectLine(canvas, targetPt, vertexPt, linePaint, circlePaint, true, true);
        }
    }

    private void defineSatelites(Canvas canvas) {
        if (this.sunPt != null) {
            drawSingleSatelite(new CustomPointF(topLeftPt), new CustomPointF(sunPt), canvas);
            drawSingleSatelite(new CustomPointF(topRightPt), new CustomPointF(sunPt), canvas);
            drawSingleSatelite(new CustomPointF(bottomLeftPt), new CustomPointF(sunPt), canvas);
            drawSingleSatelite(new CustomPointF(bottomRightPt), new CustomPointF(sunPt), canvas);
            drawSingleSatelite(new CustomPointF(topCenterPt), new CustomPointF(sunPt), canvas);
            drawSingleSatelite(new CustomPointF(bottomCenterPt), new CustomPointF(sunPt), canvas);
        }
    }

    private void drawSingleSatelite(CustomPointF potHoleCenter, CustomPointF sunPt, Canvas canvas) {

        if (potHoleCenter != null && sunPt != null){

            CustomPointF satelitePoint = this.getSateliteCenter(potHoleCenter, sunPt);

            Paint sateListPaint = new Paint();
            sateListPaint.setColor(Color.RED);
            sateListPaint.setStrokeWidth(3);
            sateListPaint.setStyle(Paint.Style.STROKE);
            //        canvas.drawCircle(satelitePoint.getX(), satelitePoint.getY(), DrawView.rBall, sateListPaint);
            canvas.drawLine(satelitePoint.getX(), satelitePoint.getY(), sunPt.getX(), sunPt.getY(), sateListPaint);
            //center dot
            canvas.drawCircle(satelitePoint.getX(), satelitePoint.getY(), 4, sateListPaint);
        }

    }

    private CustomPointF getSateliteCenter(CustomPointF potHoleCenter, CustomPointF sunPt) {
        float len = (float) Math.sqrt(((sunPt.getX()-potHoleCenter.getX()) * (sunPt.getX()-potHoleCenter.getX()))
                + ((sunPt.getY() - potHoleCenter.getY())*(sunPt.getY() - potHoleCenter.getY())));

        float dx = (sunPt.getX()-potHoleCenter.getX()) / len;
        float dy = (sunPt.getY()-potHoleCenter.getY()) / len;

        CustomPointF satelitePoint = new CustomPointF(sunPt.getX() + (DrawView.rBall * dx * 2), sunPt.getY() + (DrawView.rBall * dy * 2));
        return satelitePoint;
    }

    private CustomPointF getSateliteCenter(CustomPointF sunPt, PotHole potHole) {
        CustomPointF potHoleCenter;
        switch (potHole) {
            case TOP_LEFT:
                potHoleCenter = new CustomPointF(topLeftPt);
                break;
            case TOP_RIGHT:
                potHoleCenter = new CustomPointF(topRightPt);
                break;
            case BOTTOM_LEFT:
                potHoleCenter = new CustomPointF(bottomLeftPt);
                break;
            case BOTTOM_RIGHT:
                potHoleCenter = new CustomPointF(bottomRightPt);
                break;
            case TOP_CENTER:
                potHoleCenter = new CustomPointF(topCenterPt);
                break;
            case BOTTOM_CENTER:
                potHoleCenter = new CustomPointF(bottomCenterPt);
                break;
            default:
                potHoleCenter = null;
                break;
        }
        CustomPointF satelitePoint = this.getSateliteCenter(potHoleCenter, sunPt);
        return satelitePoint;
    }

    private boolean isInsideRect(MotionEvent event) {
        RectF rect = new RectF(DrawView.xMin, DrawView.yMin, DrawView.xMax, DrawView.yMax);

        return rect.contains(event.getX(), event.getY());
    }


    private PotHole getNearestPocket(MotionEvent event) {
        PotHole returnValue = PotHole.NONE;

        double topLeftD = Math.abs(Math.hypot(event.getX() - DrawView.xMin, event.getY() - DrawView.yMin));
        double topRightD = Math.abs(Math.hypot(event.getX() - DrawView.xMax, event.getY() - DrawView.yMin));
        double bottomLeftD = Math.abs(Math.hypot(event.getX() - DrawView.xMin, event.getY() - DrawView.yMax));
        double bottomRightD = Math.abs(Math.hypot(event.getX() - DrawView.xMax, event.getY() - DrawView.yMax));

        double topCenterD = Math.abs(Math.hypot(event.getX() - (DrawView.xMax - DrawView.xMin)/2 + DrawView.xMin, event.getY() - DrawView.yMin));
        double bottomCenterD = Math.abs(Math.hypot(event.getX() - (DrawView.xMax - DrawView.xMin)/2 + DrawView.xMin, event.getY() - DrawView.yMax));

        double minD = topLeftD;
        returnValue = PotHole.TOP_LEFT;
        if (Double.compare(minD, topRightD) > 0) {
            minD = topRightD;
            returnValue = PotHole.TOP_RIGHT;
        }

        if (Double.compare(minD, bottomLeftD) > 0) {
            returnValue = PotHole.BOTTOM_LEFT;
            minD = bottomLeftD;
        }

        if (Double.compare(minD, bottomRightD) > 0) {
            returnValue = PotHole.BOTTOM_RIGHT;
            minD = bottomRightD;
        }

        if (Double.compare(minD, topCenterD) > 0) {
            returnValue = PotHole.TOP_CENTER;
            minD = topCenterD;
        }

        if (Double.compare(minD, bottomCenterD) > 0)
            returnValue = PotHole.BOTTOM_CENTER;

        return returnValue;
    }

    public void setStartToEndVertices(){
        DrawView.currentVerticesTobeShown = PotHole.CENTER;
        this.setAllVertexPoints();
        invalidate();
    }

    public void printLogs(){
        Log.i(CacheClass.APP_NAME, "StartPoint: " + startXPt.printCords());
        Log.i(CacheClass.APP_NAME, "SunPoint: " + sunPt.printCords());
        Log.i(CacheClass.APP_NAME, "EndPoint: " + endXPt.printCords());
    }

    public void toggleDeviationReflection() {
        DrawView.IS_DEVIATED_REFLECTION = ! DrawView.IS_DEVIATED_REFLECTION;
        this.setAllVertexPoints();
        this.invalidate();
    }

    private void thisInvalidate(){
        this.invalidate();
    }

    private void printToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

}