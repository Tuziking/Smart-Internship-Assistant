package com.example.smart_internship_assistant.activity;

import static com.tencent.cloud.libqcloudtts.engine.offlineModule.auth.AuthErrorCode.OFFLINE_AUTH_SUCCESS;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.smart_internship_assistant.MainApplication;
import com.example.smart_internship_assistant.R;
import com.example.smart_internship_assistant.User;
import com.example.smart_internship_assistant.bean.ContactInfo;
import com.example.smart_internship_assistant.config.DemoConfig;
import com.example.smart_internship_assistant.constant.ChatConst;
import com.example.smart_internship_assistant.util.SocketUtil;
import com.example.smart_internship_assistant.utils.DemoAudioRecordDataSource;
import com.example.smart_internship_assistant.utils.MediaPlayerDemo;
import com.example.smart_internship_assistant.webrtc.Peer;
import com.example.smart_internship_assistant.webrtc.ProxyVideoSink;
import com.tencent.aai.AAIClient;
import com.tencent.aai.audio.utils.WavCache;
import com.tencent.aai.auth.LocalCredentialProvider;
import com.tencent.aai.config.ClientConfiguration;
import com.tencent.aai.exception.ClientException;
import com.tencent.aai.exception.ServerException;
import com.tencent.aai.listener.AudioRecognizeResultListener;
import com.tencent.aai.listener.AudioRecognizeStateListener;
import com.tencent.aai.log.AAILogger;
import com.tencent.aai.log.LoggerListener;
import com.tencent.aai.model.AudioRecognizeConfiguration;
import com.tencent.aai.model.AudioRecognizeRequest;
import com.tencent.aai.model.AudioRecognizeResult;
import com.tencent.cloud.libqcloudtts.MediaPlayer.QCloudMediaPlayer;
import com.tencent.cloud.libqcloudtts.MediaPlayer.QCloudPlayerCallback;
import com.tencent.cloud.libqcloudtts.MediaPlayer.QPlayerError;
import com.tencent.cloud.libqcloudtts.TtsController;
import com.tencent.cloud.libqcloudtts.TtsError;
import com.tencent.cloud.libqcloudtts.TtsMode;
import com.tencent.cloud.libqcloudtts.TtsResultListener;
import com.tencent.cloud.libqcloudtts.engine.offlineModule.auth.QCloudOfflineAuthInfo;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.audio.AudioDeviceModule;
import org.webrtc.audio.JavaAudioDeviceModule;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import io.socket.client.Socket;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class SpeechActivity extends AppCompatActivity {
    private Handler reconnectHandler = new Handler(Looper.getMainLooper());
    private Runnable reconnectRunnable = this::initWebSocket;
    //TTS
    private volatile TtsController mTtsController;
    private MediaPlayerDemo mediaPlayer;
    TtsMode mTtsmode = TtsMode.ONLINE;
    private static final int RECONNECT_INTERVAL = 5000;  // 重连间隔时间（毫秒）

    private static final String host = "8.140.250.103:8081";
    private WebSocket webSocket;
    //在线参数
    float mVoiceSpeed = 0f;
    float mVoiceVolume = 0f;
    int mVoiceType = 1001;
    int mPrimaryLanguage = 1; //主语言类型：1-中文（默认）2-英文
    //时间配置
    int mConnectTimeout = 15 *1000; //连接超时默认15000ms(15s) 范围[500,30000] 单位ms ， Mix模式下建议调小此值，以获得更好的体验
    int mReadTimeout = 30 *1000; //读取超时超时默认30000ms(30s) 范围[2200,60000] 单位ms， Mix模式下建议调小此值，以获得更好的体验
    int mCheckNetworkIntervalTime = 5 * 60;
    boolean isPlay = true;
    Button start;
    Button cancel;

    TextView recognizeState;
    TextView volume;

    TextView voiceDb;

    TextView recognizeResult;
    ScrollView mScrollView;
    Switch mIsCompressSW;
//    Switch mEnableAEC;
//    Switch mEnablePlayer;

    Switch mSilentSwitch;
    //    EditText mFileName;
    FileOutputStream mStream;

    boolean isRecording = false;

    boolean isCompress = true;//音频压缩，默认true

    boolean isOpenSilentCheck = false;

    Handler handler;

    private String  TAG = SpeechActivity.class.getName();

    final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    AAIClient aaiClient;


    boolean isSaveAudioRecordFiles = false;

    MediaPlayer mp = null;

    //推流部分
    private Socket mSocket; // 声明一个套接字对象
    private SurfaceViewRenderer svr_local; // 本地的表面视图渲染器（我方）
    private PeerConnectionFactory mConnFactory; // 点对点连接工厂
    private EglBase mEglBase; // OpenGL ES 与本地设备之间的接口对象
    private MediaStream mMediaStream; // 媒体流
    private VideoCapturer mVideoCapturer; // 视频捕捉器
    private MediaConstraints mOfferConstraints; // 提供方的媒体条件
    private MediaConstraints mAudioConstraints; // 音频的媒体条件
    private List<PeerConnection.IceServer> mIceServers = ChatConst.getIceServerList(); // ICE服务器列表
    private Peer mPeer; // 点对点对象
    private ContactInfo mContact = new ContactInfo("提供方", "接收方");

    private void checkPermissions() {

        List<String> permissions = new LinkedList<>();
        addPermission(permissions, Manifest.permission.RECORD_AUDIO);
        addPermission(permissions, Manifest.permission.INTERNET);
        addPermission(permissions, Manifest.permission.CAMERA);

        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[permissions.size()]),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        initRender(); // 初始化渲染图层
        initStream(); // 初始化音视频的媒体流
        initSocket(); // 初始化信令交互的套接字
        initView(); // 初始化视图界面
    }
    private void initView() {
//        TextView tv_title = findViewById(R.id.tv_title);
//        tv_title.setText("这里是视频提供方");
//        findViewById(R.id.iv_back).setOnClickListener(v -> dialOff()); // 挂断通话
    }
    private void initRender() {
        svr_local = findViewById(R.id.svr_local);
        mEglBase = EglBase.create(); // 创建EglBase实例
        // 以下初始化我方的渲染图层
        svr_local.init(mEglBase.getEglBaseContext(), null);
        svr_local.setMirror(true); // 是否设置镜像
        svr_local.setZOrderMediaOverlay(true); // 是否置于顶层
        // 设置缩放类型，SCALE_ASPECT_FILL表示充满视图
        svr_local.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        svr_local.setEnableHardwareScaler(false); // 是否开启硬件缩放
    }
    private void initConstraints() {
        // 创建发起方的媒体条件
        mOfferConstraints = new MediaConstraints();
        // 是否接受音频流
        mOfferConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        // 是否接受视频流
        mOfferConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        // 创建音频流的媒体条件
        mAudioConstraints = new MediaConstraints();
        // 是否消除回声
        mAudioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        // 是否自动增益
        mAudioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
        // 是否过滤高音
        mAudioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));
        // 是否抑制噪音
        mAudioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression", "true"));
    }
    private void initStream() {
        Log.d(TAG, "initStream");
        // 初始化点对点连接工厂
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(getApplicationContext())
                        .createInitializationOptions());
        // 创建视频的编解码方式
        VideoEncoderFactory encoderFactory;
        VideoDecoderFactory decoderFactory;
        encoderFactory = new DefaultVideoEncoderFactory(
                mEglBase.getEglBaseContext(), true, true);
        decoderFactory = new DefaultVideoDecoderFactory(mEglBase.getEglBaseContext());
        AudioDeviceModule audioModule = JavaAudioDeviceModule.builder(this).createAudioDeviceModule();
        // 创建点对点连接工厂
        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        mConnFactory = PeerConnectionFactory.builder()
                .setOptions(options)
                .setAudioDeviceModule(audioModule)
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory)
                .createPeerConnectionFactory();
        initConstraints(); // 初始化视频通话的各项条件
        // 创建音视频的媒体流
        mMediaStream = mConnFactory.createLocalMediaStream("local_stream");
        // 以下创建并添加音频轨道
//        AudioSource audioSource = mConnFactory.createAudioSource(mAudioConstraints);
//        AudioTrack audioTrack = mConnFactory.createAudioTrack("audio_track", audioSource);
//        mMediaStream.addTrack(audioTrack);
        // 以下创建并初始化视频捕捉器
        mVideoCapturer = createVideoCapture();
        VideoSource videoSource = mConnFactory.createVideoSource(mVideoCapturer.isScreencast());
        SurfaceTextureHelper surfaceHelper = SurfaceTextureHelper.create("CaptureThread", mEglBase.getEglBaseContext());
        mVideoCapturer.initialize(surfaceHelper, this, videoSource.getCapturerObserver());
        // 设置视频画质。三个参数分别表示：视频宽度、视频高度、每秒传输帧数fps
        mVideoCapturer.startCapture(500, 500, 30);
        // 以下创建并添加视频轨道
        VideoTrack videoTrack = mConnFactory.createVideoTrack("video_track", videoSource);
        mMediaStream.addTrack(videoTrack);
        ProxyVideoSink localSink = new ProxyVideoSink();
        localSink.setTarget(svr_local); // 指定视频轨道中我方的渲染图层
        mMediaStream.videoTracks.get(0).addSink(localSink);
    }

    // 创建视频捕捉器
    private VideoCapturer createVideoCapture() {
        VideoCapturer videoCapturer;
        if (Camera2Enumerator.isSupported(this)) { // 优先使用二代相机
            videoCapturer = createCameraCapture(new Camera2Enumerator(this));
        } else { // 如果不支持二代相机，就使用传统相机
            videoCapturer = createCameraCapture(new Camera1Enumerator(true));
        }
        return videoCapturer;
    }

    private VideoCapturer createCameraCapture(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // 先使用前置摄像头
        for (String deviceName : deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        // 没有后置摄像头再找 前置摄像头
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }
        return null;
    }

    private void initSocket() {
        mSocket = MainApplication.getInstance().getSocket();
        mSocket.connect(); // 建立Socket连接
        // 等待接入ICE候选者，目的是打通流媒体传输网络
        mSocket.on("IceInfo", args -> {
            Log.d(TAG, "IceInfo");
            try {
                JSONObject json = (JSONObject) args[0];
                IceCandidate candidate = new IceCandidate(json.getString("id"),
                        json.getInt("label"), json.getString("candidate")
                );
                mPeer.getConnection().addIceCandidate(candidate); // 添加ICE候选者
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        // 等待对方的会话连接，以便建立双方的通信链路
        mSocket.on("SdpInfo", args -> {
            Log.d(TAG, "SdpInfo");
            try {
                JSONObject json = (JSONObject) args[0];
                SessionDescription sd = new SessionDescription
                        (SessionDescription.Type.fromCanonicalForm(
                                json.getString("type")), json.getString("description"));
                mPeer.getConnection().setRemoteDescription(mPeer, sd); // 设置对方的会话描述
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        mSocket.on("other_hang_up", (args) -> dialOff()); // 等待对方挂断通话
        Log.d(TAG, "self_dial_in");
        SocketUtil.emit(mSocket, "self_dial_in", mContact); // 我方发起了视频通话
        // 等待对方接受视频通话
        mSocket.on("other_dial_in", (args) -> {
            String other_name = (String) args[0];
            Log.d(TAG, mContact.from+" to "+mContact.to+", other_name="+other_name);
            // 第四个参数表示对方接受视频通话之后，如何显示对方的视频画面
            mPeer = new Peer(mSocket, mContact.from, mContact.to, (userId, remoteStream) -> Log.d(TAG, "new Peer"));
            mPeer.init(mConnFactory, mMediaStream, mIceServers); // 初始化点对点连接
            mPeer.getConnection().createOffer(mPeer, mOfferConstraints); // 创建供应
        });
    }
    // 挂断通话
    private void dialOff() {
        mSocket.off("other_hang_up"); // 取消监听对方的挂断请求
        SocketUtil.emit(mSocket, "self_hang_up", mContact); // 发出挂断通话消息
        finish(); // 关闭当前页面
    }

    private void addPermission(List<String> permissionList, String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(permission);
        }
    }

    LinkedHashMap<String, String> resMap = new LinkedHashMap<>();
    private String buildMessage(Map<String, String> msg) {

        StringBuffer stringBuffer = new StringBuffer();
        Iterator<Map.Entry<String, String>> iter = msg.entrySet().iterator();
        while (iter.hasNext()) {
            String value = iter.next().getValue();
            stringBuffer.append(value+"\r\n");
        }
        return stringBuffer.toString();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        // 初始化相应的控件
        start = findViewById(R.id.start);
        cancel = findViewById(R.id.cancel);
        recognizeState = findViewById(R.id.recognize_state);
        volume = findViewById(R.id.volume);
        voiceDb = findViewById(R.id.voice_db);
        mSilentSwitch = findViewById(R.id.silent_switch);
//        mScrollView = findViewById(R.id.scroll_view);
        mIsCompressSW = findViewById(R.id.switch1);
//        mEnableAEC = findViewById(R.id.aec_enable);
//        mEnablePlayer = findViewById(R.id.play_sound);
//        mFileName = findViewById(R.id.file_name);
//        recognizeResult = findViewById(R.id.recognize_result);
        handler = new Handler(getMainLooper());
        initWebSocket();
        initTTS();
//        mTtsController.synthesize("傻逼钱波傻逼钱波傻逼钱波傻逼钱波傻逼钱波傻逼钱波傻逼钱波");
//日志配置 默认打开，您可以关闭
//        AAILogger.disableDebug();
//        AAILogger.disableError();
//        AAILogger.disableInfo();
//        AAILogger.disableWarn();

        AAILogger.setLoggerListener(new LoggerListener() {
            @Override
            public void onLogInfo(String s) {
//                Log.d(TAG, "onLogInfo: "+ s);
            }
        });



        // 检查sdk运行的必要条件权限
        checkPermissions();

        // 用户配置：需要在控制台申请相关的账号;
        final int appid;
        if (!TextUtils.isEmpty(DemoConfig.apppId)) {
            appid = Integer.valueOf(DemoConfig.apppId);
        } else {
            appid = 0;
        }
        //设置ProjectId 不设置默认使用0，说明：项目功能用于按项目管理云资源，可以对云资源进行分项目管理，详情见 https://console.cloud.tencent.com/project
        final int projectId = 0;
        final String secretId = DemoConfig.secretId;
        final String secretKey = DemoConfig.secretKey;

        // okhttp全局配置
        ClientConfiguration.setAudioRecognizeConnectTimeout(3000);
        ClientConfiguration.setAudioRecognizeWriteTimeout(5000);

        // 识别结果回调监听器
        final AudioRecognizeResultListener audioRecognizeResultlistener = new AudioRecognizeResultListener() {
            /**
             * 返回分片的识别结果
             * @param request 相应的请求
             * @param result 识别结果
             * @param seq 该分片所在句子的序号 (0, 1, 2...)
             *   此为中间态结果，会被持续修正
             */
            @Override
            public void onSliceSuccess(AudioRecognizeRequest request, AudioRecognizeResult result, int seq) {
                AAILogger.info(TAG, "分片on slice success..");
                AAILogger.info(TAG, "分片slice seq =" + seq + "voiceid =" + result.getVoiceId() + "result = " + result.getText() + "startTime =" + result.getStartTime() + "endTime = " + result.getEndTime());
                AAILogger.info(TAG, "分片on slice success..   ResultJson =" + result.getResultJson());//后端返回的未解析的json文本，您可以自行解析获取更多信息

                resMap.put(String.valueOf(seq), result.getText());
                final String msg = buildMessage(resMap);
                AAILogger.info(TAG, "分片slice msg="+msg);
                Log.d(TAG, "onSliceSuccess: "+msg);

            }

            /**
             * 返回语音流的识别结果
             * @param request 相应的请求
             * @param result 识别结果
             * @param seq 该句子的序号 (1, 2, 3...)
             *     此为稳定态结果，可做为识别结果用与业务
             */
            @Override
            public void onSegmentSuccess(AudioRecognizeRequest request, AudioRecognizeResult result, int seq) {
                AAILogger.info(TAG, "语音流on segment success");
                AAILogger.info(TAG, "语音流segment seq =" + seq + "voiceid =" + result.getVoiceId() + "result = " + result.getText() + "startTime =" + result.getStartTime() + "endTime = " + result.getEndTime());
                AAILogger.info(TAG, "语音流on segment success..   ResultJson =" + result.getResultJson());//后端返回的未解析的json文本，您可以自行解析获取更多信息
                resMap.put(String.valueOf(seq), result.getText());
                webSocket.send(result.getText());
                final String msg = buildMessage(resMap);
                AAILogger.info(TAG, "语音流segment msg="+msg);
                Log.d(TAG, "onSegmentSuccess: "+msg);


            }

            /**
             * 识别结束回调，返回所有的识别结果
             * @param request 相应的请求
             * @param result 识别结果,sdk内会把所有的onSegmentSuccess结果合并返回，如果业务不需要，可以只使用onSegmentSuccess返回的结果
             *    注意：仅收到onStopRecord回调时，表明本次语音流录音任务已经停止，但识别任务还未停止，需要等待后端返回最终识别结果，
             *               如果此时立即启动下一次录音，结果上一次结果仍会返回，可以调用cancelAudioRecognize取消上一次识别任务
             *         当收到 onSuccess 或者  onFailure时，表明本次语音流识别完毕，可以进行下一次识别；
             */
            @Override
            public void onSuccess(AudioRecognizeRequest request, String result) {
                handler.post(() -> {
                    start.setEnabled(true);
                });
                AAILogger.info(TAG, "识别结束, onSuccess..");
                AAILogger.info(TAG, "识别结束, result = " + result);
            }

            /**
             * 识别失败
             * @param request 相应的请求
             * @param clientException 客户端异常
             * @param serverException 服务端异常
             * @param response   服务端返回的json字符串（如果有）
             *    注意：仅收到onStopRecord回调时，表明本次语音流录音任务已经停止，但识别任务还未停止，需要等待后端返回最终识别结果，
             *               如果此时立即启动下一次录音，结果上一次结果仍会返回，可以调用cancelAudioRecognize取消上一次识别任务
             *         当收到 onSuccess 或者  onFailure时，表明本次语音流识别完毕，可以进行下一次识别；
             */
            @Override
            public void onFailure(AudioRecognizeRequest request, final ClientException clientException, final ServerException serverException, String response) {
                if(response != null){
                    AAILogger.info(TAG, "onFailure response.. :"+response);
                }
                if (clientException!=null) {
                    AAILogger.info(TAG, "onFailure..:"+clientException);
                }
                if (serverException!=null) {
                    AAILogger.info(TAG, "onFailure..:"+serverException);
                }
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        start.setEnabled(true);
                        if (clientException!=null) {
                            recognizeState.setText("识别状态：失败,  "+clientException);

                            AAILogger.info(TAG, "识别状态：失败,  "+clientException);
                        } else if (serverException!=null) {
                            recognizeState.setText("识别状态：失败,  "+serverException);

                        }
                    }
                });
            }
        };


        /**
         * 识别状态监听器
         */
        final AudioRecognizeStateListener audioRecognizeStateListener = new AudioRecognizeStateListener() {
            DataOutputStream dataOutputStream;
            String fileName = null;
            String filePath = null;
            ExecutorService mExecutorService;

            float minVoiceDb = Float.MAX_VALUE;
            float maxVoiceDb = Float.MIN_VALUE;
            /**
             * 开始录音
             * @param request
             */
            @Override
            public void onStartRecord(AudioRecognizeRequest request) {

                isRecording = true;
                minVoiceDb = Float.MAX_VALUE;
                maxVoiceDb = Float.MIN_VALUE;
                AAILogger.info(TAG, "onStartRecord..");
                handler.post(() -> {
                    recognizeState.setText(getString(R.string.start_record));
                    start.setEnabled(true);
                    start.setText("STOP");
                });
//                //为本次录音创建缓存一个文件
//                if(isSaveAudioRecordFiles) {
//                    if(mExecutorService == null){
//                        mExecutorService = Executors.newSingleThreadExecutor();
//                    }
//                    filePath = getFilesDir() + "/tencent_audio_sdk_cache";
//                    fileName = mFileName.getText().toString() + ".pcm";
//                    if(fileName.isEmpty()) {
//                        fileName = System.currentTimeMillis() + ".pcm";
//                    }
//                    dataOutputStream = WavCache.creatPmcFileByPath(filePath, fileName);
//                }
            }

            /**
             * 结束录音
             * @param request
             */
            @Override
            public void onStopRecord(AudioRecognizeRequest request) {
                AAILogger.info(TAG, "onStopRecord..");
                isRecording = false;
                handler.post(() -> {
                    recognizeState.setText(getString(R.string.end_record));
//                    start.setEnabled(true);
                    start.setText("START");
                });

                //如果设置了保存音频
                if(isSaveAudioRecordFiles) {
                    mExecutorService.execute(() -> {
                        WavCache.closeDataOutputStream(dataOutputStream);
                        WavCache.makePCMFileToWAVFile(filePath, fileName); //sdk内提供了一套pcm转wav工具类
                    });
                }
            }

            /**
             * 返回音频流，
             * 用于返回宿主层做录音缓存业务。
             * 由于方法跑在sdk线程上，这里多用于文件操作，宿主需要新开一条线程专门用于实现业务逻辑
             * @param audioDatas
             */
            @Override
            public void onNextAudioData(final short[] audioDatas, final int readBufferLength) {
                if(isSaveAudioRecordFiles) {
                    mExecutorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            WavCache.savePcmData(dataOutputStream, audioDatas, readBufferLength);
                        }
                    });
                }
            }

            /**
             * 静音检测回调
             * 当设置AudioRecognizeConfiguration  setSilentDetectTimeOut为true时，如触发静音超时，将触发此回调
             * 当setSilentDetectTimeOutAutoStop 为true时，触发此回调的同时会停止本次识别，相当于手动调用了 aaiClient.stopAudioRecognize()
             */
            @Override
            public void onSilentDetectTimeOut() {
                Log.d(TAG, "onSilentDetectTimeOut: ");
                //您的业务逻辑
            }

            /**
             * 音量变化时回调。该方法已废弃
             *
             * 建议使用 {@link #onVoiceDb(float db)}
             *
             * @deprecated 建议使用 {@link #onVoiceDb(float db)}.
             */
            @Override
            public void onVoiceVolume(AudioRecognizeRequest request, final int volume) {
                AAILogger.info(TAG, "onVoiceVolume..");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        SpeechActivity.this.volume.setText(getString(R.string.volume)+volume);
                    }
                });
            }

            @Override
            public void onVoiceDb(float volumeDb) {
                AAILogger.info(TAG, "onVoiceDb: " + volumeDb);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (volumeDb > maxVoiceDb) {
                            maxVoiceDb = volumeDb;
                        }
                        if (volumeDb < minVoiceDb) {
                            minVoiceDb = volumeDb;
                        }
                        if (minVoiceDb != Float.MAX_VALUE && maxVoiceDb != Float.MIN_VALUE) {
                            SpeechActivity.this.voiceDb.setText(getString(R.string.voice_db) + volumeDb
                                    + "(" + minVoiceDb + " ~ " + maxVoiceDb + ")");
                        }
                    }
                });
            }
        };

        if (aaiClient==null) {
            /**直接鉴权**/
            aaiClient = new AAIClient(SpeechActivity.this, appid, projectId, secretId ,new LocalCredentialProvider(secretKey));
            /**使用临时密钥鉴权
             * * 1.通过sts 获取到临时证书 （secretId secretKey  token） ,此步骤应在您的服务器端实现，见https://cloud.tencent.com/document/product/598/33416
             *   2.通过临时密钥调用接口
             * **/
//            aaiClient = new AAIClient(SpeechActivity.this, appid, projectId,"临时secretId", "临时secretKey","对应的token");

        }



//启动&停止 识别按钮
        start.setOnClickListener(v -> {
            AAILogger.info(TAG, "the start button has clicked..");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    start.setEnabled(false);
                }
            });

            if (isRecording){
                AAILogger.info(TAG, "stop button is clicked..");
                new Thread(() -> {
                    if (aaiClient!=null) {
                        aaiClient.stopAudioRecognize();
                    }
                    if (mp != null) {
                        mp.stop();
                        mp = null;
                    }
                }).start();
            } else {
                resMap.clear();
                if (aaiClient != null) { //丢弃上一次结果
                    boolean taskExist = aaiClient.cancelAudioRecognize();
                    AAILogger.info(TAG, "taskExist=" + taskExist);
                }

                isSaveAudioRecordFiles = false;
//                if (!mFileName.getText().toString().isEmpty()) {
//                    isSaveAudioRecordFiles = true;
//                }
                DemoAudioRecordDataSource dataSource = new DemoAudioRecordDataSource(isSaveAudioRecordFiles, this);
//                if (mEnablePlayer.isChecked()) {
//                    mp = new MediaPlayer();
//                    AssetFileDescriptor fd = null;
//                    try {
//                        fd = getAssets().openFd("test2.mp3");
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            mp.setDataSource(fd);
//                        } else {
//                            mp.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
//                        }
//                        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
//                        mp.prepare();
//                        mp.start();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                dataSource.enableAEC(mEnableAEC.isChecked());

                AudioRecognizeRequest.Builder builder = new AudioRecognizeRequest.Builder();
                // 初始化识别请求
                final AudioRecognizeRequest audioRecognizeRequest = builder

                        //设置数据源，数据源要求实现PcmAudioDataSource接口，您可以自己实现此接口来定制您的自定义数据源，例如从第三方推流中获取音频数据
                        //注意：音频数据必须为16k采样率的PCM音频，否则将导致语音识别输出错误的识别结果！！！！
                        .pcmAudioDataSource(dataSource) //使用Demo提供的录音器源码作为数据源，源码与SDK内置录音器一致，您可以参考此源代码自由定制修改，详情查阅DemoAudioRecordDataSource.java内注释
//                        .pcmAudioDataSource(new AudioRecordDataSource(isSaveAudioRecordFiles)) // 使用SDK内置录音器作为数据源
                        .setEngineModelType("16k_zh") // 设置引擎(16k_zh--通用引擎，支持中文普通话+英文),更多引擎请关注官网文档https://cloud.tencent.com/document/product/1093/48982 ，引擎种类持续增加中
                        .setFilterDirty(0)  // 0 ：默认状态 不过滤脏话 1：过滤脏话
                        .setFilterModal(0) // 0 ：默认状态 不过滤语气词  1：过滤部分语气词 2:严格过滤
                        .setFilterPunc(0) // 0 ：默认状态 不过滤句末的句号 1：滤句末的句号
                        .setConvert_num_mode(1) //1：默认状态 根据场景智能转换为阿拉伯数字；0：全部转为中文数字。
//                        .setVadSilenceTime(1000) // 语音断句检测阈值，静音时长超过该阈值会被认为断句（多用在智能客服场景，需配合 needvad = 1 使用） 默认不传递该参数，不建议更改
                        .setNeedvad(1) //0：关闭 vad，1：默认状态 开启 vad。语音时长超过一分钟需要开启,如果对实时性要求较高,并且时间较短
                        // 的输入,建议关闭,可以显著降低onSliceSuccess结果返回的时延以及stop后onSegmentSuccess和onSuccess返回的时延
//                        .setHotWordId("")//热词 id。用于调用对应的热词表，如果在调用语音识别服务时，不进行单独的热词 id 设置，自动生效默认热词；如果进行了单独的热词 id 设置，那么将生效单独设置的热词 id。
                        .setWordInfo(1)
//                        .setCustomizationId("")//自学习模型 id。如果设置了该参数，那么将生效对应的自学习模型,如果您不了解此参数，请不要设置
//                        .setReinforceHotword(1)
//                        .setNoiseThreshold(0)
//                        .setMaxSpeakTime(5000) // 强制断句功能，取值范围 5000-90000(单位:毫秒），默认值0(不开启)。 在连续说话不间断情况下，该参数将实现强制断句（此时结果变成稳态，slice_type=2）。如：游戏解说场景，解说员持续不间断解说，无法断句的情况下，将此参数设置为10000，则将在每10秒收到 slice_type=2的回调。
                        .build();

                // 自定义识别配置
                final AudioRecognizeConfiguration audioRecognizeConfiguration = new AudioRecognizeConfiguration.Builder()
                        //分片默认40ms，可设置40-5000,必须为20的整倍数，如果不是，sdk内将自动调整为20的整倍数，例如77将被调整为60，如果您不了解此参数不建议更改
                        //.sliceTime(40)
                        // 是否使能静音检测，
                        .setSilentDetectTimeOut(isOpenSilentCheck)
                        //触发静音超时后是否停止识别，默认为true:停止，setSilentDetectTimeOut为true时参数有效
                        .setSilentDetectTimeOutAutoStop(true)
                        // 静音检测超时可设置>2000ms，setSilentDetectTimeOut为true有效，超过指定时间没有说话超过指定时间没有说话收到onSilentDetectTimeOut回调；需要大于等于sliceTime，实际时间为sliceTime的倍数，如果小于sliceTime，则按sliceTime的时间为准
                        .audioFlowSilenceTimeOut(5000)
                        // 音量回调时间，需要大于等于sliceTime，实际时间为sliceTime的倍数，如果小于sliceTime，则按sliceTime的时间为准
                        .minVolumeCallbackTime(80)
                        //是否压缩音频。默认压缩，压缩音频有助于优化弱网或网络不稳定时的识别速度及稳定性
                        //SDK历史版本均默认压缩且未提供配置开关，如无特殊需求，建议使用默认值
                        .isCompress(isCompress)
                        .build();
                //启动识别器
                new Thread(() -> aaiClient.startAudioRecognize(audioRecognizeRequest,
                        audioRecognizeResultlistener,
                        audioRecognizeStateListener,
                        audioRecognizeConfiguration)).start();
            }

        });

//取消识别，丢弃识别结果，不等待最终识别结果返回
        cancel.setOnClickListener(v -> {
            AAILogger.info(TAG, "cancel button is clicked..");
            new Thread(() -> {
                boolean taskExist = false;
                if (aaiClient!=null) {
                    taskExist = aaiClient.cancelAudioRecognize();
                    handler.post(() -> {
                        start.setEnabled(true);
                    });
                }
                if (!taskExist) {
                    handler.post(() -> recognizeState.setText(getString(R.string.cant_cancel)));
                }
            }).start();

        });

        //音频压缩开关
        mIsCompressSW.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isCompress = isChecked;
            Toast.makeText(SpeechActivity.this, "音频压缩修改将在下次识别生效:"+isChecked, Toast.LENGTH_LONG).show();
        });

        mSilentSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isOpenSilentCheck = isChecked;
        });

    }

    private void initTTS() {
        //获得TTS合成器实例
        mTtsController = TtsController.getInstance();
        /***********************在线参数设置，如果仅用离线合成 不需要设置***************/
        mTtsController.setSecretId(DemoConfig.secretId);
        mTtsController.setSecretKey(DemoConfig.secretKey);
        mTtsController.setOnlineProjectId(0);

        mTtsController.setOnlineVoiceSpeed(mVoiceSpeed);//设置在线所合成音频的语速,语速，范围：[-2，2]，分别对应不同语速：-2代表0.6倍,-1代表0.8倍,0代表1.0倍（默认）,1代表1.2倍,2代表1.5倍,如果需要更细化的语速，可以保留小数点后一位，例如0.5 1.1 1.8等。
        mTtsController.setOnlineVoiceVolume(mVoiceVolume); //设置在线所合成音频的音量
        mTtsController.setOnlineVoiceType(mVoiceType);//设置在线所合成音频的音色id,完整的音色id列表见https://cloud.tencent.com/document/product/1073/37995
        mTtsController.setOnlineVoiceLanguage(mPrimaryLanguage);//主语言类型：1-中文（默认）2-英文

        mTtsController.setConnectTimeout(mConnectTimeout);
        mTtsController.setReadTimeout(mReadTimeout);
        mTtsController.setCheckNetworkIntervalTime(mCheckNetworkIntervalTime);
        // true: 服务器会返回Subtitles，false: 服务器不返回Subtitles，Subtitles为了精准计算播放器里的playProgress
        mTtsController.setOnlineParam("EnableSubtitle", false);

        mTtsController.init(this,mTtsmode , new TtsResultListener() {
            /**
             * 该方法已过期，建议使用其他签名方式的onSynthesizeData
             *
             * @param bytes       语音流
             * @param utteranceId 语句id
             * @param text        文本
             * @param engineType  引擎类型 0:在线 1:离线
             */
            @Override
            public void onSynthesizeData(byte[] bytes, String utteranceId, String text, int engineType) {
                Log.d(TAG, "onSynthesizeData: " + bytes.length + ":" + utteranceId + ":" + text + ":" + engineType);
            }

            /**
             * @param bytes 语音数据
             * @param utteranceId 语句id
             * @param text 文本
             * @param engineType 引擎类型 0:在线 1:离线
             */
            @Override
            public void onSynthesizeData(byte[] bytes, String utteranceId, String text, int engineType, String requestId, String respJson) {
                Log.d(TAG, "onSynthesizeData: " + bytes.length + ":" + utteranceId + ":" + text + ":" + engineType);

                if (respJson != null) {
                    try {
                        JSONObject object = new JSONObject(respJson);
                        Log.d(TAG, "Subtitles: " + object.getJSONObject("Response").getJSONObject("Subtitles").toString());
                    }catch (Exception e){

                    }
                }

                if (isPlay) {
                    if (mediaPlayer == null)return;
                    //将合成语音数据送入SDK内置播放器，如果sdk的内置播放器无法满足您的需求，您也可以使用自己实现的播放器替换
                    // 如果需要使用Server端的Subtitles做播放进度的计算，需要将respJson一同enqueue，前提是设置mTtsController.setOnlineParam("EnableSubtitle", true);
                    QPlayerError err = mediaPlayer.enqueue(bytes, text, utteranceId, respJson);
                    if (err != null){
                        Log.d(TAG, "mediaPlayer enqueue error" + err.getmMessage());

                    }
                } else {
                    //将byteBuffer保存到文件
                    try {
                        File file = null;
                        if (engineType == 1){
                            file = File.createTempFile("temp", ".wav");
                        } else {
                            file = File.createTempFile("temp", ".mp3");
                        }

                        OutputStream os = new FileOutputStream(file);
                        os.write(bytes);
                        os.flush();
                        os.close();
                        //QPlayerError err = mediaPlayer.enqueue(file, text, utteranceId);     //播放器也支持文件入参
                        Log.d(TAG, "file: "+file.toString());


                    } catch (IOException e) {

                        return;
                    }
                }
            }

            /**
             * @param error 错误信息
             * @param text 文本(如果有则返回)
             * @param utteranceId 语句id(如果有则返回)
             */
            @Override
            public void onError(TtsError error, String utteranceId, String text) {
                Log.d(TAG, "onError: " + error.getCode() + ":" + error.getMessage() + ":" + utteranceId);
                return;
            }

            @Override
            public void onOfflineAuthInfo(QCloudOfflineAuthInfo offlineAuthInfo) {
                return;
            }

        });
        mediaPlayer = new MediaPlayerDemo(new QCloudPlayerCallback(){ //使用SDK中提供的播放器

            @Override
            public void onTTSPlayStart() {
                Log.d(TAG, "开始播放");

            }

            @Override
            public void onTTSPlayWait() {
                Log.d(TAG, "播放完成，等待音频数据");

            }

            @Override
            public void onTTSPlayResume() {
                Log.d(TAG, "恢复播放");

            }

            @Override
            public void onTTSPlayPause() {
                Log.d(TAG, "暂停播放");

            }

            @Override
            public void onTTSPlayNext(String text, String utteranceId) {
                Log.d(TAG, "开始播放: " + utteranceId + "|" + text);

            }

            @Override
            public void onTTSPlayStop() {
                Log.d(TAG, "播放停止，内部队列已清空");

            }

            @Override
            public void onTTSPlayError(QPlayerError error) {
                Log.d(TAG, "播放器发生异常:"+error.getmCode() + ":" + error.getmMessage());

            }

            /**
             * @param currentWord 当前播放的字符
             * @param currentIndex 当前播放的字符在所在的句子中的下标.
             */
            @Override
            public void onTTSPlayProgress(String currentWord, int currentIndex) {
                Log.d(TAG, "onTTSPlayProgress: " + currentWord + "|" + currentIndex);

            }
        });

    }

    private void initWebSocket() {
        OkHttpClient client = new OkHttpClient();
        String temp = "ws://" + host+"/chat/chatServer/5";
        Request request = new Request.Builder().url(temp).addHeader("Authorization", User.getInstance().getToken()).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                runOnUiThread(() -> {
                    Toast.makeText(SpeechActivity.this, "WebSocket Connected", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onMessage(WebSocket webSocket,String message) {
                super.onMessage(webSocket, message);
                mTtsController.synthesize(message);
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                runOnUiThread(() -> Toast.makeText(SpeechActivity.this, "WebSocket Closing"+reason, Toast.LENGTH_SHORT).show());
                reconnectWebSocket();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> {
                    Toast.makeText(SpeechActivity.this, "WebSocket Failed", Toast.LENGTH_SHORT).show();
                });
                reconnectWebSocket();
            }
        });
    }

    private void reconnectWebSocket() {
        // 延迟一段时间后重连
        reconnectHandler.postDelayed(reconnectRunnable, RECONNECT_INTERVAL);
    }

    @Override
    protected void onDestroy() {
        if (aaiClient != null) {
            aaiClient.release();
        }
        if (mp != null) {
            mp.stop();
            mp.release();
            mp = null;
        }
        super.onDestroy();

    }


}