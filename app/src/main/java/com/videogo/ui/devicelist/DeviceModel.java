package com.videogo.ui.devicelist;

import ezviz.ezopensdk.R;

public enum DeviceModel {

    // C1
    C1("C1", R.drawable.device_c1_rotate, R.drawable.device_c1_rotate, true, DeviceCategory.IP_CAMERA, "CS-C1", "DS-2CD8464"),
    OTHER("", R.drawable.device_c1_rotate, R.drawable.device_c1_rotate, false, null, "OTHER");

    private String[] key;
    private String display;
    private int drawable1ResId;
    private int drawable2ResId;
    private boolean camera;
    private DeviceCategory category;

    private DeviceModel(String display, int drawable1ResId, int drawable2ResId, boolean camera,
        DeviceCategory category, String... key) {
        this.display = display;
        this.drawable1ResId = drawable1ResId;
        this.drawable2ResId = drawable2ResId;
        this.key = key;
        this.camera = camera;
        this.category = category;
    }

    public int getDrawable1ResId() {
        return drawable1ResId;
    }

    public int getDrawable2ResId() {
        return drawable2ResId;
    }

    public String getDisplay() {
        return display;
    }

    public boolean isCamera() {
        return camera;
    }

    public DeviceCategory getCategory() {
        return category;
    }

//    public static DeviceModel getDeviceModel(DeviceInfoEx device) {
//        return C1;
//    }

    public static DeviceModel getDeviceModel(String model) {
//        if (TextUtils.isEmpty(model))
//            return null;

        return OTHER;
    }

    public String[] getKey() {
        return key;
    }

    public void setKey(String[] key) {
        this.key = key;
    }
}