package com.videogo.test;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.videogo.EzvizApplication;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.main.EZLeaveMsgController;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.RestfulUtils;
import com.videogo.openapi.bean.EZAlarmInfo;
import com.videogo.openapi.bean.EZDeviceUpgradeStatus;
import com.videogo.openapi.bean.EZLeaveMessage;
import com.videogo.openapi.bean.EZProbeDeviceInfo;
import com.videogo.openapi.bean.EZSDKConfiguration;
import com.videogo.openapi.bean.EZStorageStatus;
import com.videogo.openapi.bean.EZUserInfo;
import com.videogo.util.LocalValidate;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ezviz.ezopensdk.R;

public class InterfaceSelfTestActivity extends Activity implements View.OnClickListener
    , EZLeaveMsgController.EZLeaveMsgGetDataCb {
    public static final String TAG = "InterfaceTestActivity";
    private static final int MSG_PLAY_NEXT = 101;
    private EditText mSerialEdit;
    private Button mTestButton;
    private final String filePath = "/sdcard/videogo_test_cfg";
    private Map<String, String> mMap;
    private String mSerial;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface_self_test);
        mMap = new HashMap<>();
        parseTestConfigFile(filePath, mMap);
        mSerial = mMap.get("deviceSerial");
        findViews();

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.interface_self_test_button:
                testV32Interface();
                break;
            case R.id.interface_v33_self_test_button:
                testV33Interface();
                break;
            case R.id.id_interface_self_test_openCloudPage:
                final String deviceSerial = mSerialEdit.getEditableText().toString();
                try {
                    if (EZOpenSDK.class.isInstance(EzvizApplication.getOpenSDK())) {
                        EZOpenSDK.getInstance().openCloudPage(deviceSerial);
                    }
                } catch (BaseException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.id_interface_self_test_forgetPassword:
                    EzvizApplication.getOpenSDK().openChangePasswordPage();
                break;
            case R.id.id_show_stream_limit_dialog2:
                showLimitDialog(this, 0, 900/60);
                break;
            default:
                break;
        }
    }

    private void findViews() {
        mSerialEdit=(EditText) findViewById(R.id.interface_self_test_editText);
        mTestButton = (Button) findViewById(R.id.interface_self_test_button);
        if(!TextUtils.isEmpty(mSerial)) {
            mSerialEdit.setText(mSerial);
        }
    }

    // test v3.2 interfaces
    private void testV31Interface() {
        final String deviceSerial = mSerialEdit.getEditableText().toString();
        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                // test interface capturePicture
            }
        });
        thr.start();
    }
    // test v3.2 interfaces
    List<EZLeaveMessage> mLeaveMsgList;
    int mPlayIndex = 0;
    private void testV32Interface() {
        final String deviceSerial = mSerialEdit.getEditableText().toString();
        Thread thr = new Thread(new Runnable(){
            @Override
            public void run() {

                try {
                    invoke_localValidatDeviceSerial();
                } catch (BaseException e) {
                    e.printStackTrace();
                }

            }});
        thr.start();
    }

    private void testV33Interface() {
        final String deviceSerial = mSerialEdit.getEditableText().toString();
        Thread thr = new Thread(new Runnable() {
            @Override
            public void run() {
                test_getConfiguration();
            }
        });

        thr.start();
    }

    private void invoke_getDeviceList() {
                try {
                    EzvizApplication.getOpenSDK().getDeviceList(0, 5);
                } catch (BaseException e) {
                    e.printStackTrace();
                }
    }
    private void invoke_setDeviceDefence(){
        try {
            EzvizApplication.getOpenSDK().setDefence("097226598", EZConstants.EZDefenceStatus.EZDefence_ALARMHOST_OUTER);
        } catch (BaseException e) {
            e.printStackTrace();
        }
    }

    private void invoke_capturePicture(String deviceSerial) {
        // test interface capturePicture
        try {
            String picUrl = EzvizApplication.getOpenSDK().captureCamera(deviceSerial, 0);
            LogUtil.i(TAG, "testV32Interface: capturePicture: " + picUrl);
        } catch (BaseException e) {
            e.printStackTrace();
        }
    }

    private void invoke_getUserInfo() {
        // test interface getUserInfo
        try {
            EZUserInfo userInfo = EzvizApplication.getOpenSDK().getUserInfo();
            LogUtil.i(TAG, "EZUserInfo:" + userInfo);
        } catch (BaseException e) {
            e.printStackTrace();

            ErrorInfo errorInfo = (ErrorInfo) e.getObject();
            LogUtil.debugLog(TAG, errorInfo.toString());
        }
    }

    private void invoke_getUnreadMessageCount (String deviceSerial) {
        // test interface getUnreadMessageCount
        try {
            int msgCount = EzvizApplication.getOpenSDK().getUnreadMessageCount(deviceSerial, EZConstants.EZMessageType.EZMessageTypeAlarm);
            LogUtil.i(TAG, "unReadMessageCount:" + msgCount);
        } catch (BaseException e) {
            e.printStackTrace();

            ErrorInfo errorInfo = (ErrorInfo) e.getObject();
            LogUtil.debugLog(TAG, errorInfo.toString());
        }

    }

    private void invoke_getAlarmListBySerial(String deviceSerial) {
        List<EZAlarmInfo> result = null;
        try {
            Calendar begin = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            begin.set(begin.get(Calendar.YEAR), begin.get(Calendar.MONTH), begin.get(Calendar.DAY_OF_MONTH),
                    0, 0, 0);
            end.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH),
                    23, 59, 59);

            result = EzvizApplication.getOpenSDK().getAlarmList(deviceSerial, 0, 5, begin, end);
            LogUtil.i(TAG, "invoke_getAlarmListBySerial: " + result);
        } catch (BaseException e) {
            e.printStackTrace();

            ErrorInfo errorInfo = (ErrorInfo) e.getObject();
            LogUtil.debugLog(TAG, errorInfo.toString());
        }

        //device serial is null
        try {
            Calendar begin = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            begin.set(begin.get(Calendar.YEAR), begin.get(Calendar.MONTH), begin.get(Calendar.DAY_OF_MONTH),
                    0, 0, 0);
            end.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH),
                    23, 59, 59);

            result = EzvizApplication.getOpenSDK().getAlarmList(null, 0, 5, begin, end);
            LogUtil.i(TAG, "invoke_getAlarmListBySerial: " + result);
        } catch (BaseException e) {
            e.printStackTrace();

            ErrorInfo errorInfo = (ErrorInfo) e.getObject();
            LogUtil.debugLog(TAG, errorInfo.toString());
        }

        //begin time  is null
        try {
            Calendar begin = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            begin.set(begin.get(Calendar.YEAR), begin.get(Calendar.MONTH), begin.get(Calendar.DAY_OF_MONTH),
                    0, 0, 0);
            end.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH),
                    23, 59, 59);

            result = EzvizApplication.getOpenSDK().getAlarmList(null, 0, 5, null, end);
            LogUtil.i(TAG, "invoke_getAlarmListBySerial: " + result);
        } catch (BaseException e) {
//            e.printStackTrace();
        }

        //end time  is null
        try {
            Calendar begin = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            begin.set(begin.get(Calendar.YEAR), begin.get(Calendar.MONTH), begin.get(Calendar.DAY_OF_MONTH),
                    0, 0, 0);
            end.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH),
                    23, 59, 59);

            result = EzvizApplication.getOpenSDK().getAlarmList(null, 0, 5, begin, null);
            LogUtil.i(TAG, "invoke_getAlarmListBySerial: " + result);
            Assert(true);
        } catch (BaseException e) {
            e.printStackTrace();
        }

        //begin and end are null
        try {
            Calendar begin = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            begin.set(begin.get(Calendar.YEAR), begin.get(Calendar.MONTH), begin.get(Calendar.DAY_OF_MONTH),
                    0, 0, 0);
            end.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH),
                    23, 59, 59);

            result = EzvizApplication.getOpenSDK().getAlarmList(null, 0, 5, null, null);
            LogUtil.i(TAG, "invoke_getAlarmListBySerial: " + result);
            Assert(true, "begin and end time are null");
        } catch (BaseException e) {
//            e.printStackTrace();
            Assert(true, "begin and end time are null");
        }

        //device serial is invalid
        try {
            Calendar begin = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            begin.set(begin.get(Calendar.YEAR), begin.get(Calendar.MONTH), begin.get(Calendar.DAY_OF_MONTH),
                    0, 0, 0);
            end.set(end.get(Calendar.YEAR), end.get(Calendar.MONTH), end.get(Calendar.DAY_OF_MONTH),
                    23, 59, 59);

            result = EzvizApplication.getOpenSDK().getAlarmList("12345678a", 0, 5, begin, end);
            LogUtil.i(TAG, "invoke_getAlarmListBySerial: " + result);
            Assert(false, "");
        } catch (BaseException e) {
//            e.printStackTrace();
            //any exception will be printed by Assertion
        }
    }

    private void invoke_localValidatDeviceSerial() throws BaseException {
        LocalValidate localValidate = new LocalValidate();
        Assert(localValidate.localValidatDeviceSerial("123456789") == 0);
        Assert(localValidate.localValidatDeviceSerial("12345678a") < 0);
        Assert(localValidate.localValidatDeviceSerial("12345678") < 0);
        Assert(localValidate.localValidatDeviceSerial("-23456789") < 0);
    }


    private void test_getConfiguration() {
        try {
            EZSDKConfiguration config = com.videogo.openapi.EzvizAPI.getInstance().getConfiguration();
            LogUtil.i(TAG, "test_getConfiguration: ret" + config);
        } catch (BaseException e) {
            e.printStackTrace();
        }
    }

    private void invoke_other(String deviceSerial) {
        // test interface getLeaveMessageList
        try {
            Calendar begin = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            begin.set(Calendar.HOUR_OF_DAY, 0);
            end.set(Calendar.HOUR_OF_DAY, 23);

            List<EZLeaveMessage> ret = EZOpenSDK.getInstance().getLeaveMessageList(deviceSerial, 0, 5, begin, end);
            LogUtil.i(TAG, "getLeaveMessageList:" + ret);
        } catch (BaseException e) {
            e.printStackTrace();
        }
        try {
            Calendar begin = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            begin.set(Calendar.HOUR_OF_DAY, 0);
            end.set(Calendar.HOUR_OF_DAY, 23);

            List<EZLeaveMessage> ret = EZOpenSDK.getInstance().getLeaveMessageList("", 0, 5, begin, end);
            LogUtil.i(TAG, "getLeaveMessageList:" + ret);
        } catch (BaseException e) {
            e.printStackTrace();
        }
        try {
            Calendar begin = Calendar.getInstance();
            Calendar end = Calendar.getInstance();
            begin.set(Calendar.HOUR_OF_DAY, 0);
            end.set(Calendar.HOUR_OF_DAY, 23);

            mLeaveMsgList = EZOpenSDK.getInstance().getLeaveMessageList(null, 0, 5, begin, end);
//                    LogUtil.i(TAG, "getLeaveMessageList:" + ret);
        } catch (BaseException e) {
            e.printStackTrace();

            ErrorInfo errorInfo = (ErrorInfo) e.getObject();
            LogUtil.debugLog(TAG, errorInfo.toString());
        }

        // test interface probeDeviceInfo
        try {
            EZProbeDeviceInfo probeInfo = EzvizApplication.getOpenSDK().probeDeviceInfo(deviceSerial);
            LogUtil.i(TAG, "probeDeviceInfo:" + probeInfo);
        } catch (BaseException e) {
            e.printStackTrace();

            ErrorInfo errorInfo = (ErrorInfo) e.getObject();
            LogUtil.debugLog(TAG, errorInfo.toString());
        }

        // test interface formatStorage
        try {
            boolean formatResult = EzvizApplication.getOpenSDK().formatStorage(deviceSerial, 1);
            LogUtil.i(TAG, "formatStorage:" + formatResult);
        } catch (BaseException e) {
            ErrorInfo errorInfo = (ErrorInfo) e.getObject();
            LogUtil.debugLog(TAG, errorInfo.toString());
        }

        // test interface getStorageStatus
        try {
            List<EZStorageStatus> storageList = EzvizApplication.getOpenSDK().getStorageStatus(deviceSerial);
            LogUtil.i(TAG, "getStorageStatus:" + storageList);
        } catch (BaseException e) {
            e.printStackTrace();

            ErrorInfo errorInfo = (ErrorInfo) e.getObject();
            LogUtil.debugLog(TAG, errorInfo.toString());
        }

        // test interface getDeviceUpgradeStatus
        try {
            EZDeviceUpgradeStatus status = EzvizApplication.getOpenSDK().getDeviceUpgradeStatus(deviceSerial);
            LogUtil.i(TAG, "run: getDeviceUpgradeStatus" + status);
        } catch (BaseException e) {
            e.printStackTrace();

            ErrorInfo errorInfo = (ErrorInfo) e.getObject();
            LogUtil.debugLog(TAG, errorInfo.toString());
        }

        // test interface upgradeDevice
        try {
            EzvizApplication.getOpenSDK().upgradeDevice(deviceSerial);
        } catch (BaseException e) {
            e.printStackTrace();

            ErrorInfo errorInfo = (ErrorInfo) e.getObject();
            LogUtil.debugLog(TAG, errorInfo.toString());
        }


        // test interface getPushNoticeMessage
//        try {
//            EzvizApplication.getOpenSDK().getTransferMessageInfo("af");
//        } catch (BaseException e) {
//            e.printStackTrace();
//
//            ErrorInfo errorInfo = (ErrorInfo) e.getObject();
//            LogUtil.debugLog(TAG, errorInfo.toString());
//        }
    }
    private void Assert(boolean result) throws BaseException{
//        if(!result)
//            throw new BaseException(0);
        if(!result)
            LogUtil.i(TAG, "Assert failed: ");
    }
    private void Assert(boolean result, String assertDesc){
//        if(!result)
//            throw new BaseException("Assert failed:"  + assertDesc, 0);
        if(!result)
            LogUtil.i(TAG, "Assert failed: " + assertDesc);
    }

    // 测试如何解密数据
    private void test_decryptData() {
        // url是加密后的报警图片url， EZAlarmInfo.alarmPicUrl
        String url = "https://i.ys7.com/streamer/alarm/url/get?fileId=562bf00c-daec-11e5-8000-e02ec7d605d5&deviceSerialNo=497413200&isEncrypted=1&isCloudStored=1";
        RestfulUtils restfulUtils = RestfulUtils.getInstance();
        {
            //1, 下载数据，可能需要自己实现, 我这里调用的是postForStream
//            InputStream is = restfulUtils.postForStream(null, url);
            InputStream is = null;//restfulUtils.postForStream(null, url);
            try {
                byte data[] = new byte[500 * 1024];
                int len = 0, size = 0;
                while (size != -1) {
                    size = is.read(data, len, data.length - len);
                    if (size != -1)
                        len += size;
                }
                byte pureContent[] = Arrays.copyOf(data, len);

                // 2, 调用decryptData得到解密后的bitmap数据, NXXJOO为密码(默认是设备验证码)
                byte[] decryptData = EzvizApplication.getOpenSDK().decryptData(pureContent, "NXXJOO");
                LogUtil.i(TAG, "test_decryptData: decrypt finish");
                
                // 3, 将解密后的数据写到文件中，看是否解密成功
//                FileOutputStream fos2 = new FileOutputStream(new File("/sdcard/decrypt_img.jpg"));
//                LogUtil.i(TAG, "test_decryptData: fos2 created");
//                Bitmap bmp = BitmapFactory.decodeByteArray(decryptData, 0, decryptData.length);
//                LogUtil.i(TAG, "test_decryptData: bitmap created");
//                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos2);
//                LogUtil.i(TAG, "test_decryptData:compressed to file");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtil.i(TAG, "handleMessage: msg: " + msg.what);

            switch (msg.what) {
                case EZLeaveMsgController.MSG_LEAVEMSG_DOWNLOAD_SUCCESS:
                    break;
                case EZLeaveMsgController.MSG_LEAVEMSG_DOWNLOAD_FAIL:
                    break;
                case MSG_PLAY_NEXT:
//                    if(mPlayIndex < mLeaveMsgList.size() ) {
//                        Thread thr = new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                boolean result = EzvizApplication.getOpenSDK().getLeaveMessageData(mHandler, mLeaveMsgList.get(mPlayIndex), InterfaceSelfTestActivity.this);
//                                LogUtil.i(TAG, "run: getLeaveMessageData returns:" + result);
//                            }
//                        });
//                        thr.start();
//                    }
                    break;
                default:
                    break;
            }
        }
    };


    //--------------

    private Context mContext;
    private Handler mMainHandler;
    private AlertDialog mLimitStreamDialog;
    private final static int LIMIT_STREAM_MSG = 10001;

    private void showLimitDialog(Context context, int type, int limit_minutes) {
        if(limit_minutes <= 0) {
            LogUtil.i(TAG, "showLimitDialog: limite_minutes invalid params");
            return;
        }
        LinearLayout passwordErrorLayout = new LinearLayout(context);
        FrameLayout.LayoutParams layoutLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        passwordErrorLayout.setOrientation(LinearLayout.VERTICAL);
        passwordErrorLayout.setLayoutParams(layoutLp);

        TextView message1 = new TextView(context);
        LinearLayout.LayoutParams message1Lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        message1Lp.gravity = Gravity.CENTER_HORIZONTAL;
        message1Lp.leftMargin = Utils.dip2px(context, 10);
        message1Lp.rightMargin = Utils.dip2px(context, 10);
        message1Lp.topMargin = Utils.dip2px(context, 20);
        message1.setGravity(Gravity.CENTER);
        message1.setTextColor(Color.rgb(255, 255, 255));
        message1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        passwordErrorLayout.addView(message1, message1Lp);

        Button btn = new Button(context);
        btn.setText("继续");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                closePasswordDialog(mRealPlayMgr);
//                mMainHandler.removeMessages(LIMIT_STREAM_MSG);
                mLimitStreamDialog.dismiss();
            }
        });
        LinearLayout.LayoutParams buttonLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        buttonLp.leftMargin = Utils.dip2px(context, 10);
        buttonLp.gravity = Gravity.CENTER_HORIZONTAL;
        buttonLp.topMargin = Utils.dip2px(context, 30);
        buttonLp.bottomMargin = Utils.dip2px(context, 10);
        btn.setPadding(50, 10, 50, 10);

        btn.setBackgroundDrawable(new ShapeDrawable(new RectShape()));
        btn.setTextColor(Color.rgb(255, 255, 255));
        btn.setBackgroundColor(Color.GRAY);
        passwordErrorLayout.addView(btn, buttonLp);

        // 使用布局中的视图创建AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        message1.setText("您已观看视频" + limit_minutes + "分钟，是否继续?");

        builder.setCancelable(true);
        builder.setView(passwordErrorLayout);

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
//                closePasswordDialog(mRealPlayMgr);
            }
        });

        mLimitStreamDialog = builder.create();

        Window window = mLimitStreamDialog.getWindow();
//        mPasswordDialog.getWindow().setGravity(Gravity.TOP);
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setGravity(Gravity.TOP);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//        window.setbac

        lp.y = 50;
        lp.alpha = 0.85f;
        window.setAttributes(lp);
        mLimitStreamDialog.show();
    }

    @Override
    public void onData(byte[] bytes) {
        LogUtil.i(TAG, "onData: size " + bytes.length);
        processDataWithAudioTrack8bit(bytes);

        ++ mPlayIndex;
        Message msg = Message.obtain();
        msg.what = MSG_PLAY_NEXT;
        mHandler.sendMessageDelayed(msg, 1000);

    }

    AudioTrack mAudioTrack = null;
    private void processDataWithAudioTrack8bit(byte[] bytess) {
        LogUtil.i(TAG, "Enter processDataWithAudioTrack8bit: ");
        if (mAudioTrack == null) {
            mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    500000,
                    AudioTrack.MODE_STREAM);
        }
        // Start playback
        try {
            mAudioTrack.play();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return;
        }

        mAudioTrack.write(bytess,0, bytess.length);
        mAudioTrack.stop() ;
    }

    // content of file /sdcard/videogo_test_cfg:
    // deviceSerial:427734168
    private void parseTestConfigFile(String filePath, Map<String,String> map) {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
            String lineStr;
            lineStr = br.readLine();
            while (lineStr != null) {
                String[] values = lineStr.split(":");
                if(values.length == 2) {
                    map.put(values[0], values[1]);
                }
                lineStr = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
