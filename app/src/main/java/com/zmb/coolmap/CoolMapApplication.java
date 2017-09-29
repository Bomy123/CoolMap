package com.zmb.coolmap;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Process;
import android.util.Log;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

/**
 * Created by zhangmingbao on 17-9-25.
 */
public class CoolMapApplication extends Application {
    private String TAG="CoolMapApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
        SDKInitializer.setCoordType(CoordType.BD09LL);


    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d(TAG, "onTerminate: ");
    }
}
