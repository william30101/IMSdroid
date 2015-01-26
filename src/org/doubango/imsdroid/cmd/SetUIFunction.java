package org.doubango.imsdroid.cmd;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartCmd;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.BeaconUtils;
import org.doubango.imsdroid.Screens.ScreenDraw;
import org.doubango.imsdroid.Screens.ScreenUIJoyStick;
import org.doubango.imsdroid.Screens.ScreenUIVerticalSeekBar;
import org.doubango.imsdroid.Utils.NetworkStatus;
import org.doubango.imsdroid.map.Game;
import org.doubango.imsdroid.map.GameView;
import org.doubango.imsdroid.map.MapList;
import org.doubango.imsdroid.map.SendCmdToBoardAlgorithm;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.capricorn.ArcMenu;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;
import com.larswerkman.holocolorpicker.ValueBar;

public class SetUIFunction {

	static Activity globalActivity;
	Context mContext;

	private String TAG = "App";
	private XMPPSetting XMPPSet;
	private NetworkStatus loggin;
	public UartCmd uartCmd = UartCmd.getInstance();

	// public UartCmd uartCmd;

	//private UartReceive uartRec;

	//private BLEDeviceScanActivity BLEActivity;
	//private BLEDeviceControlActivity BLEDevCon;

	// For map use
	private Button jsRunBtn;

	GameView gameView;
	TextView Axis_show_X, Axis_show_Y;
	EditText Axis_TestAxisInput;
	Game game;

	// End for Map use
	MapList map = new MapList();

	SendCmdToBoardAlgorithm SendAlgo;

	/* ThreadPool declare for JoyStick operate */
	int height, width;

	private ExecutorService newService = Executors.newFixedThreadPool(1);
	private ExecutorService cleanService = Executors.newFixedThreadPool(1);
	private ScheduledExecutorService wifiService = Executors
			.newScheduledThreadPool(1);
	private ScheduledExecutorService bleService = Executors
			.newScheduledThreadPool(1);

	/* Parameter declare */
	private volatile boolean isContinue = false;
	private int joystickAction, menuAction, navigationAction;
	private String[] str = { "stop", "forward", "forRig", "right", "bacRig",
			"backward", "bacLeft", "left", "forLeft" };
	private int instructor; /* Robot Commands Direction Instructor */
	int colorValue;

	/* JoyStick object declare */
	RelativeLayout layout_joystick, layout_menu, layout_robot;
	ScreenUIJoyStick js;
	ScreenDraw myDraw;

	/* ARC Menu object declare */
	ArcMenu arcMenu;
	int arcLayoutsize;
	private static final int[] ITEM_DRAWABLES = { R.drawable.robot_bowhead,
			R.drawable.robot_normal, R.drawable.robot_headup };
	private static final String[] message = { "Head up", "Normal", "Bow head" };

	/* Robot vertical seekbar object declare */
	ScreenUIVerticalSeekBar seekbar = null;

	/* TextView vsProgress; */
	RelativeLayout seekbarlayout;
	LayoutParams seekbarparams, seekBarlayoutparams;

	/* DrogMenu */
	ViewGroup dragMenu;
	private View selected_item = null;
	private int offset_x = 0;
	private int offset_y = 0;
	ImageView img;
	int clickCount = 0;

	/* BlueTooth temporary declare */
	private Button BLEWrite;
	public static EditText BLEDataText;
	public String BLEData = null;

	/* Robot body - WiFI & BlueTooth */
	private ImageView bleConnect, wifistatus1, wifistatus2, wifistatus3,
			wifistatus4;
	private Button lightingSwitch;
	private boolean isConnectBlueTooth;

	private Handler handler = new Handler();
	//private Handler BLEhandler = new Handler();

	//Runnable rBLEScan = new bluetoothMonitorThread();

	/* Detect Robot Location */
	Runnable Axis_trigger_thread = new Axis_thread();

	/* Navigation parameter */
	public int Axis_InputY_fromDW1000;
	public int Axis_InputX_fromDW1000;
	public int Axis_BRSserchArray_Index_Y = 0;
	public int Axis_BRSserchArray_Index_X = 0;

	private int Axis_GetPollTime = 1000;

	/* Beacon reset declare */
	private final static String ResetInterface = "/sys/class/gpio/gpio175/value";
	private BeaconUtils beaconUtils;
	private Button BeaconReset, right90AngleBtn, left90AngleBtn;

	private static boolean supportBLEDevice = false;

	/* Temporary declare */

	private static SetUIFunction instance;

	public SetUIFunction(Activity activity) {
		globalActivity = activity;
		mContext = activity.getWindow().getDecorView().getContext();
	}

	public static SetUIFunction getInstance() {
		if (instance == null) {
			synchronized (SetUIFunction.class) {
				if (instance == null) {
					instance = new SetUIFunction(globalActivity);
				}
			}
		}
		return instance;
	}

	@SuppressLint("NewApi")
	public void StartUIFunction() {

		Axis_show_X = (TextView) globalActivity.findViewById(R.id.Axis_show_X);
		Axis_show_Y = (TextView) globalActivity.findViewById(R.id.Axis_show_Y);
		// Axis_TestAxisInput = (EditText) globalActivity
		// .findViewById(R.id.Axis_TestInputAxis);

		loggin = NetworkStatus.getInstance();

		gameView = (GameView) globalActivity.findViewById(R.id.gameView1);
		game = new Game();

		XMPPSet = new XMPPSetting();
		XMPPSet.setGameView(gameView);

		SendAlgo = new SendCmdToBoardAlgorithm();

		getScreenSize(globalActivity);

		/* Joy Stick */
		layout_joystick = (RelativeLayout) globalActivity.findViewById(R.id.layout_joystick);
		setJoyStickParameter(globalActivity);
		layout_joystick.setOnTouchListener(joystickListener);

		/* Button declare */
		jsRunBtn = (Button) globalActivity.findViewById(R.id.runjs);
		jsRunBtn.setOnClickListener(onClickListener);

		/* Arc Menu */
		/* Set layout size & position */
		setARClayoutSize(width);
		LayoutParams params = new RelativeLayout.LayoutParams(arcLayoutsize, arcLayoutsize);
		RelativeLayout layout = (RelativeLayout) globalActivity.findViewById(R.id.layout_robot);

		arcMenu = (ArcMenu) globalActivity.findViewById(R.id.arc_menu);
		arcMenu.setLayoutParams(params);
		initArcMenu(arcMenu, ITEM_DRAWABLES, globalActivity);

		/* Robot seekbar */
		seekbar = (ScreenUIVerticalSeekBar) globalActivity.findViewById(R.id.robotseekbar);
		seekbarlayout = (RelativeLayout) globalActivity.findViewById(R.id.layout_seekbar);
		setSeekbarParameter();
		

		supportBLEDevice = globalActivity.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH);



		/* Temporary - Wifi & bluetooth */
		wifistatus1 = (ImageView) globalActivity.findViewById(R.id.wifi_status1);
		wifistatus2 = (ImageView) globalActivity.findViewById(R.id.wifi_status2);
		wifistatus3 = (ImageView) globalActivity.findViewById(R.id.wifi_status3);
		wifistatus4 = (ImageView) globalActivity.findViewById(R.id.wifi_status4);
		
		// WiFi & BlueTooth Monitor
		wifiService.scheduleAtFixedRate(new wifiMonitorThread(), 5000, 5000,
				TimeUnit.MILLISECONDS);

		//BLEhandler.postDelayed(rBLEScan, 5000);

		/* Set listener for Beacon reset */
		// beaconUtils = new BeaconUtils();
		// BeaconReset = (Button) globalActivity.findViewById(R.id.BeaconReset);
		// BeaconReset.setOnClickListener(onClickListener);

		/*--------------------------------------------------*/
		/* Temporary */
		// BLEWrite = (Button) globalActivity.findViewById(R.id.BLEWriteBtn);
		// BLEDataText = (EditText)
		// globalActivity.findViewById(R.id.BLEDataText);

		// BLEWrite.setOnClickListener(onClickListener);
		Button getAxisBtn = (Button) globalActivity
				.findViewById(R.id.getAxisBtn);
		getAxisBtn.setOnClickListener(onClickListener);

		/* Temporary - Wifi */
		// bleConnect = (ImageView)
		// globalActivity.findViewById(R.id.imageView2);

		WifiManager wifi = (WifiManager) globalActivity
				.getSystemService(mContext.WIFI_SERVICE);

		//right90AngleBtn = (Button) globalActivity.findViewById(R.id.right90btn);
		//right90AngleBtn.setOnClickListener(onClickListener);

		//left90AngleBtn = (Button) globalActivity.findViewById(R.id.left90btn);
		//left90AngleBtn.setOnClickListener(onClickListener);

		//uartRec = new UartReceive();
		//uartRec.RunRecThread();
	}

	@SuppressLint("NewApi")
	private void getScreenSize(Activity v) {
		Display display = v.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;
	}

	private void setARClayoutSize(int width) {
		this.arcLayoutsize = width / 6;
	}

	private void setJoyStickParameter(Activity v) {
		js = new ScreenUIJoyStick(v.getApplicationContext(), layout_joystick,
				R.drawable.joystick);

		js.setStickSize(200, 200);
		js.setStickAlpha(150);
		// js.setLayoutSize(250, 250);
		js.setLayoutSize(300, 300);
		js.setLayoutAlpha(150);
		js.setoffset(70);
		js.setMinimumDistance(70); 
		js.drawStickDefault();
	}

	private void setSeekbarParameter() {
		seekBarlayoutparams = seekbarlayout.getLayoutParams();
		seekBarlayoutparams.height = (int) ((width / 6) * 1.5);
		seekBarlayoutparams.width = width / 6;

		seekbarparams = seekbar.getLayoutParams();
		seekbarparams.height = (int) ((width / 6) * 2);
		seekbar.setMax(1);
		seekbar.setProgress(0);
		seekbar.setOnSeekBarChangeListener(seekbarListener);

	}

	/* The OnTouchListener of Draw JoyStick */
	OnTouchListener joystickListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			joystickAction = event.getAction();

			/* Draw JoyStick */
			js.drawStick(event);

			switch (joystickAction) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				isContinue = true;
				instructor = js.get8Direction();
				if (instructor != 0) {
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

	/* Set Navigation & others Button onClickListener */
	private Button.OnClickListener onClickListener = new OnClickListener() {
		int indicator;

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			indicator = v.getId();
			switch (indicator) {
			case R.id.right90btn:
				sendCommand("RotateAngle P 90");
				break;
			case R.id.left90btn:
				sendCommand("RotateAngle N 90");
				break;

			case R.id.getAxisBtn:
				SendCmdToBoardAlgorithm.SetCompass();
				handler.postDelayed(Axis_trigger_thread, Axis_GetPollTime);
				// Log.d("jamesdebug", "touchBtn");

				// uartRec.RunRecThread();
				break;

			case R.id.runjs:
				synchronized (SendAlgo) {
					try {
						SendAlgo.RobotStart(gameView, game, XMPPSet);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// SendCmdToBoardAlgorithm.SetCompass();
				jsRunBtn.setEnabled(false);

				break;


			// case R.id.BeaconReset:
			// if (beaconUtils.getLibState() == true) {
			// Log.i(TAG, "Beason reset by beaconUtils");
			// beaconUtils.BeasonReset(ResetInterface);
			// }else {
			// Log.i(TAG, "Beason reset by uartCmd");
			// uartCmd.BeasonReset(ResetInterface);
			// }
			//
			// break;

			default:
				break;

			}
		}

	};

	
	/* Arc Menu */
	private void initArcMenu(final ArcMenu menu, int[] itemDrawables, Activity v) {
		final int itemCount = itemDrawables.length;
		for (int i = 0; i < itemCount; i++) {
			ImageView item = new ImageView(v);
			item.setImageResource(itemDrawables[i]);

			final int position = i;

			/* Add arcMenu child */
			menu.addItem(item, new OnClickListener() {

				@Override
				public void onClick(View v) {
					// Toast.makeText(getApplicationContext(), "position:" +
					// message[position], Toast.LENGTH_SHORT).show();
					setPanelPosition(position);
				}
			});
		}
	}

	/* Control Robot panel position */
	private void setPanelPosition(int position) {
		switch (position) {
		case 0:
			Log.i(TAG, "angleBottom");
			sendCommand("pitchAngle bottom");
			break;
			
		case 1:
			Log.i(TAG, "angleMiddle");
			sendCommand("pitchAngle middle");
			break;
		
		case 2:
			Log.i(TAG, "angleTop");
			sendCommand("pitchAngle top");
			break;
		}
	}
	
	

	/* Robot Seekbar Listener */
	SeekBar.OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

			if (progress < 1) {
				sendCommand("stretch bottom");
			} else if (progress == 1) {
				sendCommand("stretch top");
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}

	};


	/* XMPP Sendfunction */
	public void SendToBoard(String inStr) throws IOException {
		Log.i(TAG, " loggin status = " + loggin.GetLogStatus());

		if (loggin.GetLogStatus())
			XMPPSet.XMPPSendText("james1", inStr);
		else {
			String[] inM = inStr.split("\\s+");
			byte[] cmdByte = uartCmd.GetAllByte(inM);
			// String decoded = new String(cmdByte, "ISO-8859-1");
			UartCmd.SendMsgUart(1, cmdByte);
		}
	}
	
	private void sendCommand(String message){
		
		try {
			SendToBoard(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	

	private int Axis_BRSlessY(int Axis_InputVarY) throws IOException {

		int Axis_BRSArraymiddle = 0;
		int Axis_BRStimes_Y = 0;
		int Axis_BRSretIndexY = 0;
		int Axis_BRShigh = MapList.Axis_GraduateY.length - 1;
		int Axis_BRSArraylow = 0;

		while (Axis_BRSArraylow <= Axis_BRShigh) {

			Axis_BRStimes_Y++;
			Axis_BRSArraymiddle = ((Axis_BRSArraylow + Axis_BRShigh) / 2);

			if (Axis_InputVarY <= MapList.Axis_GraduateY[Axis_BRSArraymiddle]) {

				Axis_BRShigh = Axis_BRSArraymiddle - 1;

			} else {
				Axis_BRSArraylow = Axis_BRSArraymiddle + 1;
			}
		}

		// Log.d("jamesdebug", "The times is:" + Axis_BRStimes_Y);

		if (Axis_BRSArraylow > 0) {

			// Log.d("jamesdebug", "TheY "
			// + MapList.Axis_GraduateY[Axis_BRSArraylow - 1]
			// + " is less " + Axis_InputVarY + " The array index is : "
			// + (Axis_BRSArraylow - 1));

			Axis_BRSretIndexY = Axis_BRSArraylow;

		} else {
			// Log.d("jamesdebug", "Can't find the element less " +
			// Axis_InputVarY);
		}
		return Axis_BRSretIndexY;
	}

	private int Axis_BRSlessX(int Axis_InputVarX) throws IOException {

		int Axis_BRSmiddle_X = 0;
		int Axis_BRStimes_X = 0;
		int Axis_BRSlow_X = 0;
		int Axis_BRShigh_X = 0;
		int Axis_BRSretIndexX = 0;

		Axis_BRShigh_X = MapList.AxisX_Array[0][Axis_BRSserchArray_Index_Y].length - 1;

		while (Axis_BRSlow_X <= Axis_BRShigh_X) {

			Axis_BRSmiddle_X = ((Axis_BRSlow_X + Axis_BRShigh_X) / 2);

			if (Axis_InputVarX <= MapList.AxisX_Array[0][Axis_BRSserchArray_Index_Y][Axis_BRSmiddle_X]) {

				Axis_BRShigh_X = Axis_BRSmiddle_X - 1;

			} else {
				Axis_BRSlow_X = Axis_BRSmiddle_X + 1;
			}
		}

		// Log.d("jamesdebug", "The times is:" + Axis_BRStimes_X);

		if (Axis_BRSlow_X > 0) {

			// Log.d("jamesdebug","TheX "
			// +
			// MapList.AxisX_Array[0][Axis_BRSserchArray_Index_Y][Axis_BRSlow_X
			// - 1]
			// + " is less " + Axis_InputVarX
			// + " The array index is : " + (Axis_BRSlow_X - 1));

			Axis_BRSretIndexX = Axis_BRSlow_X - 1;

			if (Axis_BRSretIndexX > 14) {
				Axis_BRSretIndexX = 0;
			}

		} else {
			// Log.d("jamesdebug", "Can't find the element less " +
			// Axis_InputVarX);
		}
		return Axis_BRSretIndexX;
	}

	/* Use thread pool for XMPP communication */
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

					String sub = SendMsg.substring(SendMsg.indexOf("/") + 1);
					if (SendMsg.equals("stop"))
						SendToBoard("stop stop");
					else
						SendToBoard("direction " + sub);
					Thread.sleep(100l);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class Axis_thread implements Runnable {
		@SuppressLint("UseValueOf")
		public void run() {

			handler.postDelayed(Axis_trigger_thread, Axis_GetPollTime);

			// Log.d("jamesdebug",
			// "===================Info======================");

			gameView.postInvalidate();

			try {

				Thread.sleep(20);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/* Create ThreadPool to fix thread quantity */
	private void useThreadPool(ExecutorService service, String Msg) {
		service.execute(new MyThread(Msg));
	}

	/* Monitor wifi signal */
	private class wifiMonitorThread implements Runnable {
		int rssi, level;
		String tempString;
		Message message;

		@Override
		public void run() {
			// TODO Auto-generated method stub
			WifiManager wifi = (WifiManager) globalActivity
					.getSystemService(mContext.WIFI_SERVICE);
			rssi = wifi.getConnectionInfo().getRssi();
			level = wifi.calculateSignalLevel(rssi, 4);
			tempString = Integer.toString(rssi);

			message = wifiUIHandler.obtainMessage(1, tempString);
			wifiUIHandler.sendMessage(message);
		}

	}

	/* Update UI Handler */
	private Handler wifiUIHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			/* Change wifi UI display */
			wifiIconStatus(Integer.valueOf((String) msg.obj));
		}
	};

	/* WiFi signal display icon */
	private void wifiIconStatus(int level) {

		if (level < 0 && level >= -50) {
			wifistatus1.setVisibility(View.VISIBLE);
			wifistatus2.setVisibility(View.VISIBLE);
			wifistatus3.setVisibility(View.VISIBLE);
			wifistatus4.setVisibility(View.VISIBLE);
		} else if (level < -50 && level >= -100) {
			wifistatus1.setVisibility(View.VISIBLE);
			wifistatus2.setVisibility(View.VISIBLE);
			wifistatus3.setVisibility(View.VISIBLE);
			wifistatus4.setVisibility(View.INVISIBLE);
		} else if (level < -100 && level >= -150) {
			wifistatus1.setVisibility(View.VISIBLE);
			wifistatus2.setVisibility(View.VISIBLE);
			wifistatus3.setVisibility(View.INVISIBLE);
			wifistatus4.setVisibility(View.INVISIBLE);
		} else if (level < -150 && level >= -200) {
			wifistatus1.setVisibility(View.VISIBLE);
			wifistatus2.setVisibility(View.INVISIBLE);
			wifistatus3.setVisibility(View.INVISIBLE);
			wifistatus4.setVisibility(View.INVISIBLE);
		}
	}


}
