package com.example.mafia;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class KakaoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, "1229bfdf3e10dc4b4697212ab143c194");
    }

}
