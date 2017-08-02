/**
 * @ProjectName: 民用软件平台软件
 * @Copyright: 2012 HangZhou Hikvision System Technology Co., Ltd. All Right Reserved.
 * @address: http://www.hikvision.com
 * @date: 2014-6-17 上午11:43:40
 * @Description: 本内容仅限于杭州海康威视数字技术股份有限公司内部使用，禁止转发.
 */
package com.videogo.remoteplayback.list.play;

import com.videogo.remoteplayback.RemoteFileInfo;
import com.videogo.openapi.bean.resp.CloudFile;

/**
 * <p>
 * 播放回调函数
 * </p>
 * 
 * @author hanlifeng 2014-6-17 上午11:43:40
 * @version V2.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2014-6-17
 * @modify by reason:{方法名}:{原因}
 */
public interface PlayRemoteListTaskCallback {

    /**
     * <p>
     * 云播放密码错误
     * </p>
     * 
     * @author hanlifeng 2014-6-18 上午10:16:19
     * @param cloudFile
     */
    void playCloudPasswordError(CloudFile cloudFile);

    /**
     * <p>
     * 播放成功
     * </p>
     * 
     * @author hanlifeng 2014-6-18 下午3:17:35
     */
    void playSucess();

    /**
     * <p>
     * 本地播放密码错误
     * </p>
     * 
     * @author hanlifeng 2014-6-19 下午3:21:28
     */
    void playLocalPasswordError(RemoteFileInfo fileInfo);

    /**
     * <p>
     * 回放异常
     * </p>
     * 
     * @author hanlieng 2014-7-1 上午9:57:58
     * @param errorCode
     * @param detail
     */
    void playException(int errorCode, int retryCount, String detail);

    /**
     * <p>
     * task play over
     * </p>
     * 
     * @author hanlieng 2014-8-1 下午12:53:07
     */
    void playTaskOver(int type);

    /**
     * <p>
     * 抓图成功
     * </p>
     * 
     * @author hanlieng 2014-7-11 下午2:48:52
     * @param filePath
     */
    void capturePictureSuccess(String filePath);

    /**
     * <p>
     * 抓图失败
     * </p>
     * 
     * @author hanlieng 2014-7-11 下午2:49:20
     * @param errorCode
     */
    void capturePictureFail(int errorCode);

    /**
     * <p>
     * 开始录像成功
     * </p>
     * 
     * @author hanlieng 2014-7-12 下午3:15:17
     * @param filePath
     */
    void startRecordSuccess(String filePath);

    /**
     * <p>
     * 开始录像失败
     * </p>
     * 
     * @author hanlieng 2014-7-12 下午3:15:46
     * @param errorCode
     */
    void startRecordFail(int errorCode);

}
