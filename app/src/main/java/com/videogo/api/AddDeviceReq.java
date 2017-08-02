/* 
 * @ProjectName ezviz-openapi-android-demo
 * @Copyright HangZhou Hikvision System Technology Co.,Ltd. All Right Reserved
 * 
 * @FileName AddDeviceReq.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2015-8-12
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo.api;

import com.videogo.openapi.annotation.Serializable;

/**
 * 在此对类做相应的描述
 * @author chenxingyf1
 * @data 2015-8-12
 */
public class AddDeviceReq {
    @Serializable(name = "method")
    public String method = "device/sdk/addDevice";//方法名
    
    @Serializable(name = "params")
    public Params params = new Params();
    
    @Serializable
    public class Params {
        @Serializable(name = "deviceSerial")
        public String deviceSerial;
        @Serializable(name = "validateCode")
        public String validateCode;
        @Serializable(name = "accessToken")
        public String accessToken;
    }
}
