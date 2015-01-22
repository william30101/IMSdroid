package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartReceive;
import org.doubango.imsdroid.cmd.SetBtnFun;
import org.doubango.imsdroid.cmd.SetUIFunction;
import org.doubango.imsdroid.map.MapScreen;
import org.doubango.imsdroid.map.MapScreenView;
import org.doubango.ngn.services.INgnSipService;

import android.os.Bundle;
import android.util.Log;


public class ScreenDirection extends BaseScreen{

	
	private final INgnSipService mSipService;
	private static String TAG = ScreenDirection.class.getCanonicalName();
	
	//private XMPPSetting XMPPSet;
	private UartReceive uartRec;
	private SetBtnFun setBtn;
	private MapScreen mapScreen;


	private SetUIFunction setUI;
	private MapScreenView mapScreenView;
	
	public ScreenDirection() {
		super(SCREEN_TYPE.DIALER_T, TAG);
		
		mSipService = getEngine().getSipService();
		
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* ScreenDirection function of william's original code */ 
		//setContentView(R.layout.screen_direction);

//		setBtn = new SetBtnFun();
//		setBtn.SetBtn(this);
//		
//		mapScreen = new MapScreen();
//		mapScreen.MapScreen(this);
		
		
		
		/* Screen JayStick function of shinhua's code */
		setContentView(R.layout.screen_directionjs);

		setUI = new SetUIFunction(this);
		setUI.StartUIFunction();
		
	
		mapScreenView = new MapScreenView();
		mapScreenView.MapScreenView(this);
		Log.i("ble", "board name is " + android.os.Build.MODEL);
		

		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
}
