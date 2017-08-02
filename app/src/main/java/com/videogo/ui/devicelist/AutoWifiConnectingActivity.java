package com.videogo.ui.devicelist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.hikvision.wifi.configuration.DeviceInfo;
import com.videogo.EzvizApplication;
import com.videogo.RootActivity;
import com.videogo.constant.Constant;
import com.videogo.device.DeviceInfoEx;
import com.videogo.device.SearchDeviceInfo;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZOpenSDKListener;
import com.videogo.openapi.bean.EZProbeDeviceInfo;
import com.videogo.ui.cameralist.EZCameraListActivity;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;

import java.util.Timer;
import java.util.TimerTask;

import ezviz.ezopensdk.R;

/**
 * 一键添加摄像头界面
 *
 * @author chengjuntao
 * @data 2014-4-9
 */
public class AutoWifiConnectingActivity extends RootActivity implements OnClickListener/*, OnAuthListener*/ {

    public static final String SUPPORT_WIFI = "support_Wifi";

    public static final String SUPPORT_NET_WORK = "support_net_work";
    /**
     * 来自于哪个页面
     */
    public static final String FROM_PAGE = "from_page";

    public static final int FROM_PAGE_SERIES_NUM_SEARCH_ACTIVITY = 1;

    private static final String TAG = "AutoWifiConnectingActivity";

    private static final int MSG_ADD_CAMERA_SUCCESS = 10;

    private static final int MSG_ADD_CAMERA_FAIL = 12;

    /**
     * 一键添加的当前状态 正在连接wifi
     */
    private static final int STATUS_WIFI_CONNETCTING = 100;

    /**
     * 一键添加的当前状态 正在进行设备注册
     */
    private static final int STATUS_REGISTING = 101;

    /**
     * 一键添加的当前状态 正在添加摄像头
     */
    private static final int STATUS_ADDING_CAMERA = 102;

    /**
     * 一键添加的当前状态 添加摄像头
     */
    private static final int STATUS_ADD_CAMERA_SUCCESS = 103;
    /** 有线连接 */
    // private static final int STATUS_LINE_CONNECTING = 104;
    /**
     * 开启云存储成功
     */
    private final static int MSG_OPEN_CLOUD_STORYED_SUCCESS = 104;

    /**
     * 开启云存储失败
     */
    private final static int MSG_OPEN_CLOUD_STORYED_FAIL = 105;

    /**
     * 一键添加错误代号 设备连接wifi失败
     */
    private static final int ERROR_WIFI_CONNECT = 1000;

    /**
     * 一键添加错误代号 设备注册失败
     */
    private static final int ERROR_REGIST = 1001;

    /**
     * 一键添加错误代号 设备添加摄像头失败
     */
    private static final int ERROR_ADD_CAMERA = 1002;
    /**
     * 配置wifi超时时间 s
     */
    private static final int MAX_TIME_STEP_ONE_WIFI = 60;
    /**
     * 注册超时时间 s
     */
    private static final int MAX_TIME_STEP_TWO_REGIST = 60;
    /**
     * 添加设备超时时间 s
     */
    private static final int MAX_TIME_STEP_THREE_ADD = 15;

    // 开通2；激活1；关闭0；
    protected static final int SET_OPEN_ENABLE = 2;

    private static int SEARCH_CAMERA_TIMES = 2;
    private static int ADD_CAMERA_TIMES = 3;

    // 返回按钮
    private View btnBack;

    // title
    private TextView tvTitle;

    // 添加摄像头的容器
    private View addCameraContainer;

    // 有线连接的容器
    private View lineConnectContainer;

    // 状态
    private TextView tvStatus;

    // 重试按钮
    private View btnRetry;

    // 有线连接
    private Button btnLineConnect;

    // 线连接成功
    private View btnLineConnetOk;

    // 完成按钮
    private View btnFinish;

    // 云服务开通选择
    private CheckBox ckbCloundService;

    // 了解更多
    private View tvMore;

    private String serialNo;

    private String wifiPassword = "";

    private String wifiSSID = "";

    /**
     * 当前的错误代码
     */
    private int errorStep = 0;

    private LocalInfo mLocalInfo;

    private SearchDeviceInfo mSearchDevice = null;

    private MessageHandler mMsgHandler;

    String mVerifyCode = "";

    private DeviceInfoEx mDeviceInfoEx;

    private ImageView imgAnimation;

    private AnimationDrawable animWaiting;

    private String maskIpAddress;

    private Timer overTimeTimer;

    private MulticastLock lock;

    EZOpenSDKListener.EZStartConfigWifiCallback mEZStartConfigWifiCallback = new EZOpenSDKListener.EZStartConfigWifiCallback() {
        @Override
        public void onStartConfigWifiCallback(final EZConstants.EZWifiConfigStatus status) {
            AutoWifiConnectingActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (status == EZConstants.EZWifiConfigStatus.DEVICE_WIFI_CONNECTING) {

                    } else if (status == EZConstants.EZWifiConfigStatus.DEVICE_WIFI_CONNECTED) {
                        if (isWifiConnected) {
                            LogUtil.i(TAG, "defiveFindHandler: receiver WIFI while isWifiConnected is true");
                            return;
                        }
                        LogUtil.debugLog(TAG, "接收到设备连接上WIFI  " + serialNo);
                        isWifiOkBonjourget = true;
                        isWifiConnected = true;
                        t2 = System.currentTimeMillis();
                        stopWifiConfigOnThread();
                        changeStatuss(STATUS_REGISTING);
                    } else if (status == EZConstants.EZWifiConfigStatus.DEVICE_PLATFORM_REGISTED) {
                        LogUtil.debugLog(TAG, "接收到设备连接上PLAT信息 " + serialNo);
                        if (isPlatConnected) {
                            LogUtil.i(TAG, "defiveFindHandler: receiver PLAT while isPlatConnected is true");
                            return;
                        }
                        isPlatBonjourget = true;
                        isPlatConnected = true;
                        t3 = System.currentTimeMillis();
                        cancelOvertimeTimer();
                        changeStatuss(STATUS_ADDING_CAMERA);
                    }
                }
            });
        }
    };

    private boolean isWifiConnected = false;
    private boolean isPlatConnected = false;

    private boolean isPlatBonjourget = false;
    private boolean isWifiOkBonjourget = false;

    private long t1 = 0;
    private long t2 = 0;
    private long t3 = 0;
    private long t4 = 0;
    private long t5 = 0;
    Handler defiveFindHandler = new Handler() {

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                DeviceInfo deviceInfo = (DeviceInfo) msg.obj;
                if (deviceInfo == null || deviceInfo.getState() == null) {
                    LogUtil.debugLog(TAG, "接收到无效的bonjour信息 为空");
                    return;
                }
                // 设备序列号 相等 说明是我们要添加的设备 否则不是
                if (serialNo != null && serialNo.equals(deviceInfo.getSerialNo())) {
                    if ("WIFI".equals(deviceInfo.getState().name())) {
                        if (isWifiConnected) {
                            LogUtil.i(TAG, "defiveFindHandler: receiver WIFI while isWifiConnected is true");
                            return;
                        }
                        isWifiOkBonjourget = true;
                        isWifiConnected = true;
                        LogUtil.debugLog(TAG, "接收到设备连接上wifi信息 " + deviceInfo.toString());
                        t2 = System.currentTimeMillis();
                        stopWifiConfigOnThread();
                        changeStatuss(STATUS_REGISTING);
                    } else if ("PLAT".equals(deviceInfo.getState().name())) {
                        if (isPlatConnected) {
                            LogUtil.i(TAG, "defiveFindHandler: receiver PLAT while isPlatConnected is true");
                            return;
                        }

                        isPlatBonjourget = true;
                        isPlatConnected = true;
                        LogUtil.debugLog(TAG, "接收到设备连接上PLAT信息 " + deviceInfo.toString());
                        t3 = System.currentTimeMillis();
                        cancelOvertimeTimer();
                        changeStatuss(STATUS_ADDING_CAMERA);
                    }
                }
            }
        }

        ;
    };

    private View btnCancel;

    private View llyCloundService;

    // private WaitDialog mWaitDlg;

    private int fromPage;

    private boolean isSupportNetWork;

    private boolean isSupportWifi;

    private View tvDeviceWifiConfigTip;

    private String deviceType;

    // private long time;
    /**
     * 配置开始时间
     */
    private long recordConfigStartTime = 0;
    /**
     * 搜索设备信息的错误码
     */
    private int searchErrorCode = 0;

    private View connectStateContainer;

    private View llyStatus1;

    private View llyStatus2;

    private View llyStatus3;

    private View helpTop;

    private View help;

    private View tvSuccess;

//    private UnbindDeviceTriggerHelper mTriggerHelper;

    private WifiInfo mWifiInfo;

    private String mac;

    private int speed;

    private int strength;
    // 是否为解绑错误 如果是解绑错误 就不上报
    private boolean isUnbindDeviceError = false;
    private EZProbeDeviceInfo mEZProbeDeviceInfo = null;

    // return 0 means success, camera info be saved in mEZProbeDeviceInfo
    // return other value means fail, result is the error code
    private int probeDeviceInfo(String deviceSerial) {
        try {
            mEZProbeDeviceInfo = EzvizApplication.getOpenSDK().probeDeviceInfo(serialNo);
            if (mEZProbeDeviceInfo != null) {
                return 0;
            }
            return 1;//unknown error
        } catch (BaseException e) {
            e.printStackTrace();
            ErrorInfo errorInfo = (ErrorInfo) e.getObject();
            LogUtil.debugLog(TAG, errorInfo.toString());

            LogUtil.infoLog(TAG, "" + e);
            return errorInfo.errorCode;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_wifi_connecting);
        // 唤醒，常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        findViews();
        fromPage = getIntent().getIntExtra(FROM_PAGE, 0);
        initUI();
        setListener();
        if (fromPage == FROM_PAGE_SERIES_NUM_SEARCH_ACTIVITY) {
//            mDeviceInfoEx = DeviceManager.getInstance().getDeviceInfoExById(serialNo);
            changeStatuss(STATUS_ADD_CAMERA_SUCCESS);
        } else if (!isSupportWifi) {
            lineConnectClick();
            btnBack.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.GONE);
        } else {
            connectCamera();
        }
    }

    private void init() {

        serialNo = getIntent().getStringExtra(SeriesNumSearchActivity.BUNDE_SERIANO);
        mVerifyCode = getIntent().getStringExtra(SeriesNumSearchActivity.BUNDE_VERYCODE);
        wifiPassword = getIntent().getStringExtra(AutoWifiNetConfigActivity.WIFI_PASSWORD);
        deviceType = getIntent().getStringExtra(AutoWifiNetConfigActivity.DEVICE_TYPE);
        wifiSSID = getIntent().getStringExtra(AutoWifiNetConfigActivity.WIFI_SSID);
        isSupportNetWork = getIntent().getBooleanExtra(SUPPORT_NET_WORK, true);
        isSupportWifi = getIntent().getBooleanExtra(SUPPORT_WIFI, true);
        LogUtil.debugLog(TAG, "serialNo = " + serialNo + ",mVerifyCode = " + mVerifyCode + ",wifiSSID = " + wifiSSID + ",isSupportNetWork " + isSupportNetWork
                + ",isSupportWifi " + isSupportWifi + ",isFromDeviceSetting = " + ",deviceType=" + deviceType);
        mMsgHandler = new MessageHandler();
        mLocalInfo = LocalInfo.getInstance();

        WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
        // 路由器的mac地址
        mac = (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
        if (mWifiInfo != null) {
            speed = mWifiInfo.getLinkSpeed();
            strength = mWifiInfo.getRssi();
        }
    }

    /**
     * 这里对方法做描述
     *
     * @see
     * @since V1.0
     */
    private void findViews() {
        btnBack = findViewById(R.id.btnBack);
        btnCancel = findViewById(R.id.cancel_btn);
        tvTitle = (TextView) findViewById(R.id.tvTitle);

        addCameraContainer = findViewById(R.id.addCameraContainer);
        lineConnectContainer = findViewById(R.id.lineConnectContainer);
        tvStatus = (TextView) findViewById(R.id.tvStatus);

        btnRetry = (TextView) findViewById(R.id.btnRetry);
        btnLineConnect = (Button) findViewById(R.id.btnLineConnet);
        btnLineConnetOk = findViewById(R.id.btnLineConnetOk);
        imgAnimation = (ImageView) findViewById(R.id.imgAnimation);
        btnFinish = findViewById(R.id.btnFinish);
        ckbCloundService = (CheckBox) findViewById(R.id.ckbCloundService);
        tvMore = findViewById(R.id.tvMore);
        llyCloundService = findViewById(R.id.llyCloundService);

        connectStateContainer = findViewById(R.id.connectStateContainer);
        llyStatus1 = findViewById(R.id.llyStatus1);
        llyStatus2 = findViewById(R.id.llyStatus2);
        llyStatus3 = findViewById(R.id.llyStatus3);
        helpTop = findViewById(R.id.helpTop);
        help = findViewById(R.id.help);
        tvDeviceWifiConfigTip = findViewById(R.id.tvDeviceWifiConfigTip);
        tvSuccess = findViewById(R.id.tvSuccess);
        // mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        // mWaitDlg.setWaitText(getResources().getString(R.string.start_cloud));
        // mWaitDlg.setCancelable(false);
    }

    private Handler timerHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (isFinishing()) {
                        return;
                    }
                    TextView timer = (TextView) msg.obj;
                    int now = Integer.parseInt(timer.getText().toString()) - 1;
                    if (now >= 0) {
                        timer.setText("" + now);
                        Message newMsg = obtainMessage();
                        newMsg.what = 0;
                        newMsg.obj = timer;
                        sendMessageDelayed(newMsg, 1000);
                    }
                    break;

                default:
                    break;
            }
        }
    };

    /**
     * 是否为有线连接
     */
    private boolean isLineConnecting;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
//        mTriggerHelper.onActivityResult(requestCode, resultCode, intent);
    }

    private void showStatus(int status) {
        connectStateContainer.setVisibility(View.VISIBLE);
        TextView tip = (TextView) llyStatus1.findViewById(R.id.tip);
        View successIcon = llyStatus1.findViewById(R.id.successIcon);
        final TextView timer = (TextView) llyStatus1.findViewById(R.id.timer);
        TextView tip2 = (TextView) llyStatus2.findViewById(R.id.tip);
        View successIcon2 = llyStatus2.findViewById(R.id.successIcon);
        final TextView timer2 = (TextView) llyStatus2.findViewById(R.id.timer);
        TextView tip3 = (TextView) llyStatus3.findViewById(R.id.tip);
        View successIcon3 = llyStatus3.findViewById(R.id.successIcon);
        final TextView timer3 = (TextView) llyStatus3.findViewById(R.id.timer);
        llyStatus1.setVisibility(View.VISIBLE);
        llyStatus2.setVisibility(View.VISIBLE);
        llyStatus3.setVisibility(View.VISIBLE);
        successIcon.setVisibility(View.INVISIBLE);
        successIcon2.setVisibility(View.INVISIBLE);
        successIcon3.setVisibility(View.INVISIBLE);
        timer.setVisibility(View.INVISIBLE);
        timer2.setVisibility(View.INVISIBLE);
        timer3.setVisibility(View.INVISIBLE);
        tip.setText(R.string.auto_wifi_tip_connecting_wifi);
        tip2.setText(R.string.auto_wifi_tip_connecting_server);
        tip3.setText(R.string.auto_wifi_tip_binding_account);
        tip.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.botton_text_size)));
        tip2.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.botton_text_size)));
        tip3.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.botton_text_size)));
        tip.setTextColor(getResources().getColor(R.color.upgrade_gray));
        tip2.setTextColor(getResources().getColor(R.color.upgrade_gray));
        tip3.setTextColor(getResources().getColor(R.color.upgrade_gray));
        tip3.setVisibility(View.VISIBLE);
        // 连接wifi
        if (STATUS_WIFI_CONNETCTING == status) {
            tip.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.tab_text_size)));
            tip.setTextColor(getResources().getColor(R.color.black));
            tip.setText(R.string.auto_wifi_tip_connecting_wifi_ing);
            timer.setVisibility(View.VISIBLE);

            timer.setText(MAX_TIME_STEP_ONE_WIFI + "");
            timer2.setText(MAX_TIME_STEP_THREE_ADD + "");
            timer3.setText(MAX_TIME_STEP_THREE_ADD + "");

            Message msg = timerHandler.obtainMessage();
            msg.what = 0;
            msg.obj = timer;
            timerHandler.sendMessageDelayed(msg, 1000);

        } else if (STATUS_REGISTING == status) {
            timer2.setText(MAX_TIME_STEP_TWO_REGIST + "");
            timer3.setText(MAX_TIME_STEP_THREE_ADD + "");
            successIcon.setVisibility(View.VISIBLE);
            tip2.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.tab_text_size)));
            tip2.setTextColor(getResources().getColor(R.color.black));
            tip.setText(R.string.auto_wifi_tip_connecting_wifi_ok);
            tip2.setText(R.string.auto_wifi_tip_connecting_server_ing);
            timer2.setVisibility(View.VISIBLE);
            Message msg = timerHandler.obtainMessage();
            msg.what = 0;
            msg.obj = timer2;
            timerHandler.sendMessageDelayed(msg, 1000);

        } else if (STATUS_ADDING_CAMERA == status) {
            timer3.setText(MAX_TIME_STEP_THREE_ADD + "");
            if (isLineConnecting) {
                llyStatus1.setVisibility(View.GONE);
                llyStatus2.setVisibility(View.GONE);
            }
            successIcon.setVisibility(View.VISIBLE);
            successIcon2.setVisibility(View.VISIBLE);
            successIcon3.setVisibility(View.INVISIBLE);
            tip3.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.tab_text_size)));
            tip3.setTextColor(getResources().getColor(R.color.black));
            tip.setText(R.string.auto_wifi_tip_connecting_wifi_ok);
            tip2.setText(R.string.auto_wifi_tip_connecting_server_ok);
            tip3.setText(R.string.auto_wifi_tip_binding_account_ing);
            timer3.setVisibility(View.VISIBLE);
            //
            Message msg = timerHandler.obtainMessage();
            msg.what = 0;
            msg.obj = timer3;
            timerHandler.sendMessageDelayed(msg, 1000);
            // } else if (STATUS_LINE_CONNECTING == status) {
            // connectStateContainer.setVisibility(View.VISIBLE);
            // llyStatus1.setVisibility(View.GONE);
            // llyStatus2.setVisibility(View.GONE);
            // llyStatus3.setVisibility(View.VISIBLE);
            // tip3.setTextSize(Utils.px2dip(this, getResources().getDimension(R.dimen.twenty)));
            // tip3.setTextColor(getResources().getColor(R.color.black));
            // tip3.setText(R.string.auto_wifi_tip_binding_account_ing);
            // timer3.setVisibility(View.VISIBLE);
            // Message msg = timerHandler.obtainMessage();
            // timerHandler.sendMessage(msg);
            // msg.what = 0;
            // msg.obj = timer3;
            // timerHandler.sendMessageDelayed(msg, 1000);
        } else if (STATUS_ADD_CAMERA_SUCCESS == status || ERROR_WIFI_CONNECT == status || ERROR_REGIST == status
                || ERROR_ADD_CAMERA == status) {
            connectStateContainer.setVisibility(View.GONE);

        } else {

        }

    }

    /**
     * 这里对方法做描述
     *
     * @see
     * @since V1.0
     */
    private void initUI() {
        if (fromPage == FROM_PAGE_SERIES_NUM_SEARCH_ACTIVITY) {
            tvTitle.setText(R.string.auto_wifi_title_add_device);
        } else {
            // 一切为了转圈
            // if (TextUtils.isEmpty(deviceType)) {
            tvTitle.setText(R.string.auto_wifi_title_add_device2);
            // } else {
            // tvTitle.setText(R.string.auto_wifi_title_add_device1);
            // }
        }
    }

    /**
     * 这里对方法做描述
     *
     * @see
     * @since V1.0
     */
    private void setListener() {
        btnBack.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnLineConnect.setOnClickListener(this);
        btnLineConnetOk.setOnClickListener(this);
        btnRetry.setOnClickListener(this);
        btnFinish.setOnClickListener(this);
        tvMore.setOnClickListener(this);
        help.setOnClickListener(this);
    }

    /**
     * 添加摄像头的操作
     *
     * @see
     * @since V1.8.2
     */
    private void connectCamera() {
        changeStatuss(STATUS_WIFI_CONNETCTING);
    }

    /**
     * 开始连接wifi状态,发送bonjour信息,配置超时信息
     *
     * @see
     * @since V1.8.2
     */
    private void start() {

        isWifiConnected = false;
        isPlatConnected = false;
        isWifiOkBonjourget = false;
        isPlatBonjourget = false;
        // 检测 提前5秒搜索
        LogUtil.i(TAG, "in start: startOvertimeTimer");
        startOvertimeTimer((MAX_TIME_STEP_ONE_WIFI - 5) * 1000, new Runnable() {
            public void run() {
                final Runnable success = new Runnable() {
                    public void run() {
                        if (isPlatConnected) {
                            return;
                        }
                        // save wifipassword
                        if (!isLineConnecting && !TextUtils.isEmpty(mac) && !"NULL".equals(mac)) {
//                            LocalInfo.getInstance().setWifiPassword(mac, wifiPassword);
                        }
                        isPlatConnected = true;
                        t4 = System.currentTimeMillis();
                        changeStatuss(STATUS_ADDING_CAMERA);
                        LogUtil.debugLog(TAG, "start 超时从服务器获取设备信息成功");
                    }
                };
                final Runnable fail = new Runnable() {
                    public void run() {
                        t4 = System.currentTimeMillis();
                        LogUtil.debugLog(TAG, "超时从服务器获取设备信息失败");
                        addCameraFailed(isWifiOkBonjourget ? ERROR_REGIST : ERROR_WIFI_CONNECT, searchErrorCode);
                    }
                };

                Thread thr = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LogUtil.i(TAG, "in start, begin probeDeviceInfo");
                        int result = probeDeviceInfo(serialNo);

                        LogUtil.i(TAG, "in start, got probeDeviceInfo");
                        if (result == 0 && mEZProbeDeviceInfo != null) {
                            LogUtil.i(TAG, "in start, probeDeviceInfo success," + mEZProbeDeviceInfo);
                            runOnUiThread(success);
                            // TODO
                        } else if (result == ErrorCode.ERROR_WEB_DEVICE_ONLINE_NOT_ADD) {
                            LogUtil.i(TAG, "in start, probeDeviceInfo error:ERROR_WEB_DIVICE_ONLINE_NOT_ADD");
                            runOnUiThread(success);
                        } else {
                            LogUtil.i(TAG, "in start, probeDeviceInfo camera not online");
                            runOnUiThread(fail);
                        }
                    }
                });
                thr.start();
            }
        });
        EzvizApplication.getOpenSDK().stopConfigWiFi();
        EzvizApplication.getOpenSDK().startConfigWifi(AutoWifiConnectingActivity.this, serialNo, wifiSSID, wifiPassword, mEZStartConfigWifiCallback);
    }

    /**
     * 停止连接wifi，注册设备
     *
     * @see
     * @since V1.8.2
     */
    private synchronized void stopWifiConfigOnThread() {

        // 停止配置，停止bonjour服务
        new Thread(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                EzvizApplication.getOpenSDK().stopConfigWiFi();
                LogUtil.debugLog(TAG, "stopBonjourOnThread .cost time = "
                        + (System.currentTimeMillis() - startTime) + "ms");
            }
        }).start();
        LogUtil.debugLog(TAG, "stopBonjourOnThread ..................");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                onBackPressed();

                break;
            case R.id.cancel_btn:
                cancelOnClick();
                break;
            case R.id.btnRetry:
                retryOnclick();
                break;
            case R.id.btnLineConnet:
                lineConnectClick();
                break;
            case R.id.btnLineConnetOk:
                lineConnectOkClick();
                break;
            case R.id.btnFinish:
                finishOnClick();
                break;
            case R.id.tvMore:
                moreOnClick();
                break;
            case R.id.help:
                helpOnclick();
                break;

            default:
                break;
        }
    }

    /**
     * 这里对方法做描述
     *
     * @see
     * @since V2.4
     */
    private void helpOnclick() {
//        WebUtils.openQuestionHelp(this, HelpConsts.HOW_SOLVE_DEVICE_WIFI_SETTING);
    }

    /**
     * 更多点击的处理
     *
     * @see
     * @since V1.8.2
     */
    private void moreOnClick() {
//        WebUtils.openYsCloudIntro(this);
    }

    /**
     * 完成按钮的处理
     *
     * @see
     * @since V1.8.2
     */
    private void finishOnClick() {
        // 云存储开关,设备不在线或是不是最新的设备或是用户没有开启云存储服务是看不到这个控件的
        if (llyCloundService.getVisibility() == View.VISIBLE && ckbCloundService.isChecked()) {
            enableCloudStoryed();
        } else {
            closeActivity();
        }
    }

    /**
     * 开启 云存储 是否开启云存储功能
     *
     * @throws
     */
    private void enableCloudStoryed() {
        // 本地网络检测
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            showToast(R.string.save_encrypt_password_fail_network_exception);
            return;
        }

        // mWaitDlg.show();
        showWaitDialog(R.string.start_cloud);

        new Thread() {
            @Override
            public void run() {
            }

            ;
        }.start();

    }

    private void cancelOnClick() {
        btnCancel.setVisibility(View.GONE);
        lineConnectContainer.setVisibility(View.GONE);
        addCameraContainer.setVisibility(View.VISIBLE);

        // if (TextUtils.isEmpty(mVerifyCode)) {
        tvTitle.setText(R.string.auto_wifi_title_add_device2);
        // } else {
        // tvTitle.setText(R.string.auto_wifi_title_add_device1);
        // }
        helpTop.setVisibility(View.VISIBLE);
        help.setVisibility(View.VISIBLE);
    }

    /**
     * 重试按钮点击处理
     *
     * @see
     * @since V1.8.2
     */
    private void retryOnclick() {
        helpTop.setVisibility(View.GONE);
        help.setVisibility(View.GONE);
        switch (errorStep) {
            case ERROR_WIFI_CONNECT:
                changeStatuss(STATUS_WIFI_CONNETCTING);
                break;
            case ERROR_REGIST:
                changeStatuss(STATUS_ADDING_CAMERA);
                break;
            case ERROR_ADD_CAMERA:
                recordConfigStartTime = System.currentTimeMillis();
                changeStatuss(STATUS_ADDING_CAMERA);
                break;
            default:
                break;
        }
    }

    /**
     * 有线连接按钮点击处理
     *
     * @see
     * @since V1.8.2
     */
    private void lineConnectClick() {
        helpTop.setVisibility(View.GONE);
        help.setVisibility(View.GONE);
        connectStateContainer.setVisibility(View.GONE);
        btnCancel.setVisibility(View.VISIBLE);
        lineConnectContainer.setVisibility(View.VISIBLE);

        if (btnLineConnect.getVisibility() == View.VISIBLE) {
            tvTitle.setText(R.string.auto_wifi_line_connect_title);
        } else if (TextUtils.isEmpty(deviceType)) {
            tvTitle.setText(R.string.auto_wifi_network_add_device2);
        } else {
            tvTitle.setText(R.string.auto_wifi_network_add_device1);
        }
        addCameraContainer.setVisibility(View.GONE);
    }

    /**
     * 已经连接好按钮点击处理
     *
     * @see
     * @since V1.8.2
     */
    private void lineConnectOkClick() {
        isLineConnecting = true;
        cancelOnClick();
        help.setVisibility(View.GONE);
        helpTop.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        btnLineConnect.setVisibility(View.GONE);
        changeStatuss(STATUS_ADDING_CAMERA);
        // 一切为了转圈
        // if (TextUtils.isEmpty(deviceType)) {
        tvTitle.setText(R.string.auto_wifi_title_add_device2);
        // } else {
        // tvTitle.setText(R.string.auto_wifi_title_add_device1);
        // }
    }

    /**
     * 弹出对话确认是否退出
     *
     * @see
     * @since V1.8.2
     */
    private void showConfirmDialog() {
        new AlertDialog.Builder(this).setMessage(R.string.auto_wifi_dialog_connecting_msg)
                .setPositiveButton(R.string.update_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).setNegativeButton(R.string.wait, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }

    /**
     * 改变状态，改变文字
     *
     * @param Status
     * @see
     * @since V1.8.2
     */
    private void changeStatuss(int Status) {
        tvStatus.setVisibility(View.GONE);
        tvStatus.setText("");
        switch (Status) {
            case STATUS_WIFI_CONNETCTING:
                imgAnimation.setVisibility(View.VISIBLE);
                tvStatus.setText(R.string.auto_wifi_connecting_msg1);
                imgAnimation.setImageResource(R.drawable.connect_wifi_bg);
                animWaiting = (AnimationDrawable) imgAnimation.getDrawable();
                animWaiting.start();
                btnRetry.setVisibility(View.GONE);
                btnLineConnect.setVisibility(View.GONE);
                showStatus(STATUS_WIFI_CONNETCTING);
                recordConfigStartTime = System.currentTimeMillis();

                t1 = System.currentTimeMillis();
                t2 = 0;
                t3 = 0;
                t4 = 0;
                t5 = 0;
                searchErrorCode = 0;
                addCameraError = -1;
                start();
                break;
            case STATUS_REGISTING:
                LogUtil.i(TAG, "change status to REGISTING");
                // tvStatus.setText(R.string.auto_wifi_connecting_msg2);
                // if (isFromDeviceSetting) {
                // tvStatus.setText(R.string.device_wifi_connecting);
                // imgAnimation.setImageResource(R.drawable.divce_config_wifi_wait);
                // } else {
                // }
                // 检测
                cancelOvertimeTimer();
                LogUtil.i(TAG, "in STATUS_REGISTING: startOvertimeTimer");
                startOvertimeTimer((MAX_TIME_STEP_TWO_REGIST - 5) * 1000, new Runnable() {
                    public void run() {
                        EzvizApplication.getOpenSDK().stopConfigWiFi();
                        final Runnable success = new Runnable() {
                            public void run() {
                                if (isPlatConnected) {
                                    return;
                                }
                                // save wifipassword
                                if (!isLineConnecting && !TextUtils.isEmpty(mac) && !"NULL".equals(mac)) {
//                                    LocalInfo.getInstance().setWifiPassword(mac, wifiPassword);
                                }
                                isPlatConnected = true;
                                t4 = System.currentTimeMillis();
                                changeStatuss(STATUS_ADDING_CAMERA);
                                LogUtil.debugLog(TAG, "STATUS_REGISTING 超时从服务器获取设备信息成功");
                            }
                        };
                        final Runnable fail = new Runnable() {
                            public void run() {
                                t4 = System.currentTimeMillis();
                                LogUtil.debugLog(TAG, "超时从服务器获取设备信息失败");
                                addCameraFailed(ERROR_REGIST, searchErrorCode);
                            }
                        };

                        Thread thr = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                LogUtil.i(TAG, "in change status STATUS_REGISTING, begin probeDeviceInfo");
                                int result = probeDeviceInfo(serialNo);

                                LogUtil.i(TAG, "in change status STATUS_REGISTING, got probeDeviceInfo");
                                if (result == 0 && mEZProbeDeviceInfo != null) {
                                    LogUtil.i(TAG, "in change status STATUS_REGISTING, probeDeviceInfo success, " + mEZProbeDeviceInfo);
                                    runOnUiThread(success);
                                } else if (result == ErrorCode.ERROR_WEB_DEVICE_ONLINE_NOT_ADD) {
                                    LogUtil.i(TAG, "in change status STATUS_REGISTING, "
                                            + " probeDeviceInfo error:ERROR_WEB_DIVICE_ONLINE_NOT_ADD");
                                    runOnUiThread(success);
                                } else {
                                    LogUtil.i(TAG, "in change status STATUS_REGISTING, probeDeviceInfo camera not online");
                                    runOnUiThread(fail);
                                }
                            }
                        });
                        thr.start();
                    }
                });
                imgAnimation.setImageResource(R.drawable.register_server_bg);
                animWaiting = (AnimationDrawable) imgAnimation.getDrawable();
                animWaiting.start();
                btnRetry.setVisibility(View.GONE);
                btnLineConnect.setVisibility(View.GONE);
                showStatus(STATUS_REGISTING);
                break;
            case STATUS_ADDING_CAMERA:
                addCameraError = -1;
                tvStatus.setVisibility(View.GONE);
                tvStatus.setText("");
                // tvStatus.setText(R.string.auto_wifi_connecting_msg3);
                // if (isFromDeviceSetting) {
                // tvStatus.setText(R.string.device_wifi_connecting);
                // imgAnimation.setImageResource(R.drawable.divce_config_wifi_wait);
                // } else {
                // }
                imgAnimation.setImageResource(R.drawable.auto_wifi_link_account_bg);
                animWaiting = (AnimationDrawable) imgAnimation.getDrawable();
                animWaiting.start();
                btnRetry.setVisibility(View.GONE);
                btnLineConnect.setVisibility(View.GONE);

                // save wifipassword
                if (!isLineConnecting && !TextUtils.isEmpty(mac) && !"NULL".equals(mac)) {
//                                LocalInfo.getInstance().setWifiPassword(mac, wifiPassword);
                }

                LogUtil.debugLog(TAG, "服务器获取设备信息成功");
                t4 = System.currentTimeMillis();
                // 从设备切换wifi界面来的处理
                addQueryCamera();


                showStatus(STATUS_ADDING_CAMERA);
                break;
            case STATUS_ADD_CAMERA_SUCCESS:
                t5 = System.currentTimeMillis();
                // 记录操作时间
                recordConfigTimeAndError();
                btnFinish.setVisibility(View.VISIBLE);
                boolean bX1orX2 = false;//mDeviceInfoEx.getEnumModel() == DeviceModel.X1 || mDeviceInfoEx.getEnumModel() == DeviceModel.X2;
                if (bX1orX2) {
                    imgAnimation.setImageResource(R.drawable.success_img);
                } else {
                    imgAnimation.setImageResource(R.drawable.success);
                }
                // DeviceModel.A1
                // 是否支持营销wifi，只有support_wifi_2.4G=1的时候才生效：1-支持，0-不支持
                // tvStatus.setVisibility(View.VISIBLE);
//                if (mDeviceInfoEx.getSupportWifiPortal() != DeviceConsts.NOT_SUPPORT) {
//                    // tvStatus.setText(R.string.add_camera_success_tip);
//                }

                showStatus(STATUS_ADD_CAMERA_SUCCESS);
                break;
            default:
                break;
        }
    }

    /**
     * 记录wifi配置个阶段消耗的时间和错误码 <br>
     * 1） 如果t2’或t3’失败，e字段值分别填-2，-3；i字段中的值t3(t2’处失败时),t4,t5可不填写；<br>
     * 2）如果t4’失败，e字段值为查询设备状态返回的错误码；<br>
     * 3） ct字段不用填写； <br>
     * 4） 程序内部有重试，以重试后的操作为准，如查询设备状态失败时会重试一次，则记录重试后的数据；
     *
     * @see
     * @since V1.8.2
     */
    private void recordConfigTimeAndError() {
        // 非有线连接，不是来自添加页面，不是来自设置界面，不是解绑错误
        if (!isLineConnecting && fromPage != FROM_PAGE_SERIES_NUM_SEARCH_ACTIVITY && !isUnbindDeviceError) {
        }
    }

    /**
     * 开启超时
     *
     * @param time
     * @see
     * @since V1.8.2
     */
    private void startOvertimeTimer(long time, final Runnable run) {
        LogUtil.i(TAG, "Enter startOvertimeTimer: " + run);

        if (overTimeTimer != null) {
            LogUtil.i(TAG, " overTimeTimer.cancel: " + overTimeTimer);
            overTimeTimer.cancel();
            overTimeTimer = null;
        }
        overTimeTimer = new Timer();
        overTimeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LogUtil.debugLog(TAG, "startOvertimeTimer");
                runOnUiThread(run);
            }
        }, time);
        LogUtil.i(TAG, " startOvertimeTimer: timer:" + overTimeTimer + " runnable:" + run);
    }

    /**
     * 关闭超时
     *
     * @see
     * @since V1.8.2
     */
    private void cancelOvertimeTimer() {
        LogUtil.i(TAG, "Enter cancelOvertimeTimer: ");
        if (overTimeTimer != null) {
            LogUtil.i(TAG, " cancelOvertimeTimer: " + overTimeTimer);
            overTimeTimer.cancel();
        }
    }

    private int addCameraError = -1;

    /**
     * 连接失败的处理
     *
     * @param errorStep
     * @see
     * @since V1.8.2
     */
    private void addCameraFailed(int errorStep, int errorCode) {
        this.errorStep = errorStep;
        addCameraError = errorCode;
        tvStatus.setVisibility(View.VISIBLE);
        // 失败了清除 读秒即时
        if (timerHandler != null) {
            timerHandler.removeMessages(0);
        }
        switch (errorStep) {
            case ERROR_WIFI_CONNECT:
                showStatus(ERROR_WIFI_CONNECT);
                btnRetry.setVisibility(View.VISIBLE);
                // 支持有线连接才显示
                if (isSupportNetWork) {
                    btnLineConnect.setVisibility(View.VISIBLE);
                }
                // 来源 设备切换wifi
                btnLineConnect.setText(R.string.ez_auto_wifi_line_connect);
                imgAnimation.setImageResource(R.drawable.failure_wifi);
                tvStatus.setText(R.string.ez_auto_wifi_connecting_failed);
                helpTop.setVisibility(View.VISIBLE);
                help.setVisibility(View.VISIBLE);
                // stopBonjourOnThread();
                recordConfigTimeAndError();
                break;
            case ERROR_REGIST:
                showStatus(ERROR_REGIST);
                // stopBonjourOnThread();
                btnRetry.setVisibility(View.VISIBLE);
                btnLineConnect.setVisibility(View.GONE);
                imgAnimation.setImageResource(R.drawable.failure_server);
                tvStatus.setText(R.string.auto_wifi_register_failed);
                recordConfigTimeAndError();
                break;
            case ERROR_ADD_CAMERA:
                showStatus(ERROR_ADD_CAMERA);
                btnRetry.setVisibility(View.VISIBLE);
                btnLineConnect.setVisibility(View.GONE);
                imgAnimation.setImageResource(R.drawable.failure_account);
                if (errorCode == ErrorCode.ERROR_WEB_DEVICE_EXCEPTION) {
                    // 设备异常
                    tvStatus.setText(getString(R.string.auto_wifi_add_device_failed) + "("
                            + getString(R.string.device_error) + ")");
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_ADD_OWN_AGAIN) {
                    // 设备已被自己添加
                    // showToast(R.string.query_camera_fail_repeat_error);
                    tvStatus.setText(getString(R.string.auto_wifi_add_device_failed2) + "("
                            + getString(R.string.auto_wifi_device_you_added_already) + ")");
                    btnRetry.setVisibility(View.GONE);
                    btnFinish.setVisibility(View.VISIBLE);
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_ADDED) {
                    // TODO
                    // 设备已经添加
                    tvStatus.setText(getString(R.string.auto_wifi_add_device_failed2) + "("
                            + getString(R.string.auto_wifi_device_added_already) + ")");
                    btnRetry.setVisibility(View.GONE);
                    btnFinish.setVisibility(View.VISIBLE);
//                } else if (errorCode == ErrorCode.ERROR_WEB_NET_EXCEPTION) {
//                    // 网络异常
//                    tvStatus.setText(getString(R.string.auto_wifi_add_device_failed2) + "("
//                            + getString(R.string.network_exception) + ")");
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_NOT_ONLINE) {
                    // 设备不在线
                    tvStatus.setText(R.string.add_device_failed_not_online);
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_VERIFY_CODE_ERROR) {
                    // 验证码错误
                    tvStatus.setText(getString(R.string.auto_wifi_add_device_failed2) + "("
                            + getString(R.string.verify_code_error) + ")");
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_NOT_EXIT) {
                    // 设备不存在
                    tvStatus.setText(R.string.auto_wifi_device_not_exist);
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_ADDED_BT_OTHER) {
                    // 设备已被其他人添加
                    tvStatus.setText(R.string.auto_wifi_device_added_by_others);
                    btnRetry.setVisibility(View.GONE);
                    btnFinish.setVisibility(View.VISIBLE);
                } else if (errorCode == ErrorCode.ERROR_WEB_DEVICE_OFFLINE_NOT_ADD) {
                    // 设备不在线 未添加
                    tvStatus.setText(R.string.ez_add_device_failed_not_online);
                } else if (errorCode > 0) {
                    tvStatus.setText(getErrorTip(R.string.auto_wifi_add_device_failed, errorCode));
                } else {
                    tvStatus.setText(R.string.auto_wifi_add_device_failed);
                }
                recordConfigTimeAndError();
                break;
            default:
                break;
        }
    }

    /**
     * 这里对方法做描述
     *
     * @see
     * @since V1.0
     */
    public void addQueryCamera() {
        // 从设备切换wifi界面来的处理
//        if (mSearchDevice == null || mSearchDevice.getAvailableChannelCount() <= 0) {
//            // 将摄像头添加到用户下面
//            // showToast("该设备已经被添加");
//            LogUtil.debugLog(TAG, "该设备已被添加");
////            addCameraFailed(ERROR_ADD_CAMERA, ErrorCode.ERROR_WEB_SET_EMAIL_REPEAT_ERROR);
//            return;
//        }
        LogUtil.debugLog(TAG, "添加摄像头： mVerifyCode = " + mVerifyCode);
        //mj        
        //boolean stub = mSearchDevice.getReleaseVersion() != null && !mSearchDevice.getReleaseVersion().contains("DEFAULT");
        boolean stub = false;
        if (stub) {
            if (!TextUtils.isEmpty(mVerifyCode)) {
                // 第一次点击如果已经有验证码，则直接添加
                addQueryCameraAddVerifyCode();
            } else {
                LogUtil.debugLog(TAG, "添加摄像头： showInputCameraVerifyCodeDlg mVerifyCode = " + mVerifyCode);
                showInputCameraVerifyCodeDlg();
            }

        } else {
            String password = mLocalInfo.getPassword();
            // LogUtil.debugLog(TAG, "添加摄像头 else password = " + password);
            // mj password = null
            // if (password != null) {
            LogUtil.i(TAG, "添加摄像头： password is null?" + (TextUtils.isEmpty(password) ? "yes" : "no"));
            if (!TextUtils.isEmpty(password) || !(TextUtils.isEmpty(mVerifyCode))) {
                if (mVerifyCode == null) {
                    mVerifyCode = password;
                }
                addQueryCameraAddVerifyCode();
            } else {
                showInputCameraPswDlg();
            }
        }
    }

    private void addQueryCameraAddVerifyCode() {
        // 本地网络检测
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            showToast(R.string.add_camera_fail_network_exception);
            return;
        }
        new Thread() {
            public void run() {
                int count = ADD_CAMERA_TIMES;
                while (count > 0) {
                    // 增加手机客户端操作信息记录
                    int addCameraErrorCode = 0;
                    try {
                        EzvizApplication.getOpenSDK().addDevice(serialNo, mVerifyCode);

//                        mDeviceInfoEx = CameraMgtCtrl.addCamera(mSearchDevice.getSubSerial(), mVerifyCode);

                        /***********如有需要开发者需要自己保存此验证码***********/
//                        if (!TextUtils.isEmpty(mVerifyCode)) {
//                            //保存密码
//                            EzvizApplication.getOpenSDK().setValidateCode(mVerifyCode, serialNo);
//                        }

                        sendMessage(MSG_ADD_CAMERA_SUCCESS);
                        count = -1;
                    } catch (BaseException e) {
                        e.printStackTrace();

                        ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                        LogUtil.debugLog(TAG, errorInfo.toString());

                        LogUtil.infoLog(TAG, "" + e);
                        count--;
                        addCameraErrorCode = e.getErrorCode();
                        if (count <= 0) {
                            sendMessage(MSG_ADD_CAMERA_FAIL, errorInfo.errorCode);
                        }
                    }
                    /*try {
                        mDeviceInfoEx = CameraMgtCtrl.addCamera(mSearchDevice.getSubSerial(), mVerifyCode);

                        if (!TextUtils.isEmpty(mVerifyCode) && mLocalInfo != null && mDeviceInfoEx != null) {
                            mDeviceInfoEx.setPassword(mVerifyCode);
                            DevPwdUtil.saveVerifyCodePwd(AutoWifiConnectingActivity.this, mDeviceInfoEx.getDeviceID(),
                                    mVerifyCode, mLocalInfo.getRealUserName());
                        }

                        // 添加成功后返回到设备列表的时候就会自动刷新最新的数据
                        if (mLocalInfo != null) {
                            mLocalInfo.setAddDeviceFlag(true);
                        }

                        sendMessage(MSG_ADD_CAMERA_SUCCESS);
                        count = -1;
                    } catch (VideoGoNetSDKException e) {
                        count--;
                        addCameraErrorCode = e.getErrorCode();
                        if (count <= 0) {
                            sendMessage(MSG_ADD_CAMERA_FAIL, e.getErrorCode());
                        }
                        LogUtil.errorLog(
                                TAG,
                                "add camera:" + mSearchDevice.getSubSerial() + " failed errorCode = "
                                        + e.getErrorCode());
                    } catch (ExtraException e1) {
                        count--;
                        addCameraErrorCode = e1.getErrorCode();
                        if (count <= 0) {
                            sendMessage(MSG_ADD_CAMERA_FAIL, e1.getErrorCode());
                        }
                        LogUtil.errorLog(TAG,
                                "add camera:" + mSearchDevice.getSubSerial() + " fail errorCode = " + e1.getErrorCode());
                    }*/

                }
            }
        }.start();
    }

    /**
     * 在此对类做相应的描述
     *
     * @author Admin
     * @data 2012-9-24
     */
    private class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ADD_CAMERA_SUCCESS:
                    handleAddCameraSuccess();
                    break;
                case MSG_ADD_CAMERA_FAIL:
                    handleAddCameraFail(msg.arg1);
                    break;
                case MSG_OPEN_CLOUD_STORYED_SUCCESS:
                    openCloudSuccess();
                    break;
                case MSG_OPEN_CLOUD_STORYED_FAIL:
                    openCloudFailed(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 这里对方法做描述
     *
     * @param errCode
     * @see
     * @since V1.0
     */
    private void handleAddCameraFail(int errCode) {
        // 各种添加设备失败错误处理
/*        switch (errCode) {
            case VideoGoNetSDKException.VIDEOGONETSDK_NETWORD_EXCEPTION:
                showToast(R.string.add_camera_fail_network_exception);
                break;
            case VideoGoNetSDKException.VIDEOGONETSDK_SESSION_ERROR:
                break;
            case VideoGoNetSDKException.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                break;
            case VideoGoNetSDKException.VIDEOGONETSDK_SERVER_EXCEPTION:
                break;
            case VideoGoNetSDKException.VIDEOGONETSDK_ADD_CAMERA_VERCODE_ERROR:
                LogUtil.debugLog(TAG, "添加摄像头 失败 验证码错误 = " + errCode);
                // 验证码错误
                mVerifyCode = "";
                break;
            case VideoGoNetSDKException.VIDEOGONETSDK_DEVICE_SO_TIMEOUT:
                break;
            case VideoGoNetSDKException.VIDEOGONETSDK_WEB_DEVICE_NO_OUT_LIMIT_ERROR:
                // 处理设备添加数目达到上限
                break;
            case VideoGoNetSDKException.ERROR_WEB_DEVICE_NOTEXIT:
                break;
            default:
                break;
        }
*/
        switch (errCode) {
            case 120010:
                LogUtil.debugLog(TAG, "添加摄像头 失败 验证码错误 = " + errCode);
                mVerifyCode = "";
                break;
            default:
                break;
        }
        addCameraFailed(ERROR_ADD_CAMERA, errCode);
    }

    /**
     * 这里对方法做描述
     *
     * @param arg1
     * @see
     * @since V1.8.2
     */
    public void openCloudFailed(int arg1) {
        // mWaitDlg.dismiss();
        dismissWaitDialog();
        LogUtil.errorLog(TAG, "添加云存储失败，错误代号：" + arg1);
        new AlertDialog.Builder(this).setTitle(R.string.enable_cloud_fause)
                .setMessage(R.string.enable_cloud_fause_retry)
                .setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        enableCloudStoryed();
                        // mWaitDlg.show();
                        showWaitDialog(R.string.start_cloud);
                    }
                }).setPositiveButton(R.string.not_now, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                closeActivity();
            }
        }).setCancelable(false).create().show();

    }

    /**
     * 这里对方法做描述
     *
     * @see
     * @since V1.8.2
     */
    public void openCloudSuccess() {
        mDeviceInfoEx.setCloudServiceStatus(1);
        // mWaitDlg.dismiss();
        dismissWaitDialog();
        // showToast(id);
        closeActivity();
    }

    /**
     * 这里对方法做描述
     *
     * @see
     * @since V1.8.2
     */
    public void handleAddCameraSuccess() {
        changeStatuss(STATUS_ADD_CAMERA_SUCCESS);
    }

    /**
     * 这里对方法做描述
     *
     * @param msgCode
     * @see
     * @since V1.0
     */
    private void sendMessage(int msgCode) {
        if (mMsgHandler != null) {
            Message msg = Message.obtain();
            msg.what = msgCode;
            mMsgHandler.sendMessage(msg);
        } else {
            LogUtil.errorLog(TAG, "sendMessage-> mMsgHandler object is null");
        }
    }

    /**
     * 这里对方法做描述
     *
     * @param msgCode
     * @param errorCode
     * @see
     * @since V1.0
     */
    private void sendMessage(int msgCode, int errorCode) {
        if (mMsgHandler != null) {
            Message msg = Message.obtain();
            msg.what = msgCode;
            msg.arg1 = errorCode;
            mMsgHandler.sendMessage(msg);
        } else {
            LogUtil.errorLog(TAG, "sendMessage-> mMsgHandler object is null");
        }
    }

    ;

    /**
     * 提示用户输入验证码
     *
     * @see
     * @since V1.0
     */
    private void showInputCameraVerifyCodeDlg() {
        mVerifyCode = null;
        // 从布局中加载视图
        LayoutInflater factory = LayoutInflater.from(this);
        final View passwordErrorLayout = factory.inflate(R.layout.verifycode_layout, null);
        final EditText newPassword = (EditText) passwordErrorLayout.findViewById(R.id.new_password);
        newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        newPassword.setFocusable(true);

        final TextView message1 = (TextView) passwordErrorLayout.findViewById(R.id.message1);
        // StringBuffer sb = new StringBuffer();
        // sb.append("<font color=White >").append(getString(R.string.realplay_verifycode_error_message0))
        // .append("</font>").append("<font color= White>").append(mSearchDevice.getSubSerial()).append("</font>")
        // .append("<font color=White >").append(getString(R.string.realplay_verifycode_error_message1))
        // .append("</font>").append("<font color= White>")
        // .append(getString(R.string.realplay_verifycode_error_message2)+getString(R.string.realplay_verifycode_error_message3)).append("</font>");
        message1.setText(R.string.realplay_verifycode_error_message0);

        // 使用布局中的视图创建AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.camera_detail_verifycode_error_title);
        builder.setView(passwordErrorLayout);
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(newPassword.getWindowToken(), 0);
            }
        });

        builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mVerifyCode = newPassword.getText().toString();
                if (verifyLegality(mVerifyCode)) {
                    addQueryCameraAddVerifyCode();
                } else {
                    // 不合法则置空
                    mVerifyCode = null;
                }
            }
        });
        if (!isFinishing()) {
            Dialog dialog = builder.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.show();
        }
    }

    /**
     * 判断验证码的合法性 wnn 2013-5-23 21:48:25
     *
     * @return 设定文件
     * @throws
     */
    private boolean verifyLegality(String verifyCodeString) {
        if (verifyCodeString.equalsIgnoreCase("")) {
            showInputCameraVerifyCodeDlg();
            return false;
        }
        return true;
    }

    /**
     * 这里对方法做描述
     *
     * @see
     * @since V1.0
     */
    private void showInputCameraPswDlg() {
        // 从布局中加载视图
        LayoutInflater factory = LayoutInflater.from(this);
        final View passwordErrorLayout = factory.inflate(R.layout.password_error_layout, null);
        final EditText newPassword = (EditText) passwordErrorLayout.findViewById(R.id.new_password);
        newPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(Constant.PSW_MAX_LENGTH)});

        final TextView message1 = (TextView) passwordErrorLayout.findViewById(R.id.message1);
        message1.setText(getString(R.string.realplay_password_error_message1));

        mVerifyCode = null;

        // TextView titleView = new TextView(this);
        // titleView.setTextSize(R.dimen.button_text_size);
        // titleView.setText(getString(R.string.serial_add_password_error_title));
        // 使用布局中的视图创建AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.serial_add_password_error_title);
        builder.setView(passwordErrorLayout);
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(newPassword.getWindowToken(), 0);
            }
        });

        builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // 确定修改名称
                String password = newPassword.getText().toString();
                if (pswLegality(password)) {
                    mVerifyCode = newPassword.getText().toString();
                    addQueryCameraAddVerifyCode();
                }
            }
        });
        if (!isFinishing()) {
            Dialog dialog = builder.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            dialog.show();
        }
    }

    /**
     * 判断密码的合法性 wnn 2013-5-23 21:48:25
     *
     * @return 设定文件
     * @throws
     */
    private boolean pswLegality(String pswString) {
        if (pswString.equalsIgnoreCase("")) {
            showInputCameraPswDlg();
            return false;
        }
        return true;
    }

    /**
     * 关闭当前画面，返回主页面
     *
     * @see
     * @since V1.8.2
     */
    private void closeActivity() {
        // start the EZCameraList here
        Intent intent = new Intent(this, EZCameraListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (tvDeviceWifiConfigTip.getVisibility() == View.VISIBLE) {
/*mj            CustomApplication obg = (CustomApplication) getApplication();
            HashMap<String, Activity> activitis = obg.getSingleActivities();

            if (activitis.get(AutoWifiNetConfigActivity.class.getName()) != null) {
                activitis.get(AutoWifiNetConfigActivity.class.getName()).finish();
            }
            if (activitis.get(ResetIntroduceActivity.class.getName()) != null) {
                activitis.get(ResetIntroduceActivity.class.getName()).finish();
            }
            finish();*/
            // 已经完成
        } else if (btnFinish.getVisibility() == View.VISIBLE) {
            closeActivity();
            // 有线连接 介绍界面
        } else if (btnCancel.getVisibility() == View.VISIBLE) {
            cancelOnClick();
            // 如果正在配置中（包括有线和无线）
        } else if (connectStateContainer.getVisibility() == View.VISIBLE) {
            showConfirmDialog();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null) {
            timerHandler.removeMessages(0);
        }
        cancelOvertimeTimer();
        stopWifiConfigOnThread();
    }

}
