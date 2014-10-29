package org.doubango.imsdroid.Screens;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.ngn.services.INgnSipService;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

public class ScreenDirectionJS extends BaseScreen {
	private final INgnSipService mSipService;
	private static String TAG = ScreenTabDialer.class.getCanonicalName();

	private XMPPSetting XMPPSet;
	/* Parameter declare */
	private volatile boolean isContinue = false;
	private int joystickAction, menuAction;
	private String[] str = { "stop", "forward", "forRig", "right", "bacRig",
			"backward", "bacLeft", "left", "forLeft" };
	private int instructor; /* Direction Instructor */

	/* JoyStick & Menu class Declare */
	RelativeLayout layout_joystick, layout_menu, layout_robot;
	ScreenJoyStick js, test;

	/* ThreadPool declare for JoyStick operate */
	private ExecutorService newService = Executors.newFixedThreadPool(10);
	
	
	/* Test */
	int height, width;
	private ViewGroup viewgroup1;
	private Context context1;
	private Bitmap normal;

	
	/* Constructor */
	public ScreenDirectionJS() {
		super(SCREEN_TYPE.DIALER_T, TAG);
		mSipService = getEngine().getSipService();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_directionjs);
		getScreenSize();

		layout_joystick = (RelativeLayout) findViewById(R.id.layout_joystick);
		setJoyStickParameter();
		layout_joystick.setOnTouchListener(joystickListener);
		

		layout_menu = (RelativeLayout) findViewById(R.id.screen_menu);
		layout_menu.setOnTouchListener(menukeyListener);
		//setMenukeyParameter();
		
		layout_robot = (RelativeLayout) findViewById(R.id.layout_robot);
		setFrameParameter();
		
		XMPPSet = new XMPPSetting();
		
	}
	
	private void getScreenSize(){
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;		
	}
	
	

	/* Set JoyStick & JoyStick Direction layout parameter */
	private void setJoyStickParameter() {
		js = new ScreenJoyStick(getApplicationContext(), layout_joystick,
				R.drawable.joystick);

		js.setStickSize(200, 200);
		js.setStickAlpha(150);
		//js.setLayoutSize(250, 250);
		js.setLayoutSize(300, 300);
		js.setLayoutAlpha(150);
		js.setoffset(70);
		js.setMinimumDistance(30); /* JoyStick Sensitivity */
		js.drawStickDefault(); /* Draw JoyStick function */
	}
	
	private void setFrameParameter(){
		test = new ScreenJoyStick(getApplicationContext(), layout_robot,
				R.drawable.xyzlong);
		test.setLayoutSize(width/5, height/2);
		test.setLayoutAlpha(255);
		test.setStickSize(width/5, height/2);
		test.drawStickDefault(); 
		
		/* StupidStupidStupidStupidStupidStupid */
		test = new ScreenJoyStick(getApplicationContext(), layout_robot,
				R.drawable.xyzshort);
		test.setLayoutSize(width/5, height/2);
		test.setLayoutAlpha(255);
		test.setStickSize(width/5, height/2);
		test.drawStickDefault(); 
		
		test = new ScreenJoyStick(getApplicationContext(), layout_robot,
				R.drawable.xyzhigh);
		test.setLayoutSize(width/5, height/2);
		test.setLayoutAlpha(255);
		test.setStickSize(width/5, height/2);
		test.drawStickDefault(); 
		
		test = new ScreenJoyStick(getApplicationContext(), layout_robot,
				R.drawable.xyznormal);
		test.setLayoutSize(width/5, height/2);
		test.setLayoutAlpha(255);
		test.setStickSize(width/5, height/2);
		test.drawStickDefault(); 
		
		test = new ScreenJoyStick(getApplicationContext(), layout_robot,
				R.drawable.xyzlow);
		test.setLayoutSize(width/5, height/2);
		test.setLayoutAlpha(255);
		test.setStickSize(width/5, height/2);
		test.drawStickDefault(); 
		
		
		
		//normal = BitmapFactory.decodeResource(context1.getResources(), R.drawable.xyznormal);
		//normal = Bitmap.createScaledBitmap(normal, width/5, height/2, false);
		
		

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
				useThreadPool(newService, str[instructor]);
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

	OnTouchListener menukeyListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			menuAction = event.getAction();

			switch (menuAction) {
			case MotionEvent.ACTION_DOWN:
				System.out.printf("Menu key pressed\r\n");
				break;
			case MotionEvent.ACTION_UP:
				break;
			case MotionEvent.ACTION_MOVE:
				break;
			default:
				break;
			}

			return true;
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
