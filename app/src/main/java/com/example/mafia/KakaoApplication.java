package com.example.mafia;

import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class KakaoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KakaoSdk.init(this, "568f763dc4b54f15600e9ab5eb265ce5");
    }

}
