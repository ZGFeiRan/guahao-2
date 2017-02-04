package com.tianxing.userapps.guahao5;

import android.app.Application;

/**
 * Created by litao on 2017/2/4.
 */

public class MyApp extends Application {
    private static MyApp instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
    public static MyApp getInstance() {
        return instance;
    }
}
