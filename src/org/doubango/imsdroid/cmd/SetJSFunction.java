package org.doubango.imsdroid.cmd;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartCmd;
import org.doubango.imsdroid.UartReceive;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.Screens.ScreenDraw;
import org.doubango.imsdroid.Screens.ScreenJoyStick;
import org.doubango.imsdroid.Utils.NetworkStatus;
import org.doubango.imsdroid.map.Game;
import org.doubango.imsdroid.map.GameView;
import org.doubango.imsdroid.map.SendCmdToBoardAlgorithm;

import android.app.Activity;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.RelativeLayout;

public class SetJSFunction {

	private String TAG = "App";
	private boolean isNeedAdd = false;
	private XMPPSetting XMPPSet;
	private NetworkStatus loggin;
	private UartCmd uartCmd;
	private UartReceive uartRec;
	
	//For map use
	private Button jsRunBtn;
	GameView gameView;
	Game game;
	//End for Map use
	
	private ExecutorService service = Executors.newFixedThreadPool(10);
	SendCmdToBoardAlgorithm SendAlgo;
	
	/* ThreadPool declare for JoyStick operate */
	int height, width;
	
	private ExecutorService newService = Executors.newFixedThreadPool(10);
	
	/* Parameter declare */
	private volatile boolean isContinue = false;
	private int joystickAction, menuAction, navigationAction;
	private String[] str = { "stop", "forward", "forRig", "right", "bacRig",
			"backward", "bacLeft", "left", "forLeft" };
	private int instructor; /* Direction Instructor */
	/* JoyStick & Menu class Declare */
	RelativeLayout layout_joystick, layout_menu, layout_robot;
	ScreenJoyStick js, test;
	ScreenDraw myDraw;
	
	


	public void SetJSFunction(Activity v) {
		uartCmd = new UartCmd();
		loggin = NetworkStatus.getInstance();

		XMPPSet = new XMPPSetting();

		uartRec = new UartReceive();
		uartRec.RunRecThread();

		gameView = (GameView) v.findViewById(R.id.gameView1);

		game = new Game();

		SendAlgo = new SendCmdToBoardAlgorithm();

		getScreenSize(v);

		layout_joystick = (RelativeLayout) v.findViewById(R.id.layout_joystick);
		setJoyStickParameter(v);
		layout_joystick.setOnTouchListener(joystickListener);
		
		
		jsRunBtn = (Button) v.findViewById(R.id.runjs);
		jsRunBtn.setOnClickListener(onClickListener);



	}
	
	private void getScreenSize(Activity v) {
		// TODO Auto-generated method stub
		Display display = v.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;	
	}
	
	private void setJoyStickParameter(Activity v) {
		// TODO Auto-generated method stub
		js = new ScreenJoyStick(v.getApplicationContext(), layout_joystick,
				R.drawable.joystick);

		js.setStickSize(200, 200);
		js.setStickAlpha(150);
		//js.setLayoutSize(250, 250);
		js.setLayoutSize(300, 300);
		js.setLayoutAlpha(150);
		js.setoffset(70);
		js.setMinimumDistance(70); /* JoyStick Sensitivity */
		js.drawStickDefault(); /* Draw JoyStick function */
	}

	
	/* The OnTouchListener of Draw JoyStick */
	OnTouchListener joystickListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			joystickAction = event.getAction();
			
			/* Draw JoyStick */
			js.drawStick(event);

			switch(joystickAction){
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				isContinue = true;
				instructor = js.get8Direction();
				
				if(instructor != 0){
					useThreadPool(newService, str[instructor]);
				}
				
				break;
			case MotionEvent.ACTION_UP:
				isContinue = true;
				useThreadPool(newService, str[0]);
				isContinue = false;
				break;
			default:
				isContinue = false;
				break;
		}
			
			return true;
		}

	};
	
	/* Set Button onClickListener */
	private	Button.OnClickListener onClickListener = new OnClickListener(){
		int indicator;
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			indicator = v.getId();

			switch (indicator) {
			case R.id.runjs:
				synchronized (SendAlgo) {
					try {
						SendAlgo.RobotStart(gameView,game,XMPPSet);

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				
				jsRunBtn.setEnabled(false);
				
				break;
			default:
				break;

			}
		}
		
	};


	public class MyThread implements Runnable {
		String SendMsg;

		public MyThread(String SendMsg) {
			// store parameter for later user
			this.SendMsg = SendMsg;
		}

		public void run() {
			while (isContinue) {
				try {
					// Using SCTP transmit message
					Log.i(TAG, "Send message" + SendMsg);
					if (SendMsg.equals("stop"))
						XMPPSet.XMPPSendText("james1", "stop stop no no"); // Stop button be pressed.
					else
						XMPPSet.XMPPSendText("james1", "direction " + SendMsg);
					Thread.sleep(100l);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/* Create ThreadPool to fix thread quantity */
	private void useThreadPool(ExecutorService service, String Msg) {
		service.execute(new MyThread(Msg));
	}

}
