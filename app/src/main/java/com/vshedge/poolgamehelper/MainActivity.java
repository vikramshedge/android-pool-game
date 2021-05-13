package com.vshedge.poolgamehelper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    Button launchButton;
    Button showCanvasButton;
    DrawView drawView;
    WidgetService widgetService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        launchButton = (Button) findViewById(R.id.launchButton);

        getPermission();


        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(MainActivity.this)) {
                        getPermission();
                    } else {
//                        Intent intent = new Intent(MainActivity.this, WidgetService.class);
//                        startService(intent);
                        addNotification();
                        finish();
                    }
                } else {
//                    Toast.makeText(this, "If condition failed", Toast.LENGTH_SHORT).show();
                }

            }
        });

        showCanvasButton = (Button) findViewById(R.id.showCanvas);
        drawView = new DrawView(MainActivity.this);

        showCanvasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                setContentView( drawView);
//                addNotification();

                TextView tv1 = (TextView)findViewById(R.id.textView);
                TextView tv2 = (TextView)findViewById(R.id.textView2);
                TextView tv3 = (TextView)findViewById(R.id.textView3);

                tv1.setText("SunPt :" +DrawView.sunPtDummy.x + ", y: " + DrawView.sunPtDummy.y);
                tv2.setText("Tg Pt :" +DrawView.targetPt.x + ", y: " + DrawView.targetPt.y);
                tv3.setText("xMax :" + DrawView.xMaxR);
            }
        });
    }

    private ServiceConnection mConnection =  new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            Toast.makeText(this,)
            WidgetService.LocalBinder binder = (WidgetService.LocalBinder) service;
            widgetService = binder.getServiceInstance();
            widgetService.registerClient(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void addNotification() {
        String channelId = "ch_id_4411";

//        Intent intent = new Intent(MainActivity.this, MainActivity.class);
//        PendingIntent pendingIntent = PendingIntent.getActivity (this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent intent = new Intent(MainActivity.this, WidgetService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = (NotificationCompat.Builder) new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("My app title")
                .setContentText("this is details.")
                .setAutoCancel(false)
                .setOngoing(false)
                .setPriority(Notification.PRIORITY_MAX)
                .setContentIntent(pendingIntent);

        // Add as notification
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "my app channel", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        notificationManager.notify(0, builder.build());

    }

    public void getPermission() {
        //check for alert window permission
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)){
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent,1);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==1){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(MainActivity.this)) ;
                Toast.makeText(this, "Permission denied by user.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}