package com.vshedge.poolgamehelper;

import android.content.Context;
import android.content.SharedPreferences;

public class Utilities {

    private static final String BALL_POOL_COORDS = "INIT_COORDS_LEVEL_1";
    private static final String GAMEZY_COORDS = "INIT_COORDS_LEVEL_2";

    public static float xMin = 0;
    public static float yMin = 0;
    public static float xMax = 0;
    public static float yMax = 0;
    public static float rXY = 0;
    public static float rBall = 0;

    public static void retrievePrevPrefValues(Context context, int choice){
        SharedPreferences myPrefs = context.getSharedPreferences(choice == 1 ? Utilities.BALL_POOL_COORDS : Utilities.GAMEZY_COORDS, Context.MODE_PRIVATE);
        xMin = myPrefs.getFloat("xMin", 400.00f);
        yMin = myPrefs.getFloat("yMin", 200.00f);
        xMax = myPrefs.getFloat("xMax", 600.00f);
        yMax = myPrefs.getFloat("yMax", 300.00f);
        rXY = myPrefs.getFloat("rXY", 0.00f);
        rBall = myPrefs.getFloat("rBall", 0.00f);

        DrawView.loadPrevState(xMin, yMin, xMax, yMax, rXY, rBall) ;
    }

    public static void saveCurrentPrefValues(Context context, int choice) {
        SharedPreferences myPrefs = context.getSharedPreferences(choice == 1 ? Utilities.BALL_POOL_COORDS : Utilities.GAMEZY_COORDS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = myPrefs.edit();

        editor = DrawView.saveCurrentState(editor);
        editor.apply();
    }
}
