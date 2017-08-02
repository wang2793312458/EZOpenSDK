package com.videogo.ui.util;

import android.content.Context;

import com.bumptech.glide.load.engine.bitmap_recycle.LruBitmapPool;
import com.bumptech.glide.load.engine.cache.MemorySizeCalculator;

import java.util.HashMap;
import java.util.Map;

/**
 * Description:数据管理，demo只做参考，开发者需要自己保存相关信息
 * 需要保存视频图片加密密码，以及加密与不加密图片缓存
 * 图片、云存储录像文件、sdcard录像文件在生成时就关联了当时的视频图片加密密码，
 * 如果后续更改视频图片加密密码，那对于图片和录像文件需要用其生成时的加密密码进行解密
 * Created by dingwei3
 *
 * @date : 2016/9/24
 */
public class DataManager {
    private static DataManager mDataManager;

    private Map<String,String> mDeviceSerialVerifyCodeMap;
    private static LruBitmapPool mBitmapPool;

    private DataManager(){
        mDeviceSerialVerifyCodeMap = new HashMap<String,String>();
    }

   public static DataManager getInstance(){
       if (mDataManager == null){
            mDataManager = new DataManager();
       }
        return mDataManager;
    }

    public LruBitmapPool getBitmapPool(Context context){
        if (mBitmapPool == null){
            MemorySizeCalculator calculator = new MemorySizeCalculator(context);
            int defaultBitmapPoolSize = calculator.getBitmapPoolSize();
            mBitmapPool = new LruBitmapPool(defaultBitmapPoolSize);
        }
        return mBitmapPool;
    }

    /**
     * 缓存设备验证码信息
     * @param deviceSerial
     * @param verifyCode
     */
    public synchronized void setDeviceSerialVerifyCode(String deviceSerial,String verifyCode){
        mDeviceSerialVerifyCodeMap.put(deviceSerial, verifyCode);
    }

    /**
     * @param deviceSerial
     * @return    获取缓存的设备验证码信息
     */
    public synchronized String getDeviceSerialVerifyCode(String deviceSerial){
        if (mDeviceSerialVerifyCodeMap.containsKey(deviceSerial)){
            return mDeviceSerialVerifyCodeMap.get(deviceSerial);
        }
        return null;
    }
}


