package com.example.mafia;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link fragment_draw#newInstance} factory method to
 * create an instance of this fragment.
 */
public class fragment_draw extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public fragment_draw() {
        // Required empty public constructor
    }

    private DrawCanvas drawCanvas;

    private FloatingActionButton fbPen;             //펜 모드 버튼
    private FloatingActionButton fbEraser;          //지우개 모드 버튼
    private FloatingActionButton fbClear;
    private ConstraintLayout canvasContainer;       //캔버스 root view

    private ArrayList<fragment_draw.Pen> drawCommandList;         //그리기 경로 기록
    private ArrayList<fragment_draw.Pen> received_List = new ArrayList<>();
    View v;

    private WebSocket webSocket;
    private String SERVER_PATH = "ws://192.249.18.102:443";
    //private CanvasAdapter canvasAdapter;
    static Canvas canvas_tmp;
    static Paint paint_tmp = new Paint(Paint.ANTI_ALIAS_FLAG);

    static Pen received_drawCL = null;
    static Pen received_prevP = null;
    static int cnt = 0;
    static int is_received = 0;
    static JSONObject received_json;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment fragment_draw.
     */
    // TODO: Rename and change types and number of parameters
    public static fragment_draw newInstance(String param1, String param2) {
        fragment_draw fragment = new fragment_draw();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        v = inflater.inflate(R.layout.fragment_draw, container, false);
        findId();
        canvasContainer.addView(drawCanvas);

        initiateSocketConnection();

        setOnClickListener();

        return v;
    }

    private void initiateSocketConnection() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(SERVER_PATH).build();
        Log.d("request", String.valueOf(request));
        webSocket = client.newWebSocket(request, new fragment_draw.SocketListener());
        Log.d("request...", String.valueOf(webSocket));

    }

    private void findId() {
        canvasContainer = v.findViewById(R.id.lo_canvas);
        fbPen = v.findViewById(R.id.fb_pen);
        fbEraser = v.findViewById(R.id.fb_eraser);
        fbClear = v.findViewById(R.id.fb_clear);
        drawCanvas = new DrawCanvas(this);
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
        public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
            super.onMessage(webSocket, text);
            ((RoomActivity) getContext()).runOnUiThread(() -> {
                received_json = null;
                try {
                    received_json = new JSONObject(text);
                    if (received_json.has("canvas")) {
                        received_List.add(new Pen(received_json.getDouble("x"), received_json.getDouble("y"),
                                received_json.getInt("moveStatus"), received_json.getInt("color"), received_json.getInt("size")));

                        is_received = 1;
                        drawCanvas.invalidate();
                    }

                    else if (received_json.has("timer")){
                        drawCommandList.clear();
                        received_List.clear();
                        drawCanvas.invalidate();
                    }

                    else if (received_json.has("clear")){
                        drawCommandList.clear();
                        received_List.clear();
                        drawCanvas.invalidate();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            });
        }

    }

    private void initializeView() {
    }

    private void setOnClickListener() {
        fbPen.setOnClickListener((v)->{
            drawCanvas.changeTool(fragment_draw.DrawCanvas.MODE_PEN);
        });

        fbEraser.setOnClickListener((v)->{
            drawCanvas.changeTool(fragment_draw.DrawCanvas.MODE_ERASER);
        });

        fbClear.setOnClickListener((v)->{
            drawCommandList.clear();
            received_List.clear();
            drawCanvas.invalidate();
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("clear", "1");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            webSocket.send(jsonObject.toString());
        });

    }

    class Pen {
        public static final int STATE_START = 0;        //펜의 상태(움직임 시작)
        public static final int STATE_MOVE = 1;         //펜의 상태(움직이는 중)
        double x, y;                                     //펜의 좌표
        int moveStatus;                                 //현재 움직임 여부
        int color;                                      //펜 색
        int size;                                       //펜 두께

        public Pen(double x, double y, int moveStatus, int color, int size) {
            this.x = x;
            this.y = y;
            this.moveStatus = moveStatus;
            this.color = color;
            this.size = size;
        }

        public boolean isMove() {
            return moveStatus == STATE_MOVE;
        }
    }


    class DrawCanvas extends View {
        public static final int MODE_PEN = 1;                     //모드 (펜)
        public static final int MODE_ERASER = 0;                  //모드 (지우개)
        final int PEN_SIZE = 3;                                   //펜 사이즈
        final int ERASER_SIZE = 30;                               //지우개 사이즈

        Paint paint;                                              //펜
        Bitmap loadDrawImage;                                     //호출된 이전 그림
        int color;                                                //현재 펜 색상
        int size;                                                 //현재 펜 크기

        public DrawCanvas(fragment_draw context) {
            super(v.getContext());
            init();
        }

        public DrawCanvas(Context context, @Nullable AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        public DrawCanvas(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            init();
        }


        private void init() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            drawCommandList = new ArrayList<>();
            loadDrawImage = null;
            color = Color.BLACK;
            size = PEN_SIZE;
        }

        public Bitmap getCurrentCanvas() {
            Bitmap bitmap = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            this.draw(canvas);
            return bitmap;
        }


        private void changeTool(int toolMode) {
            if (toolMode == MODE_PEN) {
                this.color = Color.BLACK;
                size = PEN_SIZE;
            } else {
                this.color = Color.WHITE;
                size = ERASER_SIZE;
            }
            paint.setColor(color);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Log.d("json Test", "onDraw");
            canvas_tmp = canvas;
            canvas.drawColor(Color.WHITE);
            if (loadDrawImage != null) {
                canvas.drawBitmap(loadDrawImage, 0, 0, null);
            }
            if (is_received == 1) {
                for (int i = 0; i < received_List.size(); i++) {
                    Log.d("drawCommandList", String.valueOf(i));
                    Log.d("drawCommandList", String.valueOf(received_List.get(i)));
                    fragment_draw.Pen p = received_List.get(i);
                    paint.setColor(p.color);
                    paint.setStrokeWidth(p.size);

                    if (p.isMove()) {
                        if (i - 1 >= 0) {
                            fragment_draw.Pen prevP = received_List.get(i - 1);
                            canvas.drawLine((float) prevP.x, (float) prevP.y, (float) p.x, (float) p.y, paint);
                        }
                    }
                }
                is_received = 0;
            } else {
                for (int i = 0; i < drawCommandList.size(); i++) {
                    Log.d("drawCommandList", String.valueOf(i));
                    Log.d("drawCommandList", String.valueOf(drawCommandList.get(i)));
                    fragment_draw.Pen p = drawCommandList.get(i);
                    paint.setColor(p.color);
                    paint.setStrokeWidth(p.size);


                    if (p.isMove()) {
                        if (i - 1 >= 0) {
                            fragment_draw.Pen prevP = drawCommandList.get(i - 1);
                            canvas.drawLine((float) prevP.x, (float) prevP.y, (float) p.x, (float) p.y, paint);
                        }
                    }
                }
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            cnt++;
            Log.d("cnt", String.valueOf(cnt));
            int action = e.getAction();
            int state = action == MotionEvent.ACTION_DOWN ? fragment_draw.Pen.STATE_START : fragment_draw.Pen.STATE_MOVE;
            drawCommandList.add(new fragment_draw.Pen(e.getX(), e.getY(), state, color, size));

            JSONObject jsonObject = new JSONObject();

            try {
                jsonObject.put("canvas", "1");
                jsonObject.put("x", e.getX());
                jsonObject.put("y", e.getY());
                jsonObject.put("moveStatus", state);
                jsonObject.put("color", color);
                jsonObject.put("size", size);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
            webSocket.send(jsonObject.toString());

            invalidate();
            return true;
        }
    }

    public static Bitmap StringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static String BitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 70, baos);
        byte[] bytes = baos.toByteArray();
        String temp = Base64.encodeToString(bytes, Base64.DEFAULT);
        return temp;
    }
}