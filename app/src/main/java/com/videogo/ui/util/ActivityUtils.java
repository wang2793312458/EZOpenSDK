/* 
 * @ProjectName VideoGo
 * @Copyright HangZhou Hikvision System Technology Co.,Ltd. All Right Reserved
 * 
 * @FileName UIUtils.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2015-4-23
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.ui.util;

import android.app.Activity;

import com.videogo.EzvizApplication;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZGlobalSDK;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZAreaInfo;
import com.videogo.util.LogUtil;

import java.util.List;

//import com.videogo.login.LoginActivity;
//import com.videogo.login.LoginAgainActivity;
//import com.videogo.login.VerifyHardwareSignatresActivity;
//import com.videogo.main.CustomApplication;
//import com.videogo.main.EmptyActivity;
//import com.videogo.main.MainTabActivity;
//import com.videogo.personal.UserTerminalActivity;
//import com.videogo.stat.HikAction;

/**
 * 界面跳转
 * 
 * @author chenxingyf1
 * @data 2015-4-23
 */
public class ActivityUtils {
    /**
     * 处理token过期的错误
     * 
     * @throws
     */
    public static void handleSessionException(Activity activity) {
        goToLoginAgain(activity);
    }

    public static void goToLoginAgain(Activity activity) {
        if (EZGlobalSDK.class.isInstance(EzvizApplication.getOpenSDK())) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<EZAreaInfo> areaList = EZGlobalSDK.getInstance().getAreaList();
                        if (areaList != null) {
                            LogUtil.debugLog("application", "list count: " + areaList.size());

                            EZAreaInfo areaInfo = areaList.get(0);
                            EZGlobalSDK.getInstance().openLoginPage(areaInfo.getId());
                        }
                    } catch (BaseException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {
            EZOpenSDK.getInstance().openLoginPage();
        }
    }


}