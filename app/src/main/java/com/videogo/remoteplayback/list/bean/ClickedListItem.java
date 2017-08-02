/**
 * @ProjectName: 民用软件平台软件
 * @Copyright: 2012 HangZhou Hikvision System Technology Co., Ltd. All Right Reserved.
 * @address: http://www.hikvision.com
 * @date: 2014-6-16 下午4:58:48
 * @Description: 本内容仅限于杭州海康威视数字技术股份有限公司内部使用，禁止转发.
 */
package com.videogo.remoteplayback.list.bean;

import java.util.Calendar;

/**
 * <p>
 * 被点击的回放Item
 * </p>
 * 
 * @author hanlifeng 2014-6-16 下午4:58:48
 * @version V2.0
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2014-6-16
 * @modify by reason:{方法名}:{原因}
 */
public class ClickedListItem {

    private int index;

    private int type;

    private long beginTime;

    private long endTime;

    private Calendar uiPlayTimeOnStop = null;

    private int position;
    
    private int fileSize;

    public int getIndex() {
        return index;
    }

    public ClickedListItem(int index, int type, long beginTime, long endTime, int position) {
        super();
        this.index = index;
        this.type = type;
        this.beginTime = beginTime;
        this.endTime = endTime;
        this.position = position;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public Calendar getUiPlayTimeOnStop() {
        return uiPlayTimeOnStop;
    }

    public void setUiPlayTimeOnStop(Calendar uiPlayTimeOnStop) {
        this.uiPlayTimeOnStop = uiPlayTimeOnStop;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return "[index=" + index + ", type=" + type + "]";
    }

}
