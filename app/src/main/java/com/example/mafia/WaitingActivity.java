package com.example.mafia;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WaitingActivity extends AppCompatActivity {

    private WebSocket webSocket;
    private String SERVER_PATH = "ws://192.249.18.102:443";
    static JSONObject received_json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);
        initiateSocketConnection();
    }

    private void initiateSocketConnection() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        Log.d("request", String.valueOf(request));
        webSocket = client.newWebSocket(request, new WaitingActivity.SocketListener());
        Log.d("request...", String.valueOf(webSocket));

    }

    private class SocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response){
            super.onOpen(webSocket, response);

            runOnUiThread(() -> {
                Toast.makeText(getApplicationContext(),
                        "Socket Connection Successful!",
                        Toast.LENGTH_SHORT).show();
            });

        }

        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            runOnUiThread(() -> {
                received_json = null;
                try {
                    received_json = new JSONObject(text);
                    if (received_json.has("full")){
                        Intent intent = new Intent(WaitingActivity.this, RoomActivity.class);

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("complete", "1");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        webSocket.send(jsonObject.toString());
                        Thread.sleep(2000);
                        startActivity(intent);
                        finish();
                    }
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

    }

}
