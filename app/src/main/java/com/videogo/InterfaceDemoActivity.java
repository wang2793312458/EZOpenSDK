package com.videogo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.videogo.test.InterfaceSelfTestActivity;
import com.videogo.test.InterfaceTestActivity;
import ezviz.ezopensdk.R;
public class InterfaceDemoActivity extends Activity implements OnClickListener{
	private static final String TAG = "InterfaceDemoActivity";
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ez_demo_interface_activity);
        
        initData();
        initView();  
	}
    private void initData() {
//    	mEZOpenSDK = EZOpenSDK.getInstance();
    }
    
    private void initView() {
        
    }

    @Override
    public void onClick(View v) {
        Intent intent  = null;
        switch(v.getId()) {
            case R.id.ez_square_btn: 

                break;
            case R.id.ez_platform_login_btn:
                break;
            case R.id.ez_v32_api_test:
                Intent i = new Intent(InterfaceDemoActivity.this, InterfaceTestActivity.class);
                startActivity(i);
                break;
            case R.id.ez_api_self_test:
                intent = new Intent(InterfaceDemoActivity.this, InterfaceSelfTestActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

	@Override
	protected void onResume() {
		super.onResume();
	}

}
