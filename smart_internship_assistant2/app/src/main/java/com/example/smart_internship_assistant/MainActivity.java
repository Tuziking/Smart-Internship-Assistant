package com.example.smart_internship_assistant;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_internship_assistant.activity.LoginActivity;
import com.example.smart_internship_assistant.activity.RegisterActivity;
import com.airbnb.lottie.LottieAnimationView;
import com.example.smart_internship_assistant.constant.ChatConst;
import com.example.smart_internship_assistant.util.PermissionUtil;
import com.example.smart_internship_assistant.util.SocketUtil;
//import com.example.d_emo.emotion.Emotions;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Button btn_login;
    private TextView tv_register;
    private LottieAnimationView lav_house;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SocketUtil.checkSocketAvailable(this, ChatConst.CHAT_IP, ChatConst.CHAT_PORT);
        initViews();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void initViews() {
        btn_login = findViewById(R.id.login_button);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        tv_register = findViewById(R.id.register_textView);
        tv_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        lav_house = findViewById(R.id.lav_house);
        lav_house.playAnimation();
    }
}