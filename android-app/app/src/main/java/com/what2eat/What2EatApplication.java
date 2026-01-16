package com.what2eat;

import android.app.Application;

/**
 * 自定义Application类
 */
public class What2EatApplication extends Application {

    private static What2EatApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static What2EatApplication getInstance() {
        return instance;
    }
}
