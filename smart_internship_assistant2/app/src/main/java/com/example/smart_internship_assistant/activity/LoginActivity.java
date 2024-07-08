package com.example.smart_internship_assistant.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_internship_assistant.Constants;
import com.example.smart_internship_assistant.R;
import com.example.smart_internship_assistant.User;
import com.example.smart_internship_assistant.utils.SharedPreferenceHelper;
import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText et_mail, et_password;
    private Button btn_login;
    private static final String TAG = "LoginActivity";
    private LottieAnimationView lav_smile;
    private SharedPreferenceHelper spHelper;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 1:
                    Log.d(TAG, "handleMessage: ");
                    Toast.makeText(LoginActivity.this, message.obj.toString(), Toast.LENGTH_SHORT).show();
                    et_password.setText("");
                    break;
                default:
                    break;
            }
            return false;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        InstallUtils.downloadApp(activity);

        initViews();
    }
    private void initData(){
        spHelper = new SharedPreferenceHelper(this.getApplicationContext());
        Map<String, String> data = spHelper.read();
        et_mail.setText(data.get("mail"));
        et_password.setText(data.get("password"));
    }
    private void initViews() {
        et_mail = findViewById(R.id.email_editText);
        et_password = findViewById(R.id.password_editText1);
        btn_login = findViewById(R.id.login_button);
        lav_smile = findViewById(R.id.lav_smile);
        lav_smile.playAnimation();
        initData();
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                String mail = "1398861921@qq.com";
//                String password = "123456";

                String mail = et_mail.getText().toString();
                String password = et_password.getText().toString();
                MediaType JSON = MediaType.get("application/json; charset=utf-8");
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("mail", mail);
                    jsonObject.put("password", password);
                }catch (Exception e){
                    e.printStackTrace();
                }
                // 将JSON对象转换为字符串
                String jsonString = jsonObject.toString();
                Log.d(TAG, "onClick: " + mail + " " + password);
                RequestBody body = RequestBody.create(jsonString, JSON);
                Request request = new Request.Builder()
                        .url("http://" + Constants.IP_ADDRESS + "/auth/" + "login")
                        .post(body)
                        .build();
                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "onFailure: ");
                    }
                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String body = response.body().string();
                            if (body == null) {
                                onFailure(call, new IOException("body is null"));
                                return;
                            }
//                        Gson gson = new Gson();
//                        Result<String> result = gson.fromJson(body.string(), new TypeToken<Result<String>>() {
//                        }.getType());
                            Log.d(TAG, "result: " + body);
                            JsonElement element = JsonParser.parseString(body);
                            JsonElement dataElement = element.getAsJsonObject().get("data");
                            int code = element.getAsJsonObject().get("code").getAsInt();
//                        Log.d(TAG, "token" + token);
                            if (code == 1) {
                                String token = dataElement.getAsJsonObject().get("token").getAsString();
//                                String uname = dataElement.getAsJsonObject().get("uname").getAsString();
//                            String head = dataElement.getAsJsonObject().get("head").getAsString();
//                                String id = dataElement.getAsJsonObject().get("id").getAsString();
//                                Log.d(TAG, "id: " + id);
//                                String signature;
//                                if (!dataElement.getAsJsonObject().get("signature").isJsonNull()){
//                                    signature = dataElement.getAsJsonObject().get("signature").getAsString();
//                                } else {
//                                    signature = null;
//                                }
                                User.getInstance().setToken(token);
                                User.getInstance().setEmail(mail);
                                User.getInstance().setPassword(password);
//                                User.getInstance().setUsername(uname);
//                                User.getInstance().setAvatar("1");
//                                User.getInstance().setId(id);

                                //再次登录
                                spHelper.save(mail,password);

                                Intent intent = new Intent(LoginActivity.this,SpeechActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                String msg = element.getAsJsonObject().get("msg").getAsString();
                                Message message = new Message();
                                message.what = 1;
                                message.obj = msg;
                                handler.sendMessage(message);
                            }
                        }

                    }
                });
            }
        });
    }
}