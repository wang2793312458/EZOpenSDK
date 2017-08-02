/* 
 * @ProjectName ezviz-openapi-android-demo
 * @Copyright HangZhou Hikvision System Technology Co.,Ltd. All Right Reserved
 * 
 * @FileName AddDeviceTask.java
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
package com.videogo.ui.devicelist;

import android.os.AsyncTask;

import com.videogo.api.TransferAPI;
import com.videogo.exception.BaseException;
import com.videogo.exception.ErrorCode;
import com.videogo.util.DevPwdUtil;

import org.json.JSONException;

/**
 * 在此对类做相应的描述
 * @author chenxingyf1
 * @data 2015-8-12
 */
/**
 * 删除设备任务
 */
public class AddDeviceTask extends AsyncTask<AddDeviceTask.TaskLisener, Void, Boolean> {
    private String mDeviceSerial;
    private String mValidateCode;
    private int mErrorCode = 0;
    private TaskLisener mTaskLisener;
    
    public interface TaskLisener {
        void onPreExecute();

        void onPostExecute(boolean result, int errorCode);
    }
    
    public AddDeviceTask(TaskLisener taskLisener, String deviceSerial, String validateCode) {
        mDeviceSerial = deviceSerial;
        mValidateCode = validateCode;
        mTaskLisener = taskLisener;
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mTaskLisener.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(AddDeviceTask.TaskLisener... params) {
        try {
            return TransferAPI.addDevice(mDeviceSerial, mValidateCode);
        } catch (BaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            mErrorCode = e.getErrorCode();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            mErrorCode = ErrorCode.ERROR_WEB_NET_EXCEPTION;
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        if (result != null && result) {
            DevPwdUtil.savePwd(mDeviceSerial, mValidateCode);
        }
        
        mTaskLisener.onPostExecute(result, mErrorCode);
    }
}
