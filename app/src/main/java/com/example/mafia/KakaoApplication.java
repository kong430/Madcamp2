package com.example.mafia;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class KakaoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, "7ccfd07a71a565d5aee702e2d7d569f1");
    }
}
