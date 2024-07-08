package com.example.smart_internship_assistant;

import android.app.Application;
import android.content.Context;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.navi.NaviSetting;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Context mContext = this;
        // 定位隐私政策同意
        AMapLocationClient.updatePrivacyShow(mContext,true,true);
        AMapLocationClient.updatePrivacyAgree(mContext,true);
        // 地图隐私政策同意
        MapsInitializer.updatePrivacyShow(mContext,true,true);
        MapsInitializer.updatePrivacyAgree(mContext,true);
        // 导航隐私政策
        NaviSetting.updatePrivacyShow(mContext, true, true);
        NaviSetting.updatePrivacyAgree(mContext, true);

    }
}
