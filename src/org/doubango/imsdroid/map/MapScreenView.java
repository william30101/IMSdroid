package org.doubango.imsdroid.map;

import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.Screens.BaseScreen;
import org.doubango.imsdroid.map.Game;
import org.doubango.imsdroid.map.GameView;
import org.doubango.imsdroid.map.MapList;
import org.doubango.imsdroid.map.NetworkStatus;


import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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

public class MapScreenView{
	
	private static String TAG = "Shinhua";
	
	private UartReceive uartRec;

	GameView gameView;		
	Game game;
	//Encoder encoder;
	
	
	private NetworkStatus loggin;
	
	private String mName;	//For XMPP thread user name
	private String mPass;	//For XMPP thread user password
	
	private XMPPSetting XMPPSet;

	private ArrayAdapter<String> adapter;
	private ArrayAdapter<String> adapter2;
	
	
	Button navigationBtn, jsRunBtn;
	
	
	public void MapScreenView(Activity v) {
		Log.i("shinhua", "New Map Screen View");
		
	    //gameView = (GameView) v.findViewById(R.id.gameView1);
		

		gameView = (GameView) v.findViewById(R.id.gameView1);
		
		game = new Game();
		game.reloadMap(0,gameView);

		/* Navigation way display */
		navigationBtn = (Button) v.findViewById(R.id.navigation);
		navigationBtn.setOnClickListener(
        	new Button.OnClickListener(){
				public void onClick(View v) {
					game.runAlgorithm();
					navigationBtn.setEnabled(false);
				}
	        }
        );
		
		jsRunBtn = (Button) v.findViewById(R.id.runjs);
		jsRunBtn.setEnabled(false);

		
		initIoc();
	}
	

	

	public void signin(String name) {
		mName = name;
		mPass = "0000";

		loggin = NetworkStatus.getInstance();
		XMPPSet = new XMPPSetting();
		// XMPPThreadv.start();
	}
    


    public void initIoc(){
    	gameView.game = this.game;
    	game.gameView = this.gameView;
    	game.runButton = this.jsRunBtn;
    }
}
