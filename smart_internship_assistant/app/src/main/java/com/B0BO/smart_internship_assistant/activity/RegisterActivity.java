package com.B0BO.smart_internship_assistant.activity;

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

import com.B0BO.smart_internship_assistant.R;
import com.B0BO.smart_internship_assistant.Constants;
import com.B0BO.smart_internship_assistant.Result;
import com.B0BO.smart_internship_assistant.User;
import com.B0BO.smart_internship_assistant.view.CodeInputDialog;
import com.B0BO.smart_internship_assistant.view.CodeInputView;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class RegisterActivity extends AppCompatActivity {
    private EditText et_mail, et_password1, et_password2, et_username;
    private Button btn_register;
    private CodeInputDialog dialog;
    private static final String TAG = "RegisterActivity";
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case 1:
//                    Log.d(TAG, "handleMessage: ");
                    Toast.makeText(RegisterActivity.this, message.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(RegisterActivity.this, message.obj.toString(), Toast.LENGTH_SHORT).show();
                    et_password1.setText("");
                    et_password2.setText("");
                    break;
                case 3:
                    Toast.makeText(RegisterActivity.this, message.obj.toString(), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    break;
                case 4:
                    Toast.makeText(RegisterActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    break;
                case 5:
                    Toast.makeText(RegisterActivity.this, "验证码已发送", Toast.LENGTH_SHORT).show();
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
        setContentView(R.layout.activity_register);
        initViews();

    }
    //设置沉浸式
    private void initViews() {
        et_mail = findViewById(R.id.email_editText);
        et_password1 = findViewById(R.id.password_editText1);
        et_password2 = findViewById(R.id.password_editText2);
        et_username = findViewById(R.id.username_editText);
        btn_register = findViewById(R.id.register_button);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });
    }

    private void register() {
        String mail = et_mail.getText().toString();
        String password1 = et_password1.getText().toString();
        String password2 = et_password2.getText().toString();
        String username = et_username.getText().toString();
        if (mail.equals("") || password1.equals("") || password2.equals("") || username.equals("")) {
            Message message = new Message();
            message.what = 1;
            message.obj = "请将个人信息填充完整";
            handler.sendMessage(message);
            return;
        } else if (!password1.equals(password2)) {
            Message message = new Message();
            message.what = 2;
            message.obj = "两次密码不一致";
            handler.sendMessage(message);
            return;
        } else {
            requestCode(mail, password1, password2, username);
            dialog = new CodeInputDialog(RegisterActivity.this);
            dialog.setFinish(new CodeInputView.OnTextInputListener() {
                @Override
                public void onTextInputChanged(String text, int length) {

                }
                @Override
                public void onTextInputCompleted(String text) {
                    sendRequest(et_mail.getText().toString(),
                            et_password1.getText().toString(),
                            et_password2.getText().toString(),
                            et_username.getText().toString(),
                            text);
                }
            }).show();
            return;
        }
    }

    private void requestCode(String mail, String password, String confirm, String username) {
        FormBody body = new FormBody.Builder()
                .add("mail", mail)
                .build();
        Request request = new Request.Builder()
                .url("http://" + Constants.IP_ADDRESS + "/" + "code")
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
                    Log.d(TAG, "body: " + body);
                    if (body == null) {
                        onFailure(call, new IOException("body is null"));
                        return;
                    }
                    JsonElement element = JsonParser.parseString(body);
                    JsonElement dataElement = element.getAsJsonObject().get("data");
                    int code = element.getAsJsonObject().get("code").getAsInt();
                    if (code == 1) {
                        Message message = new Message();
                        message.what = 5;
                        handler.sendMessage(message);
//                    sendRequest(mail, password, confirm, username);
                    } else {

                    }
                }

            }
        });
    }

    private void sendRequest(String mail, String password, String confirm, String username, String code) {
        FormBody body = new FormBody.Builder()
                .add("mail", mail)
                .add("name", username)
                .add("password", password)
                .add("confirm", confirm)
                .add("code", code)
                .build();
        Request request = new Request.Builder()
                .url("http://" + Constants.IP_ADDRESS + "/" + "register")
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
                if (response.isSuccessful()){
                    ResponseBody body = response.body();
                    if (body == null) {
                        onFailure(call, new IOException("body is null"));
                        return;
                    }
                    Gson gson = new Gson();
                    Result<String> result = gson.fromJson(body.string(), new TypeToken<Result<String>>() {
                    }.getType());
                    Log.d(TAG, "result: " + result);
                    if (result.code == 1) {
                        User.getInstance().setToken(result.data);
                        User.getInstance().setUsername(username);
                        User.getInstance().setEmail(mail);
                        User.getInstance().setPassword(password);
                        Message message = new Message();
                        message.what = 4;
                        message.obj = result.msg;
                        handler.sendMessage(message);
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                    } else {
                        Message message = new Message();
                        message.what = 3;
                        message.obj = result.msg;
                        handler.sendMessage(message);
                    }
                }
            }
        });
    }
}