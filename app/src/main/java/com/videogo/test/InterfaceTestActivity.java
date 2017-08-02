package com.videogo.test;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.videogo.EzvizApplication;
import com.videogo.exception.BaseException;
import com.videogo.openapi.EZConstants;
import com.videogo.openapi.EZConstants.EZAlarmStatus;
import com.videogo.openapi.EZConstants.EZMessageStatus;
import com.videogo.openapi.EZOpenSDK;
import com.videogo.openapi.bean.EZAlarmInfo;
import com.videogo.openapi.bean.EZDeviceInfo;
import com.videogo.openapi.bean.EZDeviceUpgradeStatus;
import com.videogo.openapi.bean.EZLeaveMessage;
import com.videogo.openapi.bean.EZProbeDeviceInfo;
import com.videogo.openapi.bean.EZStorageStatus;
import com.videogo.openapi.bean.EZUserInfo;
import com.videogo.ui.util.EZUtils;
import com.videogo.util.LogUtil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ezviz.ezopensdk.R;


public class InterfaceTestActivity extends Activity implements View.OnClickListener{
    public static final String TAG = "InterfaceTestActivity";
    private EditText mSerialEdit;
    private Button mTestButton;
    private final String filePath = "/sdcard/videogo_test_cfg";
    private Map<String, String> mMap;
    private String mSerial;

    //This user is the open user of app1
    public static final String User1Phone = "13758182040";

    //This user should be registered in app1
    public static final String Phone2 = "18969189899";

    public static final String Phone_unregistered = "18969189876";

    public static final String Actoken = "at.6a4own7p2atcxr0c7zfgus1010l5v4ju-41um2p2kej-1h63qll-m3axk55h2";
    public static final String AcTokenExpired = "at.7hjdduo578e52osc31uopu906xoi11vu-32e92nb45m-0zwok01-lhxpijpj0";

    //this device should be already in your accounr and ONLINE
    public static final String DeviceSerial = "487777232";
    public static final String F1DeviceSerial = "475355589";
    public static final String A1DeviceSerial = "552172236";
    public static final String A1Validation = "NRASYR";

    public static final String DeviceSerialForNoOpenUser = "487777405";

    public static final String DeviceAdd = "487253259";

    public static final String DeviceAddValidation = "VAUOZA";

    //this device should be already in your account and not online
    public static final String DeviceAddedNotOnline = "509784207";

    //his device should not be added to any account and not online
    public static final String DeviceOffline = "488333811";

    public static final String DeviceAddedToOtherAccount = "487777405";
    public static final String DeviceAddedToOtherAccountNotOnline = "487253225";

    public static final String Memcached = "10.80.6.119:11211,10.80.2.3:11211";

    public static final String Database = "10.80.7.86,root,88075998";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interface_test);
        mMap = new HashMap<String, String>();
        parseTestConfigFile(filePath, mMap);
        mSerial = mMap.get("DeviceSerial");
        findViews();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.interface_test_button:
                testV32Interface();
                break;
            case R.id.id_interface_test_openCloudPage:
//                final String DeviceSerial = mSerialEdit.getEditableText().toString();
                try {
                    if (EZOpenSDK.class.isInstance(EzvizApplication.getOpenSDK())) {
                        EZOpenSDK.getInstance().openCloudPage(DeviceSerial);
                    }
                } catch (BaseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                break;
            case R.id.interface_v33_test_button:
                testV33Interface();
                break;
            default:
                break;
        }
    }

    private void findViews() {
        //mSerialEdit=(EditText) findViewById(R.id.interface_test_editText);
        mTestButton = (Button) findViewById(R.id.interface_test_button);
        if(!TextUtils.isEmpty(mSerial)) {
            mSerialEdit.setText(mSerial);
        }
    }

    // test v3.2 interfaces
    private void testV32Interface() {

        Thread thr = new Thread(new Runnable(){
            @Override
            public void run() {
                // test interface capturePicture
                List<EZLeaveMessage> LeaveMessageList = null;
                Calendar begin = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                begin.set(Calendar.HOUR_OF_DAY, 0);
                end.set(Calendar.HOUR_OF_DAY, 23);
                LogUtil.i(TAG, "==========================capturePicture Started==========================");
                capturePictureTemplate(DeviceSerial, 1, 0);
                capturePictureTemplate(DeviceSerial, 1234, 0);
                capturePictureTemplate(DeviceSerial, -1, 400500);
                capturePictureTemplate("21231", 1, 400500);
                capturePictureTemplate("", 1, 400500);
                capturePictureTemplate(null, 1, 400500);
                capturePictureTemplate("#$%#", 1, 400500);

                // test interface getUserInfo
                LogUtil.i(TAG, "==========================getUserInfo Started==========================");
                try {
                    EZUserInfo userInfo = EzvizApplication.getOpenSDK().getUserInfo();
                    LogUtil.i(TAG, "EZUserInfo:" + userInfo);
                } catch (BaseException e) {
                    e.printStackTrace();
                }

                // test interface getUnreadMessageCount
                LogUtil.i(TAG, "==========================getUnreadMessageCount Started==========================");
                getUnreadMessageCountTemplate(DeviceSerial, EZConstants.EZMessageType.EZMessageTypeAlarm, 0);
                getUnreadMessageCountTemplate("45345", EZConstants.EZMessageType.EZMessageTypeAlarm, 400500);
                getUnreadMessageCountTemplate("", EZConstants.EZMessageType.EZMessageTypeAlarm, 400500);
                getUnreadMessageCountTemplate("#@$%#", EZConstants.EZMessageType.EZMessageTypeAlarm, 400500);
                getUnreadMessageCountTemplate(null, EZConstants.EZMessageType.EZMessageTypeAlarm, 400500);


                // test interface getLeaveMessageList
                LogUtil.i(TAG, "==========================getLeaveMessageList Started==========================");
                try {
                    LeaveMessageList =EzvizApplication.getOpenSDK().getInstance().getLeaveMessageList(F1DeviceSerial, 0, 20, begin, end);
                    LogUtil.i("ResponseData", "getLeaveMessageList:" + LeaveMessageList);
                } catch (BaseException e) {
                    e.printStackTrace();

                }
                getLeaveMessageListTemplate("#$%", 0, 5, begin, end, 400500);
                getLeaveMessageListTemplate("", 0, 5, begin, end, 400500);
                getLeaveMessageListTemplate("425", 0, 5, begin, end, 400500);
                getLeaveMessageListTemplate(null, 0, 5, begin, end, 400500);
                getLeaveMessageListTemplate(F1DeviceSerial, -3, 5, begin, end, 400500);
                getLeaveMessageListTemplate(F1DeviceSerial, 0, -5, begin, end, 400500);
                getLeaveMessageListTemplate(F1DeviceSerial, 0, 5, null, end, 400500);

                // test interface setLeaveMsgStatus
                LogUtil.i(TAG, "==========================setLeaveMessageStatus Started==========================");
//                try {
//                	List<String> leaveIds = new ArrayList<String>();
//                	leaveIds.add(LeaveMessageList.get(0).getMsgId());
//                	EzvizApplication.getOpenSDK().setLeaveMessageStatus(leaveIds, EZMessageStatus.EZMessageStatusRead);
//                	//verify
//                    try {
//                        List<EZLeaveMessage> verifyLeaveMessageList = EzvizApplication.getOpenSDK().getLeaveMessageList(F1DeviceSerial, 0, 5, begin, end);
//                        for(EZLeaveMessage temp : verifyLeaveMessageList){
//                        	if (temp.getMsgId().equals(leaveIds.get(0))){
//                        		if (temp.getMsgStatus() != EZMessageStatus.EZMessageStatusRead.getStatus()){
//                        			throw new BaseException("this leaveID's status " + temp.getMsgStatus() + " != " + EZMessageStatus.EZMessageStatusRead + " ,message id : " + temp.getMsgId(),0);
//                        		};
//                        	}
//                        }
//                    } catch (BaseException e) {
//                        e.printStackTrace();
//                    }
//
//                }catch (BaseException e) {
//                	e.printStackTrace();
//                }

                //correct data
                List<String> leaveIds = new ArrayList<String>();
                leaveIds.add(LeaveMessageList.get(0).getMsgId());
                setLeaveMessageStatusTemplate(leaveIds, EZMessageStatus.EZMessageStatusRead, 0);
                //empty list
                setLeaveMessageStatusTemplate(new ArrayList<String>(), EZMessageStatus.EZMessageStatusRead, 400500);
                //list = null
                setLeaveMessageStatusTemplate(null, EZMessageStatus.EZMessageStatusRead, 400500);
                //list include space string
                leaveIds.clear();
                leaveIds.add("");
                setLeaveMessageStatusTemplate(leaveIds, EZMessageStatus.EZMessageStatusRead, 400500);
                //special chars in the list
                leaveIds.clear();
                leaveIds.add("@$%@$");
                setLeaveMessageStatusTemplate(leaveIds, EZMessageStatus.EZMessageStatusRead, 110001);
                // test interface deleteLeaveMessages
                LogUtil.i(TAG, "==========================deleteLeaveMessages Started==========================");
                //correct data
                leaveIds.clear();
                leaveIds.add(LeaveMessageList.get(0).getMsgId());
                deleteLeaveMessagesTemplate(leaveIds,0);
                //empty list
                deleteLeaveMessagesTemplate(new ArrayList<String>(),400500);
                //list = null
                deleteLeaveMessagesTemplate(null,400500);
                //list include space string
                List<String> deleteLeaveIds = new ArrayList<String>();
                deleteLeaveIds.add("");
                deleteLeaveMessagesTemplate(deleteLeaveIds,400500);
                //special chars in the list
                deleteLeaveIds.clear();
                deleteLeaveIds.add("$%^$@%");
                deleteLeaveMessagesTemplate(deleteLeaveIds,110001);

                // test interface formatStorage
                LogUtil.i(TAG, "==========================formatStorage Started==========================");
                formatStorageTemplate(DeviceSerial,1,0);
                formatStorageTemplate(DeviceAddedToOtherAccount,1,120018);
                formatStorageTemplate("",1,400500);
                formatStorageTemplate("234",1,400500);
                formatStorageTemplate("#$#$",1,400500);
                formatStorageTemplate(null,1,400500);


                // test interface getStorageStatus
                LogUtil.i(TAG, "==========================getStorageStatus Started==========================");
                getStorageStatusTemplate(DeviceSerial,0);
                getStorageStatusTemplate(DeviceAddedToOtherAccount,120018);
                getStorageStatusTemplate("",400500);
                getStorageStatusTemplate("234",400500);
                getStorageStatusTemplate("%$^",400500);
                getStorageStatusTemplate(null,400500);


                // test interface probeDeviceInfo
                LogUtil.i(TAG, "==========================probeDeviceInfo Started==========================");
                probeDeviceInfoTemplate(DeviceAdd, 0);
                probeDeviceInfoTemplate(DeviceSerial, 120020);
                probeDeviceInfoTemplate(DeviceOffline, 120023);
                probeDeviceInfoTemplate(DeviceAddedNotOnline, 120029);
                probeDeviceInfoTemplate(DeviceAddedToOtherAccount, 120022);
                probeDeviceInfoTemplate(DeviceAddedToOtherAccountNotOnline, 120024);
                probeDeviceInfoTemplate(DeviceSerial, 120020);
                probeDeviceInfoTemplate("", 400500);
                probeDeviceInfoTemplate(null, 400500);
                probeDeviceInfoTemplate("354", 400500);
                probeDeviceInfoTemplate("@#$54", 400500);


                // test interface getDeviceUpgradeStatus
                LogUtil.i(TAG, "==========================getDeviceUpgradeStatus Started==========================");
                getDeviceUpgradeStatusTemplate(DeviceSerial, 0);
                getDeviceUpgradeStatusTemplate(DeviceAddedToOtherAccount, 120018);
                getDeviceUpgradeStatusTemplate(DeviceOffline, 120018);
                getDeviceUpgradeStatusTemplate("", 400500);
                getDeviceUpgradeStatusTemplate(null, 400500);
                getDeviceUpgradeStatusTemplate("#$%", 400500);
                getDeviceUpgradeStatusTemplate("345", 400500);


                // test interface upgradeDevice
                LogUtil.i(TAG, "==========================upgradeDevice Started==========================");

                upgradeDeviceTemplate(DeviceSerial, 0);
                upgradeDeviceTemplate(DeviceAddedToOtherAccount, 120018);
                upgradeDeviceTemplate(DeviceOffline, 120018);
                upgradeDeviceTemplate("", 400500);
                upgradeDeviceTemplate("345", 400500);
                upgradeDeviceTemplate(null, 400500);
                upgradeDeviceTemplate("#$%", 400500);

                // test interface getDeviceInfoBySerial
                LogUtil.i(TAG, "==========================getDeviceInfoBySerial Started==========================");
                getDeviceInfoBySerialTemplate(DeviceSerial, 0);
                getDeviceInfoBySerialTemplate(DeviceAddedToOtherAccount, 120018);
                getDeviceInfoBySerialTemplate(DeviceOffline, 120018);
                getDeviceInfoBySerialTemplate("", 400500);
                getDeviceInfoBySerialTemplate("345", 400500);
                getDeviceInfoBySerialTemplate(null, 400500);
                getDeviceInfoBySerialTemplate("#$%", 400500);





                // test interface getPushNoticeMessage
                // 海外版不需要测试这个 add by yudan @ 07-21
//                try {
//                    LogUtil.i(TAG, "==========================getPushNoticeMessage Started==========================");
//                    EzvizApplication.getOpenSDK().getPushNoticeMessage("af");
//                } catch (BaseException e) {
//                    e.printStackTrace();
//                }

                try {
                    LogUtil.i(TAG, "==========================getPrivateMethodInvoke Started==========================");
                    Object result = EZUtils.getPrivateMethodInvoke(EzvizApplication.getOpenSDK(), "getHTTPPublicParam", new Class<?>[]{String.class}, "clientType");
                    Log.i(TAG, "run: getHTTPPublicParam(clientType)" + (String) result);
                } catch (Exception e) {
                    e.printStackTrace();
                }



            }});
        thr.start();
    }

    // test v3.3 interfaces
    private void testV33Interface(){
        Thread thr = new Thread(new Runnable(){
            @Override
            public void run() {
                // test interface getDeviceList
                List<EZAlarmInfo> alarmList = null;
                Calendar begin = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                begin.set(Calendar.HOUR_OF_DAY, 0);
                end.set(Calendar.HOUR_OF_DAY, 23);
                LogUtil.i(TAG, "==========================getDeviceList Started==========================");
                getDeviceListTemplate(0, 20, 0);
                getDeviceListTemplate(-1, 20, 400500);
                getDeviceListTemplate(0, -2, 400500);
                // test interface getDetectorList
                LogUtil.i(TAG, "==========================getDetectorList Started==========================");
                getDetectorListTemplate(A1DeviceSerial, 0);
                getDetectorListTemplate(DeviceSerial, 0);
                getDetectorListTemplate(DeviceAddedToOtherAccount, 120018);
                getDetectorListTemplate(DeviceOffline, 120018);
                getDetectorListTemplate("", 400500);
                getDetectorListTemplate("345", 400500);
                getDetectorListTemplate(null, 400500);
                getDetectorListTemplate("#$%", 400500);

                // test interface getAlarmListBySerial
                LogUtil.i(TAG, "==========================getAlarmListBySerial Started==========================");
                //if deviceSerial == null, it will return alarms for all devices belong to the account
                try {
                    alarmList = EzvizApplication.getOpenSDK().getAlarmList(A1DeviceSerial, 0, 20, begin, end);
                    Log.i("ReturnData", "run: Alarm info:" + alarmList);
                } catch (BaseException e) {
                    e.printStackTrace();
                }
                getAlarmListBySerialTemplate(A1DeviceSerial, 0, 5, begin, end, 0);
                getAlarmListBySerialTemplate(DeviceSerial, 0, 5, begin, end, 0);
                getAlarmListBySerialTemplate(DeviceAddedToOtherAccount, 0, 5, begin, end, 120018);
                getAlarmListBySerialTemplate(DeviceOffline, 0, 5, begin, end, 120018);
                getAlarmListBySerialTemplate("", 0, 20, begin, end, 0);
                getAlarmListBySerialTemplate("345", 0, 5, begin, end, 400500);
                getAlarmListBySerialTemplate(null, 0, 20, begin, end, 0);
                getAlarmListBySerialTemplate("#$%", 0, 5, begin, end, 400500);
                getAlarmListBySerialTemplate(A1DeviceSerial, -1, 5, begin, end, 400500);
                getAlarmListBySerialTemplate(A1DeviceSerial, 0, 5, null, end, 0);
                getAlarmListBySerialTemplate(A1DeviceSerial, 0, 5, null, null, 0);

                // test interface setDeviceDefence
                LogUtil.i(TAG, "==========================setDeviceDefence Started==========================");
                setDeviceDefenceTemplate("", EZConstants.EZDefenceStatus.EZDefence_IPC_CLOSE, 400500);
                setDeviceDefenceTemplate("123", EZConstants.EZDefenceStatus.EZDefence_IPC_CLOSE, 400500);
                setDeviceDefenceTemplate(null, EZConstants.EZDefenceStatus.EZDefence_IPC_CLOSE, 400500);
                setDeviceDefenceTemplate("@#$%", EZConstants.EZDefenceStatus.EZDefence_IPC_CLOSE, 400500);
                setDeviceDefenceTemplate(DeviceSerial, EZConstants.EZDefenceStatus.EZDefence_IPC_OPEN, 0);
                setDeviceDefenceTemplate(DeviceSerial, EZConstants.EZDefenceStatus.EZDefence_IPC_CLOSE, 110001);
                setDeviceDefenceTemplate(DeviceSerial, EZConstants.EZDefenceStatus.EZDefence_ALARMHOST_ATHOME, 110001);
                setDeviceDefenceTemplate(A1DeviceSerial, EZConstants.EZDefenceStatus.EZDefence_ALARMHOST_SLEEP, 0);
                setDeviceDefenceTemplate(A1DeviceSerial, EZConstants.EZDefenceStatus.EZDefence_ALARMHOST_ATHOME, 0);
                setDeviceDefenceTemplate(A1DeviceSerial, EZConstants.EZDefenceStatus.EZDefence_ALARMHOST_OUTER, 0);
                setDeviceDefenceTemplate(A1DeviceSerial, EZConstants.EZDefenceStatus.EZDefence_IPC_OPEN, 110001);


                // test interface sdeleteDevice
                LogUtil.i(TAG, "==========================deleteDevice Started==========================");
                deleteDeviceTemplate(A1DeviceSerial, 0);
                deleteDeviceTemplate("", 400500);
                deleteDeviceTemplate("123", 400500);
                deleteDeviceTemplate(null, 400500);
                deleteDeviceTemplate(DeviceAddedToOtherAccount, 120018);

                // test interface addDevice
                LogUtil.i(TAG, "==========================addDevice Started==========================");
                addDeviceTemplate(A1DeviceSerial, A1Validation, 0);
                addDeviceTemplate(DeviceSerial, A1Validation, 120017);
                addDeviceTemplate("", A1Validation, 400500);
                addDeviceTemplate("123", A1Validation, 400500);

                // test interface setDeviceDefence
                LogUtil.i(TAG, "==========================deleteAlarm Started==========================");
                List<String> alarmIdList = new ArrayList();
                alarmIdList.add(alarmList.get(0).getAlarmId());
                deleteAlarmTemplate(alarmIdList, 0);
                deleteAlarmTemplate(null, 400500);

                // test interface setAlarmStatus
                LogUtil.i(TAG, "==========================setAlarmStatus Started==========================");
                alarmIdList = new ArrayList();
                alarmIdList.add(alarmList.get(1).getAlarmId());
                try {
                    EzvizApplication.getOpenSDK().getAlarmList(A1DeviceSerial, 0, 5, begin, end);
                } catch (BaseException e) {
                    e.printStackTrace();
                }
                //setAlarmStatusTemplate(alarmIdList, EZAlarmStatus.EZAlarmStatusUnRead, 0);
                setAlarmStatusTemplate(alarmIdList, EZAlarmStatus.EZAlarmStatusRead, 0);
                //setAlarmStatusTemplate(alarmIdList, EZAlarmStatus.EZAlarmStatusUnRead, 0);
                setAlarmStatusTemplate(null, EZAlarmStatus.EZAlarmStatusRead, 400500);

                // test interface setDeviceDefence
                LogUtil.i(TAG, "==========================setDefence Started==========================");
                setDefenceTemplate(true, A1DeviceSerial, 110001);
                setDefenceTemplate(false, A1DeviceSerial, 0);
                setDefenceTemplate(false, DeviceAddedToOtherAccount, 120018);
                setDefenceTemplate(false, "", 400500);
                setDefenceTemplate(false, "123", 400500);
            }
        });
        thr.start();
    }

    // content of file /sdcard/videogo_test_cfg:
    // DeviceSerial:427734168
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
        } catch (java.io.IOException e) {
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

    private void capturePictureTemplate(String deviceSerial, int channelNo, int expectedCode){
        LogUtil.i(TAG, "capturePicture params, " + "deviceSerial:"+deviceSerial + " channelNo:"+channelNo + " expectedCode:"+expectedCode );
        try {
            String picUrl = EzvizApplication.getOpenSDK().captureCamera(deviceSerial, channelNo);
            LogUtil.i("ReturnData", "capturePicture: " + picUrl);
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void getUnreadMessageCountTemplate(String deviceSerial, EZConstants.EZMessageType messageType, int expectedCode){
        LogUtil.i(TAG, "unReadMessageCount params, " + "deviceSerial:"+deviceSerial + " messageType:"+messageType + " expectedCode:"+expectedCode );
        try {
            int msgCount = EzvizApplication.getOpenSDK().getUnreadMessageCount(deviceSerial, messageType);
            LogUtil.i("ReturnData", "unReadMessageCount:" + msgCount);
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void getLeaveMessageListTemplate(String deviceSerial, int pageIndex, int pageSize,Calendar beginTime, Calendar endTime, int expectedCode){
        LogUtil.i(TAG, "getLeaveMessageList params, " + "deviceSerial:"+deviceSerial + " pageIndex:"+pageIndex + " pageSize:"+pageSize + " beginTime:"+beginTime + " endTime:"+endTime + " expectedCode:"+expectedCode);
        try {
            List<EZLeaveMessage> LeaveMessageList =EzvizApplication.getOpenSDK().getLeaveMessageList(deviceSerial, pageIndex, pageSize, beginTime, endTime);
            LogUtil.i("ReturnData", "getLeaveMessageList:" + LeaveMessageList);
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void setLeaveMessageStatusTemplate(List<String> msgIdList, EZMessageStatus messageStatus, int expectedCode){
        LogUtil.i(TAG, "setLeaveMessageStatus params, " + "msgIdList:"+msgIdList + " messageStatus:"+messageStatus + " expectedCode:"+expectedCode );
        try {
            EzvizApplication.getOpenSDK().setLeaveMessageStatus(msgIdList, messageStatus);
            //verify
            try {
                Calendar begin = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                begin.set(Calendar.HOUR_OF_DAY, 0);
                end.set(Calendar.HOUR_OF_DAY, 23);
                List<EZLeaveMessage> verifyLeaveMessageList = EzvizApplication.getOpenSDK().getLeaveMessageList(F1DeviceSerial, 0, 5, begin, end);
                for(EZLeaveMessage temp : verifyLeaveMessageList){
                    if (temp.getMsgId().equals(msgIdList.get(0))){
                        LogUtil.i(TAG, "the value of EZMessageStatus.EZMessageStatusRead.getStatus():"+EZMessageStatus.EZMessageStatusRead.getStatus());
                        if (temp.getMsgStatus() != EZMessageStatus.EZMessageStatusRead.getStatus()){
                            throw new BaseException("this leaveID's status " + temp.getMsgStatus() + " != " + EZMessageStatus.EZMessageStatusRead + " ,message id : " + temp.getMsgId(),0);
                        };
                    }
                }
            } catch (BaseException e) {
                e.printStackTrace();
            }

        }catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void deleteLeaveMessagesTemplate(List<String> msgIdList, int expectedCode){
        LogUtil.i(TAG, "deleteLeaveMessages params, " + "msgIdList:"+msgIdList + " expectedCode:"+expectedCode );
        try {
            EzvizApplication.getOpenSDK().deleteLeaveMessages(msgIdList);
            //verify
            try {
                Calendar begin = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                begin.set(Calendar.HOUR_OF_DAY, 0);
                end.set(Calendar.HOUR_OF_DAY, 23);
                List<EZLeaveMessage> verifyLeaveMessageList = EzvizApplication.getOpenSDK().getLeaveMessageList(F1DeviceSerial, 0, 5, begin, end);
                for(EZLeaveMessage temp : verifyLeaveMessageList){
                    if (temp.getMsgId().equals(msgIdList.get(0))){
                        throw new BaseException("this leave id is still exist : " + msgIdList.get(0), 0);
                    }
                }
            } catch (BaseException e) {
                e.printStackTrace();

            }

        }catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }


    private void formatStorageTemplate(String deviceSerial, int partitionIndex, int expectedCode){
        LogUtil.i(TAG, "formatStorage params, " + "deviceSerial:"+deviceSerial + " partitionIndex:"+partitionIndex +" expectedCode:"+expectedCode );
        try {

            boolean formatResult = EzvizApplication.getOpenSDK().formatStorage(deviceSerial, partitionIndex);
            LogUtil.i("ReturnData", "formatStorage:" + formatResult);
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void getStorageStatusTemplate(String deviceSerial, int expectedCode){
        LogUtil.i(TAG, "getDeviceInfoBySerial params, " + "deviceSerial:"+deviceSerial + " expectedCode:"+expectedCode );
        try {
            List<EZStorageStatus> storageList = EzvizApplication.getOpenSDK().getStorageStatus(deviceSerial);
            LogUtil.i("ReturnData", "getStorageStatus:" + storageList);
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void probeDeviceInfoTemplate(String deviceSerial, int expectedCode){
        LogUtil.i(TAG, "probeDeviceInfo params, " + "deviceSerial:"+deviceSerial + " expectedCode:"+expectedCode );
        try {
            EZProbeDeviceInfo probeInfo = EzvizApplication.getOpenSDK().probeDeviceInfo(deviceSerial);
            LogUtil.i("ReturnData", "probeDeviceInfo:" + probeInfo);
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void getDeviceUpgradeStatusTemplate(String deviceSerial, int expectedCode){
        LogUtil.i(TAG, "getDeviceUpgradeStatus params, " + "deviceSerial:"+deviceSerial + " expectedCode:"+expectedCode );
        try {
            EZDeviceUpgradeStatus status = EzvizApplication.getOpenSDK().getDeviceUpgradeStatus(deviceSerial);
            LogUtil.i("ReturnData", "run: getDeviceUpgradeStatus" + status);
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void upgradeDeviceTemplate(String deviceSerial, int expectedCode){
        LogUtil.i(TAG, "upgradeDevice params, " + "deviceSerial:"+deviceSerial + " expectedCode:"+expectedCode );
        try {
            EzvizApplication.getOpenSDK().upgradeDevice(deviceSerial);
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void getDeviceInfoBySerialTemplate(String deviceSerial, int expectedCode)
    {
        LogUtil.i(TAG, "getDeviceInfoBySerial params, " + "deviceSerial:"+deviceSerial + " expectedCode:"+expectedCode );
//        try {
//            EZDeviceInfo info = EzvizApplication.getOpenSDK().getDeviceInfoBySerial(deviceSerial);
//            Log.i("ReturnData", "run: device info:" + info);
//        } catch (BaseException e) {
//            e.printStackTrace();
//            assertEq(e.getErrorCode(), expectedCode);
//        }

    }

    private void getDeviceListTemplate(int pageIndex, int pageSize, int expectedCode)
    {
        LogUtil.i(TAG, "getDeviceList params, " + "pageIndex:"+pageIndex + " pageSize:"+pageSize + " expectedCode:"+expectedCode );
        try {
            List<EZDeviceInfo> infoList = EzvizApplication.getOpenSDK().getDeviceList(pageIndex, pageSize);
            Log.i("ReturnData", "run: device info:" + infoList);
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }

    }

    private void getDetectorListTemplate(String deviceSerial, int expectedCode)
    {
        LogUtil.i(TAG, "getDetectorList params, " + "deviceSerial:"+deviceSerial + " expectedCode:"+expectedCode );
//        try {
//            List<EZDetectorInfo> infoList = EzvizApplication.getOpenSDK().getDetectorList(deviceSerial);
//            Log.i("ReturnData", "run: detector info:" + infoList);
//        } catch (BaseException e) {
//            e.printStackTrace();
//            assertEq(e.getErrorCode(), expectedCode);
//        }
    }

    private void getAlarmListBySerialTemplate(String deviceSerial, int pageIndex, int pageSize, Calendar beginTime, Calendar endTime, int expectedCode)
    {
        LogUtil.i(TAG, "getAlarmListBySerial params, " + "deviceSerial:"+deviceSerial + " pageIndex:"+pageIndex + " pageSize:"+pageSize  + " beginTime:"+beginTime + " endTime:"+endTime + " expectedCode:"+expectedCode );
        try {
            List<EZAlarmInfo> infoList = EzvizApplication.getOpenSDK().getAlarmList(deviceSerial, pageIndex, pageSize, beginTime, endTime);
            Log.i("ReturnData", "run: Alarm info:" + infoList);
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void setDeviceDefenceTemplate(String deviceSerial, EZConstants.EZDefenceStatus defence, int expectedCode)
    {
        LogUtil.i(TAG, "setDeviceDefence params, " + "deviceSerial:"+deviceSerial + " defence:"+defence + " expectedCode:"+expectedCode );
        try {
            EzvizApplication.getOpenSDK().setDefence(deviceSerial, defence);
            //verify
            try {
                // thread to sleep for 1000 milliseconds
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println(e);
            }
//            try {
//                EZDeviceInfo temp = EzvizApplication.getOpenSDK().getDeviceInfoBySerial(deviceSerial);
//                Log.i("VerifyData", "run: device info:" + temp);
//                assertEq(temp.getIsDefence(), defence);
//            } catch (BaseException e) {
//                e.printStackTrace();
//
//            }
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void addDeviceTemplate(String deviceSerial, String deviceCode, int expectedCode)
    {
        LogUtil.i(TAG, "addDevice params, " + "deviceSerial:"+deviceSerial + " deviceCode:"+deviceCode + " expectedCode:"+expectedCode );
        try {
            EzvizApplication.getOpenSDK().addDevice(deviceSerial, deviceCode);
            //verify
            try {
                // thread to sleep for 1000 milliseconds
                //Thread.sleep(1000);
                List<EZDeviceInfo> infoList = EzvizApplication.getOpenSDK().getDeviceList(0, 20);
                Log.i("ReturnData", "run: device info:" + infoList);
                for (EZDeviceInfo tempDeviceInfo : infoList){
                    if(tempDeviceInfo.getDeviceSerial().equals(deviceSerial)){
                        LogUtil.i(TAG, "Find added device " + deviceSerial +" !!!");
                        break;
                    }
                }
            } catch (BaseException e) {
                e.printStackTrace();
            }
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void deleteDeviceTemplate(String deviceSerial, int expectedCode)
    {
        LogUtil.i(TAG, "deleteDevice params, " + "deviceSerial:"+deviceSerial + " expectedCode:"+expectedCode );
        try {
            EzvizApplication.getOpenSDK().deleteDevice(deviceSerial);
            //verify
            try {
                // thread to sleep for 1000 milliseconds
                //Thread.sleep(1000);
                List<EZDeviceInfo> infoList = EzvizApplication.getOpenSDK().getDeviceList(0, 20);
                Log.i("ReturnData", "run: device info:" + infoList);
                for (EZDeviceInfo tempDeviceInfo : infoList){
                    if(tempDeviceInfo.getDeviceSerial().equals(deviceSerial)){
                        LogUtil.i(TAG, "Find added device " + deviceSerial +" !!!");
                        throw new BaseException("this device is still exist : " + deviceSerial, 0);
                    }
                }
            } catch (BaseException e) {
                e.printStackTrace();
            }
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void deleteAlarmTemplate(List<String> alarmIdList, int expectedCode)
    {
        LogUtil.i(TAG, "deleteAlarm params, " + "alarmIdList:"+alarmIdList + " expectedCode:"+expectedCode );
        try {
            EzvizApplication.getOpenSDK().deleteAlarm(alarmIdList);
            //verify
            try {
                Calendar begin = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                begin.set(Calendar.HOUR_OF_DAY, 0);
                end.set(Calendar.HOUR_OF_DAY, 23);
                List<EZAlarmInfo> verifyAlarmList = EzvizApplication.getOpenSDK().getAlarmList(A1DeviceSerial, 0, 5, begin, end);
                for(EZAlarmInfo temp : verifyAlarmList){
                    if (temp.getAlarmId().equals(alarmIdList.get(0))){
                        throw new BaseException("this leave id is still exist : " + alarmIdList.get(0), 0);
                    }
                }
            } catch (BaseException e) {
                e.printStackTrace();

            }
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void setAlarmStatusTemplate(List<String> alarmIdList, EZAlarmStatus alarmStatus,int expectedCode)
    {
        LogUtil.i(TAG, "setAlarmStatus params, " + "alarmIdList:"+alarmIdList + " alarmStatus:"+alarmStatus + " expectedCode:"+expectedCode );
        try {
            EzvizApplication.getOpenSDK().setAlarmStatus(alarmIdList, alarmStatus);
            //verify
            try {
                Calendar begin = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                begin.set(Calendar.HOUR_OF_DAY, 0);
                end.set(Calendar.HOUR_OF_DAY, 23);
                List<EZAlarmInfo> verifyAlarmList = EzvizApplication.getOpenSDK().getAlarmList(A1DeviceSerial, 0, 5, begin, end);
                for(EZAlarmInfo temp : verifyAlarmList){
                    if (temp.getAlarmId().equals(alarmIdList.get(0))){
                        assertEq(temp.getIsRead(), alarmStatus.getAlarmStatus()-1);
                    }
                }
            } catch (BaseException e) {
                e.printStackTrace();

            }
        } catch (BaseException e) {
            e.printStackTrace();
            assertEq(e.getErrorCode(), expectedCode);
        }
    }

    private void setDefenceTemplate(boolean isDefence, String deviceSerial, int expectedCode)
    {
        LogUtil.i(TAG, "setDefence params, " + "isDefence:"+isDefence + " deviceSerial:"+deviceSerial + " expectedCode:"+expectedCode );
//        try {
//            EzvizApplication.getOpenSDK().setDefence(isDefence, deviceSerial);
//            //verify
//            try {
//                List<EZDeviceInfo> infoList = EzvizApplication.getOpenSDK().getDeviceList(0, 20);
//                Log.i("ReturnData", "run: device info:" + infoList);
//                for (EZDeviceInfo tempDeviceInfo : infoList){
//                    if(tempDeviceInfo.getDeviceSerial().equals(deviceSerial)){
//                        if(isDefence){
//                            Assert.assertEquals(tempDeviceInfo.getIsDefence(), 1);
//                        }else{
//                            assertEq(tempDeviceInfo.getIsDefence(), 0);
//                        }
//                    }
//                }
//            } catch (BaseException e) {
//                e.printStackTrace();
//
//            }
//        } catch (BaseException e) {
//            e.printStackTrace();
//            assertEq(e.getErrorCode(), expectedCode);
//        }
    }

    private void assertEq(int code, int expectedCode){
        if (code != expectedCode){
            LogUtil.errorLog(TAG, "ASSERT ERROR!!!!!!! " + code +" != " + expectedCode);
            //throw new BaseException("assertEq Error: " + code + " is not equal to " + expectedCode , 0);
        }
    }
}
