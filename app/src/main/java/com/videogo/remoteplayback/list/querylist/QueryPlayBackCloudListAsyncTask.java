package com.videogo.remoteplayback.list.querylist;

import android.app.Activity;

import com.videogo.EzvizApplication;
import com.videogo.device.DeviceReportInfo;
import com.videogo.errorlayer.ErrorInfo;
import com.videogo.exception.BaseException;
import com.videogo.openapi.bean.EZCloudRecordFile;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;
import com.videogo.remoteplayback.list.RemoteListContant;
import com.videogo.remoteplayback.list.bean.CloudPartInfoFileEx;
import com.videogo.ui.common.HikAsyncTask;
import com.videogo.util.CollectionUtil;
import com.videogo.util.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ezviz.ezopensdk.R;

//import com.videogo.restful.exception.VideoGoNetSDKException;

/**
 * 查询云数据文件（新接口）
 * 
 * @author miguofei
 * @data 2014-10-20
 */
public class QueryPlayBackCloudListAsyncTask extends HikAsyncTask<String, Void, Integer> {
    private final String MINUTE;

    // 设备序列号
    private String deviceSerial;
    // 通道号
    private int channelNo;
    // 搜索日期（格式为：yyyy-MM-dd）
    private Date searchDate;

//    private RemoteListFileCtrl remoteListFileCtrl = null;

    private volatile boolean abort = false;

    private int cloudErrorCode = 0;

    private QueryPlayBackListTaskCallback queryPlayBackListTaskCallback;

    private List<CloudPartInfoFile> cloudPartFiles;

    List<CloudPartInfoFileEx> cloudPartInfoFileExList = new ArrayList<CloudPartInfoFileEx>();

    public QueryPlayBackCloudListAsyncTask(String deviceSerial, int channelNo,
        QueryPlayBackListTaskCallback queryPlayBackListTaskCallback) {
        MINUTE = ((Activity) queryPlayBackListTaskCallback).getString(R.string.play_hour);
        this.deviceSerial = deviceSerial;
        this.channelNo = channelNo;
//        remoteListFileCtrl = new RemoteListFileCtrl();
        this.queryPlayBackListTaskCallback = queryPlayBackListTaskCallback;
    }

    public void setAbort(boolean abort) {
        this.abort = abort;
//        remoteListFileCtrl.setAbort(abort);
    }

    public Date getSearchDate() {
        return searchDate;
    }

    public void setSearchDate(Date searchDate) {
        this.searchDate = searchDate;
    }

    @Override
    protected Integer doInBackground(String... params) {
        boolean cloudException = false;
        boolean localException = false;

        int queryCloudFiles = RemoteListContant.QUERY_NO_DATA;
//        try {
            // int supportCloud = deviceInfoEx.getSupportCloud();
            // int supportCloudVersion = deviceInfoEx.getSupportCloudVersion();
            // if (supportCloud == 1 && supportCloudVersion == 1) {
            queryCloudFiles = queryCloudFile();
            // }
//        } catch (VideoGoNetSDKException e1) {
//            e1.printStackTrace();
//            cloudErrorCode = e1.getErrorCode();
//            cloudException = true;
//        }

//        try {
//            hasLocalFiles = RemoteListVideoDataManager.getIntance().hasLocalFiles(deviceInfoEx, channelNo, searchDate);
//        } catch (CASClientSDKException e) {
//            e.printStackTrace();
//            localException = true;
//        }

        if (cloudException && localException) {
            return RemoteListContant.QUERY_EXCEPTION;
        }

        if (cloudException && !localException) {
                return RemoteListContant.QUERY_EXCEPTION;
        }

        if (!cloudException && localException) {
            if (queryCloudFiles == RemoteListContant.QUERY_NO_DATA) {
                return RemoteListContant.QUERY_EXCEPTION;
            } else {
                return RemoteListContant.QUERY_CLOUD_SUCCESSFUL_LOCAL_EX;
            }
        }

        if (!cloudException && !localException) {
            if (queryCloudFiles == RemoteListContant.QUERY_NO_DATA) {
                    return RemoteListContant.QUERY_NO_DATA;
            } else {
                    return RemoteListContant.QUERY_CLOUD_SUCCESSFUL_NOLOACL;
            }
        }
        return RemoteListContant.QUERY_NO_DATA;
    }

    @Override
    protected void onPostExecute(Integer result) {
//    	if(mStub) {
//    		 queryPlayBackListTaskCallback.queryCloudSucess(cloudPartInfoFileExList, RemoteListContant.NO_LOCAL,
//                     cloudPartFiles);
//    		return ;
//    	}
        if (!abort) {
            queryPlayBackListTaskCallback.queryTaskOver(RemoteListContant.TYPE_CLOUD,
                    DeviceReportInfo.REPOERT_QUERY_CLOUD, cloudErrorCode, "");
            // 云和本地都没有数据
            if (result == RemoteListContant.QUERY_NO_DATA) {
                queryPlayBackListTaskCallback.queryHasNoData();
            }
            // 云有数据，本地没有数据
            else if (result == RemoteListContant.QUERY_CLOUD_SUCCESSFUL_NOLOACL) {
                queryPlayBackListTaskCallback.queryCloudSucess(cloudPartInfoFileExList, RemoteListContant.NO_LOCAL,
                        cloudPartFiles);
            }
            // 云有数据，本地也有数据
            else if (result == RemoteListContant.QUERY_CLOUD_SUCCESSFUL_HASLOCAL) {
                queryPlayBackListTaskCallback.queryCloudSucess(cloudPartInfoFileExList, RemoteListContant.HAS_LOCAL,
                        cloudPartFiles);
            }
            // 云有数据，本地异常
            else if (result == RemoteListContant.QUERY_CLOUD_SUCCESSFUL_LOCAL_EX) {
                queryPlayBackListTaskCallback.queryCloudSucess(cloudPartInfoFileExList,
                        RemoteListContant.EXCEPTION_LOCAL, cloudPartFiles);
            }
            // 云没数据，本地有数据
            else if (result == RemoteListContant.QUERY_ONLY_LOCAL) {
                queryPlayBackListTaskCallback.queryOnlyHasLocalFile();
            }
            // 云和本地查询都异常
            else if (result == RemoteListContant.QUERY_EXCEPTION) {
                queryPlayBackListTaskCallback.queryException();
            }
        }
    }

    private void convertEZCloudRecordFile2CloudPartInfoFile(CloudPartInfoFile dst, EZCloudRecordFile src, int pos) {
    	String startT = new SimpleDateFormat("yyyyMMddHHmmss").format(src.getStartTime().getTime());
		String endT = new SimpleDateFormat("yyyyMMddHHmmss").format(src.getStopTime().getTime());

    	dst.setCloud(true);
    	dst.setDownloadPath(src.getDownloadPath());
    	//dst.setEndMillis(src.get);
    	dst.setEndTime(endT);
    	dst.setFileId(src.getFileId());
    	//dst.setFileName(src.getFileName());
//    	dst.setFileSize(Integer.parseInt(src.getFileSize()));
//    	dst.setFileType(src.getFileType());
    	dst.setKeyCheckSum(src.getEncryption());
    	dst.setPicUrl(src.getCoverPic());
    	dst.setPosition(pos);
    	//dst.setStartMillis(startMillis);
    	dst.setStartTime(startT);
    	//dst.setStartMillis(System.currentTimeMillis()-1000000l);
    }

    private int queryCloudFile() /*throws VideoGoNetSDKException */{
//        String startTime = DateTimeUtil.formatDateToString(searchDate, DateTimeUtil.DAY_FORMAT);
//        cloudPartFiles = remoteListFileCtrl.queryCloudPartInfo(deviceSerial, channelNo, startTime);

		// translate the searchDate to calendar, e.g startTime to
		// 2015/11/04:00:00:00, endTime to 2015/11/04:23:59:59
		Calendar startTime = Calendar.getInstance();
		Calendar endTime = Calendar.getInstance();
		startTime.setTime(searchDate);
		endTime.setTime(searchDate);
       
		startTime.set(Calendar.HOUR_OF_DAY, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.SECOND, 0);
		endTime.set(Calendar.HOUR_OF_DAY, 23);
		endTime.set(Calendar.MINUTE, 59);
		endTime.set(Calendar.SECOND, 59);

		List<EZCloudRecordFile> tmpList = null;
		try {
			tmpList = EzvizApplication.getOpenSDK().searchRecordFileFromCloud(deviceSerial,channelNo,
					startTime, endTime);
		} catch (BaseException e) {
			e.printStackTrace();

            ErrorInfo errorInfo = (ErrorInfo) e.getObject();
		}

        cloudPartFiles = new ArrayList<CloudPartInfoFile>();
		if (tmpList != null && tmpList.size() > 0) {
			for (int i = 0; i < tmpList.size(); i++) {
				EZCloudRecordFile file = tmpList.get(i);
				CloudPartInfoFile cpif = new CloudPartInfoFile();

				convertEZCloudRecordFile2CloudPartInfoFile(cpif, file, i);
				cloudPartFiles.add(cpif);
			}
		}

		if (CollectionUtil.isNotEmpty(cloudPartFiles)) {
			Collections.sort(cloudPartFiles);
		}

        int length = cloudPartFiles.size();
        int i = 0;
        while (i < length) {
            CloudPartInfoFileEx cloudPartInfoFileEx = new CloudPartInfoFileEx();
            CloudPartInfoFile dataOne = cloudPartFiles.get(i);
            dataOne.setPosition(i);
            Calendar beginCalender = Utils.convert14Calender(dataOne.getStartTime());
            String hour = getHour(beginCalender.get(Calendar.HOUR_OF_DAY));
            cloudPartInfoFileEx.setHeadHour(hour);
            cloudPartInfoFileEx.setDataOne(dataOne);
            i++;
            if (i > length - 1) {
                cloudPartInfoFileExList.add(cloudPartInfoFileEx);
                continue;
            }
            CloudPartInfoFile dataTwo = cloudPartFiles.get(i);
            if (hour.equals(getHour(Utils.convert14Calender(dataTwo.getStartTime()).get(Calendar.HOUR_OF_DAY)))) {
                dataTwo.setPosition(i);
                cloudPartInfoFileEx.setDataTwo(dataTwo);
                i++;
                if (i > length - 1) {
                    cloudPartInfoFileExList.add(cloudPartInfoFileEx);
                    continue;
                }
                CloudPartInfoFile dataThree = cloudPartFiles.get(i);
                if (hour.equals(getHour(Utils.convert14Calender(dataThree.getStartTime()).get(Calendar.HOUR_OF_DAY)))) {
                    dataThree.setPosition(i);
                    cloudPartInfoFileEx.setDataThree(dataThree);
                    i++;
                }
            }
            cloudPartInfoFileExList.add(cloudPartInfoFileEx);
        }
        if (CollectionUtil.isNotEmpty(cloudPartInfoFileExList)) {
            return RemoteListContant.QUERY_CLOUD_SUCCESSFUL_NOLOACL;
        }
        return RemoteListContant.QUERY_NO_DATA;
    }

    private String getHour(int hourOfDay) {
        // if (hourOfDay < 10) {
        // return "  " + hourOfDay + MINUTE;
        // } else {
        return hourOfDay + MINUTE;
        // }
    }
    
    private boolean mStub = true; // switch the stub code
}
