package com.iblogstreet.cachemanager;

import android.app.Application;

import com.iblogstreet.cachemanager.utils.cache.CacheManager;

/**
 * 类描述：${DESC}
 * 创建人：Administrator
 * 创建时间：2018/3/8
 */

public class AppAppliction extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CacheManager.getInstance().init(this);
    }
}
