package com.example.smart_internship_assistant;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.navi.NaviSetting;
import com.example.smart_internship_assistant.constant.ChatConst;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

public class MainApplication extends Application {
    private static final String TAG = "MainApplication";
    private static MainApplication mApp; // 声明一个当前应用的静态实例
    private Socket mSocket; // 声明一个套接字对象

    // 利用单例模式获取当前应用的唯一实例
    public static MainApplication getInstance() {
        return mApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: 成功进入");
        mApp = this; // 在打开应用时对静态的应用实例赋值
        try {
            @SuppressLint("DefaultLocale") String uri = String.format("http://%s:%d/", ChatConst.CHAT_IP, ChatConst.CHAT_PORT);
            mSocket = IO.socket(uri); // 创建指定地址和端口的套接字实例
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

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

    // 获取套接字对象的唯一实例
    public Socket getSocket() {
        return mSocket;
    }
}