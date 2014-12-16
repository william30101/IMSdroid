package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartCmd;
import org.doubango.imsdroid.UartReceive;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.BLE.BLEDeviceControlActivity;
import org.doubango.imsdroid.BLE.BLEDeviceScanActivity;
import org.doubango.imsdroid.cmd.SetBtnFun;
import org.doubango.imsdroid.cmd.SetUIFunction;
import org.doubango.imsdroid.map.MapScreen;
import org.doubango.imsdroid.map.MapScreenView;
import org.doubango.ngn.services.INgnSipService;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

public class ScreenDirection extends BaseScreen{

	
	private final INgnSipService mSipService;
	private static String TAG = ScreenDirection.class.getCanonicalName();
	
	//private XMPPSetting XMPPSet;
	private UartReceive uartRec;
	private SetBtnFun setBtn;
	private MapScreen mapScreen;


	private SetUIFunction setUI;
	private MapScreenView mapScreenView;

	private BLEDeviceScanActivity BLEActivity;
	private BLEDeviceControlActivity BLEDevCon;
	public static TextView mConnectionState;
	
	public ScreenDirection() {
		super(SCREEN_TYPE.DIALER_T, TAG);
		
		mSipService = getEngine().getSipService();
		
	}
	
	
	public static Handler BLEStatusHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				Log.i(TAG,"ble status handler = " + msg.obj);
				//CDTextView.setText("Step" + (Integer) msg.obj);
				// game.pathFlag = false;
				mConnectionState.setText((String)msg.obj);
			}
		}
	};
	
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
		
		/* Judge device support BlueTooth 4.0 */
		if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
		    //Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
		    Log.i(TAG,"support BT 4.0");
		    BLEActivity = new BLEDeviceScanActivity();
			BLEActivity.BLEDeviceScanStart(this);
		}
		else{
			Log.i(TAG,"not support BT 4.0");
		}
		mConnectionState = (TextView) findViewById(R.id.BLEconnectStatus);
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.i("ble", "onResume");
		BLEActivity.scanLeDeviceStart(true);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		BLEActivity.scanLeDeviceStart(false);
		 //unbindService(BLEDevCon.getmServiceConnection());
		 //BLEDeviceControlActivity.setmBluetoothLeService(null);
	        //mBluetoothLeService = null;
	}
	
}
