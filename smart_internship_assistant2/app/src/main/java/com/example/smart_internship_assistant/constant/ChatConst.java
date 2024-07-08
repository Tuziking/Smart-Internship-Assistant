package com.example.smart_internship_assistant.constant;
import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;

public class ChatConst {
    public final static String CHAT_IP = "192.168.43.20"; // 聊天服务的ip
    public final static int CHAT_PORT = 9999; // 聊天服务的端口

    private final static String STUN_URL = "stun:101.200.162.162";
    private final static String STUN_USERNAME = "wck";
    private final static String STUN_PASSWORD = "123456";

    // 获取ICE服务器列表
    public static List<PeerConnection.IceServer> getIceServerList() {
        List<PeerConnection.IceServer> iceServerList = new ArrayList<>();
        iceServerList.add(PeerConnection.IceServer.builder(STUN_URL)
                .setUsername(STUN_USERNAME).setPassword(STUN_PASSWORD).createIceServer());
        return iceServerList;
    }
}