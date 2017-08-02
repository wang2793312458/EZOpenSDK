package com.videogo.ui.devicelist;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.videogo.RootActivity;
import com.videogo.widget.BoldSpan;
import com.videogo.widget.TitleBar;

import ezviz.ezopensdk.R;

/**
 * 一键配置准备界面（配置前介绍界面，三种设备 不同的提示介绍）
 * 
 * @author chengjuntao
 * @data 2014-4-9
 */
public class AutoWifiPrepareStepOneActivity extends RootActivity implements OnClickListener {

    /**
     * 下一步按钮
     */
    private Button btnNext;
    /**
     * 设备类型model
     */
    private String deviceType;
    /**
     * 头顶示意文字
     */
    private TextView topTip;
    /**
     * 帮助按钮
     */
    private Button btnIntroduce;
    /**
     * 灯光闪烁背景
     */
    private ImageView imageBg;
    /**
     * 灯光闪烁动画
     */
    private AnimationDrawable aminBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.auto_wifi_prepare_step_on);
        initTitleBar();
        initUI();
    }

    /**
     * 初始化标题栏
     */
    private void initTitleBar() {
        TitleBar mTitleBar = (TitleBar) findViewById(R.id.title_bar);
        mTitleBar.setTitle(R.string.auto_wifi_step_one_title);
        mTitleBar.addBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 这里对方法做描述
     * 
     * @see
     * @since V1.0
     */
    private void initUI() {
        topTip = (TextView) findViewById(R.id.topTip);
        imageBg = (ImageView) findViewById(R.id.imageBg);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnIntroduce = (Button) findViewById(R.id.btnIntroduce);
        btnNext.setOnClickListener(this);
        btnIntroduce.setOnClickListener(this);

        SpannableString spanStr = new SpannableString(getString(R.string.tip_heard_voice));
        spanStr.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.auto_wifi_tip_red)), 12, 14,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanStr.setSpan(new BoldSpan(Typeface.NORMAL), 12, 14, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        topTip.setText(spanStr);
        imageBg.setImageResource(R.drawable.video_camera1_3);
        btnNext.setText(R.string.autowifi_heard_voice);
        btnIntroduce.setText(R.string.autowifi_not_heard_voice);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.btnNext:
                intent = new Intent(this, AutoWifiNetConfigActivity.class);
                intent.putExtras(getIntent());
                startActivity(intent);
                break;
            case R.id.btnIntroduce:
                intent = new Intent(this, AutoWifiResetActivity.class);
                intent.putExtras(getIntent());
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (aminBg != null) {
            aminBg.stop();
            aminBg = null;
        }
        super.onDestroy();
    }
}
