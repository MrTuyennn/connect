package com.connect.module.native_view;


import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.app.Activity;
import android.util.Log;
import android.widget.Toast;


import com.connect.module.module.action.PlayerAction;
import com.connect.module.module.bean.DeviceBean;
import com.connect.module.module.bean.EventResult;
import com.connect.utils.AppConstant;
import com.connect.utils.SharedPreferencesUtils;
import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.IOTCAPIs;
import com.tutk.IOTC.TUTKGlobalAPIs;
import com.xc.p2pVideo.NativeMediaPlayer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class JoyStick {

    private static final String TAG = "CameraPlayerWidget";

    private static final String LICENSE_KEY = "AQAAAMHY3vUDYAhbA/F5ekE+00jq1ACuTIznLJDK55p/jpI7riWN6bp7KYLTDrsQ3XJkzsVkJSBK3rmD3ZPAWF4JlZzn3J/qpmA3O31yfX7VxVNDXd1h3vJYFtgsjOcl9vn4c4k2oPKXHUGjtGxH3O+4Wc14AI/mkmvJIFVI2k3M2J9eanoTqXbEhLMRRpXa+tmbCzM4/L/q3NMZqc4sdErADNIb";

    private final Activity activity;
    private final List<DeviceBean> players = new ArrayList<>();

    private NativeMediaPlayer nativeMediaPlayer;
    private int currentPlayers = 0;//标记当前选哪个播放器 控制音频

    private String UID = "", PWD = "";

    private MethodChannel channel;
    public JoyStick( Activity activity, BinaryMessenger messenger, Object args) {
        this.activity = activity;
        SharedPreferencesUtils.getInstance().init(activity);
        EventBus.getDefault().register(this);


        channel = new MethodChannel(messenger, AppConstant.CHANNEL_CAMERA_PLAYER);
        channel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
            @Override
            public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
                if(call.method.equals("methodCamera")){
                    if (!(call.arguments instanceof Map)) {
                        result.error("ARG_ERROR", "expected map {uuid, pass}", null);
                        return;
                    }
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) call.arguments;
                    String newUid = (String) map.get("uuid");
                    String newPwd = (String) map.get("pass");
                    if (newUid == null || newPwd == null) {
                        result.error("ARG_ERROR", "uuid/pass missing", null);
                        return;
                    }

                    UID = newUid;
                    PWD = newPwd;
//                    myRequetPermission();
//                    initDeviceSource();


                } else if (call.method.equals("methodPtz")) {
                    Integer code = (call.arguments instanceof Integer) ? (Integer) call.arguments : null;
                    if (code == null) {
                        return; // QUAN TRỌNG
                    }
                    Log.d(TAG, "methodPtz code=" + code);
                    ptzCamera(code);
                    result.success(true);
                } else {
                    result.notImplemented();
                }
            }
        });

        //startPlay(0);
    }

    private void initTUTK() {
        int ret = TUTKGlobalAPIs.TUTK_SDK_Set_License_Key(LICENSE_KEY);
        if (ret != TUTKGlobalAPIs.TUTK_ER_NoERROR) return;

        ret = IOTCAPIs.IOTC_Initialize2(0);
        if (ret != IOTCAPIs.IOTC_ER_NoERROR) return;

        AVAPIs.avInitialize(32);
        NativeMediaPlayer.JniInitClassToJni();
    }

    private void initDeviceSource() {
        initTUTK();
        DeviceBean deviceBean = new DeviceBean();
        deviceBean.setPlayerId(1);
        deviceBean.setDeviceUid(UID);
        deviceBean.setDevicePwd(PWD);
        deviceBean.setDeviceName("admin");
        deviceBean.setPlayerAction(new PlayerAction(deviceBean, NativeMediaPlayer.SOFTDECODE,
                activity, null, PlayerAction.MAINSTREAM));
        players.add(deviceBean);
    }

    private void myRequetPermission() {
        // RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        } else {
            Toast.makeText(activity, "Bạn đã cấp quyền ghi âm!", Toast.LENGTH_SHORT).show();
        }

        // WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        // READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void ptzCamera(int ptz) {
        players.get(currentPlayers).getPlayerAction().ptzControl(ptz);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventResult event) {
        if (event.getRequestUrl().equals(NativeMediaPlayer.DECODE)) {
            int playerId = (int) event.getObject();
            Toast.makeText(activity, "Kết nối player " + playerId + " thành công!", Toast.LENGTH_SHORT).show();


        } else if (event.getRequestUrl().equals(NativeMediaPlayer.CONNECT)) {
            int playerId = (int) event.getObject();
            Log.d(TAG, "Connect failed for playerId=" + playerId);
            stopPlayer(playerId - 1);
            Toast.makeText(activity, "Kết nối player " + playerId + " thất bại!", Toast.LENGTH_SHORT).show();
        } else if (event.getRequestUrl().equals(NativeMediaPlayer.TIMEOUT)) {
            int playerId = (int) event.getObject();
            Log.d(TAG, "Timeout for playerId=" + playerId);
            stopPlayer(playerId);
            Toast.makeText(activity, "Player chờ dữ liệu quá lâu, kết nối thất bại!", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopPlayer(int index) {
        if (players.get(index).getAvIndex() > -1) {
            AVAPIs.avClientStop(players.get(index).getAvIndex());
        }
        if (players.get(index).getSid() > -1) {
            IOTCAPIs.IOTC_Session_Close(players.get(index).getSid());
        }

        players.get(index).getPlayerAction().setStartRead(false);
        NativeMediaPlayer.JniCloseVideoPlay(players.get(index).getPlayerId());

    }

    private void startPlay(final int index) {
        if (players.isEmpty()) return;
        players.get(index).setDeviceUid(UID);
        players.get(index).setDevicePwd(PWD);
        players.get(index).getPlayerAction().setStartRead(true);
        SharedPreferencesUtils.getInstance().saveDeviceId(UID, PWD);
        new Thread(new Runnable() {
            @Override
            public void run() {
                NativeMediaPlayer.JniVideoPlay(players.get(index).getPlayerId());

                players.get(index).getPlayerAction().startDeviceConnection();
            }
        }).start();
    }

}