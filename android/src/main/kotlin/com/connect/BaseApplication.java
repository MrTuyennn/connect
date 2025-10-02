package com.connect;

import android.app.Application;
import android.util.Log;

import com.connect.utils.SharedPreferencesUtils;
import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.IOTCAPIs;
import com.tutk.IOTC.TUTKGlobalAPIs;
import com.xc.p2pVideo.NativeMediaPlayer;

public class BaseApplication extends Application {
    private static final String lincense = "AQAAAMHY3vUDYAhbA/F5ekE+00jq1ACuTIznLJDK55p/jpI7riWN6bp7KYLTDrsQ3XJkzsVkJSBK3rmD3ZPAWF4JlZzn3J/qpmA3O31yfX7VxVNDXd1h3vJYFtgsjOcl9vn4c4k2oPKXHUGjtGxH3O+4Wc14AI/mkmvJIFVI2k3M2J9eanoTqXbEhLMRRpXa+tmbCzM4/L/q3NMZqc4sdErADNIb";
    public static final String TAG = "TUTK_CONN" ;
    private static final int SESSION_CONN_MAX = 32;
    public static BaseApplication ctx;
    public static BaseApplication getInstance() {
        return ctx;
    }
    public NativeMediaPlayer nativeMediaPlayer;
    @Override
    public void onCreate() {
        super.onCreate();
        ctx = this;
        // CrashHandler crashHandler = CrashHandler.getInstance();
        //crashHandler.init(getApplicationContext());
        initConfig();
        SharedPreferencesUtils.getInstance().init(getApplicationContext());
        nativeMediaPlayer = new NativeMediaPlayer();
        nativeMediaPlayer.JniInitClassToJni();

    }

    private void initConfig() {

        /*St_LogAttr st_logAttr = new St_LogAttr();
        st_logAttr.file_max_size = 20*1024*1024;
        FileUtils.createFile(BaseApplication.this, "log.txt");
        st_logAttr.path = "/storage/emulated/0/Android/data/com.acesee.smartorange/files/log.txt";
        st_logAttr.log_level = LogLevel.LEVEL_VERBOSE;
        AVAPIs.AV_Set_Log_Attr(st_logAttr);

        St_LogAttr st_logAttr1 = new St_LogAttr();
        st_logAttr1.file_max_size = 20*1024*1024;
        FileUtils.createFile(BaseApplication.this, "IotcLog.txt");
        st_logAttr1.path = "/storage/emulated/0/Android/data/com.acesee.smartorange/files/IotcLog.txt";
        st_logAttr1.log_level = LogLevel.LEVEL_VERBOSE;
        IOTCAPIs.IOTC_Set_Log_Attr(st_logAttr1);*/

        int ret = TUTKGlobalAPIs.TUTK_SDK_Set_License_Key(lincense);
        Log.d(TAG,"TUTK_SDK_Set_License_Key() ret ="+ret);
        if (ret != TUTKGlobalAPIs.TUTK_ER_NoERROR) {
            Log.d(TAG,"TUTK_SDK_Set_License_Key exit...!!");
            return;
        }
        ret = IOTCAPIs.IOTC_Initialize2(0);
        //IOTCAPIs.IOTC_Set_Max_Session_Number(SESSION_CONN_MAX);
        Log.d(TAG,"IOTC_Initialize2() ret = %d"+ret);
        if (ret != IOTCAPIs.IOTC_ER_NoERROR) {
            Log.d(TAG,"IOTCAPIs_Device exit...!!");
            return;
        }
        // alloc 3 sessions for video and two-way audio
        AVAPIs.avInitialize(SESSION_CONN_MAX);
    }

}
