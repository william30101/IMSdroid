package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.cmd.SetBtnFun;
import org.doubango.imsdroid.map.Game;
import org.doubango.imsdroid.map.GameView;
import org.doubango.imsdroid.map.MapList;
import org.doubango.imsdroid.map.MapScreen;
import org.doubango.imsdroid.map.NetworkStatus;


import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.doubango.imsdroid.IMSDroid;
import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartReceive;
import org.doubango.ngn.services.INgnSipService;

public class ScreenMapNavigaton extends BaseScreen{
	
	private static String TAG = "william";
	
	private final INgnSipService mSipService;
	
	public ScreenMapNavigaton() {
		super(SCREEN_TYPE.DIALER_T, TAG);
		
		mSipService = getEngine().getSipService();
		
	}
	
	
	SetBtnFun setBtn = new SetBtnFun();
	MapScreen mapScreen = new MapScreen();
	
	EditText nameText;
	
	
	
	private ArrayAdapter<String> adapter;
	private ArrayAdapter<String> adapter2;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

      
        setContentView(R.layout.screen_direction);
		setBtn = new SetBtnFun();
		setBtn.SetBtn(this);
        
		MapScreen mapScreen = new MapScreen();
		mapScreen.MapScreen(this);
    }
    
    
}
