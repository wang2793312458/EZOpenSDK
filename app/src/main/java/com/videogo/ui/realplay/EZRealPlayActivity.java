package com.videogo.ui.realplay;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.videogo.EzvizApplication;
import com.videogo.constant.Config;
import com.videogo.constant.Constant;
import com.videogo.constant.IntentConsts;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.exception.InnerException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZConstants.EZPTZAction;
import com.videogo.openapi.EZConstants.EZPTZCommand;
import com.videogo.openapi.EZConstants.EZRealPlayConstants;
import com.videogo.openapi.EZConstants.EZVideoLevel;
import com.videogo.openapi.EZOpenSDKListener;
import com.videogo.openapi.EZPlayer;
import com.videogo.openapi.bean.EZCameraInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.realplay.RealPlayStatus;
import com.videogo.ui.cameralist.EZCameraListActivity;
import com.videogo.ui.common.ScreenOrientationHelper;
import com.videogo.ui.realplay.RealPlaySquareInfo;
import com.videogo.ui.util.ActivityUtils;
import com.videogo.ui.util.AudioPlayUtil;
import com.videogo.ui.util.DataManager;
import com.videogo.ui.util.EZUtils;
import com.videogo.ui.util.VerifyCodeInput;
import com.videogo.util.ConnectionDetector;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.MediaScanner;
import com.videogo.util.RotateViewUtil;
import com.videogo.util.SDCardUtil;
import com.videogo.util.Utils;
import com.videogo.widget.CheckTextButton;
import com.videogo.widget.CustomRect;
import com.videogo.widget.CustomTouchListener;
import com.videogo.widget.RingView;
import com.videogo.widget.TitleBar;
import com.videogo.widget.WaitDialog;
import com.videogo.widget.loading.LoadingTextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ezviz.ezopensdk.R;

/**
 * 实时预览2.7
 *
 * @author xiaxingsuo
 * @data 2015-11-11
 */
public class EZRealPlayActivity extends Activity implements OnClickListener, SurfaceHolder.Callback,
        Handler.Callback, OnTouchListener, VerifyCodeInput.VerifyCodeInputListener {
    private static final String TAG = "RealPlayerActivity";
    /**
     * 动画时间
     */
    private static final int ANIMATION_DURING_TIME = 500;

    public static final float BIG_SCREEN_RATIO = 1.60f;

    // UI消息
    public static final int MSG_PLAY_UI_UPDATE = 200;

    public static final int MSG_AUTO_START_PLAY = 202;

    public static final int MSG_CLOSE_PTZ_PROMPT = 203;

    public static final int MSG_HIDE_PTZ_DIRECTION = 204;

    public static final int MSG_HIDE_PAGE_ANIM = 205;

    public static final int MSG_PLAY_UI_REFRESH = 206;

    public static final int MSG_PREVIEW_START_PLAY = 207;

    public static final int MSG_SET_VEDIOMODE_SUCCESS = 105;

    /**
     * 设置视频质量成功
     */
    public static final int MSG_SET_VEDIOMODE_FAIL = 106;

    // 视频广场URL
    private String mRtspUrl = null;
    // 视频广场播放信息
    private RealPlaySquareInfo mRealPlaySquareInfo = null;
//    private EZCameraInfo mCameraInfo = null;

    private AudioPlayUtil mAudioPlayUtil = null;
    private LocalInfo mLocalInfo = null;
    private Handler mHandler = null;

    private float mRealRatio = Constant.LIVE_VIEW_RATIO;
    /**
     * 标识是否正在播放
     */
    private int mStatus = RealPlayStatus.STATUS_INIT;
    private boolean mIsOnStop = false;
    /**
     * 屏幕当前方向
     */
    private int mOrientation = Configuration.ORIENTATION_PORTRAIT;
    private int mForceOrientation = 0;
    private Rect mRealPlayRect = null;

    private LinearLayout mRealPlayPageLy = null;
    private TitleBar mPortraitTitleBar = null;
    private TitleBar mLandscapeTitleBar = null;
    private Button mTiletRightBtn = null;
    private RelativeLayout mRealPlayPlayRl = null;

    private SurfaceView mRealPlaySv = null;
    private SurfaceHolder mRealPlaySh = null;
    private CustomTouchListener mRealPlayTouchListener = null;

    // 提示布局界面
    private RelativeLayout mRealPlayPromptRl = null;
    //loading控件
    private RelativeLayout mRealPlayLoadingRl;
    private TextView mRealPlayTipTv;
    private ImageView mRealPlayPlayIv;
    private LoadingTextView mRealPlayPlayLoading;
    private LinearLayout mRealPlayPlayPrivacyLy;
    //隐私保护动画
    private ImageView mPageAnimIv = null;
    private AnimationDrawable mPageAnimDrawable = null;

    private LinearLayout mRealPlayControlRl = null;
    private ImageButton mRealPlayBtn = null;
    private ImageButton mRealPlaySoundBtn = null;
    private TextView mRealPlayFlowTv = null;
    private int mControlDisplaySec = 0;

    // 播放比例
    private float mPlayScale = 1;

    private RelativeLayout mRealPlayCaptureRl = null;
    private LayoutParams mRealPlayCaptureRlLp = null;
    private ImageView mRealPlayCaptureIv = null;
    private ImageView mRealPlayCaptureWatermarkIv = null;
    private int mCaptureDisplaySec = 0;
    private LinearLayout mRealPlayRecordLy = null;
    private ImageView mRealPlayRecordIv = null;
    private TextView mRealPlayRecordTv = null;

    private boolean mIsRecording = false;
    private String mRecordTime = null;
    /**
     * 录像时间
     */
    private int mRecordSecond = 0;

    private HorizontalScrollView mRealPlayOperateBar = null;

    private LinearLayout mRealPlayPtzBtnLy = null;
    private LinearLayout mRealPlayTalkBtnLy = null;
    private LinearLayout mRealPlaySslBtnLy = null;
    //    private LinearLayout mRealPlayPrivacyBtnLy = null;
    private LinearLayout mRealPlayCaptureBtnLy = null;
    private LinearLayout mRealPlayRecordContainerLy = null;

    private ImageButton mRealPlayPtzBtn = null;
    private ImageButton mRealPlayTalkBtn = null;
    private Button mRealPlaySslBtn = null;
    private ImageButton mRealPlayPrivacyBtn = null;
    private ImageButton mRealPlayCaptureBtn = null;
    private View mRealPlayRecordContainer = null;

    private ImageButton mRealPlayRecordBtn = null;
    private ImageButton mRealPlayRecordStartBtn = null;
    private RotateViewUtil mRecordRotateViewUtil = null;

    private Button mRealPlayQualityBtn = null;

    private RelativeLayout mRealPlayFullOperateBar = null;
    private ImageButton mRealPlayFullPlayBtn = null;
    private ImageButton mRealPlayFullSoundBtn = null;
    private ImageButton mRealPlayFullTalkBtn = null;
    private ImageButton mRealPlayFullCaptureBtn = null;
    private ImageButton mRealPlayFullPtzBtn = null;
    private ImageButton mRealPlayFullRecordBtn = null;
    private ImageButton mRealPlayFullRecordStartBtn = null;
    private View mRealPlayFullRecordContainer = null;
    private LinearLayout mRealPlayFullFlowLy = null;
    private TextView mRealPlayFullRateTv = null;
    private TextView mRealPlayFullFlowTv = null;
    private TextView mRealPlayRatioTv = null;

    // 横屏云台
    private ImageButton mRealPlayFullPtzAnimBtn = null;
    private ImageView mRealPlayFullPtzPromptIv = null;
    private boolean mIsOnPtz = false;
    private ImageView mRealPlayPtzDirectionIv = null;
    private ImageButton mRealPlayFullAnimBtn = null;
    private int[] mStartXy = new int[2];
    private int[] mEndXy = new int[2];

    private PopupWindow mQualityPopupWindow = null;
    private PopupWindow mPtzPopupWindow = null;
    private LinearLayout mPtzControlLy = null;
    private PopupWindow mTalkPopupWindow = null;
    private RingView mTalkRingView = null;
    private Button mTalkBackControlBtn = null;

    private WaitDialog mWaitDialog = null;

    /**
     * 监听锁屏解锁的事件
     */
    private RealPlayBroadcastReceiver mBroadcastReceiver = null;
    /**
     * 定时器
     */
    private Timer mUpdateTimer = null;
    /**
     * 定时器执行的任务
     */
    private TimerTask mUpdateTimerTask = null;

    // 全屏按钮
    private CheckTextButton mFullscreenButton;
    private CheckTextButton mFullscreenFullButton;
    private ScreenOrientationHelper mScreenOrientationHelper;

    // 弱提示预览信息
    private long mStartTime = 0;
    private long mStopTime = 0;

    // 云台控制状态
    private float mZoomScale = 0;
    private int mCommand = -1;

    // 横屏对讲
    private ImageButton mRealPlayFullTalkAnimBtn;
    // 对讲模式
    private boolean mIsOnTalk = false;

    // 直播预告
    private TextView mRealPlayPreviewTv = null;

    /**
     * 演示点预览控制对象
     */
    private EZPlayer mEZPlayer = null;
    //    private StubPlayer mStub = new StubPlayer();
    private CheckTextButton mFullScreenTitleBarBackBtn;
    private EZVideoLevel mCurrentQulityMode = EZVideoLevel.VIDEO_LEVEL_HD;
    private EZDeviceInfo mDeviceInfo = null;
    private EZCameraInfo mCameraInfo = null;
    private String mVerifyCode;

    /**
     * 存放上一次的流量
     */
    private long mStreamFlow = 0;

    /**
     * 当前流量
     */
    private int mRealFlow = 0;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
//    private GoogleApiClient client;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initData();
        initView();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        mScreenOrientationHelper.postOnStart();
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "EZRealPlay Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.videogo.open/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return;
        }
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mRealPlaySv != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mRealPlaySv.getWindowToken(), 0);
                }
            }
        }, 200);

        initUI();
        mRealPlaySv.setVisibility(View.VISIBLE);

        LogUtil.infoLog(TAG, "onResume real play status:" + mStatus);
        if (mCameraInfo != null && mDeviceInfo != null &&  mDeviceInfo.getStatus() != 1) {
            if (mStatus != RealPlayStatus.STATUS_STOP) {
                stopRealPlay();
            }
            setRealPlayFailUI(getString(R.string.realplay_fail_device_not_exist));
        } else {
            if (mStatus == RealPlayStatus.STATUS_INIT || mStatus == RealPlayStatus.STATUS_PAUSE
                    || mStatus == RealPlayStatus.STATUS_DECRYPT) {
                // 开始播放
                startRealPlay();
            }
        }
        mIsOnStop = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "EZRealPlay Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app deep link URI is correct.
//                Uri.parse("android-app://com.videogo.open/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(client, viewAction);
        if (mScreenOrientationHelper != null) {
            mScreenOrientationHelper.postOnStop();
        }

        mHandler.removeMessages(MSG_AUTO_START_PLAY);
        hidePageAnim();

        if (mCameraInfo == null && mRtspUrl == null) {
            return;
        }

        closePtzPopupWindow();
        closeTalkPopupWindow(true, false);
        if (mStatus != RealPlayStatus.STATUS_STOP) {
            mIsOnStop = true;
            stopRealPlay();
            mStatus = RealPlayStatus.STATUS_PAUSE;
            setRealPlayStopUI();
        } else {
            setStopLoading();
        }
        mRealPlaySv.setVisibility(View.INVISIBLE);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.disconnect();
    }

    /*
        * (non-Javadoc)
        * @see android.app.Activity#onDestroy()
        */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mEZPlayer != null) {
            mEZPlayer.release();

        }

        mHandler.removeMessages(MSG_AUTO_START_PLAY);
        mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
        mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
        mHandler.removeMessages(MSG_HIDE_PAGE_ANIM);
        mHandler = null;

        if (mBroadcastReceiver != null) {
            // 取消锁屏广播的注册
            unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }

        mScreenOrientationHelper = null;
    }

    private void  exit(){
       closePtzPopupWindow();
       closeTalkPopupWindow(true, false);
       if (mStatus != RealPlayStatus.STATUS_STOP) {
           stopRealPlay();
           setRealPlayStopUI();
       }
       mHandler.removeMessages(MSG_AUTO_START_PLAY);
       mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
       mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
       mHandler.removeMessages(MSG_HIDE_PAGE_ANIM);
       if (mBroadcastReceiver != null) {
           // 取消锁屏广播的注册
           unregisterReceiver(mBroadcastReceiver);
           mBroadcastReceiver = null;
       }
       finish();
    }

    @Override
    public void finish() {
        if (mCameraInfo != null){
            Intent intent = new Intent();
            intent.putExtra(IntentConsts.EXTRA_DEVICE_ID,mCameraInfo.getDeviceSerial());
            intent.putExtra(IntentConsts.EXTRA_CAMERA_NO,mCameraInfo.getCameraNo());
            intent.putExtra("video_level",mCameraInfo.getVideoLevel().getVideoLevel());
            setResult(EZCameraListActivity.RESULT_CODE, intent);
        }
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            mScreenOrientationHelper.portrait();
            return;
        }
        exit();
    }

    // 初始化数据对象
    private void initData() {
        // 获取本地信息
        Application application = (Application) getApplication();
        mAudioPlayUtil = AudioPlayUtil.getInstance(application);
        // 获取配置信息操作对象
        mLocalInfo = LocalInfo.getInstance();
        // 获取屏幕参数
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        mLocalInfo.setScreenWidthHeight(metric.widthPixels, metric.heightPixels);
        mLocalInfo.setNavigationBarHeight((int) Math.ceil(25 * getResources().getDisplayMetrics().density));

        mHandler = new Handler(this);
        mRecordRotateViewUtil = new RotateViewUtil();

        mBroadcastReceiver = new RealPlayBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mBroadcastReceiver, filter);

        mRealPlaySquareInfo = new RealPlaySquareInfo();
        Intent intent = getIntent();
        if (intent != null) {
            mCameraInfo = intent.getParcelableExtra(IntentConsts.EXTRA_CAMERA_INFO);
            mDeviceInfo = intent.getParcelableExtra(IntentConsts.EXTRA_DEVICE_INFO);
            mRtspUrl = intent.getStringExtra(IntentConsts.EXTRA_RTSP_URL);
            if (mCameraInfo != null) {
                mCurrentQulityMode = (mCameraInfo.getVideoLevel());
            }
            LogUtil.debugLog(TAG, "rtspUrl:" + mRtspUrl);

            getRealPlaySquareInfo();
        }
        if (mDeviceInfo != null && mDeviceInfo.getIsEncrypt() == 1) {
            mVerifyCode = DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial());
        }
    }

    private void getRealPlaySquareInfo() {
        if (TextUtils.isEmpty(mRtspUrl)) {
            return;
        }

        Uri uri = Uri.parse(mRtspUrl.replaceFirst("&", "?"));
        try {
            mRealPlaySquareInfo.mSquareId = Integer.parseInt(uri.getQueryParameter("squareid"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            mRealPlaySquareInfo.mChannelNo = Integer.parseInt(Utils.getUrlValue(mRtspUrl, "channelno=", "&"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        mRealPlaySquareInfo.mCameraName = uri.getQueryParameter("cameraname");
        try {
            mRealPlaySquareInfo.mSoundType = Integer.parseInt(uri.getQueryParameter("soundtype"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        mRealPlaySquareInfo.mCoverUrl = uri.getQueryParameter("md5Serial");
        if (!TextUtils.isEmpty(mRealPlaySquareInfo.mCoverUrl)) {
            mRealPlaySquareInfo.mCoverUrl = mLocalInfo.getServAddr() + mRealPlaySquareInfo.mCoverUrl + "_mobile.jpeg";
        }
    }

    /**
     * screen状态广播接收者
     */
    private class RealPlayBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                closePtzPopupWindow();
                closeTalkPopupWindow(true, false);
                if (mStatus != RealPlayStatus.STATUS_STOP) {
                    stopRealPlay();
                    mStatus = RealPlayStatus.STATUS_PAUSE;
                    setRealPlayStopUI();
                }
            }
        }
    }

    private void initTitleBar() {
        mPortraitTitleBar = (TitleBar) findViewById(R.id.title_bar_portrait);
        mPortraitTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                closePtzPopupWindow();
                closeTalkPopupWindow(true, false);
                if (mStatus != RealPlayStatus.STATUS_STOP) {
                    stopRealPlay();
                    setRealPlayStopUI();
                }
                finish();
            }
        });
        if (mRtspUrl == null) {
        } else {
            //mPortraitTitleBar.setBackgroundColor(getResources().getColor(R.color.black_bg));
        }
        mLandscapeTitleBar = (TitleBar) findViewById(R.id.title_bar_landscape);
        mLandscapeTitleBar.setStyle(Color.rgb(0xff, 0xff, 0xff), getResources().getDrawable(R.color.dark_bg_70p),
                getResources().getDrawable(R.drawable.message_back_selector));
        mLandscapeTitleBar.setOnTouchListener(this);
        mFullScreenTitleBarBackBtn = new CheckTextButton(this);
        mFullScreenTitleBarBackBtn.setBackground(getResources().getDrawable(R.drawable.common_title_back_selector));
        mLandscapeTitleBar.addLeftView(mFullScreenTitleBarBackBtn);
    }

    private void initRealPlayPageLy() {
        mRealPlayPageLy = (LinearLayout) findViewById(R.id.realplay_page_ly);
        /** 测量状态栏高度 **/
        ViewTreeObserver viewTreeObserver = mRealPlayPageLy.getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mRealPlayRect == null) {
                    // 获取状况栏高度
                    mRealPlayRect = new Rect();
                    getWindow().getDecorView().getWindowVisibleDisplayFrame(mRealPlayRect);
                }
            }
        });
    }

    // 初始化界面
    private void initView() {
        setContentView(R.layout.ez_realplay_page);
        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initTitleBar();
        initRealPlayPageLy();
        initLoadingUI();

        mRealPlayPlayRl = (RelativeLayout) findViewById(R.id.realplay_play_rl);
        mRealPlaySv = (SurfaceView) findViewById(R.id.realplay_sv);
        mRealPlaySh = mRealPlaySv.getHolder();
        mRealPlaySh.addCallback(this);
        mRealPlayTouchListener = new CustomTouchListener() {

            @Override
            public boolean canZoom(float scale) {
                if (mStatus == RealPlayStatus.STATUS_PLAY) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public boolean canDrag(int direction) {
                if (mStatus != RealPlayStatus.STATUS_PLAY) {
                    return false;
                }
                if (mEZPlayer != null && mDeviceInfo != null) {
                    // 出界判断
                    if (DRAG_LEFT == direction || DRAG_RIGHT == direction) {
                        // 左移/右移出界判断
                        if (mDeviceInfo.isSupportPTZ()) {
                            return true;
                        }
                    } else if (DRAG_UP == direction || DRAG_DOWN == direction) {
                        // 上移/下移出界判断
                        if (mDeviceInfo.isSupportPTZ()) {
                            return true;
                        }
                    }
                }
                return false;
            }

            @Override
            public void onSingleClick() {
                onRealPlaySvClick();
            }

            @Override
            public void onDoubleClick(MotionEvent e) {
            }

            @Override
            public void onZoom(float scale) {
                LogUtil.debugLog(TAG, "onZoom:" + scale);
                if (mEZPlayer != null && mDeviceInfo != null &&  mDeviceInfo.isSupportZoom()) {
                    startZoom(scale);
                }
            }

            @Override
            public void onDrag(int direction, float distance, float rate) {
                LogUtil.debugLog(TAG, "onDrag:" + direction);
                if (mEZPlayer != null) {
                    //Utils.showLog(RealPlayActivity.this, "onDrag rate:" + rate);
                    startDrag(direction, distance, rate);
                }
            }

            @Override
            public void onEnd(int mode) {
                LogUtil.debugLog(TAG, "onEnd:" + mode);
                if (mEZPlayer != null) {
                    stopDrag(false);
                }
                if (mEZPlayer != null && mDeviceInfo != null && mDeviceInfo.isSupportZoom()) {
                    stopZoom();
                }
            }

            @Override
            public void onZoomChange(float scale, CustomRect oRect, CustomRect curRect) {
                LogUtil.debugLog(TAG, "onZoomChange:" + scale);
                if (mEZPlayer != null && mDeviceInfo != null && mDeviceInfo.isSupportZoom()) {
                    //采用云台调焦
                    return;
                }
                if (mStatus == RealPlayStatus.STATUS_PLAY) {
                    if (scale > 1.0f && scale < 1.1f) {
                        scale = 1.1f;
                    }
                    setPlayScaleUI(scale, oRect, curRect);
                }
            }
        };
        mRealPlaySv.setOnTouchListener(mRealPlayTouchListener);

        mRealPlayPtzDirectionIv = (ImageView) findViewById(R.id.realplay_ptz_direction_iv);

        mRealPlayPromptRl = (RelativeLayout) findViewById(R.id.realplay_prompt_rl);
        mRealPlayControlRl = (LinearLayout) findViewById(R.id.realplay_control_rl);
        mRealPlayBtn = (ImageButton) findViewById(R.id.realplay_play_btn);
        mRealPlaySoundBtn = (ImageButton) findViewById(R.id.realplay_sound_btn);
        mRealPlayFlowTv = (TextView) findViewById(R.id.realplay_flow_tv);
        mRealPlayFlowTv.setText("0k/s");

        mRealPlayCaptureRl = (RelativeLayout) findViewById(R.id.realplay_capture_rl);
        mRealPlayCaptureRlLp = (LayoutParams) mRealPlayCaptureRl.getLayoutParams();
        mRealPlayCaptureIv = (ImageView) findViewById(R.id.realplay_capture_iv);
        mRealPlayCaptureWatermarkIv = (ImageView) findViewById(R.id.realplay_capture_watermark_iv);
        mRealPlayRecordLy = (LinearLayout) findViewById(R.id.realplay_record_ly);
        mRealPlayRecordIv = (ImageView) findViewById(R.id.realplay_record_iv);
        mRealPlayRecordTv = (TextView) findViewById(R.id.realplay_record_tv);

        mRealPlayQualityBtn = (Button) findViewById(R.id.realplay_quality_btn);

        mRealPlayFullFlowLy = (LinearLayout) findViewById(R.id.realplay_full_flow_ly);
        mRealPlayFullRateTv = (TextView) findViewById(R.id.realplay_full_rate_tv);
        mRealPlayFullFlowTv = (TextView) findViewById(R.id.realplay_full_flow_tv);
        mRealPlayRatioTv = (TextView) findViewById(R.id.realplay_ratio_tv);
        mRealPlayFullRateTv.setText("0k/s");
        mRealPlayFullFlowTv.setText("0MB");

        mFullscreenButton = (CheckTextButton) findViewById(R.id.fullscreen_button);
        mFullscreenFullButton = (CheckTextButton) findViewById(R.id.fullscreen_full_button);

        if (mRtspUrl == null) {
            initOperateBarUI(false);

            initFullOperateBarUI();

            // mj           LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
            //                    LayoutParams.WRAP_CONTENT);
            //            realPlayPlayRlLp.gravity = Gravity.CENTER;
            //            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
            mRealPlayOperateBar.setVisibility(View.VISIBLE);
        } else {
            //mRealPlayPageLy.setBackgroundColor(getResources().getColor(R.color.black_bg));
            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            realPlayPlayRlLp.gravity = Gravity.CENTER;
            //mj 2015/11/01 realPlayPlayRlLp.weight = 1;
            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
            mRealPlayPlayRl.setBackgroundColor(getResources().getColor(R.color.common_bg));
        }

        setRealPlaySvLayout();
        initCaptureUI();
        mScreenOrientationHelper = new ScreenOrientationHelper(this, mFullscreenButton, /*mFullscreenFullButton*/mFullScreenTitleBarBackBtn);

        mWaitDialog = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDialog.setCancelable(false);
    }

    public void startDrag(int direction, float distance, float rate) {
    }

    public void stopDrag(boolean control) {
    }

    private void startZoom(float scale) {
        if (mEZPlayer == null) {
            return;
        }

        hideControlRlAndFullOperateBar(false);
        boolean preZoomIn = mZoomScale > 1.01 ? true : false;
        boolean zoomIn = scale > 1.01 ? true : false;
        if (mZoomScale != 0 && preZoomIn != zoomIn) {
            LogUtil.debugLog(TAG, "startZoom stop:" + mZoomScale);
            //            mEZOpenSDK.controlPTZ(mZoomScale > 1.01 ? RealPlayStatus.PTZ_ZOOMIN
            //                    : RealPlayStatus.PTZ_ZOOMOUT, RealPlayStatus.PTZ_SPEED_DEFAULT, EZPlayer.PTZ_COMMAND_STOP);
            mZoomScale = 0;
        }
        if (scale != 0 && (mZoomScale == 0 || preZoomIn != zoomIn)) {
            mZoomScale = scale;
            LogUtil.debugLog(TAG, "startZoom start:" + mZoomScale);
            //            mEZOpenSDK.controlPTZ(mZoomScale > 1.01 ? RealPlayStatus.PTZ_ZOOMIN
            //                    : RealPlayStatus.PTZ_ZOOMOUT, RealPlayStatus.PTZ_SPEED_DEFAULT, EZPlayer.PTZ_COMMAND_START);
        }
    }

    private void stopZoom() {
        if (mEZPlayer == null) {
            return;
        }
        if (mZoomScale != 0) {
            LogUtil.debugLog(TAG, "stopZoom stop:" + mZoomScale);
            //            mEZOpenSDK.controlPTZ(mZoomScale > 1.01 ? RealPlayStatus.PTZ_ZOOMIN
            //                    : RealPlayStatus.PTZ_ZOOMOUT, RealPlayStatus.PTZ_SPEED_DEFAULT, EZPlayer.PTZ_COMMAND_STOP);
            mZoomScale = 0;
        }
    }

    private void setPtzDirectionIv(int command) {
        setPtzDirectionIv(command, 0);
    }

    private void setPtzDirectionIv(int command, int errorCode) {
        if (command != -1 && errorCode == 0) {
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            switch (command) {
                case RealPlayStatus.PTZ_LEFT:
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.left_twinkle);
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case RealPlayStatus.PTZ_RIGHT:
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.right_twinkle);
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case RealPlayStatus.PTZ_UP:
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.up_twinkle);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case RealPlayStatus.PTZ_DOWN:
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.down_twinkle);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.realplay_sv);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                default:
                    break;
            }
            mRealPlayPtzDirectionIv.setVisibility(View.VISIBLE);
            mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
            Message msg = new Message();
            msg.what = MSG_HIDE_PTZ_DIRECTION;
            msg.arg1 = 1;
            mHandler.sendMessageDelayed(msg, 500);
        } else if (errorCode != 0) {
            LayoutParams svParams = (LayoutParams) mRealPlaySv.getLayoutParams();
            LayoutParams params = null;
            switch (errorCode) {
                case ErrorCode.ERROR_CAS_PTZ_ROTATION_LEFT_LIMIT_FAILED:
                    params = new LayoutParams(LayoutParams.WRAP_CONTENT, svParams.height);
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.ptz_left_limit);
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case ErrorCode.ERROR_CAS_PTZ_ROTATION_RIGHT_LIMIT_FAILED:
                    params = new LayoutParams(LayoutParams.WRAP_CONTENT, svParams.height);
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.ptz_right_limit);
                    params.addRule(RelativeLayout.CENTER_VERTICAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case ErrorCode.ERROR_CAS_PTZ_ROTATION_UP_LIMIT_FAILED:
                    params = new LayoutParams(svParams.width, LayoutParams.WRAP_CONTENT);
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.ptz_top_limit);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                case ErrorCode.ERROR_CAS_PTZ_ROTATION_DOWN_LIMIT_FAILED:
                    params = new LayoutParams(svParams.width, LayoutParams.WRAP_CONTENT);
                    mRealPlayPtzDirectionIv.setBackgroundResource(R.drawable.ptz_bottom_limit);
                    params.addRule(RelativeLayout.CENTER_HORIZONTAL);
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.realplay_sv);
                    mRealPlayPtzDirectionIv.setLayoutParams(params);
                    break;
                default:
                    break;
            }
            mRealPlayPtzDirectionIv.setVisibility(View.VISIBLE);
            mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
            Message msg = new Message();
            msg.what = MSG_HIDE_PTZ_DIRECTION;
            msg.arg1 = 1;
            mHandler.sendMessageDelayed(msg, 500);
        } else {
            mRealPlayPtzDirectionIv.setVisibility(View.GONE);
            mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
        }
    }

    private int getSupportPtz() {
        if (mEZPlayer == null || mDeviceInfo == null) {
            return 0;
        }

        if (mDeviceInfo.isSupportPTZ() || mDeviceInfo.isSupportZoom()) {
            return 1;
        } else {
            return 0;
        }
    }

    // 初始化UI
    @SuppressWarnings("deprecation")
    private void initUI() {
        mPageAnimDrawable = null;
        mRealPlaySoundBtn.setVisibility(View.VISIBLE);

        if (mCameraInfo != null) {
            mPortraitTitleBar.setTitle(mCameraInfo.getCameraName());
            mLandscapeTitleBar.setTitle(mCameraInfo.getCameraName());

            setCameraInfoTiletRightBtn();

            if (mLocalInfo.isSoundOpen()) {
                mRealPlaySoundBtn.setBackgroundResource(R.drawable.ezopen_vertical_preview_sound_selector);
                mRealPlayFullSoundBtn.setBackgroundResource(R.drawable.play_full_soundon_btn_selector);
            } else {
                mRealPlaySoundBtn.setBackgroundResource(R.drawable.ezopen_vertical_preview_sound_off_selector);
                mRealPlayFullSoundBtn.setBackgroundResource(R.drawable.play_full_soundoff_btn_selector);
            }

            mRealPlayCaptureBtnLy.setVisibility(View.VISIBLE);
            mRealPlayFullCaptureBtn.setVisibility(View.VISIBLE);
            mRealPlayRecordContainerLy.setVisibility(View.VISIBLE);
            mRealPlayFullRecordContainer.setVisibility(View.VISIBLE);
            mRealPlayQualityBtn.setVisibility(View.VISIBLE);
            mRealPlayFullSoundBtn.setVisibility(View.VISIBLE);
            mRealPlayFullPtzAnimBtn.setVisibility(View.GONE);
            mRealPlayFullPtzPromptIv.setVisibility(View.GONE);

            updateUI();
        } else if (mRtspUrl != null) {
            if (!TextUtils.isEmpty(mRealPlaySquareInfo.mCameraName)) {
                mPortraitTitleBar.setTitle(mRealPlaySquareInfo.mCameraName);
                mLandscapeTitleBar.setTitle(mRealPlaySquareInfo.mCameraName);
            }
            mRealPlaySoundBtn.setVisibility(View.GONE);
            mRealPlayQualityBtn.setVisibility(View.GONE);
        }

        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            updateOperatorUI();
        }
    }

    private void setCameraInfoTiletRightBtn() {
        if (mTiletRightBtn != null && mDeviceInfo != null) {
            if (mDeviceInfo.getStatus() == 1) {
                mTiletRightBtn.setVisibility(View.VISIBLE);
            } else {
                mTiletRightBtn.setVisibility(View.GONE);
            }
        }
    }

    private void initOperateBarUI(boolean bigScreen) {
        bigScreen = false;
        if (mRealPlayOperateBar != null) {
            mRealPlayOperateBar.setVisibility(View.GONE);
            mRealPlayOperateBar = null;
        }
        if (bigScreen) {
            mRealPlayOperateBar = (HorizontalScrollView) findViewById(R.id.ezopen_realplay_operate_bar2);
            findViewById(R.id.ezopen_realplay_operate_bar).setVisibility(View.GONE);

            mRealPlayPtzBtnLy = (LinearLayout) findViewById(R.id.realplay_ptz_btn_ly2);
            mRealPlayTalkBtnLy = (LinearLayout) findViewById(R.id.realplay_talk_btn_ly2);
            mRealPlaySslBtnLy = (LinearLayout) findViewById(R.id.realplay_ssl_btn_ly2);
            mRealPlayCaptureBtnLy = (LinearLayout) findViewById(R.id.realplay_previously_btn_ly2);
            mRealPlayRecordContainerLy = (LinearLayout) findViewById(R.id.realplay_video_container_ly2);

            mRealPlayTalkBtn = (ImageButton) findViewById(R.id.realplay_talk_btn2);
            mRealPlaySslBtn = (Button) findViewById(R.id.realplay_ssl_btn2);
            mRealPlayPrivacyBtn = (ImageButton) findViewById(R.id.realplay_privacy_btn2);
            mRealPlayCaptureBtn = (ImageButton) findViewById(R.id.realplay_previously_btn2);
            mRealPlayRecordContainer = findViewById(R.id.realplay_video_container2);
            mRealPlayRecordBtn = (ImageButton) findViewById(R.id.realplay_video_btn2);
            mRealPlayRecordStartBtn = (ImageButton) findViewById(R.id.realplay_video_start_btn2);
            mRealPlayPtzBtn = (ImageButton) findViewById(R.id.realplay_ptz_btn2);
        } else {
            mRealPlayOperateBar = (HorizontalScrollView) findViewById(R.id.ezopen_realplay_operate_bar);
            findViewById(R.id.ezopen_realplay_operate_bar2).setVisibility(View.GONE);

            mRealPlayPtzBtnLy = (LinearLayout) findViewById(R.id.realplay_ptz_btn_ly);
            mRealPlayTalkBtnLy = (LinearLayout) findViewById(R.id.realplay_talk_btn_ly);
            mRealPlaySslBtnLy = (LinearLayout) findViewById(R.id.realplay_ssl_btn_ly);
            mRealPlayCaptureBtnLy = (LinearLayout) findViewById(R.id.realplay_previously_btn_ly);
            mRealPlayRecordContainerLy = (LinearLayout) findViewById(R.id.realplay_video_container_ly);

            mRealPlayTalkBtn = (ImageButton) findViewById(R.id.realplay_talk_btn);
            mRealPlaySslBtn = (Button) findViewById(R.id.realplay_ssl_btn);
            mRealPlayPrivacyBtn = (ImageButton) findViewById(R.id.realplay_privacy_btn);
            mRealPlayCaptureBtn = (ImageButton) findViewById(R.id.realplay_previously_btn);
            mRealPlayRecordContainer = findViewById(R.id.realplay_video_container);
            mRealPlayRecordBtn = (ImageButton) findViewById(R.id.realplay_video_btn);
            mRealPlayRecordStartBtn = (ImageButton) findViewById(R.id.realplay_video_start_btn);
            mRealPlayPtzBtn = (ImageButton) findViewById(R.id.realplay_ptz_btn);
        }
        mRealPlayTalkBtn.setEnabled(false);
        mRealPlayOperateBar.setVisibility(View.VISIBLE);
    }

    private void setBigScreenOperateBtnLayout() {
    }

    private void initFullOperateBarUI() {
        mRealPlayFullOperateBar = (RelativeLayout) findViewById(R.id.realplay_full_operate_bar);
        mRealPlayFullPlayBtn = (ImageButton) findViewById(R.id.realplay_full_play_btn);
        mRealPlayFullSoundBtn = (ImageButton) findViewById(R.id.realplay_full_sound_btn);
        mRealPlayFullTalkBtn = (ImageButton) findViewById(R.id.realplay_full_talk_btn);
        mRealPlayFullCaptureBtn = (ImageButton) findViewById(R.id.realplay_full_previously_btn);
        mRealPlayFullPtzBtn = (ImageButton) findViewById(R.id.realplay_full_ptz_btn);
        mRealPlayFullRecordContainer = findViewById(R.id.realplay_full_video_container);
        mRealPlayFullRecordBtn = (ImageButton) findViewById(R.id.realplay_full_video_btn);
        mRealPlayFullRecordStartBtn = (ImageButton) findViewById(R.id.realplay_full_video_start_btn);
        mRealPlayFullOperateBar.setOnTouchListener(this);

        mRealPlayFullPtzAnimBtn = (ImageButton) findViewById(R.id.realplay_full_ptz_anim_btn);
        mRealPlayFullPtzPromptIv = (ImageView) findViewById(R.id.realplay_full_ptz_prompt_iv);

        mRealPlayFullTalkAnimBtn = (ImageButton) findViewById(R.id.realplay_full_talk_anim_btn);

        mRealPlayFullAnimBtn = (ImageButton) findViewById(R.id.realplay_full_anim_btn);
    }

    private void startFullBtnAnim(final View animView, final int[] startXy, final int[] endXy,
                                  final AnimationListener animationListener) {
        animView.setVisibility(View.VISIBLE);
        TranslateAnimation anim = new TranslateAnimation(startXy[0], endXy[0], startXy[1], endXy[1]);
        anim.setAnimationListener(animationListener);
        anim.setDuration(ANIMATION_DURING_TIME);
        animView.startAnimation(anim);
    }

    private void setVideoLevel() {
        if (mCameraInfo == null || mEZPlayer == null || mDeviceInfo == null) {
            return;
        }

        if (mDeviceInfo.getStatus() == 1) {
            mRealPlayQualityBtn.setEnabled(true);
        } else {
            mRealPlayQualityBtn.setEnabled(false);
        }

        /************** 本地数据保存 需要更新之前获取到的设备列表信息，开发者自己设置 *********************/
        mCameraInfo.setVideoLevel(mCurrentQulityMode.getVideoLevel());

        // 视频质量，2-高清，1-标清，0-流畅
        if (mCurrentQulityMode.getVideoLevel() == EZVideoLevel.VIDEO_LEVEL_FLUNET.getVideoLevel()) {
            mRealPlayQualityBtn.setText(R.string.quality_flunet);
        } else if (mCurrentQulityMode.getVideoLevel() == EZVideoLevel.VIDEO_LEVEL_BALANCED.getVideoLevel()) {
            mRealPlayQualityBtn.setText(R.string.quality_balanced);
        } else if (mCurrentQulityMode.getVideoLevel() == EZVideoLevel.VIDEO_LEVEL_HD.getVideoLevel()) {
            mRealPlayQualityBtn.setText(R.string.quality_hd);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        mOrientation = newConfig.orientation;

        onOrientationChanged();
        super.onConfigurationChanged(newConfig);
    }

    private void updateOrientation() {
        if (mIsOnTalk) {
            if (mEZPlayer != null && mDeviceInfo != null && mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                setForceOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (mStatus == RealPlayStatus.STATUS_PLAY) {
                setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            } else {
                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
        }
    }

    private void updateOperatorUI() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            // 显示状态栏
            fullScreen(false);
            updateOrientation();
            mPortraitTitleBar.setVisibility(View.VISIBLE);
            mLandscapeTitleBar.setVisibility(View.GONE);
            mRealPlayControlRl.setVisibility(View.VISIBLE);
            if (mRtspUrl == null) {
                mRealPlayPageLy.setBackgroundColor(getResources().getColor(R.color.common_bg));
                mRealPlayOperateBar.setVisibility(View.VISIBLE);
                mRealPlayFullOperateBar.setVisibility(View.GONE);
                mFullscreenFullButton.setVisibility(View.GONE);
                if (mIsRecording) {
                    mRealPlayRecordBtn.setVisibility(View.GONE);
                    mRealPlayRecordStartBtn.setVisibility(View.VISIBLE);
                } else {
                    mRealPlayRecordBtn.setVisibility(View.VISIBLE);
                    mRealPlayRecordStartBtn.setVisibility(View.GONE);
                }
            }
        } else {
            // 隐藏状态栏
            fullScreen(true);
            mPortraitTitleBar.setVisibility(View.GONE);
            // hide the
            mRealPlayControlRl.setVisibility(View.GONE);
            if (!mIsOnTalk && !mIsOnPtz) {
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
            }
            if (mRtspUrl == null) {
                mRealPlayOperateBar.setVisibility(View.GONE);
                mRealPlayPageLy.setBackgroundColor(getResources().getColor(R.color.black_bg));
                mRealPlayFullOperateBar.setVisibility(View.GONE);
                if (!mIsOnTalk && !mIsOnPtz) {
                    mFullscreenFullButton.setVisibility(View.GONE);
                }
                if (mIsRecording) {
                    mRealPlayFullRecordBtn.setVisibility(View.GONE);
                    mRealPlayFullRecordStartBtn.setVisibility(View.VISIBLE);
                } else {
                    mRealPlayFullRecordBtn.setVisibility(View.VISIBLE);
                    mRealPlayFullRecordStartBtn.setVisibility(View.GONE);
                }
            }
        }

        //        mRealPlayControlRl.setVisibility(View.GONE);
        closeQualityPopupWindow();
        if (mStatus == RealPlayStatus.STATUS_START) {
            showControlRlAndFullOperateBar();
        }
    }

    private void updatePtzUI() {
        if (!mIsOnPtz) {
            return;
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setFullPtzStopUI(false);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    openPtzPopupWindow(mRealPlayPlayRl);
                }
            });
        } else {
            closePtzPopupWindow();
            setFullPtzStartUI(false);
        }
    }

    private void updateTalkUI() {
        if (!mIsOnTalk) {
            return;
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mRealPlayFullTalkAnimBtn != null) {
                mRealPlayFullTalkAnimBtn.setVisibility(View.GONE);
                mFullscreenFullButton.setVisibility(View.GONE);
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    openTalkPopupWindow(false);
                }
            });
        } else {
            if (mRealPlayFullTalkAnimBtn != null) {
                mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
                mRealPlayFullOperateBar.post(new Runnable() {

                    @Override
                    public void run() {
                        mRealPlayFullTalkBtn.getLocationInWindow(mStartXy);
                        mEndXy[0] = Utils.dip2px(EZRealPlayActivity.this, 20);
                        mEndXy[1] = mStartXy[1];

                        mRealPlayFullOperateBar.setVisibility(View.GONE);
                        mRealPlayFullTalkAnimBtn.setVisibility(View.VISIBLE);
                        //                        mFullscreenFullButton.setVisibility(View.VISIBLE);
                        ((AnimationDrawable) mRealPlayFullTalkAnimBtn.getBackground()).start();
                    }

                });
            }
            closeTalkPopupWindow(false, false);
        }
    }

    private void fullScreen(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private void onOrientationChanged() {
//        mRealPlaySv.setVisibility(View.INVISIBLE);
        setRealPlaySvLayout();
//        mRealPlaySv.setVisibility(View.VISIBLE);

        updateOperatorUI();
        updateCaptureUI();
        updateTalkUI();
        updatePtzUI();
    }

    /*
     * (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder , int,
     * int, int)
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /*
     * (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder )
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(holder);
        }
        mRealPlaySh = holder;
    }

    /*
     * (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view. SurfaceHolder)
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mEZPlayer != null) {
            mEZPlayer.setSurfaceHold(null);
        }
        mRealPlaySh = null;
    }


    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.realplay_play_btn:
            case R.id.realplay_full_play_btn:
            case R.id.realplay_play_iv:
                if (mStatus != RealPlayStatus.STATUS_STOP) {
                    stopRealPlay();
                    setRealPlayStopUI();
                } else {
                    startRealPlay();
                }
                break;
            case R.id.realplay_previously_btn:
            case R.id.realplay_previously_btn2:
            case R.id.realplay_full_previously_btn:
                onCapturePicBtnClick();
                break;
            case R.id.realplay_capture_rl:
                onCaptureRlClick();
                break;
            case R.id.realplay_video_btn:
            case R.id.realplay_video_start_btn:
            case R.id.realplay_video_btn2:
            case R.id.realplay_video_start_btn2:
            case R.id.realplay_full_video_btn:
            case R.id.realplay_full_video_start_btn:
                onRecordBtnClick();
                break;
            case R.id.realplay_talk_btn:
            case R.id.realplay_talk_btn2:
            case R.id.realplay_full_talk_btn:
                startVoiceTalk();
                break;

            case R.id.realplay_quality_btn:
                openQualityPopupWindow(mRealPlayQualityBtn);
                break;
            case R.id.realplay_ptz_btn:
            case R.id.realplay_ptz_btn2:
                openPtzPopupWindow(mRealPlayPlayRl);
                break;
            case R.id.realplay_full_ptz_btn:
                setFullPtzStartUI(true);
                break;
            case R.id.realplay_full_ptz_anim_btn:
                setFullPtzStopUI(true);
                break;
            case R.id.realplay_sound_btn:
            case R.id.realplay_full_sound_btn:
                onSoundBtnClick();
                break;
            case R.id.realplay_full_talk_anim_btn:
                closeTalkPopupWindow(true, true);
                break;
            default:
                break;
        }
    }

    private void setFullPtzStartUI(boolean startAnim) {
        mIsOnPtz = true;
        setPlayScaleUI(1, null, null);
        if (mLocalInfo.getPtzPromptCount() < 3) {
            mRealPlayFullPtzPromptIv.setBackgroundResource(R.drawable.ptz_prompt);
            mRealPlayFullPtzPromptIv.setVisibility(View.VISIBLE);
            mLocalInfo.setPtzPromptCount(mLocalInfo.getPtzPromptCount() + 1);
            mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
            mHandler.sendEmptyMessageDelayed(MSG_CLOSE_PTZ_PROMPT, 2000);
        }
        if (startAnim) {
            mRealPlayFullAnimBtn.setBackgroundResource(R.drawable.yuntai_pressed);
            mRealPlayFullPtzBtn.getLocationInWindow(mStartXy);
            mEndXy[0] = Utils.dip2px(this, 20);
            mEndXy[1] = mStartXy[1];
            startFullBtnAnim(mRealPlayFullAnimBtn, mStartXy, mEndXy, new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mRealPlayFullPtzAnimBtn.setVisibility(View.VISIBLE);
                    mRealPlayFullAnimBtn.setVisibility(View.GONE);
                    onRealPlaySvClick();
                    //                    mFullscreenFullButton.setVisibility(View.VISIBLE);
                }
            });
        } else {
            mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
            mRealPlayFullOperateBar.post(new Runnable() {

                @Override
                public void run() {
                    mRealPlayFullPtzBtn.getLocationInWindow(mStartXy);
                    mEndXy[0] = Utils.dip2px(EZRealPlayActivity.this, 20);
                    mEndXy[1] = mStartXy[1];

                    mRealPlayFullOperateBar.setVisibility(View.GONE);
                    mRealPlayFullPtzAnimBtn.setVisibility(View.VISIBLE);
                    //                    mFullscreenFullButton.setVisibility(View.VISIBLE);
                }

            });
        }
    }

    private void setFullPtzStopUI(boolean startAnim) {
        mIsOnPtz = false;
        if (startAnim) {
            mRealPlayFullPtzAnimBtn.setVisibility(View.GONE);
            mFullscreenFullButton.setVisibility(View.GONE);
            mRealPlayFullAnimBtn.setBackgroundResource(R.drawable.yuntai_pressed);
            startFullBtnAnim(mRealPlayFullAnimBtn, mEndXy, mStartXy, new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mRealPlayFullAnimBtn.setVisibility(View.GONE);
                    onRealPlaySvClick();
                }
            });
        } else {
            mRealPlayFullPtzAnimBtn.setVisibility(View.GONE);
            mFullscreenFullButton.setVisibility(View.GONE);
        }
        mRealPlayFullPtzPromptIv.setVisibility(View.GONE);
        mHandler.removeMessages(MSG_CLOSE_PTZ_PROMPT);
    }

    private void onSoundBtnClick() {
        if (mLocalInfo.isSoundOpen()) {
            mLocalInfo.setSoundOpen(false);
            mRealPlaySoundBtn.setBackgroundResource(R.drawable.ezopen_vertical_preview_sound_off_selector);
            if (mRealPlayFullSoundBtn != null) {
                mRealPlayFullSoundBtn.setBackgroundResource(R.drawable.play_full_soundoff_btn_selector);
            }
        } else {
            mLocalInfo.setSoundOpen(true);
            mRealPlaySoundBtn.setBackgroundResource(R.drawable.ezopen_vertical_preview_sound_selector);
            if (mRealPlayFullSoundBtn != null) {
                mRealPlayFullSoundBtn.setBackgroundResource(R.drawable.play_full_soundon_btn_selector);
            }
        }

        setRealPlaySound();
    }

    private void setRealPlaySound() {
        if (mEZPlayer != null) {
            if (mRtspUrl == null) {
                if (mLocalInfo.isSoundOpen()) {
                    mEZPlayer.openSound();
                } else {
                    mEZPlayer.closeSound();
                }
            } else {
                if (mRealPlaySquareInfo.mSoundType == 0) {
                    mEZPlayer.closeSound();
                } else {
                    mEZPlayer.openSound();
                }
            }
        }
    }

    /**
     * 设备对讲
     *
     * @see
     * @since V2.0
     */
    private void startVoiceTalk() {
        LogUtil.debugLog(TAG, "startVoiceTalk");
        if (mEZPlayer == null) {
            LogUtil.debugLog(TAG, "EZPlaer is null");
            return;
        }
        if (mCameraInfo == null) {
            return;
        }
        mIsOnTalk = true;

        updateOrientation();

        Utils.showToast(this, R.string.start_voice_talk);
        mRealPlayTalkBtn.setEnabled(false);
        mRealPlayFullTalkBtn.setEnabled(false);
        mRealPlayFullTalkAnimBtn.setEnabled(false);
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            mRealPlayFullAnimBtn.setBackgroundResource(R.drawable.speech_1);
            mRealPlayFullTalkBtn.getLocationInWindow(mStartXy);
            mEndXy[0] = Utils.dip2px(this, 20);
            mEndXy[1] = mStartXy[1];
            startFullBtnAnim(mRealPlayFullAnimBtn, mStartXy, mEndXy, new AnimationListener() {

                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    Utils.showToast(EZRealPlayActivity.this, R.string.realplay_full_talk_start_tip);
                    mRealPlayFullTalkAnimBtn.setVisibility(View.VISIBLE);
                    mRealPlayFullAnimBtn.setVisibility(View.GONE);
                    onRealPlaySvClick();
                    //                    mFullscreenFullButton.setVisibility(View.VISIBLE);
                }
            });
        }

        if (mEZPlayer != null) {
            mEZPlayer.closeSound();
        }
        mEZPlayer.startVoiceTalk();
    }

    /**
     * 停止对讲
     *
     * @see
     * @since V2.0
     */
    private void stopVoiceTalk(boolean startAnim) {
        if (mCameraInfo == null || mEZPlayer == null) {
            return;
        }
        LogUtil.debugLog(TAG, "stopVoiceTalk");

        mEZPlayer.stopVoiceTalk();
        handleVoiceTalkStoped(startAnim);
    }

    private OnClickListener mOnPopWndClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.quality_hd_btn:
                    setQualityMode(EZVideoLevel.VIDEO_LEVEL_HD);
                    break;
                case R.id.quality_balanced_btn:
                    setQualityMode(EZVideoLevel.VIDEO_LEVEL_BALANCED);
                    break;
                case R.id.quality_flunet_btn:
                    setQualityMode(EZVideoLevel.VIDEO_LEVEL_FLUNET);
                    break;
                case R.id.ptz_close_btn:
                    closePtzPopupWindow();
                    break;
                case R.id.ptz_flip_btn:
                    //                    setPtzFlip();
                    break;
                case R.id.talkback_close_btn:
                    closeTalkPopupWindow(true, false);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 云台操作
     *
     * @param command ptz控制命令
     * @param action  控制启动/停止
     */
    private void ptzOption(final EZPTZCommand command, final EZPTZAction action) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean ptz_result = false;
                try {
                    ptz_result = EzvizApplication.getOpenSDK().controlPTZ(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), command,
                            action, EZConstants.PTZ_SPEED_DEFAULT);
                } catch (BaseException e) {
                    e.printStackTrace();
                }
                LogUtil.i(TAG, "controlPTZ ptzCtrl result: " + ptz_result);
            }
        }).start();
    }

    private OnTouchListener mOnTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View view, MotionEvent motionevent) {
            boolean ptz_result = false;
            int action = motionevent.getAction();
            final int speed = EZConstants.PTZ_SPEED_DEFAULT;
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    switch (view.getId()) {
                        case R.id.talkback_control_btn:
                            mTalkRingView.setVisibility(View.VISIBLE);
                            mEZPlayer.setVoiceTalkStatus(true);
                            break;
                        case R.id.ptz_top_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_up_sel);
                            setPtzDirectionIv(RealPlayStatus.PTZ_UP);
                            ptzOption(EZPTZCommand.EZPTZCommandUp, EZPTZAction.EZPTZActionSTART);
                            break;
                        case R.id.ptz_bottom_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bottom_sel);
                            setPtzDirectionIv(RealPlayStatus.PTZ_DOWN);
                            ptzOption(EZPTZCommand.EZPTZCommandDown, EZPTZAction.EZPTZActionSTART);
                            break;
                        case R.id.ptz_left_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_left_sel);
                            setPtzDirectionIv(RealPlayStatus.PTZ_LEFT);
                            ptzOption(EZPTZCommand.EZPTZCommandLeft, EZPTZAction.EZPTZActionSTART);
                            break;
                        case R.id.ptz_right_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_right_sel);
                            setPtzDirectionIv(RealPlayStatus.PTZ_RIGHT);
                            ptzOption(EZPTZCommand.EZPTZCommandRight, EZPTZAction.EZPTZActionSTART);
                            break;
                        default:
                            break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    switch (view.getId()) {
                        case R.id.talkback_control_btn:
                            mEZPlayer.setVoiceTalkStatus(false);
                            mTalkRingView.setVisibility(View.GONE);
                            break;
                        case R.id.ptz_top_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
                            ptzOption(EZPTZCommand.EZPTZCommandUp, EZPTZAction.EZPTZActionSTOP);
                            break;
                        case R.id.ptz_bottom_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
                            ptzOption(EZPTZCommand.EZPTZCommandDown, EZPTZAction.EZPTZActionSTOP);
                            break;
                        case R.id.ptz_left_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
                            ptzOption(EZPTZCommand.EZPTZCommandLeft, EZPTZAction.EZPTZActionSTOP);
                            break;
                        case R.id.ptz_right_btn:
                            mPtzControlLy.setBackgroundResource(R.drawable.ptz_bg);
                            ptzOption(EZPTZCommand.EZPTZCommandRight, EZPTZAction.EZPTZActionSTOP);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    public void setSoundLocalization(int status) {
    }

    /**
     * 码流配置 清晰度 2-高清，1-标清，0-流畅
     *
     * @see
     * @since V2.0
     */
    private void setQualityMode(final EZVideoLevel mode) {
        // 检查网络是否可用
        if (!ConnectionDetector.isNetworkAvailable(EZRealPlayActivity.this)) {
            // 提示没有连接网络
            Utils.showToast(EZRealPlayActivity.this, R.string.realplay_set_fail_network);
            return;
        }

        if (mEZPlayer != null) {
            mWaitDialog.setWaitText(this.getString(R.string.setting_video_level));
            mWaitDialog.show();

            Thread thr = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // need to modify by yudan at 08-11
                        EzvizApplication.getOpenSDK().setVideoLevel(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo(), mode.getVideoLevel());
                        mCurrentQulityMode = mode;
                        Message msg = Message.obtain();
                        msg.what = MSG_SET_VEDIOMODE_SUCCESS;
                        mHandler.sendMessage(msg);
                        LogUtil.i(TAG, "setQualityMode success");
                    } catch (BaseException e) {
                        mCurrentQulityMode = EZVideoLevel.VIDEO_LEVEL_FLUNET;
                        e.printStackTrace();
                        Message msg = Message.obtain();
                        msg.what = MSG_SET_VEDIOMODE_FAIL;
                        mHandler.sendMessage(msg);
                        LogUtil.i(TAG, "setQualityMode fail");
                    }

                }
            }) {
            };
            thr.start();
        }
    }

    /**
     * 打开对讲控制窗口
     *
     * @see
     * @since V1.8.3
     */
    private void openTalkPopupWindow(boolean showAnimation) {
        if (mEZPlayer == null && mDeviceInfo == null) {
            return;
        }
        closeTalkPopupWindow(false, false);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_talkback_wnd, null, true);
        layoutView.setFocusable(true);
        layoutView.setFocusableInTouchMode(true);
        layoutView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
                if (arg1 == KeyEvent.KEYCODE_BACK) {
                    LogUtil.infoLog(TAG, "KEYCODE_BACK DOWN");
                    closeTalkPopupWindow(true, false);
                }
                return false;
            }
        });

        ImageButton talkbackCloseBtn = (ImageButton) layoutView.findViewById(R.id.talkback_close_btn);
        talkbackCloseBtn.setOnClickListener(mOnPopWndClickListener);
        mTalkRingView = (RingView) layoutView.findViewById(R.id.talkback_rv);
        mTalkBackControlBtn = (Button) layoutView.findViewById(R.id.talkback_control_btn);
        mTalkBackControlBtn.setOnTouchListener(mOnTouchListener);

        if (mDeviceInfo.isSupportTalk() == EZConstants.EZTalkbackCapability.EZTalkbackFullDuplex) {
            mTalkRingView.setVisibility(View.VISIBLE);
            mTalkBackControlBtn.setEnabled(false);
            mTalkBackControlBtn.setText(R.string.talking);
        }

        int height = mLocalInfo.getScreenHeight() - mPortraitTitleBar.getHeight() - mRealPlayPlayRl.getHeight()
                - (mRealPlayRect != null ? mRealPlayRect.top : mLocalInfo.getNavigationBarHeight());
        mTalkPopupWindow = new PopupWindow(layoutView, LayoutParams.MATCH_PARENT, height, true);
        // mTalkPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        if (showAnimation) {
            mTalkPopupWindow.setAnimationStyle(R.style.popwindowUpAnim);
        }
        mTalkPopupWindow.setFocusable(false);
        mTalkPopupWindow.setOutsideTouchable(false);
//        mTalkPopupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        mTalkPopupWindow.showAsDropDown(mRealPlayPlayRl);
        // mTalkPopupWindow.setOnDismissListener(new OnDismissListener() {
        //
        // @Override
        // public void onDismiss() {
        // LogUtil.infoLog(TAG, "KEYCODE_BACK DOWN");
        // mTalkPopupWindow = null;
        // closeTalkPopupWindow();
        // }
        // });
        mTalkPopupWindow.update();
        mTalkRingView.post(new Runnable() {
            @Override
            public void run() {
                if (mTalkRingView != null) {
                    mTalkRingView.setMinRadiusAndDistance(mTalkBackControlBtn.getHeight() / 2f,
                            Utils.dip2px(EZRealPlayActivity.this, 22));
                }
            }
        });
    }

    private void closeTalkPopupWindow(boolean stopTalk, boolean startAnim) {
        if (mTalkPopupWindow != null) {
            LogUtil.infoLog(TAG, "closeTalkPopupWindow");
            dismissPopWindow(mTalkPopupWindow);
            mTalkPopupWindow = null;
        }
        mTalkRingView = null;
        if (stopTalk)
            stopVoiceTalk(startAnim);
    }

    /**
     * 打开云台控制窗口
     *
     * @see
     * @since V1.8.3
     */
    private void openPtzPopupWindow(View parent) {
        closePtzPopupWindow();
        mIsOnPtz = true;
        setPlayScaleUI(1, null, null);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_ptz_wnd, null, true);

        mPtzControlLy = (LinearLayout) layoutView.findViewById(R.id.ptz_control_ly);
        ImageButton ptzCloseBtn = (ImageButton) layoutView.findViewById(R.id.ptz_close_btn);
        ptzCloseBtn.setOnClickListener(mOnPopWndClickListener);
        ImageButton ptzTopBtn = (ImageButton) layoutView.findViewById(R.id.ptz_top_btn);
        ptzTopBtn.setOnTouchListener(mOnTouchListener);
        ImageButton ptzBottomBtn = (ImageButton) layoutView.findViewById(R.id.ptz_bottom_btn);
        ptzBottomBtn.setOnTouchListener(mOnTouchListener);
        ImageButton ptzLeftBtn = (ImageButton) layoutView.findViewById(R.id.ptz_left_btn);
        ptzLeftBtn.setOnTouchListener(mOnTouchListener);
        ImageButton ptzRightBtn = (ImageButton) layoutView.findViewById(R.id.ptz_right_btn);
        ptzRightBtn.setOnTouchListener(mOnTouchListener);
        ImageButton ptzFlipBtn = (ImageButton) layoutView.findViewById(R.id.ptz_flip_btn);
        ptzFlipBtn.setOnClickListener(mOnPopWndClickListener);

        int height = mLocalInfo.getScreenHeight() - mPortraitTitleBar.getHeight() - mRealPlayPlayRl.getHeight()
                - (mRealPlayRect != null ? mRealPlayRect.top : mLocalInfo.getNavigationBarHeight());
        mPtzPopupWindow = new PopupWindow(layoutView, LayoutParams.MATCH_PARENT, height, true);
        mPtzPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPtzPopupWindow.setAnimationStyle(R.style.popwindowUpAnim);
        mPtzPopupWindow.setFocusable(true);
        mPtzPopupWindow.setOutsideTouchable(true);
        mPtzPopupWindow.showAsDropDown(parent);
        mPtzPopupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                LogUtil.infoLog(TAG, "KEYCODE_BACK DOWN");
                mPtzPopupWindow = null;
                mPtzControlLy = null;
                closePtzPopupWindow();
            }
        });
        mPtzPopupWindow.update();
    }

    private void closePtzPopupWindow() {
        mIsOnPtz = false;
        if (mPtzPopupWindow != null) {
            dismissPopWindow(mPtzPopupWindow);
            mPtzPopupWindow = null;
            mPtzControlLy = null;
            setForceOrientation(0);
        }
    }

    private void openQualityPopupWindow(View anchor) {
        if (mEZPlayer == null) {
            return;
        }
        closeQualityPopupWindow();
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewGroup layoutView = (ViewGroup) layoutInflater.inflate(R.layout.realplay_quality_items, null, true);

        Button qualityHdBtn = (Button) layoutView.findViewById(R.id.quality_hd_btn);
        qualityHdBtn.setOnClickListener(mOnPopWndClickListener);
        Button qualityBalancedBtn = (Button) layoutView.findViewById(R.id.quality_balanced_btn);
        qualityBalancedBtn.setOnClickListener(mOnPopWndClickListener);
        Button qualityFlunetBtn = (Button) layoutView.findViewById(R.id.quality_flunet_btn);
        qualityFlunetBtn.setOnClickListener(mOnPopWndClickListener);

        // 视频质量，2-高清，1-标清，0-流畅
        if (mCameraInfo.getVideoLevel() == EZVideoLevel.VIDEO_LEVEL_FLUNET) {
            qualityFlunetBtn.setEnabled(false);
        } else if (mCameraInfo.getVideoLevel() == EZVideoLevel.VIDEO_LEVEL_BALANCED) {
            qualityBalancedBtn.setEnabled(false);
        } else if (mCameraInfo.getVideoLevel() == EZVideoLevel.VIDEO_LEVEL_HD) {
            qualityHdBtn.setEnabled(false);
        }

        int height = 105;

        qualityFlunetBtn.setVisibility(View.VISIBLE);
        qualityBalancedBtn.setVisibility(View.VISIBLE);
        qualityHdBtn.setVisibility(View.VISIBLE);

        height = Utils.dip2px(this, height);
        mQualityPopupWindow = new PopupWindow(layoutView, LayoutParams.WRAP_CONTENT, height, true);
        mQualityPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mQualityPopupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                LogUtil.infoLog(TAG, "KEYCODE_BACK DOWN");
                mQualityPopupWindow = null;
                closeQualityPopupWindow();
            }
        });
        try {
            mQualityPopupWindow.showAsDropDown(anchor, -Utils.dip2px(this, 5),
                    -(height + anchor.getHeight() + Utils.dip2px(this, 8)));
        } catch (Exception e) {
            e.printStackTrace();
            closeQualityPopupWindow();
        }
    }

    private void closeQualityPopupWindow() {
        if (mQualityPopupWindow != null) {
            dismissPopWindow(mQualityPopupWindow);
            mQualityPopupWindow = null;
        }
    }

    /**
     * 开始录像
     *
     * @see
     * @since V1.0
     */
    private void onRecordBtnClick() {
        mControlDisplaySec = 0;
        if (mIsRecording) {
            stopRealPlayRecord();
            return;
        }

        if (!SDCardUtil.isSDCardUseable()) {
            // 提示SD卡不可用
            Utils.showToast(EZRealPlayActivity.this, R.string.remoteplayback_SDCard_disable_use);
            return;
        }

        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
            // 提示内存不足
            Utils.showToast(EZRealPlayActivity.this, R.string.remoteplayback_record_fail_for_memory);
            return;
        }

        if (mEZPlayer != null) {
            mCaptureDisplaySec = 4;
            updateCaptureUI();

            mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);

            // 可以采用deviceSerial+时间作为文件命名，demo中简化，只用时间命名
            java.util.Date date = new java.util.Date();
            String strRecordFile = Environment.getExternalStorageDirectory().getPath() + "/EZOpenSDK/Records/" + String.format("%tY", date)
                    + String.format("%tm", date) + String.format("%td", date) + "/"
                    + String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) + ".mp4";

            if (mEZPlayer.startLocalRecordWithFile(strRecordFile))
            {
                handleRecordSuccess(strRecordFile);
            } else {
                handleRecordFail();
            }
        }
    }

    /**
     * 停止录像
     *
     * @see
     * @since V1.0
     */
    private void stopRealPlayRecord() {
        if (mEZPlayer == null || !mIsRecording) {
            return;
        }
        Toast.makeText(EZRealPlayActivity.this, getResources().getString(R.string.already_saved_to_volume), Toast.LENGTH_SHORT).show();

        // 设置录像按钮为check状态
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRealPlayRecordContainer, mRealPlayRecordStartBtn,
                        mRealPlayRecordBtn, 0, 90);
            } else {
                mRealPlayRecordStartBtn.setVisibility(View.GONE);
                mRealPlayRecordBtn.setVisibility(View.VISIBLE);
            }
            mRealPlayFullRecordStartBtn.setVisibility(View.GONE);
            mRealPlayFullRecordBtn.setVisibility(View.VISIBLE);
        } else {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRealPlayFullRecordContainer, mRealPlayFullRecordStartBtn,
                        mRealPlayFullRecordBtn, 0, 90);
            } else {
                mRealPlayFullRecordStartBtn.setVisibility(View.GONE);
                mRealPlayFullRecordBtn.setVisibility(View.VISIBLE);

            }
            mRealPlayRecordStartBtn.setVisibility(View.GONE);
            mRealPlayRecordBtn.setVisibility(View.VISIBLE);
        }
        mAudioPlayUtil.playAudioFile(AudioPlayUtil.RECORD_SOUND);
        mEZPlayer.stopLocalRecord();

        // 计时按钮不可见
        mRealPlayRecordLy.setVisibility(View.GONE);
        //        mRealPlayCaptureRl.setVisibility(View.VISIBLE);
        mCaptureDisplaySec = 0;
        //        try {
        //            mRealPlayCaptureIv.setImageURI(Uri.parse(mRecordFilePath));
        //        } catch (OutOfMemoryError e) {
        //            e.printStackTrace();
        //        }
        //        mRealPlayCaptureWatermarkIv.setTag(mRecordFilePath);
        mIsRecording = false;
        updateCaptureUI();
    }

    private void onCaptureRlClick() {
    }

    /**
     * 抓拍按钮响应函数
     *
     * @since V1.0
     */
    private void onCapturePicBtnClick() {

        mControlDisplaySec = 0;
        if (!SDCardUtil.isSDCardUseable()) {
            // 提示SD卡不可用
            Utils.showToast(EZRealPlayActivity.this, R.string.remoteplayback_SDCard_disable_use);
            return;
        }

        if (SDCardUtil.getSDCardRemainSize() < SDCardUtil.PIC_MIN_MEM_SPACE) {
            // 提示内存不足
            Utils.showToast(EZRealPlayActivity.this, R.string.remoteplayback_capture_fail_for_memory);
            return;
        }

        if (mEZPlayer != null) {
            mCaptureDisplaySec = 4;
            updateCaptureUI();

            Thread thr = new Thread() {
                @Override
                public void run() {
                    Bitmap bmp = mEZPlayer.capturePicture();
                    if (bmp != null) {
                        try {
                            mAudioPlayUtil.playAudioFile(AudioPlayUtil.CAPTURE_SOUND);

                            // 可以采用deviceSerial+时间作为文件命名，demo中简化，只用时间命名
                            java.util.Date date = new java.util.Date();
                            final String path = Environment.getExternalStorageDirectory().getPath() + "/EZOpenSDK/CapturePicture/" + String.format("%tY", date)
                                    + String.format("%tm", date) + String.format("%td", date) + "/"
                                    + String.format("%tH", date) + String.format("%tM", date) + String.format("%tS", date) + String.format("%tL", date) +".jpg";

                            if (TextUtils.isEmpty(path)) {
                                bmp.recycle();
                                bmp = null;
                                return;
                            }
                            EZUtils.saveCapturePictrue(path, bmp);


                            MediaScanner mMediaScanner = new MediaScanner(EZRealPlayActivity.this);
                            mMediaScanner.scanFile(path, "jpg");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(EZRealPlayActivity.this, getResources().getString(R.string.already_saved_to_volume)+path, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (InnerException e) {
                            e.printStackTrace();
                        } finally {
                            if (bmp != null) {
                                bmp.recycle();
                                bmp = null;
                                return;
                            }
                        }
                    }
                    super.run();
                }
            };
            thr.start();
        }
    }

    private void onRealPlaySvClick() {
        if (mCameraInfo != null && mEZPlayer != null && mDeviceInfo != null) {
            if (mDeviceInfo.getStatus() != 1) {
                return;
            }
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                setRealPlayControlRlVisibility();
            } else {
                setRealPlayFullOperateBarVisibility();
            }
        } else if (mRtspUrl != null) {
            setRealPlayControlRlVisibility();
        }
    }

    private void setRealPlayControlRlVisibility() {
        if (mLandscapeTitleBar.getVisibility() == View.VISIBLE || mRealPlayControlRl.getVisibility() == View.VISIBLE) {
            //            mRealPlayControlRl.setVisibility(View.GONE);
            mLandscapeTitleBar.setVisibility(View.GONE);
            closeQualityPopupWindow();
        } else {
            mRealPlayControlRl.setVisibility(View.VISIBLE);
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!mIsOnTalk && !mIsOnPtz) {
                    mLandscapeTitleBar.setVisibility(View.VISIBLE);
                }
            } else {
                mLandscapeTitleBar.setVisibility(View.GONE);
            }
            mControlDisplaySec = 0;
        }
    }

    private void setRealPlayFullOperateBarVisibility() {
        if (mLandscapeTitleBar.getVisibility() == View.VISIBLE) {
            mRealPlayFullOperateBar.setVisibility(View.GONE);
            if (!mIsOnTalk && !mIsOnPtz) {
                mFullscreenFullButton.setVisibility(View.GONE);
            }
            mLandscapeTitleBar.setVisibility(View.GONE);
        } else {
            if (!mIsOnTalk && !mIsOnPtz) {
                //mj mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
                //                mFullscreenFullButton.setVisibility(View.VISIBLE);
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
            }
            mControlDisplaySec = 0;
        }
    }

    /**
     * 开始播放
     *
     * @see
     * @since V2.0
     */
    private void startRealPlay() {
        // 增加手机客户端操作信息记录
        LogUtil.debugLog(TAG, "startRealPlay");

        if (mStatus == RealPlayStatus.STATUS_START || mStatus == RealPlayStatus.STATUS_PLAY) {
            return;
        }

        // 检查网络是否可用
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            // 提示没有连接网络
            setRealPlayFailUI(getString(R.string.realplay_play_fail_becauseof_network));
            return;
        }

        mStatus = RealPlayStatus.STATUS_START;
        setRealPlayLoadingUI();

        if (mCameraInfo != null) {
            if (mEZPlayer == null) {
                mEZPlayer = EzvizApplication.getOpenSDK().createPlayer(mCameraInfo.getDeviceSerial(), mCameraInfo.getCameraNo());
            }

            if (mEZPlayer == null)
                return;
            if (mDeviceInfo == null) {
                return;
            }
            if (mDeviceInfo.getIsEncrypt() == 1) {
                mEZPlayer.setPlayVerifyCode(DataManager.getInstance().getDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial()));
            }

            mEZPlayer.setHandler(mHandler);
            mEZPlayer.setSurfaceHold(mRealPlaySh);
            mEZPlayer.startRealPlay();
        } else if (mRtspUrl != null) {
            mEZPlayer = EzvizApplication.getOpenSDK().createPlayerWithUrl(mRtspUrl);
            //mStub.setCameraId(mCameraInfo.getCameraId());////****  mj
            if (mEZPlayer == null)
                return;
            mEZPlayer.setHandler(mHandler);
            mEZPlayer.setSurfaceHold(mRealPlaySh);

            mEZPlayer.startRealPlay();
        }
        updateLoadingProgress(0);
    }

    /**
     * 停止播放
     *
     * @see
     * @since V1.0
     */
    private void stopRealPlay() {
        LogUtil.debugLog(TAG, "stopRealPlay");
        mStatus = RealPlayStatus.STATUS_STOP;

        stopUpdateTimer();
        if (mEZPlayer != null) {
            stopRealPlayRecord();

            mEZPlayer.stopRealPlay();
        }
        mStreamFlow = 0;
    }

    private void setRealPlayLoadingUI() {
        mStartTime = System.currentTimeMillis();
        mRealPlaySv.setVisibility(View.INVISIBLE);
        mRealPlaySv.setVisibility(View.VISIBLE);
        setStartloading();
        mRealPlayBtn.setBackgroundResource(R.drawable.play_stop_selector);

        if (mCameraInfo != null  && mDeviceInfo != null) {
            mRealPlayCaptureBtn.setEnabled(false);
            mRealPlayRecordBtn.setEnabled(false);
            if (mDeviceInfo.getStatus() == 1) {
                mRealPlayQualityBtn.setEnabled(true);
            } else {
                mRealPlayQualityBtn.setEnabled(false);
            }
            mRealPlayPtzBtn.setEnabled(false);

            mRealPlayFullPlayBtn.setBackgroundResource(R.drawable.play_full_stop_selector);
            mRealPlayFullCaptureBtn.setEnabled(false);
            mRealPlayFullRecordBtn.setEnabled(false);
            mRealPlayFullFlowLy.setVisibility(View.GONE);
            mRealPlayFullPtzBtn.setEnabled(false);
        }

        showControlRlAndFullOperateBar();
    }

    private void showControlRlAndFullOperateBar() {
        if (mRtspUrl != null || mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            mRealPlayControlRl.setVisibility(View.VISIBLE);
            if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (!mIsOnTalk && !mIsOnPtz) {
                    mLandscapeTitleBar.setVisibility(View.VISIBLE);
                }
            } else {
                mLandscapeTitleBar.setVisibility(View.GONE);
            }
            mControlDisplaySec = 0;
        } else {
            if (!mIsOnTalk && !mIsOnPtz) {
                mRealPlayFullOperateBar.setVisibility(View.VISIBLE);
                //                mFullscreenFullButton.setVisibility(View.VISIBLE);
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
            }
            mControlDisplaySec = 0;
        }
    }

    private void setRealPlayStopUI() {
        stopUpdateTimer();
        updateOrientation();
        setRealPlaySvLayout();
        setStopLoading();
        hideControlRlAndFullOperateBar(true);
        mRealPlayBtn.setBackgroundResource(R.drawable.play_play_selector);
        if (mCameraInfo != null && mDeviceInfo != null) {
            closePtzPopupWindow();
            setFullPtzStopUI(false);
            mRealPlayCaptureBtn.setEnabled(false);
            mRealPlayRecordBtn.setEnabled(false);
            if (mDeviceInfo.getStatus() == 1) {
                mRealPlayQualityBtn.setEnabled(true);
            } else {
                mRealPlayQualityBtn.setEnabled(false);
            }
            mRealPlayFullPtzBtn.setEnabled(false);
            if (mDeviceInfo.getStatus() == 1) {
                mRealPlayPrivacyBtn.setEnabled(true);
                mRealPlaySslBtn.setEnabled(true);
            } else {
                mRealPlayPrivacyBtn.setEnabled(false);
                mRealPlaySslBtn.setEnabled(false);
            }

            mRealPlayFullPlayBtn.setBackgroundResource(R.drawable.play_full_play_selector);
            mRealPlayFullCaptureBtn.setEnabled(false);
            mRealPlayFullRecordBtn.setEnabled(false);
            mRealPlayPtzBtn.setEnabled(false);
        }
    }

    private void setRealPlayFailUI(String errorStr) {
        mStopTime = System.currentTimeMillis();
        showType();

        stopUpdateTimer();
        updateOrientation();

        {
            setLoadingFail(errorStr);
        }
        mRealPlayFullFlowLy.setVisibility(View.GONE);
        mRealPlayBtn.setBackgroundResource(R.drawable.play_play_selector);

        hideControlRlAndFullOperateBar(true);

        if (mCameraInfo != null && mDeviceInfo != null) {
            closePtzPopupWindow();
            setFullPtzStopUI(false);

            mRealPlayCaptureBtn.setEnabled(false);
            mRealPlayRecordBtn.setEnabled(false);
            if (mDeviceInfo.getStatus() == 1 && (mEZPlayer == null)) {
                mRealPlayQualityBtn.setEnabled(true);
            } else {
                mRealPlayQualityBtn.setEnabled(false);
            }
            mRealPlayPtzBtn.setEnabled(false);
            if (mDeviceInfo.getStatus() == 1) {
                mRealPlayPrivacyBtn.setEnabled(true);
                mRealPlaySslBtn.setEnabled(true);
            } else {
                mRealPlayPrivacyBtn.setEnabled(false);
                mRealPlaySslBtn.setEnabled(false);
            }

            mRealPlayFullPlayBtn.setBackgroundResource(R.drawable.play_full_play_selector);
            mRealPlayFullCaptureBtn.setEnabled(false);
            mRealPlayFullRecordBtn.setEnabled(false);
            mRealPlayFullPtzBtn.setEnabled(false);
        }
    }

    private void setRealPlaySuccessUI() {
        mStopTime = System.currentTimeMillis();
        showType();

        updateOrientation();
        setLoadingSuccess();
        mRealPlayFlowTv.setVisibility(View.VISIBLE);
        mRealPlayFullFlowLy.setVisibility(View.VISIBLE);
        mRealPlayBtn.setBackgroundResource(R.drawable.play_stop_selector);

        if (mCameraInfo != null && mDeviceInfo != null) {
            mRealPlayCaptureBtn.setEnabled(true);
            mRealPlayRecordBtn.setEnabled(true);
            if (mDeviceInfo.getStatus() == 1) {
                mRealPlayQualityBtn.setEnabled(true);
            } else {
                mRealPlayQualityBtn.setEnabled(false);
            }
            if (getSupportPtz() == 1) {
                mRealPlayPtzBtn.setEnabled(true);
            }

            mRealPlayFullPlayBtn.setBackgroundResource(R.drawable.play_full_stop_selector);
            mRealPlayFullCaptureBtn.setEnabled(true);
            mRealPlayFullRecordBtn.setEnabled(true);
            mRealPlayFullPtzBtn.setEnabled(true);
        }

//        setRealPlaySound();

        startUpdateTimer();
    }

    /**
     * 更新流量统计
     *
     * @see
     * @since V1.0
     */
    private void checkRealPlayFlow() {
        if ((mEZPlayer != null && mRealPlayFlowTv.getVisibility() == View.VISIBLE)) {
            // 更新流量数据
            long streamFlow = mEZPlayer.getStreamFlow();
            updateRealPlayFlowTv(streamFlow);
        }
    }

    private void updateRealPlayFlowTv(long streamFlow) {
        long streamFlowUnit = streamFlow - mStreamFlow;
        if (streamFlowUnit < 0)
            streamFlowUnit = 0;
        float fKBUnit = (float) streamFlowUnit / (float) Constant.KB;
        String descUnit = String.format("%.2f k/s ", fKBUnit);
        // 显示流量
        mRealPlayFlowTv.setText(descUnit);
        mStreamFlow = streamFlow;
    }


    private void setOrientation(int sensor) {
        if (mForceOrientation != 0) {
            LogUtil.debugLog(TAG, "setOrientation mForceOrientation:" + mForceOrientation);
            return;
        }

        if (sensor == ActivityInfo.SCREEN_ORIENTATION_SENSOR)
            mScreenOrientationHelper.enableSensorOrientation();
        else
            mScreenOrientationHelper.disableSensorOrientation();
    }

    public void setForceOrientation(int orientation) {
        if (mForceOrientation == orientation) {
            LogUtil.debugLog(TAG, "setForceOrientation no change");
            return;
        }
        mForceOrientation = orientation;
        if (mForceOrientation != 0) {
            if (mForceOrientation != mOrientation) {
                if (mForceOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                    mScreenOrientationHelper.portrait();
                } else {
                    mScreenOrientationHelper.landscape();
                }
            }
            mScreenOrientationHelper.disableSensorOrientation();
        } else {
            updateOrientation();
        }
    }

    /*
     * (non-Javadoc)
     * @see android.os.Handler.Callback#handleMessage(android.os.Message)
     */
    @SuppressLint("NewApi")
    @Override
    public boolean handleMessage(Message msg) {
        // LogUtil.infoLog(TAG, "handleMessage:" + msg.what);
        if (this.isFinishing()) {
            return false;
        }
        switch (msg.what) {
            case EZRealPlayConstants.MSG_GET_CAMERA_INFO_SUCCESS:
                updateLoadingProgress(20);
                handleGetCameraInfoSuccess();
                break;
            case EZRealPlayConstants.MSG_REALPLAY_PLAY_START:
                updateLoadingProgress(40);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_CONNECTION_START:
                updateLoadingProgress(60);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_CONNECTION_SUCCESS:
                updateLoadingProgress(80);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_PLAY_SUCCESS:
                handlePlaySuccess(msg);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_PLAY_FAIL:
                handlePlayFail(msg.obj);
                break;
            case EZRealPlayConstants.MSG_SET_VEDIOMODE_SUCCESS:
                handleSetVedioModeSuccess();
                break;
            case EZRealPlayConstants.MSG_SET_VEDIOMODE_FAIL:
                handleSetVedioModeFail(msg.arg1);
                break;
            case EZRealPlayConstants.MSG_PTZ_SET_FAIL:
                handlePtzControlFail(msg);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_SUCCESS:
                handleVoiceTalkSucceed();
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_STOP:
                handleVoiceTalkStoped(false);
                break;
            case EZRealPlayConstants.MSG_REALPLAY_VOICETALK_FAIL:
                ErrorInfo errorInfo = (ErrorInfo) msg.obj;
                handleVoiceTalkFailed(errorInfo);
                break;
            case MSG_PLAY_UI_UPDATE:
                updateRealPlayUI();
                break;
            case MSG_AUTO_START_PLAY:
                startRealPlay();
                break;
            case MSG_CLOSE_PTZ_PROMPT:
                mRealPlayFullPtzPromptIv.setVisibility(View.GONE);
                break;
            case MSG_HIDE_PTZ_DIRECTION:
                handleHidePtzDirection(msg);
                break;
            case MSG_HIDE_PAGE_ANIM:
                hidePageAnim();
                break;
            case MSG_PLAY_UI_REFRESH:
                initUI();
                break;
            case MSG_PREVIEW_START_PLAY:
                mPageAnimIv.setVisibility(View.GONE);
                mRealPlayPreviewTv.setVisibility(View.GONE);
                mStatus = RealPlayStatus.STATUS_INIT;
                startRealPlay();
                break;
            default:
                break;
        }
        return false;
    }

    private void handleHidePtzDirection(Message msg) {
        if (mHandler == null) {
            return;
        }
        mHandler.removeMessages(MSG_HIDE_PTZ_DIRECTION);
        if (msg.arg1 > 2) {
            mRealPlayPtzDirectionIv.setVisibility(View.GONE);
        } else {
            mRealPlayPtzDirectionIv.setVisibility(msg.arg1 == 1 ? View.GONE : View.VISIBLE);
            Message message = new Message();
            message.what = MSG_HIDE_PTZ_DIRECTION;
            message.arg1 = msg.arg1 + 1;
            mHandler.sendMessageDelayed(message, 500);
        }
    }

    private void handlePtzControlFail(Message msg) {
        LogUtil.debugLog(TAG, "handlePtzControlFail:" + msg.arg1);
        switch (msg.arg1) {
            case ErrorCode.ERROR_CAS_PTZ_CONTROL_CALLING_PRESET_FAILED:// 正在调用预置点，键控动作无效
                Utils.showToast(EZRealPlayActivity.this, R.string.camera_lens_too_busy, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_PRESETING_FAILE:// 当前正在调用预置点
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_is_preseting, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_CONTROL_TIMEOUT_SOUND_LACALIZATION_FAILED:// 当前正在声源定位
                break;
            case ErrorCode.ERROR_CAS_PTZ_CONTROL_TIMEOUT_CRUISE_TRACK_FAILED:// 键控动作超时(当前正在轨迹巡航)
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_control_timeout_cruise_track_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_INVALID_POSITION_FAILED:// 当前预置点信息无效
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_preset_invalid_position_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_CURRENT_POSITION_FAILED:// 该预置点已是当前位置
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_preset_current_position_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_SOUND_LOCALIZATION_FAILED:// 设备正在响应本次声源定位
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_preset_sound_localization_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_OPENING_PRIVACY_FAILED:// 当前正在开启隐私遮蔽
            case ErrorCode.ERROR_CAS_PTZ_CLOSING_PRIVACY_FAILED:// 当前正在关闭隐私遮蔽
            case ErrorCode.ERROR_CAS_PTZ_MIRRORING_FAILED:// 设备正在镜像操作（设备镜像要几秒钟，防止频繁镜像操作）
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_operation_too_frequently, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_CONTROLING_FAILED:// 设备正在键控动作（上下左右）(一个客户端在上下左右控制，另外一个在开其它东西)
                break;
            case ErrorCode.ERROR_CAS_PTZ_FAILED:// 云台当前操作失败
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRESET_EXCEED_MAXNUM_FAILED:// 当前预置点超过最大个数
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_preset_exceed_maxnum_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_PRIVACYING_FAILED:// 设备处于隐私遮蔽状态（关闭了镜头，再去操作云台相关）
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_privacying_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_TTSING_FAILED:// 设备处于语音对讲状态(区别以前的语音对讲错误码，云台单独列一个）
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_mirroring_failed, msg.arg1);
                break;
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_UP_LIMIT_FAILED:// 设备云台旋转到达上限位
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_DOWN_LIMIT_FAILED:// 设备云台旋转到达下限位
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_LEFT_LIMIT_FAILED:// 设备云台旋转到达左限位
            case ErrorCode.ERROR_CAS_PTZ_ROTATION_RIGHT_LIMIT_FAILED:// 设备云台旋转到达右限位
                setPtzDirectionIv(-1, msg.arg1);
                break;
            default:
                Utils.showToast(EZRealPlayActivity.this, R.string.ptz_operation_failed, msg.arg1);
                break;
        }
    }

    private void hidePageAnim() {
        mHandler.removeMessages(MSG_HIDE_PAGE_ANIM);
        if (mPageAnimDrawable != null) {
            if (mPageAnimDrawable.isRunning()) {
                mPageAnimDrawable.stop();
            }
            mPageAnimDrawable = null;
            mPageAnimIv.setBackgroundDrawable(null);
            mPageAnimIv.setVisibility(View.GONE);
        }
        if (mPageAnimIv != null) {
            mPageAnimIv.setBackgroundDrawable(null);
            mPageAnimIv.setVisibility(View.GONE);
        }
    }

    private void setRealPlayTalkUI() {
        if (mEZPlayer != null && mDeviceInfo != null && (mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport)) {
            mRealPlayTalkBtnLy.setVisibility(View.VISIBLE);
            if (mCameraInfo != null && mDeviceInfo.getStatus() == 1) {
                mRealPlayTalkBtn.setEnabled(true);
            } else {
                mRealPlayTalkBtn.setEnabled(false);
            }
            if (mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport) {
                mRealPlayFullTalkBtn.setVisibility(View.VISIBLE);
            } else {
                mRealPlayFullTalkBtn.setVisibility(View.GONE);
            }
        } else {
            mRealPlayTalkBtnLy.setVisibility(View.GONE);
            mRealPlayFullTalkBtn.setVisibility(View.GONE);
        }
        mRealPlayTalkBtnLy.setVisibility(View.VISIBLE);
        //mRealPlayTalkBtn.setEnabled(false);
    }

    private void updatePermissionUI() {
        mRealPlayTalkBtnLy.setVisibility(View.VISIBLE);
    }

    private void updateUI() {
        //通过能力级设置
        setRealPlayTalkUI();

        setVideoLevel();
        
/*
        if (mRealPlayMgr != null && mRealPlayMgr.getSupportPtzPrivacy() == 1) {
            mRealPlayPrivacyBtnLy.setVisibility(View.VISIBLE);
            if (mCameraInfo.getOnlineStatus() == 1 && mRealPlayMgr.getPrivacyStatus() != 1) {
                mRealPlayQualityBtn.setEnabled(true);
            } else {
                mRealPlayQualityBtn.setEnabled(false);
            }
        } else {
            mRealPlayPrivacyBtnLy.setVisibility(View.GONE);
        }
        */
        //        mRealPlayPrivacyBtnLy.setVisibility(View.GONE);
        //        setSoundLocalizationUI();

        {
            mRealPlaySslBtnLy.setVisibility(View.GONE);
        }

        if (getSupportPtz() == 1) {
            mRealPlayPtzBtnLy.setVisibility(View.VISIBLE);
            mRealPlayFullPtzBtn.setVisibility(View.VISIBLE);
        } else {
            //mRealPlayPtzBtnLy.setVisibility(View.GONE);
            //mRealPlayFullPtzBtn.setVisibility(View.GONE);
            mRealPlayPtzBtnLy.setEnabled(false);
            mRealPlayFullPtzBtn.setEnabled(false);
        }

        updatePermissionUI();
    }

    /**
     * 获取设备信息成功
     *
     * @see
     * @since V1.0
     */
    private void handleGetCameraInfoSuccess() {
        LogUtil.infoLog(TAG, "handleGetCameraInfoSuccess");

        //通过能力级设置
        updateUI();

    }

    private void handleVoiceTalkSucceed() {
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            openTalkPopupWindow(true);
        } else {
            mRealPlayFullTalkAnimBtn.setVisibility(View.VISIBLE);
            //            mFullscreenFullButton.setVisibility(View.VISIBLE);
            ((AnimationDrawable) mRealPlayFullTalkAnimBtn.getBackground()).start();
        }

        mRealPlayTalkBtn.setEnabled(true);
        mRealPlayFullTalkBtn.setEnabled(true);
        mRealPlayFullTalkAnimBtn.setEnabled(true);
    }

    private void handleVoiceTalkFailed(ErrorInfo errorInfo) {
        LogUtil.debugLog(TAG, "Talkback failed. " + errorInfo.toString());

        closeTalkPopupWindow(true, false);

        switch (errorInfo.errorCode) {
            case ErrorCode.ERROR_TRANSF_DEVICE_TALKING:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_fail_ison);
                break;
            case ErrorCode.ERROR_TRANSF_DEVICE_PRIVACYON:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_fail_privacy);
                break;
            case ErrorCode.ERROR_TRANSF_DEVICE_OFFLINE:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_fail_device_not_exist);
                break;
            case ErrorCode.ERROR_TTS_MSG_REQ_TIMEOUT:
            case ErrorCode.ERROR_TTS_MSG_SVR_HANDLE_TIMEOUT:
            case ErrorCode.ERROR_TTS_WAIT_TIMEOUT:
            case ErrorCode.ERROR_TTS_HNADLE_TIMEOUT:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_request_timeout, errorInfo.errorCode);
                break;
            case ErrorCode.ERROR_CAS_AUDIO_SOCKET_ERROR:
            case ErrorCode.ERROR_CAS_AUDIO_RECV_ERROR:
            case ErrorCode.ERROR_CAS_AUDIO_SEND_ERROR:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_network_exception, errorInfo.errorCode);
                break;
            default:
                Utils.showToast(EZRealPlayActivity.this, R.string.realplay_play_talkback_fail, errorInfo.errorCode);
                break;
        }
    }

    private void handleVoiceTalkStoped(boolean startAnim) {
        if (mIsOnTalk) {
            mIsOnTalk = false;
            setForceOrientation(0);
        }
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (startAnim) {
                mRealPlayFullTalkAnimBtn.setVisibility(View.GONE);
                mFullscreenFullButton.setVisibility(View.GONE);
                mRealPlayFullAnimBtn.setBackgroundResource(R.drawable.speech_1);
                startFullBtnAnim(mRealPlayFullAnimBtn, mEndXy, mStartXy, new AnimationListener() {

                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        mRealPlayFullAnimBtn.setVisibility(View.GONE);
                        onRealPlaySvClick();
                    }
                });
            } else {
                mRealPlayFullTalkAnimBtn.setVisibility(View.GONE);
                mFullscreenFullButton.setVisibility(View.GONE);
            }
        }

        mRealPlayTalkBtn.setEnabled(true);
        mRealPlayFullTalkBtn.setEnabled(true);
        mRealPlayFullTalkAnimBtn.setEnabled(true);

        if (mStatus == RealPlayStatus.STATUS_PLAY) {
            if (mEZPlayer != null) {
                if (mLocalInfo.isSoundOpen()) {
                    mEZPlayer.openSound();
                } else {
                    mEZPlayer.closeSound();
                }
            }
        }
    }

    private void handleSetVedioModeSuccess() {
        closeQualityPopupWindow();
        setVideoLevel();
        try {
            mWaitDialog.setWaitText(null);
            mWaitDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mStatus == RealPlayStatus.STATUS_PLAY) {
            // 停止播放
            stopRealPlay();
            //下面语句防止stopRealPlay线程还没释放surface, startRealPlay线程已经开始使用surface
            //因此需要等待500ms
            SystemClock.sleep(500);
            // 开始播放
            startRealPlay();
        }
    }

    private void handleSetVedioModeFail(int errorCode) {
        closeQualityPopupWindow();
        setVideoLevel();
        try {
            mWaitDialog.setWaitText(null);
            mWaitDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Utils.showToast(EZRealPlayActivity.this, R.string.realplay_set_vediomode_fail, errorCode);
    }

    /**
     * 开始录像成功
     *
     * @param recordFilePath
     * @see
     * @since V2.0
     */
    private void handleRecordSuccess(String recordFilePath) {
        if (mCameraInfo == null) {
            return;
        }

        // 设置录像按钮为check状态
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRealPlayRecordContainer, mRealPlayRecordBtn,
                        mRealPlayRecordStartBtn, 0, 90);
            } else {
                mRealPlayRecordBtn.setVisibility(View.GONE);
                mRealPlayRecordStartBtn.setVisibility(View.VISIBLE);
            }
            mRealPlayFullRecordBtn.setVisibility(View.GONE);
            mRealPlayFullRecordStartBtn.setVisibility(View.VISIBLE);
        } else {
            if (!mIsOnStop) {
                mRecordRotateViewUtil.applyRotation(mRealPlayFullRecordContainer, mRealPlayFullRecordBtn,
                        mRealPlayFullRecordStartBtn, 0, 90);
            } else {
                mRealPlayFullRecordBtn.setVisibility(View.GONE);
                mRealPlayFullRecordStartBtn.setVisibility(View.VISIBLE);
            }
            mRealPlayRecordBtn.setVisibility(View.GONE);
            mRealPlayRecordStartBtn.setVisibility(View.VISIBLE);
        }
        mIsRecording = true;
        // 计时按钮可见
        mRealPlayRecordLy.setVisibility(View.VISIBLE);
        mRealPlayRecordTv.setText("00:00");
        mRecordSecond = 0;
    }

    private void handleRecordFail() {
        Utils.showToast(EZRealPlayActivity.this, R.string.remoteplayback_record_fail);
        if (mIsRecording) {
            stopRealPlayRecord();
        }
    }

    private void hideControlRlAndFullOperateBar(boolean excludeLandscapeTitle) {
        //        mRealPlayControlRl.setVisibility(View.GONE);
        closeQualityPopupWindow();
        if (mRealPlayFullOperateBar != null) {
            mRealPlayFullOperateBar.setVisibility(View.GONE);
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                mFullscreenFullButton.setVisibility(View.GONE);
            } else {
                if (!mIsOnTalk && !mIsOnPtz) {
                    mFullscreenFullButton.setVisibility(View.GONE);
                }
            }
        }
        if (excludeLandscapeTitle && mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!mIsOnTalk && !mIsOnPtz) {
                mLandscapeTitleBar.setVisibility(View.VISIBLE);
            }
        } else {
            mLandscapeTitleBar.setVisibility(View.GONE);
        }
    }

    private void updateRealPlayUI() {
        if (mControlDisplaySec == 5) {
            mControlDisplaySec = 0;
            hideControlRlAndFullOperateBar(false);
        }
        checkRealPlayFlow();
        updateCaptureUI();

        if (mIsRecording) {
            updateRecordTime();
        }
    }

    private void initCaptureUI() {
        mCaptureDisplaySec = 0;
        mRealPlayCaptureRl.setVisibility(View.GONE);
        mRealPlayCaptureIv.setImageURI(null);
        mRealPlayCaptureWatermarkIv.setTag(null);
        mRealPlayCaptureWatermarkIv.setVisibility(View.GONE);
    }

    // 更新抓图/录像显示UI
    private void updateCaptureUI() {
        if (mRealPlayCaptureRl.getVisibility() == View.VISIBLE) {
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                if (mRealPlayControlRl.getVisibility() == View.VISIBLE) {
                    mRealPlayCaptureRlLp.setMargins(0, 0, 0, Utils.dip2px(this, 40));
                } else {
                    mRealPlayCaptureRlLp.setMargins(0, 0, 0, 0);
                }
                mRealPlayCaptureRl.setLayoutParams(mRealPlayCaptureRlLp);
            } else {
                LayoutParams realPlayCaptureRlLp = new LayoutParams(
                        Utils.dip2px(this, 65), Utils.dip2px(this, 45));
                realPlayCaptureRlLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                realPlayCaptureRlLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                mRealPlayCaptureRl.setLayoutParams(realPlayCaptureRlLp);
            }
            if (mRealPlayCaptureWatermarkIv.getTag() != null) {
                mRealPlayCaptureWatermarkIv.setVisibility(View.VISIBLE);
                mRealPlayCaptureWatermarkIv.setTag(null);
            }
        }
        if (mCaptureDisplaySec >= 4) {
            initCaptureUI();
        }
    }

    /**
     * 更新录像时间
     *
     * @see
     * @since V1.0
     */
    private void updateRecordTime() {
        if (mRealPlayRecordIv.getVisibility() == View.VISIBLE) {
            mRealPlayRecordIv.setVisibility(View.INVISIBLE);
        } else {
            mRealPlayRecordIv.setVisibility(View.VISIBLE);
        }
        // 计算分秒
        int leftSecond = mRecordSecond % 3600;
        int minitue = leftSecond / 60;
        int second = leftSecond % 60;

        // 显示录像时间
        String recordTime = String.format("%02d:%02d", minitue, second);
        mRealPlayRecordTv.setText(recordTime);
    }

    // 处理密码错误
    private void handlePasswordError(int title_resid, int msg1_resid, int msg2_resid) {
        stopRealPlay();
        setRealPlayStopUI();
        LogUtil.debugLog(TAG, "startRealPlay");

        if (mCameraInfo == null || mStatus == RealPlayStatus.STATUS_START || mStatus == RealPlayStatus.STATUS_PLAY) {
            return;
        }

        // 检查网络是否可用
        if (!ConnectionDetector.isNetworkAvailable(this)) {
            // 提示没有连接网络
            setRealPlayFailUI(getString(R.string.realplay_play_fail_becauseof_network));
            return;
        }

        mStatus = RealPlayStatus.STATUS_START;
        setRealPlayLoadingUI();

        updateLoadingProgress(0);
    }

    /**
     * 处理播放成功的情况
     *
     * @see
     * @since V1.0
     */
    private void handlePlaySuccess(Message msg) {
        LogUtil.d(TAG,"handlePlaySuccess");
        mStatus = RealPlayStatus.STATUS_PLAY;

        // 声音处理
        setRealPlaySound();

        // temp solution for OPENSDK-92
        // Android 预览3Q10的时候切到流畅之后 视频播放窗口变大了 
        //        if (msg.arg1 != 0) {
        //            mRealRatio = (float) msg.arg2 / msg.arg1;
        //        } else {
        //            mRealRatio = Constant.LIVE_VIEW_RATIO;
        //        }
        mRealRatio = Constant.LIVE_VIEW_RATIO;

        boolean bSupport = true;//(float) mLocalInfo.getScreenHeight() / mLocalInfo.getScreenWidth() >= BIG_SCREEN_RATIO;
        if (bSupport) {
            initOperateBarUI(mRealRatio <= Constant.LIVE_VIEW_RATIO);
            initUI();
            if (mRealRatio <= Constant.LIVE_VIEW_RATIO) {
                setBigScreenOperateBtnLayout();
            }
        }
        setRealPlaySvLayout();
        setRealPlaySuccessUI();
        updatePtzUI();
        //        startPrivacyAnim();
        updateTalkUI();
        if (mDeviceInfo != null && mDeviceInfo.isSupportTalk() != EZConstants.EZTalkbackCapability.EZTalkbackNoSupport) {
            mRealPlayTalkBtn.setEnabled(true);
        }else{
            mRealPlayTalkBtn.setEnabled(false);
        }
        if (mEZPlayer != null) {
            mStreamFlow = mEZPlayer.getStreamFlow();
        }
    }

    private void setRealPlaySvLayout() {
        // 设置播放窗口位置
        final int screenWidth = mLocalInfo.getScreenWidth();
        final int screenHeight = (mOrientation == Configuration.ORIENTATION_PORTRAIT) ? (mLocalInfo.getScreenHeight() - mLocalInfo
                .getNavigationBarHeight()) : mLocalInfo.getScreenHeight();
        final LayoutParams realPlaySvlp = Utils.getPlayViewLp(mRealRatio, mOrientation,
                mLocalInfo.getScreenWidth(), (int) (mLocalInfo.getScreenWidth() * Constant.LIVE_VIEW_RATIO),
                screenWidth, screenHeight);

        LayoutParams loadingR1Lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                realPlaySvlp.height);
        //        loadingR1Lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        //        mRealPlayLoadingRl.setLayoutParams(loadingR1Lp);
        //        mRealPlayPromptRl.setLayoutParams(loadingR1Lp);
        LayoutParams svLp = new LayoutParams(realPlaySvlp.width, realPlaySvlp.height);
        //mj svLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mRealPlaySv.setLayoutParams(svLp);

        if (mRtspUrl == null) {
            //            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
            //                    LayoutParams.WRAP_CONTENT);
            //            realPlayPlayRlLp.gravity = Gravity.CENTER;
            //            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
        } else {
            LinearLayout.LayoutParams realPlayPlayRlLp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            realPlayPlayRlLp.gravity = Gravity.CENTER;
            //realPlayPlayRlLp.weight = 1;
            mRealPlayPlayRl.setLayoutParams(realPlayPlayRlLp);
        }
        mRealPlayTouchListener.setSacaleRect(Constant.MAX_SCALE, 0, 0, realPlaySvlp.width, realPlaySvlp.height);
        setPlayScaleUI(1, null, null);
    }

    /**
     * 处理播放失败的情况
     *
     * @param obj - 错误码
     * @see
     * @since V1.0
     */
    private void handlePlayFail(Object obj) {
        int errorCode = 0;
        if (obj != null) {
            ErrorInfo errorInfo = (ErrorInfo) obj;
            errorCode = errorInfo.errorCode;
            LogUtil.debugLog(TAG, "handlePlayFail:" + errorInfo.errorCode);
        }


        hidePageAnim();

        stopRealPlay();

        updateRealPlayFailUI(errorCode);
    }

    private void updateRealPlayFailUI(int errorCode) {
        String txt = null;
        LogUtil.i(TAG, "updateRealPlayFailUI: errorCode:" + errorCode);
        // 判断返回的错误码
        switch (errorCode) {
            case ErrorCode.ERROR_TRANSF_ACCESSTOKEN_ERROR:
                ActivityUtils.goToLoginAgain(EZRealPlayActivity.this);
                return;
            case ErrorCode.ERROR_CAS_MSG_PU_NO_RESOURCE:
                txt = getString(R.string.remoteplayback_over_link);
                break;
            case ErrorCode.ERROR_TRANSF_DEVICE_OFFLINE:
                if (mCameraInfo != null) {
                    mCameraInfo.setIsShared(0);
                }
                txt = getString(R.string.realplay_fail_device_not_exist);
                break;
            case ErrorCode.ERROR_INNER_STREAM_TIMEOUT:
                txt = getString(R.string.realplay_fail_connect_device);
                break;
            case ErrorCode.ERROR_WEB_CODE_ERROR:
                //VerifySmsCodeUtil.openSmsVerifyDialog(Constant.SMS_VERIFY_LOGIN, this, this);
                //txt = Utils.getErrorTip(this, R.string.check_feature_code_fail, errorCode);
                break;
            case ErrorCode.ERROR_WEB_HARDWARE_SIGNATURE_OP_ERROR:
                //VerifySmsCodeUtil.openSmsVerifyDialog(Constant.SMS_VERIFY_HARDWARE, this, null);
//                SecureValidate.secureValidateDialog(this, this);
                //txt = Utils.getErrorTip(this, R.string.check_feature_code_fail, errorCode);
                break;
            case ErrorCode.ERROR_TRANSF_TERMINAL_BINDING:
                txt = "请在萤石客户端关闭终端绑定";
                break;
            // 收到这两个错误码，可以弹出对话框，让用户输入密码后，重新取流预览
            case ErrorCode.ERROR_INNER_VERIFYCODE_NEED:
            case ErrorCode.ERROR_INNER_VERIFYCODE_ERROR: {
                DataManager.getInstance().setDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial(), null);
                VerifyCodeInput.VerifyCodeInputDialog(this, this).show();
            }
            break;
            case ErrorCode.ERROR_EXTRA_SQUARE_NO_SHARING:
            default:
                txt = Utils.getErrorTip(this, R.string.realplay_play_fail, errorCode);
                break;
        }

        if (!TextUtils.isEmpty(txt)) {
            setRealPlayFailUI(txt);
        } else {
            setRealPlayStopUI();
        }
    }


    /**
     * 启动定时器
     *
     * @see
     * @since V1.0
     */
    private void startUpdateTimer() {
        stopUpdateTimer();
        // 开始录像计时
        mUpdateTimer = new Timer();
        mUpdateTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (mLandscapeTitleBar != null && mRealPlayControlRl != null
                        && (mLandscapeTitleBar.getVisibility() == View.VISIBLE || mRealPlayControlRl.getVisibility() == View.VISIBLE)
                        && mControlDisplaySec < 5) {
                    mControlDisplaySec++;
                }
                if (mRealPlayCaptureRl != null && mRealPlayCaptureRl.getVisibility() == View.VISIBLE
                        && mCaptureDisplaySec < 4) {
                    mCaptureDisplaySec++;
                }

                // 更新录像时间
                if (mEZPlayer != null && mIsRecording) {
                    // 更新录像时间
                    Calendar OSDTime = mEZPlayer.getOSDTime();
                    if (OSDTime != null) {
                        String playtime = Utils.OSD2Time(OSDTime);
                        if (!TextUtils.equals(playtime, mRecordTime)) {
                            mRecordSecond++;
                            mRecordTime = playtime;
                        }
                    }
                }
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_PLAY_UI_UPDATE);
                }
            }
        };
        // 延时1000ms后执行，1000ms执行一次
        mUpdateTimer.schedule(mUpdateTimerTask, 0, 1000);
    }

    /**
     * 停止定时器
     *
     * @see
     * @since V1.0
     */
    private void stopUpdateTimer() {
        mCaptureDisplaySec = 4;
        updateCaptureUI();
        mHandler.removeMessages(MSG_PLAY_UI_UPDATE);
        // 停止录像计时
        if (mUpdateTimer != null) {
            mUpdateTimer.cancel();
            mUpdateTimer = null;
        }

        if (mUpdateTimerTask != null) {
            mUpdateTimerTask.cancel();
            mUpdateTimerTask = null;
        }
    }

    private void dismissPopWindow(PopupWindow popupWindow) {
        if (popupWindow != null && !isFinishing()) {
            try {
                popupWindow.dismiss();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    private void setPlayScaleUI(float scale, CustomRect oRect, CustomRect curRect) {
        if (scale == 1) {
            if (mPlayScale == scale) {
                return;
            }
            mRealPlayRatioTv.setVisibility(View.GONE);
            try {
                if (mEZPlayer != null) {
                    mEZPlayer.setDisplayRegion(false, null, null);
                }
            } catch (BaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            if (mPlayScale == scale) {
                try {
                    if (mEZPlayer != null) {
                        mEZPlayer.setDisplayRegion(true, oRect, curRect);
                    }
                } catch (BaseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return;
            }
            LayoutParams realPlayRatioTvLp = (LayoutParams) mRealPlayRatioTv
                    .getLayoutParams();
            if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                realPlayRatioTvLp.setMargins(Utils.dip2px(this, 10), Utils.dip2px(this, 10), 0, 0);
            } else {
                realPlayRatioTvLp.setMargins(Utils.dip2px(this, 70), Utils.dip2px(this, 20), 0, 0);
            }
            mRealPlayRatioTv.setLayoutParams(realPlayRatioTvLp);
            String sacleStr = String.valueOf(scale);
            mRealPlayRatioTv.setText(sacleStr.subSequence(0, Math.min(3, sacleStr.length())) + "X");
            //mj mRealPlayRatioTv.setVisibility(View.VISIBLE);
            mRealPlayRatioTv.setVisibility(View.GONE);
            hideControlRlAndFullOperateBar(false);
            try {
                if (mEZPlayer != null) {
                    mEZPlayer.setDisplayRegion(true, oRect, curRect);
                }
            } catch (BaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        mPlayScale = scale;
    }

    /*
     * (non-Javadoc)
     * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.realplay_pages_gallery:
                mRealPlayTouchListener.touch(event);
                break;
            case R.id.realplay_full_operate_bar:
                return true;
            default:
                break;
        }
        return false;
    }

    /**
     * 分享 需要密码时候的弹框
     *
     * @see
     * @since V1.8.2
     */
    private void showSharePasswordError() {
        // 处理播放密码错误
        handlePasswordError(R.string.realplay_encrypt_password_error_title, R.string.realplay_password_error_message4,
                0);
    }

    public int getPlayStatus() {
        return mStatus;
    }

    /**
     * 这里对方法做描述
     *
     * @see
     * @since V1.8
     */
    private void showType() {
        if (Config.LOGGING && mEZPlayer != null) {
            Utils.showLog(EZRealPlayActivity.this, "getType " + ",取流耗时：" + (mStopTime - mStartTime));
        }
    }

    private void initLoadingUI() {
        mRealPlayLoadingRl = (RelativeLayout) findViewById(R.id.realplay_loading_rl);
        mRealPlayTipTv = (TextView) findViewById(R.id.realplay_tip_tv);
        mRealPlayPlayIv = (ImageView) findViewById(R.id.realplay_play_iv);
        mRealPlayPlayLoading = (LoadingTextView) findViewById(R.id.realplay_loading);
        mRealPlayPlayPrivacyLy = (LinearLayout) findViewById(R.id.realplay_privacy_ly);

        // 设置点击图标的监听响应函数
        mRealPlayPlayIv.setOnClickListener(this);

        mPageAnimIv = (ImageView) findViewById(R.id.realplay_page_anim_iv);
    }

    private void updateLoadingProgress(final int progress) {
        mRealPlayPlayLoading.setTag(Integer.valueOf(progress));
        mRealPlayPlayLoading.setText(progress + "%");
        mHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                if (mRealPlayPlayLoading != null) {
                    Integer tag = (Integer) mRealPlayPlayLoading.getTag();
                    if (tag != null && tag.intValue() == progress) {
                        Random r = new Random();
                        mRealPlayPlayLoading.setText((progress + r.nextInt(20)) + "%");
                    }
                }
            }

        }, 500);
    }

    private void setStartloading() {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.VISIBLE);
        mRealPlayPlayIv.setVisibility(View.GONE);
        mRealPlayPlayPrivacyLy.setVisibility(View.GONE);
    }

    public void setStopLoading() {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.VISIBLE);
        mRealPlayPlayPrivacyLy.setVisibility(View.GONE);
    }

    public void setLoadingFail(String errorStr) {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setText(errorStr);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.GONE);
        mRealPlayPlayPrivacyLy.setVisibility(View.GONE);
    }

    private void setPrivacy() {
        mRealPlayLoadingRl.setVisibility(View.VISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.GONE);
        mRealPlayPlayPrivacyLy.setVisibility(View.VISIBLE);
    }

    private void setLoadingSuccess() {
        mRealPlayLoadingRl.setVisibility(View.INVISIBLE);
        mRealPlayTipTv.setVisibility(View.GONE);
        mRealPlayPlayLoading.setVisibility(View.GONE);
        mRealPlayPlayIv.setVisibility(View.GONE);
    }

    @Override
    public void onInputVerifyCode(final String verifyCode) {
        LogUtil.debugLog(TAG, "verify code is " + verifyCode);
        DataManager.getInstance().setDeviceSerialVerifyCode(mCameraInfo.getDeviceSerial(), verifyCode);
        if (mEZPlayer != null) {
            startRealPlay();
        }
    }

    FileOutputStream mOs;

    /* 录像回调函数
     * @param var1:视频数据
     * @param var2:数据长度
     * @param var3:数据类型
     * @param var4:自定义数据
     */
    private EZOpenSDKListener.EZStandardFlowCallback mLocalRecordCb = new EZOpenSDKListener.EZStandardFlowCallback() {
        @Override
        public void onStandardFlowCallback(int type, byte[] data, int dataLen) {
            LogUtil.verboseLog(TAG, "standard flow. type is " + type + ". dataLen is " + dataLen + ". data0 is " + data[0]);

            if (mOs == null) {
                File f = new File("/sdcard/videogo.mp4");
                try {
                    mOs = new FileOutputStream(f);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    LogUtil.errorLog(TAG, "new record file failed");

                    return;
                }
            }
            try {
                mOs.write(data, 0, dataLen);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

            }
        }
    };

//    private StreamConvertCB.OutputDataCB mLocalRecordCb = new StreamConvertCB.OutputDataCB() {
//        @Override
//        public void onOutputData(byte[] var1, int var2, int var3, byte[] var4) {
//            LogUtil.debugLog("out", "standard flow. type is " + var3 + ". dataLen is " + var2 + ". data0 is " + var1[0]);
//            if (mOs == null) {
//                File f = new File("/sdcard/videogo2.mp4");
//                try {
//                    mOs = new FileOutputStream(f);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }
//            try {
//                mOs.write(var1, 0, var2);
//            } catch (IOException e) {
//                e.printStackTrace();
//            } finally {
//
//            }
//        }
//    };

}
