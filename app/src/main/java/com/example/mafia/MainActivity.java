package com.example.mafia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kakao.sdk.auth.LoginClient;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApi;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private View loginButton;
    private TextView textView;

    MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mediaPlayer = MediaPlayer.create(this, R.raw.bgm);
        mediaPlayer.start();
        mediaPlayer.setLooping(true);


        loginButton = findViewById(R.id.login);
        textView = findViewById(R.id.textView);

        //결과에 대한 처리. 로그인 버튼/프로필 사진 보임 or 숨김
        Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() {
            @Override
            public Unit invoke(OAuthToken oAuthToken, Throwable throwable) {
                Log.d("invoke test", "invoke ok");
                if (oAuthToken != null) {

                }
                if (throwable != null) {

                }
                updateKakaoLoginUi();
                return null;
            }
        };

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("invoke test", "clicked");
                if (LoginClient.getInstance().isKakaoTalkLoginAvailable(MainActivity.this)) {
                    LoginClient.getInstance().loginWithKakaoAccount(MainActivity.this, callback);
                } else {
                    //웹페이지
                    LoginClient.getInstance().loginWithKakaoAccount(MainActivity.this, callback);
                }
            }
        });
    }
    private void updateKakaoLoginUi() {
        Log.d("invoke test", "ui");
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                Log.d("invoke test", "update");
                if (user != null) {
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.putExtra("user_id", user.getId());
                    intent.putExtra("user_nickname", user.getKakaoAccount().getProfile().getNickname());
                    intent.putExtra("user_image", user.getKakaoAccount().getProfile().getThumbnailImageUrl());
                    startActivity(intent);
                    finish();
                }
                return null;
            }
        });
    }

    private void getHashKey(){
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo == null)
            Log.e("KeyHash", "KeyHash:null");

        for (Signature signature : packageInfo.signatures) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            } catch (NoSuchAlgorithmException e) {
                Log.e("KeyHash", "Unable to get MessageDigest. signature=" + signature, e);
            }
        }
    }

}