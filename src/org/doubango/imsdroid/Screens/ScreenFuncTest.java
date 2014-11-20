package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.Screens.ScreenTabDialer.PhoneInputType;
import org.doubango.ngn.services.INgnSipService;
import org.jivesoftware.smack.XMPPConnection;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ScreenFuncTest extends BaseScreen {

	//private static String TAG = ScreenTabDialer.class.getCanonicalName();
	private static String TAG = "william";
	
	private final INgnSipService mSipService;

	public ScreenFuncTest() {
		super(SCREEN_TYPE.DIALER_T, TAG);
		
		mSipService = getEngine().getSipService();
		
	}
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_function_test);
		
		Button DirectionBtn= (Button)findViewById(R.id.DirectionBtn);
		Button AVBtn= (Button)findViewById(R.id.AVBtn);
		Button BLEBtn= (Button)findViewById(R.id.BLEBtn);
		Button NetworkBtn= (Button)findViewById(R.id.NetworkBtn);
		Button NattBtn= (Button)findViewById(R.id.NattBtn);
		
		DirectionBtn.setOnClickListener(ClickListener);
		AVBtn.setOnClickListener(ClickListener);
		BLEBtn.setOnClickListener(ClickListener);
		NetworkBtn.setOnClickListener(ClickListener);
		NattBtn.setOnClickListener(ClickListener);
			
	}
	
	
	private OnClickListener ClickListener = new OnClickListener() {
	    @Override
	    public void onClick(final View v) {
	    	switch(v.getId()){
	    		case R.id.DirectionBtn : 
	    			mScreenService.show(ScreenDirection.class, "Direction");
	    			//mScreenService.show(ScreenDirectionJS.class, "Direction");
	    		break;
	    		case R.id.AVBtn : 
	    			mScreenService.show(ScreenTabDialer.class, "Dial");
	    		break;
	    		case R.id.BLEBtn : 
	    			//SetLayout();
	    		break;
	    		case R.id.NetworkBtn : 
	    			mScreenService.show(ScreenNetwork.class, "NetworkSetting");
	    		break;
	    		case R.id.NattBtn : 
	    			mScreenService.show(ScreenNatt.class, "NattSetting");
	    		break;
	    		default:
	    			Log.i(TAG,"Invaild Button function");
	    			break;
	    	}
	    }
	};
	
}
