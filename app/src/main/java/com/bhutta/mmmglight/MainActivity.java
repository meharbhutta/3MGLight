package com.bhutta.mmmglight;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.Toast;

public class MainActivity extends AppWidgetProvider {

    private final String ACTION_FLASHIGHT = "action.mmmglight.flashlight";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_FLASHIGHT)) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            int check = preferences.getInt(context.getString(R.string.on_off), 0);
            if (check < 2) {
                updateAndControlFlash(preferences,context,check);
            }
        } else {
            super.onReceive(context, intent);
        }
    }

    @Override
    public void onEnabled(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            preferences.edit().putInt(context.getString(R.string.on_off), 0).apply();
        } else {
            Toast.makeText(context, "No Flash Supported!", Toast.LENGTH_SHORT).show();
            preferences.edit().putInt(context.getString(R.string.on_off), 2).apply();
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int check = preferences.getInt(context.getString(R.string.on_off), 0);
        if (check < 2) {
            if (check == 0) {
                updateWidget(context, appWidgetManager, check);
            } else {
                updateWidget(context, appWidgetManager, check);
            }
        }
    }

    @Override
    public void onDisabled(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int check = preferences.getInt(context.getString(R.string.on_off), 0);
        if (check < 2 && check == 1) {
            controlFlash(0,context);
        }
    }

    private void updateAndControlFlash(SharedPreferences preferences, Context context, int check) {
        if (check == 0) {
            preferences.edit().putInt(context.getString(R.string.on_off), 1).apply();
            updateWidget(context,AppWidgetManager.getInstance(context),1);
            controlFlash(1, context);
        } else {
            preferences.edit().putInt(context.getString(R.string.on_off), 0).apply();
            updateWidget(context,AppWidgetManager.getInstance(context),0);
            controlFlash(0, context);
        }
    }

    private void playSound(int check, Context context) {
        MediaPlayer mediaPlayer;
        if (check == 1) {
            mediaPlayer = MediaPlayer.create(context, R.raw.switch_on);
        } else {
            mediaPlayer = MediaPlayer.create(context, R.raw.switch_off);
        }
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
            }
        });
        mediaPlayer.start();
    }

    private void controlFlash(int check, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            CameraManager camera = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            try {
                if (check == 1) camera.setTorchMode( camera.getCameraIdList()[0], true);
                else camera.setTorchMode( camera.getCameraIdList()[0], false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (check == 1) context.startService(new Intent(context, FlashService.class));
            else context.stopService(new Intent(context, FlashService.class));
        }
        playSound(check, context);
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int check) {
        RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.activity_main);
        int drawID;
        if (check == 1) {
            drawID = R.drawable.flashlight_on;
        } else {
            drawID = R.drawable.flashlight_off;
        }
        remoteView.setImageViewResource(R.id.main_widget_id, drawID);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(ACTION_FLASHIGHT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.main_widget_id, pendingIntent);
        appWidgetManager.updateAppWidget( appWidgetManager.getAppWidgetIds(new ComponentName(context,MainActivity.class)),remoteView);
    }
}
