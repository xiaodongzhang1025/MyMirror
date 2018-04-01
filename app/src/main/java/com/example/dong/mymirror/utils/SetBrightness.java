package com.example.dong.mymirror.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.WindowManager;

/**
 * Created by dong on 2018/3/28.
 */

public class SetBrightness {
    public static boolean isAutoBrightness(ContentResolver aContentResolver){
        boolean automicBrightness = false;
        try {
            automicBrightness = Settings.System.getInt(aContentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;

        }catch (Settings.SettingNotFoundException e){
            e.printStackTrace();
        }
        return automicBrightness;
    }
    public static int getScreenBrightness(Activity activity){
        int nowBrightnessValue = 0;
        ContentResolver resolver = activity.getContentResolver();
        try{
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver,
                    Settings.System.SCREEN_BRIGHTNESS);
        }catch (Exception e){
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }
    public static void setBrightness(Activity activity, int brightness){
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness)*(1f/255f);
        activity.getWindow().setAttributes(lp);
    }
    public static void stopAutoBrightness(Activity activity){
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }
    public static void startAutoBrightness(Activity activity){
        Settings.System.putInt(activity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }
    public static void saveBrightness(ContentResolver resolver, int brightness){
        Uri uri = android.provider.Settings.System.getUriFor("screen_brightness");
        android.provider.Settings.System.putInt(resolver, "screen_brightness", brightness);
        resolver.notifyChange(uri, null);
    }
    public static ContentResolver getResolver(Activity activity){
        ContentResolver cr = activity.getContentResolver();
        return cr;
    }
}
