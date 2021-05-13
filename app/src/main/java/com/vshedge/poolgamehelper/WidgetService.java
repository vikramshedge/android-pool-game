package com.vshedge.poolgamehelper;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;

public class WidgetService extends Service {

    int LAYOUT_FLAG;
    View mFloatingView;
    WindowManager windowManager;
    Button closeButton;
    Button toggleButton;
    Button loadPrevStateButtonPool;
    Button saveCurrentSateButtonPool;
    Button loadPrevStateButtonGamezy;
    Button saveCurrentSateButtonGamezy;
    View stateButtonGroupView;
    float height, width;
    boolean isTouchable = true;
    DrawView drawView;

    WindowManager.LayoutParams drawViewParams;

    Callbacks activity;
    private final IBinder mBinder = new LocalBinder();
    Handler handler = new Handler();
    Runnable serviceRunnable = new Runnable() {
        @Override
        public void run() {
//            millis = System.currentTimeMillis() - startTime;
//            activity.updateClient(millis); //Update Activity (client) by the implementd callback
            handler.postDelayed(this, 1000);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(Activity activity){
        this.activity = (Callbacks)activity;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        return super.onStartCommand(intent, flags, startId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        //inflate widget layout
        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_widget, null);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //initial position
        layoutParams.gravity = Gravity.CENTER;
//        layoutParams.x = 0;
//        layoutParams.y = 100;

        //Layoutparams for close button
        WindowManager.LayoutParams toggleButtonParams = new WindowManager.LayoutParams(140,
                140,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        toggleButtonParams.gravity = Gravity.TOP | Gravity.RIGHT;
//        imageParams.y = 100;

        WindowManager.LayoutParams closeButtonParams = new WindowManager.LayoutParams(140,
                140,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        closeButtonParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
/*

        WindowManager.LayoutParams loadPrevStateButtonParams = new WindowManager.LayoutParams(140,
                140,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        loadPrevStateButtonParams.gravity = Gravity.CENTER | Gravity.RIGHT;
*/

        WindowManager.LayoutParams saveCurrentStateButtonParams = new WindowManager.LayoutParams(140,
                600,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        saveCurrentStateButtonParams.gravity = Gravity.CENTER | Gravity.RIGHT;

        drawViewParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        drawViewParams.gravity = Gravity.CENTER;

        toggleButton = new Button(this);
        toggleButton.setText("Toggle");
        toggleButton.setTextSize(7);
        toggleButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toggleTouch();
            }
        });

        closeButton = new Button(this);
        closeButton.setText("X");
        closeButton.setTextSize(7);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remove floating window
                stopSelf();
            }
        });

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        height = windowManager.getDefaultDisplay().getHeight();
        width = windowManager.getDefaultDisplay().getWidth();

        drawView = new DrawView(this, width/4, height/4, 3*width/4, 3*height/4);

        windowManager.addView(mFloatingView,layoutParams);
        mFloatingView.setVisibility(View.VISIBLE);
        windowManager.addView(drawView, drawViewParams);
        windowManager.addView(toggleButton, toggleButtonParams);
        windowManager.addView(closeButton, closeButtonParams);
//        windowManager.addView(loadPrevStateButton, loadPrevStateButtonParams);
//        windowManager.addView(saveCurrentSateButton, saveCurrentStateButtonParams);

        stateButtonGroupView = LayoutInflater.from(this).inflate(R.layout.layout_statebuttons, null);

        loadPrevStateButtonPool = (Button) stateButtonGroupView.findViewById(R.id.loadStateButtonPool);
        loadPrevStateButtonPool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPrevStateValues(1);
            }
        });
        saveCurrentSateButtonPool = (Button) stateButtonGroupView.findViewById(R.id.saveStateButtonPool);
        saveCurrentSateButtonPool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentStateValues(1);
            }
        });
        loadPrevStateButtonGamezy = (Button) stateButtonGroupView.findViewById(R.id.loadStateButtonGamezy);
        loadPrevStateButtonGamezy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPrevStateValues(2);
            }
        });
        saveCurrentSateButtonGamezy = (Button) stateButtonGroupView.findViewById(R.id.saveStateButtonGamezy);
        saveCurrentSateButtonGamezy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCurrentStateValues(2);
            }
        });
        windowManager.addView(stateButtonGroupView, saveCurrentStateButtonParams);
        stateButtonGroupView.setVisibility(View.VISIBLE);

        return START_STICKY;
    }

    private void loadPrevStateValues(int choice) {
        Toast.makeText(this, "Load state clicked" + (choice == 1 ? "Pool":"Gamezy"), Toast.LENGTH_SHORT).show();
        Utilities.retrievePrevPrefValues(this, choice);
        this.drawView.invalidate();
    }

    private void saveCurrentStateValues(int choice) {
        Toast.makeText(this, "Save state is disabled : " + (choice == 1 ? "Pool":"Gamezy"), Toast.LENGTH_SHORT).show();
//        Utilities.saveCurrentPrefValues(this, choice);
    }

    private void toggleTouch() {
//        Toast.makeText(this, "Toggle button clicked", Toast.LENGTH_SHORT).show();
        WindowManager.LayoutParams layoutParams, drawViewParams;
        if (isTouchable) {
            layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    LAYOUT_FLAG,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT);

            drawViewParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    LAYOUT_FLAG,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSLUCENT);
//            drawView.setAlpha((float) 0.1);

        }else{
            layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    LAYOUT_FLAG,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            drawViewParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    LAYOUT_FLAG,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
//            drawView.setAlpha((float) 0.5);
        }
        drawView.makeTranslucent(isTouchable);
        isTouchable = !isTouchable;
        WindowManager windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        windowManager.updateViewLayout(mFloatingView, layoutParams );
        windowManager.updateViewLayout(drawView, drawViewParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Toast.makeText(this, "close button called", Toast.LENGTH_SHORT).show();
        if (mFloatingView != null){
            windowManager.removeView(mFloatingView);
        }

        if (closeButton != null) {
            windowManager.removeView(closeButton);
        }

        if(toggleButton != null) {
            windowManager.removeView(toggleButton);
        }

        if (drawView != null) {
            windowManager.removeView(drawView);
        }

        if (stateButtonGroupView != null) {
            windowManager.removeView(stateButtonGroupView);
        }
    }

    public class LocalBinder extends Binder {
        public WidgetService getServiceInstance(){
            return WidgetService.this;
        }
    }

    //callbacks interface for communication with service clients!
    public interface Callbacks{
        public void updateClient(long data);
    }
}
