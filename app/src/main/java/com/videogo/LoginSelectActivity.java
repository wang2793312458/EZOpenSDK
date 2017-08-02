/* 
 * @ProjectName ezviz-openapi-android-demo
 * @Copyright HangZhou Hikvision System Technology Co.,Ltd. All Right Reserved
 * 
 * @FileName LoginSelectActivity.java
 * @Description 这里对文件进行描述
 * 
 * @author chenxingyf1
 * @data 2014-12-6
 * 
 * @note 这里写本文件的详细功能描述和注释
 * @note 历史记录
 * 
 * @warning 这里写本文件的相关警告
 */
package com.videogo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import com.videogo.ui.cameralist.EZCameraListActivity;
import com.videogo.ui.util.ActivityUtils;

import ezviz.ezopensdk.R;
/**
 * 登录选择演示
 * @author xiaxingsuo
 * @data 2015-11-6
 */
public class LoginSelectActivity extends Activity implements OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        
        initData();
        initView();
    }
    
    private void initData() {

    }
    
    private void initView() {
        
    }

    /* (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch(v.getId()) {
        	case R.id.interface_call_btn:
                if (TextUtils.isEmpty(EzvizApplication.AppKey)){
                    Toast.makeText(this,"Appkey为空",Toast.LENGTH_LONG).show();
                    return;
                }
        		intent = new Intent(LoginSelectActivity.this, InterfaceDemoActivity.class);
                startActivity(intent);
//                EzvizApplication.getOpenSDK().stopPushService();
        		break;
            case R.id.web_login_btn:
                if (TextUtils.isEmpty(EzvizApplication.AppKey)){
                    Toast.makeText(this,"Appkey为空",Toast.LENGTH_LONG).show();
                    return;
                }
//                EzvizApplication.getOpenSDK().startPushService();
                ActivityUtils.goToLoginAgain(LoginSelectActivity.this);
//                intent = new Intent(getApplication(), EzvizWebViewActivity.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.putExtra(IntentConsts.EXTRA_WEBVIEW_ACTION, EzvizWebViewActivity.WEBVIEW_ACTION_CLOUDPAGE);
//                startActivity(intent);
                break;
            case R.id.goto_cameralist_btn:
                openPlatformLoginDialog();
                break;
            case R.id.id_ll_join_qq_group:
                String key = "p57CNgQ_uf2gZMY0eYTvgQ_S_ZDzZz44";
                joinQQGroup(key);
                break;
            default:
                break;
        }
    }
    
    private void openPlatformLoginDialog() {
        final EditText editText = new EditText(this);
        new  AlertDialog.Builder(this)  
        .setTitle(R.string.please_input_platform_accesstoken_txt)   
        .setIcon(android.R.drawable.ic_dialog_info)   
        .setView(editText)  
        .setPositiveButton(R.string.certain, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //String getAccessTokenSign = SignUtil.getGetAccessTokenSign();

                EzvizApplication.getOpenSDK().setAccessToken(editText.getText().toString());
                Intent toIntent = new Intent(LoginSelectActivity.this, EZCameraListActivity.class);
                toIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                LoginSelectActivity.this.startActivity(toIntent);
            }
            
        })   
        .setNegativeButton(R.string.cancel, null)
        .show();  
    }

    private boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }
}
