package com.B0BO.smart_internship_assistant.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

public class SharedPreferenceHelper {
    private static final String TAG = "SharedPreferenceHelper";
    private Context mContext;
    public SharedPreferenceHelper(Context context) {
        this.mContext = context;
    }
    //定义一个保存数据的方法
    public void save(String mail, String password) {
        SharedPreferences sp = mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("mail", mail);
        editor.putString("password", password);
        editor.commit();
//        Toast.makeText(mContext, "信息已写入SharedPreference["+SP_TAG+"]中", Toast.LENGTH_SHORT).show();
    }
    public Map<String, String> read() {
        SharedPreferences sp = mContext.getSharedPreferences(TAG, Context.MODE_PRIVATE);
        Map<String, String> map = new HashMap<String, String>();
        map.put("mail", sp.getString("mail", ""));
        map.put("password", sp.getString("password", ""));
        return map;
    }
}
