package com.example.mafia;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.getSystemService;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_chat#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_chat extends Fragment implements TextWatcher {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String name;
    private WebSocket webSocket;
    private String SERVER_PATH = "ws://192.249.18.102:443";
    private EditText messageEdit;
    private View sendBtn, pickImgBtn;
    private RecyclerView recyclerView;
    private int IMAGE_REQUEST_ID = 1;
    private MessageAdapter messageAdapter;
    View v;

    private TextView word;
    private TextView leftTime;
    private TextView scoreView;
    private TextView otherScoreView;
    private String hidden_word = "";

    private CountDownTimer timer;
    private int score = 0;
    private int otherScore = 0;

    public fragment_chat() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_chat.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_chat newInstance(String param1, String param2) {
        fragment_chat fragment = new fragment_chat();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_chat, container, false);
        name = LoginActivity.user_nickname;
        initiateSocketConnection();

        word = v.findViewById(R.id.word);

        leftTime = v.findViewById(R.id.leftTime);
        scoreView = v.findViewById(R.id.score);
        otherScoreView = v.findViewById(R.id.otherScore);

        leftTime.setText("?????? ?????? : " + 14);
        scoreView.setText("?????? ?????? : " + 0);
        otherScoreView.setText("?????? ?????? : " + 0);

        timer = new CountDownTimer(15000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                leftTime.setText("?????? ?????? : " + (int) (millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
            }
        };


        return v;
    }

    private void initiateSocketConnection() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        webSocket = client.newWebSocket(request, new SocketListener());

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String string = s.toString().trim();

        if (string.isEmpty()) {
            resetMessageEdit();
        } else{

            sendBtn.setVisibility(View.VISIBLE);
            pickImgBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void resetMessageEdit() {

        messageEdit.removeTextChangedListener((TextWatcher) this);

        messageEdit.setText("");

        sendBtn.setVisibility(View.INVISIBLE);
        pickImgBtn.setVisibility(View.VISIBLE);

        messageEdit.addTextChangedListener((TextWatcher) this);
    }

    private class SocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response){
            super.onOpen(webSocket, response);

            ((RoomActivity) getContext()).runOnUiThread(() -> {
                Toast.makeText(getActivity(),
                        "Socket Connection Successful!",
                        Toast.LENGTH_SHORT).show();

                initializeView();
            });

        }

        @Override
        public void onMessage(WebSocket webSocket, String text){
            super.onMessage(webSocket, text);

            ((RoomActivity) getContext()).runOnUiThread(() -> {
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    if (jsonObject.has("finish")){
                        //Thread.sleep(2000);
                        if (otherScore == score) Toast.makeText(getContext(), "???????????????!!!", Toast.LENGTH_LONG).show();
                        else if (otherScore < score) Toast.makeText(getContext(), "???????????????!!!!!^0^", Toast.LENGTH_LONG).show();
                        else Toast.makeText(getContext(), "????????????....??????", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        Thread.sleep(3000);
                        startActivity(intent);
                    }
                    else if (jsonObject.has("otherScore")){
                        otherScore = jsonObject.getInt("otherScore");
                        otherScoreView.setText("?????? ?????? : " + otherScore);
                    }
                    else if (jsonObject.has("getPoint")){
                        Toast.makeText(getActivity(), "????????? ???????????????!", Toast.LENGTH_SHORT).show();
                        score += jsonObject.getInt("points");
                        scoreView.setText("?????? ?????? : " + score);
                        JSONObject jsonObject1 = new JSONObject();
                        jsonObject1.put("otherScore", score);
                        webSocket.send(jsonObject1.toString());
                    }
                    else if (!jsonObject.has("canvas") && !jsonObject.has("timer") && !jsonObject.has("clear")
                            && !(jsonObject.has("full")) && !(jsonObject.has("finish"))) {
                        if (jsonObject.has("quiz")) {
                            Log.d("The value of 'quiz'", jsonObject.getString("quiz"));
                            if (jsonObject.getString("quiz").equals("true")) {
                                word.setText(jsonObject.getString("word"));
                                Log.d("The value of 'word'", String.valueOf(word));
                            } else {
                                hidden_word = jsonObject.getString("word");
                                Log.d("The value of 'hidden_word'", hidden_word);
                            }
                        } else {
                            jsonObject.put("isSent", false);
                            Log.d("isSent", "ok");

                            messageAdapter.addItem(jsonObject);

                            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                        }
                    }
                    else if (jsonObject.has("timer")){
                        timer.start();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void initializeView() {

        messageEdit = v.findViewById(R.id.messageEdit);
        sendBtn = v.findViewById(R.id.sendBtn);
        pickImgBtn = v.findViewById(R.id.pickImgBtn);
        recyclerView = v.findViewById(R.id.recyclerView);

        messageAdapter = new MessageAdapter(getLayoutInflater());
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        messageEdit.addTextChangedListener((TextWatcher) this);

        sendBtn.setOnClickListener(v -> {

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", name);
                jsonObject.put("message", messageEdit.getText().toString());

                if (hidden_word.equals(messageEdit.getText().toString())){
                    Log.d("Sent hidden word",hidden_word);
                    jsonObject.put("rightans", "true");
                }

                webSocket.send(jsonObject.toString());

                jsonObject.put("isSent", true);
                messageAdapter.addItem(jsonObject);

                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);

                resetMessageEdit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        pickImgBtn.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            startActivityForResult(Intent.createChooser(intent, "Pick image"), IMAGE_REQUEST_ID);

        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_ID && resultCode == RESULT_OK) {
            try {
                InputStream is = getActivity().getContentResolver().openInputStream(data.getData());
                Bitmap image = BitmapFactory.decodeStream(is);

                sendImage(image);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

    }

    private void sendImage(Bitmap image) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

        String base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", name);
            jsonObject.put("image", base64String);

            webSocket.send(jsonObject.toString());

            jsonObject.put("isSent", true);

            messageAdapter.addItem(jsonObject);

            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() -  1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}