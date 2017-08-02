package com.videogo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.videogo.constant.Config;
import com.videogo.util.LocalInfo;
import com.videogo.util.LogUtil;
import com.videogo.util.Utils;
import com.videogo.widget.WaitDialog;

public class RootActivity extends Activity {
    // private static boolean isResumed = false;
    private Toast mToast = null;

    /** 标示是否在显示提示 */
    private boolean mIsTip = true;

    private static boolean mBackground = false;

    protected int pageKey = -1;
    public static boolean isOnRusumed = false;
    /** 等待框 */
    private WaitDialog mWaitDlg;

    protected void showToast(int id) {
        if (!mIsTip) {
            return;
        }

        if (isFinishing()) {
            return;
        }
        String text = getString(id);
        if (text != null && !text.equals("")) {
            if (mToast == null) {
                mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    protected void showToast(int id, int errCode) {
        if (!mIsTip) {
            return;
        }

        if (isFinishing()) {
            return;
        }

        String text = getString(id);
        if (errCode != 0) {
            int errorId = getErrorId(errCode);
            if (errorId != 0) {
                text = getString(errorId);
            } else {
                text = text + " (" + errCode + ")";
            }
        }
        if (text != null && !text.equals("")) {
            if (mToast == null) {
                mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    protected void showToast(int id, String msg) {
        if (!mIsTip) {
            return;
        }

        if (isFinishing()) {
            return;
        }

        String text = getString(id);
        if (!TextUtils.isEmpty(msg)) {
            text = text + " (" + msg + ")";
        }
        if (text != null && !text.equals("")) {
            if (mToast == null) {
                mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    protected void showToast(CharSequence text) {
        if (!mIsTip) {
            return;
        }

        if (isFinishing()) {
            return;
        }
        if (text != null && !text.equals("")) {
            if (mToast == null) {
                mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    protected int getErrorId(int errorCode) {
        int errorId = this.getResources().getIdentifier("error_code_" + errorCode, "string", this.getPackageName());
        /*
         * Field fieldError; int errorId = 0; try { fieldError =
         * R.string.class.getDeclaredField("error_code_" + errorCode);
         * fieldError.setAccessible(true); R.string string = new R.string(); try { errorId =
         * fieldError.getInt(string); } catch (IllegalAccessException e) { // TODO Auto-generated
         * catch block e.printStackTrace(); } catch (IllegalArgumentException e) { // TODO
         * Auto-generated catch block e.printStackTrace(); } } catch (NoSuchFieldException e) { //
         * TODO Auto-generated catch block e.printStackTrace(); }
         */

        return errorId;
    }

    /**
     * 消息提示控件
     * 
     * @param text
     * @see
     * @since V1.0
     */
    protected void showToast(int res1, int res2, int errCode) {
        String text = res1 != 0 ? getString(res1) : "";
        if (res2 != 0) {
            text = text + ", " + getString(res2);
        }
        if (errCode != 0) {
            int errorId = getErrorId(errCode);
            if (errorId != 0) {
                text = getString(errorId);
            } else {
                text = text + " (" + errCode + ")";
            }
        }
        if (text != null) {
            if (mToast == null) {
                mToast = Toast.makeText(this, text, Toast.LENGTH_LONG);
                mToast.setGravity(Gravity.CENTER, 0, 0);
            } else {
                mToast.setText(text);
            }
            mToast.show();
        }
    }

    /**
     * <p>
     * 设置PageKey
     * </p>
     * 
     * @param argPageKey
     */
    protected void setPageKey(int argPageKey) {
        this.pageKey = argPageKey;
    }

    /**
     * 显示加载进度
     * 
     * @param content
     * @see
     * @since V1.8.2
     */
    protected void showWaitDialog(String content) {
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        if (content != null && !content.equals("")) {
            mWaitDlg.setWaitText(content);
        }
        mWaitDlg.setCancelable(false);
        mWaitDlg.show();
    }

    /**
     * 显示加载进度圈
     * 
     * @param resId
     * @see
     * @since V1.8.2
     */
    protected void showWaitDialog(int resId) {
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDlg.setWaitText(getString(resId));
        mWaitDlg.setCancelable(false);
        mWaitDlg.show();
    }

    /**
     * 显示加载进度圈
     * 
     * @see
     * @since V1.8.2
     */
    public void showWaitDialog() {
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDlg.setCancelable(false);
        mWaitDlg.show();
    }

    /**
     * 显示加载进度圈
     * 
     * @see
     * @since V1.8.2
     */
    public void showCancelableWaitDialog() {
        mWaitDlg = new WaitDialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        mWaitDlg.setCancelable(true);
        mWaitDlg.show();
    }

    /**
     * 判断dialog是否显示
     * 
     * @see
     * @since V1.8.2
     */
    public boolean isDialogShowing() {
        if (mWaitDlg != null && mWaitDlg.isShowing()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * dismiss进度圈
     * 
     * @see
     * @since V1.8.2
     */
    public void dismissWaitDialog() {
        if (mWaitDlg != null && mWaitDlg.isShowing()) {
            mWaitDlg.dismiss();
        }
    }

    /**
     * @return the isResumed
     */
    // public static boolean isResumed() {
    // return isResumed;
    // }

    /**
     * @param isResumed
     *            the isResumed to set
     */
    // public static void setResumed(boolean isResumed) {
    // RootActivity.isResumed = isResumed;
    // }

    protected String getErrorTip(int id, int errCode) {
        StringBuffer errorTip = new StringBuffer();

        if (errCode != 0) {
            int errorId = getErrorId(errCode);
            if (errorId != 0) {
                errorTip.append(getString(errorId));
            } else {
                errorTip.append(getString(id)).append(" (").append(errCode).append(")");
            }
        } else {
            errorTip.append(getString(id));
        }
        return errorTip.toString();
    }

    /**
     * 隐藏虚拟键盘
     * 
     * @see
     * @since V1.8.2
     */
    protected void hideInputMethod() {
        if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * removeHandler事件
     * 
     * @param handler
     * @see
     * @since V1.8.2
     */
    protected void removeHandler(Handler handler) {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }
}
