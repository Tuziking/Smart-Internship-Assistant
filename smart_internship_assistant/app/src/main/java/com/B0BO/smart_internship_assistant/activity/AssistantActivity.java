package com.B0BO.smart_internship_assistant.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Bundle;

import com.B0BO.smart_internship_assistant.R;
import com.baidu.mapapi.map.MapView;

import java.util.ArrayList;
import java.util.List;

public class AssistantActivity extends BaseActivity {
    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assistant);
        getPermissionResults();
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(this, savedInstanceState);
    }

    private void getPermissionResults() {
        List<String> permissionNameList = new ArrayList<>();
        permissionNameList.add("android.permission.POST_NOTIFICATIONS");
        permissionNameList.add("android.permission.USE_FULL_SCREEN_INTENT");
        permissionNameList.add(android.Manifest.permission.CAMERA);
        permissionNameList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        permissionNameList.add(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        permissionNameList.add(Manifest.permission.RECORD_AUDIO);
        permissionNameList.add(Manifest.permission.READ_MEDIA_AUDIO);
        permissionNameList.add(Manifest.permission.READ_MEDIA_IMAGES);
        permissionNameList.add(Manifest.permission.READ_MEDIA_VIDEO);

        requestPermission(permissionNameList);
    }

    private void initView() {

    }
}