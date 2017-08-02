package com.videogo.ui.devicelist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.videogo.EzvizApplication;
import com.videogo.RootActivity;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.exception.ExtraException;
import com.videogo.openapi.bean.EZProbeDeviceInfo;
import com.videogo.ui.util.ActivityUtils;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.util.LocalValidate;
import com.videogo.util.LogUtil;
import com.videogo.widget.TitleBar;
import com.videogo.widget.WaitDialog;

import ezviz.ezopensdk.R;

public class SeriesNumSearchActivity extends RootActivity implements OnClickListener/*, OnAuthListener*/ {

    /** 常量说明 */
    private static final String TAG = "SeriesNumSearchActivity";

    protected static final int MSG_QUERY_CAMERA_FAIL = 0;

    protected static final int MSG_QUERY_CAMERA_SUCCESS = 1;

    private static final int MSG_LOCAL_VALIDATE_SERIALNO_FAIL = 8;

    private static final int MSG_LOCAL_VALIDATE_CAMERA_PSW_FAIL = 9;

    private static final int MSG_ADD_CAMERA_SUCCESS = 10;

    private static final int MSG_ADD_CAMERA_FAIL = 12;

    // private static final int SHOW_DIALOG_ADD_FINISHED = 15;

    private static final int SHOW_DIALOG_SET_WIFI = 16;

    /** Bundle中值的 */
    public static final String BUNDLE_TYPE = "type";

    public static final String BUNDE_SERIANO = "SerialNo";

    /** 传递参数中的验证码 */
    public static final String BUNDE_VERYCODE = "very_code";

    public static final String BUNDLE_ISACTIVATED = "activated";

    /** 传递参数中的类型的值 */
    private static final String BUNDE_VERYCODE_VALUE = "old";

    /** 传递参数中的提示 */
    private static final String BUNDE_DIALOG_TIP = "tip";

    /** 修改密码对话框ID */
    private final int MODIFYPSD_FAIL_DIALOG_ID = 25;

    private EditText mSeriesNumberEt = null;

    private MessageHandler mMsgHandler = null;

    private WaitDialog mWaitDlg = null;

    private LocalValidate mLocalValidate = null;

    private String mSerialNoStr = "";

    private View mQueryingCameraRyt;

    private View errorPage;

    private View mCameraListLy;

    private TextView mDeviceName = null;

    private ImageView mDeviceIcon = null;

    private Button mAddButton = null;

    // type - 0 手动输入序列号， type - 1 二维码扫描
    private int mType = 0;

    private Bundle mBundle;

    /** 添加设备输入验证码 */
    private String mVerifyCode = null;

    private boolean mHasShowInputPswDialog = false;

    private LocalInfo mLocalInfo = null;

    private View mBtnNext;

    private View mActivateHint;

    private TextView mTitle;

    private View mInputLinearlayout;

    private TextView mTvStatus;

    private TextView mConnectTip;

    private TextView mFailedMsg;

    private String mDeviceType;
    private DeviceModel mDeviceModel;
    
    private boolean isActivated; // 用来判断是否从激活页面跳转过来的
    
    private EZProbeDeviceInfo mEZProbeDeviceInfo = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_camera_by_series_number_page);

        init();
        initTitleBar();
        findViews();
        initUI();
        setListener();
        getData();
    }

    /**
     * 初始化标题栏
     */
    private void initTitleBar() {
        TitleBar mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitle = mTitleBar.setTitle(R.string.result_txt);
        mTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void init() {
        mLocalValidate = new LocalValidate();
        mMsgHandler = new MessageHandler();

        mWaitDlg = new WaitDialog(SeriesNumSearchActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
        // mWaitDlg.setCancelable(false);

        mBundle = getIntent().getExtras();
        if (mBundle != null) {
            mType = mBundle.getInt(BUNDLE_TYPE);
            if (mType == 0) {
                mSerialNoStr = "";
            } else if (mType == 1) {
                mSerialNoStr = mBundle.getString(BUNDE_SERIANO);
                mVerifyCode = mBundle.getString(BUNDE_VERYCODE);
            }
            isActivated = mBundle.getBoolean(BUNDLE_ISACTIVATED, false);
            mDeviceType = mBundle.getString(AutoWifiNetConfigActivity.DEVICE_TYPE);
            mDeviceModel = DeviceModel.getDeviceModel(mDeviceType);
        }
        LogUtil.debugLog(TAG, "mSerialNoStr = " + mSerialNoStr + ",mVerifyCode = " + mVerifyCode + ",deviceType="
                + mDeviceModel);
        mLocalInfo = LocalInfo.getInstance();

//        mTriggerHelper = new UnbindDeviceTriggerHelper(this, R.id.unbind_button);
    }

    /**
     * 这里对方法做描述
     * 
     * @see
     * @since V1.0
     */
    private void findViews() {
        mSeriesNumberEt = (EditText) findViewById(R.id.seriesNumberEt);

        if (mSerialNoStr != null) {
            mSeriesNumberEt.setText(mSerialNoStr);
        }
        mInputLinearlayout = findViewById(R.id.inputLinearlayout);
        mQueryingCameraRyt = findViewById(R.id.queryingCameraRyt);
        errorPage = findViewById(R.id.errorPage);
        mCameraListLy = findViewById(R.id.cameraListLy);

        mDeviceIcon = (ImageView) findViewById(R.id.deviceIcon);
        mDeviceName = (TextView) findViewById(R.id.deviceName);
        mTvStatus = (TextView) findViewById(R.id.tvStatus);
        mAddButton = (Button) findViewById(R.id.addBtn);
        mBtnNext = findViewById(R.id.btnNext);
        mActivateHint = findViewById(R.id.activateHint);

        mFailedMsg = (TextView) findViewById(R.id.failedMsg);

        mConnectTip = (TextView) findViewById(R.id.connectTip);

        ImageView searchAnim = (ImageView) findViewById(R.id.searchAnim);
        ((AnimationDrawable) searchAnim.getBackground()).start();
    }

    /**
     * 这里对方法做描述
     * 
     * @see
     * @since V1.0
     */
    private void initUI() {
        if (mType == 1) {
            mInputLinearlayout.setVisibility(View.GONE);
        } else {
            showInputSerialNo();
        }
    }

    /**
     * 这里对方法做描述
     * 
     * @see
     * @since V1.0
     */
    private void setListener() {
        mSeriesNumberEt.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constant.SERIAL_NO_LENGTH)});
        mAddButton.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mActivateHint.setOnClickListener(this);
    }

    private void getData() {
        if (mType == 1) {
            searchCameraBySN();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.searchBtn:
                // 只有手动搜索点击的时候才会，清空验证码，二维码过来的不会清空
                // 如果扫描的序列号与输入的序列号一样 那么不清除验证码
                final String serialNo = mSeriesNumberEt.getText().toString().trim();
                if (mSerialNoStr == null || !mSerialNoStr.equals(serialNo)) {
                    mVerifyCode = null;
                    mDeviceType = "";
                    mDeviceModel = null;
                }
                searchCameraBySN();
                break;
            case R.id.addBtn:
                addQueryCamera();
                break;
            case R.id.btnNext:
                Intent intent;
                // demo代码这里只示范wifi配置
                // 判断设备类型 （可以判断只能无线还是有线） 跳转相应的页面
                intent = new Intent(this, AutoWifiPrepareStepOneActivity.class);
                intent.putExtra(BUNDE_SERIANO, mSeriesNumberEt.getText().toString());
                if (mVerifyCode != null) {
                    intent.putExtra(BUNDE_VERYCODE, mVerifyCode);
                }
                intent.putExtra("support_Wifi", true);
                intent.putExtra("support_net_work", true);
                intent.putExtra("device_type", mDeviceType);
                startActivity(intent);
                // 不支持wifi 支持有线连接
                /* }else if (CameraUtil.isSupportNetWork(mDeviceType) || CameraUtil.isSupportNetWork(mDeviceModel)) {
                    intent = new Intent(this, AutoWifiConnectingActivity.class);
                    intent.putExtra(SeriesNumSearchActivity.BUNDE_SERIANO, mSeriesNumberEt.getText().toString());
                    intent.putExtra(SeriesNumSearchActivity.BUNDE_VERYCODE, mVerifyCode);
                    intent.putExtra(ChooseDeviceModeActivity.SUPPORT_WIFI, false);
                    intent.putExtra(ChooseDeviceModeActivity.SUPPORT_NET_WORK, true);
                    startActivity(intent);
                }*/
                break;
            case R.id.myRetry:
                searchCameraBySN();
                break;
            case R.id.activateHint:
                if (ConnectionDetector.getConnectionType(this) != ConnectionDetector.WIFI) {
                    // 配置wifi
                    showWifiRequiredDialog();
                } else {
//                    ActivateActivity.launch(this, mSerialNoStr, mVerifyCode, mType, mDeviceType);
                    // finish();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 提示需要wifi网络
     * 
     * @see
     * @since V1.8.2
     */
    private void showWifiRequiredDialog() {
        new AlertDialog.Builder(this).setTitle(R.string.auto_wifi_dialog_title_wifi_required)
                .setMessage(R.string.please_open_wifi_network_sadp)
                .setNegativeButton(R.string.connect_wlan, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                        // 跳转wifi设置界面
                        if (android.os.Build.VERSION.SDK_INT > 10) {
                            // 3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
                            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                        } else {
                            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                        }
                    }
                }).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setCancelable(false).create().show();
    }

    /**
     * 仅考虑从激活回到这个页面的情况，这个时候数据都是有的，仅需要getData()；
     * 
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mBundle = intent.getExtras();
        if (mBundle != null) {
            if (mBundle.containsKey(IntentConsts.EXTRA_DEVICE_INFO)) {
//                mTriggerHelper.onNewIntent(intent);
                return;
            }

            mType = mBundle.getInt(BUNDLE_TYPE);
            if (mType == 0) {
                mSerialNoStr = "";
            } else if (mType == 1) {
                mSerialNoStr = mBundle.getString(BUNDE_SERIANO);
                mVerifyCode = mBundle.getString(BUNDE_VERYCODE);
            }
            isActivated = mBundle.getBoolean(BUNDLE_ISACTIVATED, false);
            mDeviceType = mBundle.getString(AutoWifiNetConfigActivity.DEVICE_TYPE);
            mDeviceModel = DeviceModel.getDeviceModel(mDeviceType);
            //isFromRouterIntroduce = mBundle.getBoolean(RouterIntroduceActivity.IS_FROM_ROUTER_INTRODUCE);
        }
        getData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.debugLog(TAG, "onDestroy");
    }

    /**
     * 这里对方法做描述
     * 
     * @see
     * @since V1.0
     */
    public void searchCameraBySN() {
        hideKeyBoard();
        final String serialNo = mSeriesNumberEt.getText().toString().trim();
        mSerialNoStr = serialNo; // wwc add
        mLocalValidate = new LocalValidate();
        try {
            mLocalValidate.localValidatSerialNo(serialNo);
        } catch (BaseException e) {
            sendMessage(MSG_LOCAL_VALIDATE_SERIALNO_FAIL, e.getErrorCode());
            LogUtil.errorLog(TAG, "searchCameraBySN-> local validate serial no fail, errCode:" + e.getErrorCode());
            return;
        }

        // 本地网络检测
        if (!ConnectionDetector.isNetworkAvailable(SeriesNumSearchActivity.this)) {
            showErrorPage(R.string.query_camera_fail_network_exception, 0);
            return;
        }

        showQueryingCamera();

        new Thread() {
            public void run() {
                try {
                    mEZProbeDeviceInfo = EzvizApplication.getOpenSDK().probeDeviceInfo(serialNo);
                    sendMessage(MSG_QUERY_CAMERA_SUCCESS);
                    LogUtil.infoLog(TAG, "getCameraInfo success");
                }
                catch (BaseException e) {
                    ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                    LogUtil.debugLog(TAG, errorInfo.toString());

                    sendMessage(MSG_QUERY_CAMERA_FAIL, errorInfo.errorCode);
                    LogUtil.infoLog(TAG, "probeDeviceInfo fail :" + errorInfo);
                    e.printStackTrace();
                }
            }
        }.start();
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

    private void sendMessage(int msgCode, int errorCode) {
        if (mMsgHandler != null) {
            Message msg = Message.obtain();
            msg.what = msgCode;
            msg.arg1 = errorCode;
            mMsgHandler.sendMessage(msg);
        } else {
            LogUtil.errorLog(TAG, "sendMessage-> mMsgHandler object is null");
        }
    };

    /**
     * 在此对类做相应的描述
     * 
     * @author Admin
     * @data 2012-9-24
     */
    class MessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOCAL_VALIDATE_SERIALNO_FAIL:
                    handleLocalValidateSerialNoFail(msg.arg1);
                    break;
                case MSG_LOCAL_VALIDATE_CAMERA_PSW_FAIL:
                    handleLocalValidateCameraPswFail(msg.arg1);
                    break;
                case MSG_QUERY_CAMERA_SUCCESS:
                    handleQueryCameraSuccess();
                    break;
                case MSG_QUERY_CAMERA_FAIL:
                    handleQueryCameraFail(msg.arg1);
                    break;
                case MSG_ADD_CAMERA_SUCCESS:
                    handleAddCameraSuccess();
                    break;
                case MSG_ADD_CAMERA_FAIL:
                    handleAddCameraFail(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 这里对方法做描述
     * 
     * @see
     * @since V1.0
     */
    private void handleAddCameraSuccess() {
        mWaitDlg.dismiss();

        Intent intent = new Intent(SeriesNumSearchActivity.this, AutoWifiConnectingActivity.class);
        intent.putExtra(SeriesNumSearchActivity.BUNDE_SERIANO, mSerialNoStr);
        intent.putExtra(AutoWifiConnectingActivity.FROM_PAGE,
                AutoWifiConnectingActivity.FROM_PAGE_SERIES_NUM_SEARCH_ACTIVITY);
        startActivity(intent);
        
        mHasShowInputPswDialog = false;
    }

    /**
     * 这里对方法做描述
     * 
     * @param errCode
     * @see
     * @since V1.0
     */
    private void handleAddCameraFail(int errCode) {
        mWaitDlg.dismiss();
        mWaitDlg.hide();
        switch (errCode) {
            case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                showToast(R.string.add_camera_fail_network_exception);
                break;
            case ErrorCode.ERROR_TRANSF_ACCESSTOKEN_ERROR:
                ActivityUtils.handleSessionException(SeriesNumSearchActivity.this);
                break;
            case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                ActivityUtils.handleSessionException(SeriesNumSearchActivity.this);
                break;
            case ErrorCode.ERROR_WEB_SERVER_EXCEPTION:
                showToast(R.string.add_camera_fail_server_exception);
                break;
            case ErrorCode.ERROR_WEB_DEVICE_VERIFY_CODE_ERROR:
                mVerifyCode = null;
                {
                    // 验证码合法，但是错误
                    Bundle args = new Bundle();
                    args.putString(BUNDE_DIALOG_TIP, getString(R.string.added_camera_verycode_fail_title_txt));

                    // 弹出提示
                    if (!isFinishing() && mHasShowInputPswDialog) {
                        showDialog(MODIFYPSD_FAIL_DIALOG_ID, args);
                    } else {
                        showInputCameraVerifyCodeDlg();
                    }
                }
                break;
            case ErrorCode.ERROR_WEB_DEVICE_SO_TIMEOUT:
                showToast(R.string.device_so_timeout);
                break;
            case ErrorCode.ERROR_WEB_DEVICE_NOT_EXIT:
                showToast(R.string.query_camera_fail_not_exit);
                break;
            case ErrorCode.ERROR_WEB_DEVICE_NOT_ONLINE:
                showToast(R.string.camera_not_online);
                break;
            case ErrorCode.ERROR_WEB_DEVICE_VALICATECODE_ERROR:
                LogUtil.debugLog(TAG, "添加摄像头 失败 验证码错误 = " + errCode);
                mVerifyCode = "";
                break;
            default:
                showToast(R.string.add_camera_fail_server_exception, errCode);
                LogUtil.errorLog(TAG, "handleAddCameraFail->unkown error, errCode:" + errCode);
                //
                // mVerifyCode = null;
                break;
        }
    }

    /**
     * 这里对方法做描述
     * 
     * @param errCode
     * @see
     * @since V1.0
     */
    private void handleLocalValidateCameraPswFail(int errCode) {
        switch (errCode) {
            case ExtraException.CAMERA_PASSWORD_IS_NULL:
                showToast(R.string.camera_password_is_null);
                break;
            default:
                showToast(R.string.camera_password_error, errCode);
                LogUtil.errorLog(TAG, "handleLocalValidateCameraPswFail-> unkown error, errCode:" + errCode);
                break;
        }
        handleCmaeraPswError();
    }

    /**
     * 处理添加设备失败因为验证码错误
     * 
     * @throws
     */
    private void handleAddCameraFailByVerCode() {
        // showToast(R.string.add_camera_verify_code_error);
        showInputCameraVerifyCodeDlg();
    }

    /**
     * 处理添加设备密码错误
     *
     *
     * @throws
     */
    private void handleAddCameraFailByPsw() {
        // showToast(R.string.camera_password_error);
        showInputCameraPswDlg();
    }

    /**
     * 这里对方法做描述
     * 
     * @param errCode
     * @see
     * @since V1.0
     */
    private void handleLocalValidateSerialNoFail(int errCode) {
        switch (errCode) {
            case ExtraException.SERIALNO_IS_NULL:
                showToast(R.string.serial_number_is_null);
                break;
            case ExtraException.SERIALNO_IS_ILLEGAL:
                showToast(R.string.serial_number_put_the_right_no);
                break;
            default:
                showToast(R.string.serial_number_error, errCode);
                LogUtil.errorLog(TAG, "handleLocalValidateSerialNoFail-> unkown error, errCode:" + errCode);
                break;
        }
    }

    /**
     * 这里对方法做描述
     * 
     * @see
     * @since V1.0
     */
    private void handleQueryCameraSuccess() {
        if (mEZProbeDeviceInfo != null) {
            LogUtil.infoLog(TAG, "handleQueryCameraSuccess, msg:" );
            showAddButton();
        }

        // 更新搜索摄像头的图片
//        showCameraList();
//        mDeviceName.setText(mEZProbeDeviceInfo.getSubSerial());
//        mDeviceIcon.setImageResource(getDeviceIcon(""));
    }

    private int getDeviceIcon(String model) {
        DeviceModel deviceModel = DeviceModel.getDeviceModel(model);
        if (deviceModel == null)
            return DeviceModel.OTHER.getDrawable2ResId();
        else
            return deviceModel.getDrawable2ResId();
    }

    private void showAddButton() {
    	LogUtil.infoLog(TAG, "enter showAddButton");
        showCameraList();
        mBtnNext.setVisibility(View.GONE);
        mActivateHint.setVisibility(View.GONE);
        mAddButton.setVisibility(View.VISIBLE);
        mConnectTip.setVisibility(View.GONE);
        mTvStatus.setVisibility(View.GONE);
    }
    /**
     * 这里对方法做描述
     * 
     * @param errCode
     * @see
     * @since V1.0
     */
    private void handleQueryCameraFail(final int errCode) {
        mWaitDlg.dismiss();
        switch (errCode) {
            case ErrorCode.ERROR_WEB_PASSWORD_ERROR:
                handleCmaeraPswError();
                break;
            case ErrorCode.ERROR_WEB_DEVICE_VERSION_UNSUPPORT:
            case ErrorCode.ERROR_WEB_DEVICE_UNSUPPORT:
                showErrorPage(R.string.seek_camera_fail_device_not_support_shipin7, 0);
                break;
            case ErrorCode.ERROR_WEB_NET_EXCEPTION:
                showErrorPage(R.string.query_camera_fail_network_exception, 0);
                break;
            case ErrorCode.ERROR_WEB_SERVER_EXCEPTION:
                showErrorPage(R.string.query_camera_fail_server_exception, 0);
                break;
            case ErrorCode.ERROR_TRANSF_ACCESSTOKEN_ERROR:
                ActivityUtils.handleSessionException(SeriesNumSearchActivity.this);
                break;
            case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_ERROR:
                showErrorPage(R.string.check_feature_code_fail, errCode);
                //ActivityUtils.handleHardwareError(SeriesNumSearchActivity.this, null);
                ActivityUtils.handleSessionException(SeriesNumSearchActivity.this);
                break;
            case ErrorCode.ERROR_INNER_PARAM_ERROR:
                showErrorPage(R.string.query_camera_fail_network_exception_or_server_exception, 0);
                break;

            case ErrorCode.ERROR_WEB_DEVICE_ADD_OWN_AGAIN:     // 设备已被自己添加
            case ErrorCode.ERROR_WEB_DEVICE_OFFLINE_ADDED:   //设备不在线，已被别人添加
                showToast(R.string.query_camera_fail_repeat_error);
                showCameraList();

                mTvStatus.setVisibility(View.VISIBLE);
                mTvStatus.setText(R.string.auto_wifi_device_you_added_already);
                mTvStatus.setTextColor(getResources().getColor(R.color.common_text));
                mBtnNext.setVisibility(View.GONE);
                mAddButton.setVisibility(View.GONE);
                mConnectTip.setVisibility(View.GONE);
                break;

            case ErrorCode.ERROR_WEB_DEVICE_ONLINE_ADDED:// 已被其他用户添加
                showUnbind();
                break;

            case ErrorCode.ERROR_WEB_DEVICE_NOT_ONLINE:// 设备不在线, 走wifi配置流程
                showWifiConfig();
                break;

            case ErrorCode.ERROR_WEB_DEVICE_NOT_EXIT:// 设备未注册, 或者不在线未添加, 走wifi配置流程
            case ErrorCode.ERROR_WEB_DEVICE_OFFLINE_NOT_ADD:
                showWifiConfig();
                break;
            default:
                showErrorPage(R.string.query_camera_fail, errCode);
                LogUtil.errorLog(TAG, "handleQueryCameraFail-> unkown error, errCode:" + errCode);
                break;
        }
    }

    public void showUnbind() {
        showCameraList();
        mTvStatus.setVisibility(View.VISIBLE);
        mTvStatus.setTextColor(getResources().getColor(R.color.common_text));
        mTvStatus.setText(R.string.scan_device_add_by_others);
        mBtnNext.setVisibility(View.GONE);
        mAddButton.setVisibility(View.GONE);
        mConnectTip.setVisibility(View.GONE);
        // mTriggerHelper.onDeviceBoundByOthers(mSearchDevice);
    }

    /**
     * 这里对方法做描述
     * 
     * @see
     * @since V1.0
     */
    private void handleCmaeraPswError() {
        // showInputCameraPswDlg();
    }

    /**
     * 这里对方法做描述
     * 
     * @see
     * @since V1.0
     */
    private void showInputCameraPswDlg() {
        mHasShowInputPswDialog = true;
        // 从布局中加载视图
        LayoutInflater factory = LayoutInflater.from(SeriesNumSearchActivity.this);
        final View passwordErrorLayout = factory.inflate(R.layout.password_error_layout, null);
        final EditText newPassword = (EditText) passwordErrorLayout.findViewById(R.id.new_password);
        newPassword.setFilters(new InputFilter[] {new InputFilter.LengthFilter(Constant.PSW_MAX_LENGTH)});

        final TextView message1 = (TextView) passwordErrorLayout.findViewById(R.id.message1);
        message1.setText(getString(R.string.realplay_password_error_message1));

        mVerifyCode = null;

        // 使用布局中的视图创建AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(SeriesNumSearchActivity.this);
        builder.setTitle(R.string.serial_add_password_error_title);
        builder.setView(passwordErrorLayout);
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mHasShowInputPswDialog = false;
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                mHasShowInputPswDialog = false;
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
        // if (verifyCodeString.length() != 6) {
        // showToast(R.string.applicati_not_support_illegal_verify);
        // return false;
        // }
        return true;
    }

    /**
     * 这里对方法做描述
     *
     *
     * @see
     * @since V1.0
     */
    public void addQueryCamera() {
    	if(!TextUtils.isEmpty(mVerifyCode)){
    		addQueryCameraAddVerifyCode();
    	} else {
    		showInputCameraVerifyCodeDlg();
    	}
    }

    private void addQueryCameraAddVerifyCode() {

        // 本地网络检测
        if (!ConnectionDetector.isNetworkAvailable(SeriesNumSearchActivity.this)) {
            showToast(R.string.add_camera_fail_network_exception);
            return;
        }

        mWaitDlg.show();

        new Thread() {
            public void run() {

                try {
                    boolean result = EzvizApplication.getOpenSDK().addDevice(mSerialNoStr, mVerifyCode);

                    /***********如有需要开发者需要自己保存此验证码***********/
//                    if (!TextUtils.isEmpty(mVerifyCode)) {
//                        //保存密码
//                        EzvizApplication.getOpenSDK().setValidateCode(mVerifyCode, mSerialNoStr);
//                    }

                    // 添加成功过后
                    sendMessage(MSG_ADD_CAMERA_SUCCESS);
                } catch (BaseException e) {
                    ErrorInfo errorInfo = (ErrorInfo) e.getObject();
                    LogUtil.debugLog(TAG, errorInfo.toString());

                    sendMessage(MSG_ADD_CAMERA_FAIL, errorInfo.errorCode);
                    LogUtil.errorLog(TAG, "add camera fail");
                }

            }
        }.start();
    }

    /**
     * 手动输入序列号页面初始化
     * 
     * @see
     * @since V1.8.2
     */
    private void showInputSerialNo() {
        showKeyBoard();

        mTitle.setText(R.string.serial_input_text);
        errorPage.setVisibility(View.GONE);
        mCameraListLy.setVisibility(View.GONE);
        mQueryingCameraRyt.setVisibility(View.GONE);
        mInputLinearlayout.setVisibility(View.VISIBLE);
    }

    /**
     * 显示键盘
     * 
     * @see
     * @since V1.8.2
     */
    private void showKeyBoard() {
        mSeriesNumberEt.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mSeriesNumberEt, InputMethodManager.SHOW_FORCED);
    }

    /**
     * 隐藏键盘
     * 
     * @see
     * @since V1.8.2
     */
    private void hideKeyBoard() {
        // 键盘隐藏是个坑
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        // 隐藏键盘，最坑的就是隐藏键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSeriesNumberEt.getWindowToken(), 0);
    }

    /**
     * 正在搜索
     * 
     * @see
     * @since V1.8.2
     */
    private void showQueryingCamera() {
        mInputLinearlayout.setVisibility(View.GONE);
        errorPage.setVisibility(View.GONE);
        mCameraListLy.setVisibility(View.GONE);
        mTitle.setText(R.string.scan_device_search);
        mQueryingCameraRyt.setVisibility(View.VISIBLE);
    }

    /**
     * 搜索结果
     * 
     * @see
     * @since V1.8.2
     */
    private void showCameraList() {
    	LogUtil.infoLog(TAG, "enter showCameraList");
        mTitle.setText(R.string.result_txt);
        mActivateHint.setVisibility(View.GONE);
        errorPage.setVisibility(View.GONE);
        mCameraListLy.setVisibility(View.VISIBLE);
        mQueryingCameraRyt.setVisibility(View.GONE);
        mInputLinearlayout.setVisibility(View.GONE);
        // 下面取得 设备 是为了 如果设备已经被自己添加 不能正常显示的问题
        /*mDeviceInfoEx = DeviceManager.getInstance().getDeviceInfoExById(mSerialNoStr);
        // 自己添加的设备
        if (mDeviceInfoEx != null) {
            mDeviceModel = mDeviceInfoEx.getEnumModel();
        }
        // 他人添加的设备
        if (mSearchDevice != null) {
            mDeviceModel = DeviceModel.getDeviceModel(mSearchDevice.getModel());
        }*/

        // stub, always get the OTHER model
        mDeviceModel = DeviceModel.OTHER;

        if (mDeviceModel != null) {
            // 更新搜索摄像头的图片
            mDeviceIcon.setImageResource(mDeviceModel.getDrawable2ResId());
        } else {
            // 更新搜索摄像头的图片
            mDeviceIcon.setImageResource(DeviceModel.OTHER.getDrawable2ResId());
        }
        // 设备名称处理
        mDeviceName.setText(mSeriesNumberEt.getText().toString().trim());
    }

    /**
     * 搜错错误页面
     * 
     * @see
     * @since V1.8.2
     */
    private void showErrorPage(int errorMsgId, int errorCode) {
        mInputLinearlayout.setVisibility(View.GONE);
        errorPage.setVisibility(View.VISIBLE);
        if (errorMsgId > 0) {
            mFailedMsg.setText(errorMsgId);
        }
        if (errorCode > 0) {
            mFailedMsg.append("," + errorCode);
        }
        mCameraListLy.setVisibility(View.GONE);
        mQueryingCameraRyt.setVisibility(View.GONE);

    }

    /**
     * 设备未注册的显示
     * 
     * @see
     * @since V1.8.2
     */
    private void showWifiConfig() {
    	boolean bShowActivation = false;
        showCameraList();
        mBtnNext.setVisibility(View.VISIBLE);
        if (!bShowActivation) { // 如果是萤石设备或者从激活页面跳转过来的话，不显示激活提示，否则显示激活提示
            mActivateHint.setVisibility(View.GONE);
        } else {
            mActivateHint.setVisibility(View.VISIBLE);
        }
        mTvStatus.setVisibility(View.VISIBLE);
        mConnectTip.setVisibility(View.VISIBLE);
        mAddButton.setVisibility(View.GONE);
        mTvStatus.setTextColor(getResources().getColor(R.color.scan_yellow));
        mTvStatus.setText(R.string.scan_network_unavailible);
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        Dialog dialog = null;
        switch (id) {
            case MODIFYPSD_FAIL_DIALOG_ID: {
                String tipTxt = "";
                String type = "";
                if (args != null) {
                    tipTxt = args.getString(BUNDE_DIALOG_TIP);
                    type = args.getString(BUNDLE_TYPE);
                }
                final String typeFinal = type;
                if (!SeriesNumSearchActivity.this.isFinishing()) {
                    dialog = new AlertDialog.Builder(SeriesNumSearchActivity.this).setMessage(tipTxt)
                            .setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mHasShowInputPswDialog = false;
                                }
                            }).setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (typeFinal == null || typeFinal.equals("")) {
                                        handleAddCameraFailByVerCode();
                                    } else {
                                        handleAddCameraFailByPsw();
                                    }
                                }
                            }).create();
                }
            }
                break;
            default:
                break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case MODIFYPSD_FAIL_DIALOG_ID:
                // 修改显示布局
                if (dialog != null) {
                    TextView tv = (TextView) dialog.findViewById(android.R.id.message);
                    tv.setGravity(Gravity.CENTER);
                }
                break;
            case SHOW_DIALOG_SET_WIFI:
                // 修改显示布局
                if (dialog != null) {
                    removeDialog(SHOW_DIALOG_SET_WIFI);
                    TextView tv = (TextView) dialog.findViewById(android.R.id.message);
                    tv.setGravity(Gravity.CENTER);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 提示用户输入验证码
     * 
     * @see
     * @since V1.0
     */
    private void showInputCameraVerifyCodeDlg() {
        mHasShowInputPswDialog = true;

        mVerifyCode = null;
        // 从布局中加载视图
        LayoutInflater factory = LayoutInflater.from(SeriesNumSearchActivity.this);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(SeriesNumSearchActivity.this);
        builder.setTitle(R.string.camera_detail_verifycode_error_title);
        builder.setView(passwordErrorLayout);
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mHasShowInputPswDialog = false;
            }
        });
        builder.setOnCancelListener(new OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                mHasShowInputPswDialog = false;
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

    @Override
    public void onBackPressed() {
        //
        if (mType == 0 && mInputLinearlayout.getVisibility() != View.VISIBLE) {
            showInputSerialNo();
        } else {
            hideKeyBoard();
            finish();
        }
    }
}
