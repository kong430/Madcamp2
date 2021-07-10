package com.example.mafia;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kakao.sdk.auth.LoginClient;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApi;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class LoginActivity extends Activity {

    private TextView nickName;
    private ImageView profileImage;
    private View logoutButton;
    private View enterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nickName = findViewById(R.id.nickname);
        profileImage = findViewById(R.id.profile);
        logoutButton = findViewById(R.id.logout);
        enterButton = findViewById(R.id.enter);

        Intent intent = getIntent();
        String user_id = intent.getStringExtra("user_id");
        String user_nickname = intent.getStringExtra("user_nickname");
        Uri user_profile = intent.getParcelableExtra("user_image");

        nickName.setText(user_nickname);
        Glide.with(profileImage).load(user_profile).circleCrop().into(profileImage);

        logoutButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        updateKakaoLogoutUi();
                        return null;
                    }
                });
            }
        });
        enterButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
                intent.putExtra("user_nickname", user_nickname);
                startActivity(intent);
                //finish();
            }
        });
    }

    private void updateKakaoLogoutUi() {
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user == null) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                return null;
            }
        });
    }
}