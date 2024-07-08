package com.example.smart_internship_assistant.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.example.smart_internship_assistant.R;

public class CodeInputDialog extends Dialog{
    private CodeInputView codeInputView;
    private String mail;
    private String code;
    private CodeInputView.OnTextInputListener listener;
    public CodeInputDialog(@NonNull Context context) {
        super(context);
    }
    public CodeInputDialog setFinish(CodeInputView.OnTextInputListener listener) {
        this.listener = listener;
        return this;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_code_input);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        codeInputView = findViewById(R.id.codeInputView);

        //自定义Dialog宽度
        WindowManager m = getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = getWindow().getAttributes();
        Point size = new Point();
        d.getSize(size);
        p.width = (int) ((size.x)*0.95);        //设置为屏幕的0.7倍宽度
        getWindow().setAttributes(p);
        codeInputView.setOnTextInputListener(listener);
    }
}
