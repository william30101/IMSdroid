package org.doubango.imsdroid.cmd;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.zip.Inflater;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartCmd;
import org.doubango.imsdroid.UartReceive;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.BeaconUtils;
import org.doubango.imsdroid.BLE.BLEDeviceControlActivity;
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

	Activity globalActivity;
	Context mContext;

	private String TAG = "App";
	private XMPPSetting XMPPSet;
	private NetworkStatus loggin;
	private UartCmd uartCmd;
	private UartReceive uartRec;

	private BLEDeviceControlActivity BLEDevCon;

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
	private ScheduledExecutorService wifiService =  Executors.newScheduledThreadPool(1);

	/* Parameter declare */
	private volatile boolean isContinue = false;
	private int joystickAction, menuAction, navigationAction;
	private String[] str = { "stop", "forward", "forRig", "right", "bacRig",
			"backward", "bacLeft", "left", "forLeft" };
	private int instructor; /* Robot Commands Direction Instructor */

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
	private ImageView bleConnect, wifistatus1, wifistatus2, wifistatus3, wifistatus4;
	private Button lightingSwitch;
	
	/* Temporary declare */
	public static TextView mConnectState;

	private Handler handler = new Handler();

	/* Detect Robot Location */
	Runnable Axis_trigger_thread = new Axis_thread();

	/* Navigation parameter */ 
	public int Axis_InputY_fromDW1000;
	public int Axis_InputX_fromDW1000;
	public int Axis_BRSArraylow = 0;
	public int Axis_BRSArraymiddle = 0;
	public int Axis_BRStimes_Y = 0;
	public int Axis_BRStimes_X = 0;
	public int Axis_BRSretIndexY = 0;
	public int Axis_BRSretIndexX = 0;
	public int Axis_BRSserchArray_Index_Y = 0;
	public int Axis_BRSserchArray_Index_X = 0;
	public int Axis_BRShigh = MapList.Axis_GraduateY.length;

	private int Axis_GetPollTime = 3000;
	private int Axis_BRShigh_X = 0;
	private int Axis_BRSlow_X = 0;
	private int Axis_BRSmiddle_X = 0;

	/* Beacon reset declare */
	private final static String ResetInterface = "/sys/class/gpio/gpio175/value";
	private BeaconUtils beaconUtils;
	private Button BeaconReset;

	public SetUIFunction(Activity activity) {
		globalActivity = activity;
		mContext = activity.getWindow().getDecorView().getContext();
	}

	public void StartUIFunction() {

		Axis_show_X = (TextView) globalActivity.findViewById(R.id.Axis_show_X);
		Axis_show_Y = (TextView) globalActivity.findViewById(R.id.Axis_show_Y);
		Axis_TestAxisInput = (EditText) globalActivity
				.findViewById(R.id.Axis_TestInputAxis);

		uartCmd = UartCmd.getInstance();
		loggin = NetworkStatus.getInstance();

		XMPPSet = new XMPPSetting();
		uartRec = new UartReceive();
		uartRec.RunRecThread();

		gameView = (GameView) globalActivity.findViewById(R.id.gameView1);
		game = new Game();

		SendAlgo = new SendCmdToBoardAlgorithm();

		getScreenSize(globalActivity);

		/* Joy Stick */
		layout_joystick = (RelativeLayout) globalActivity
				.findViewById(R.id.layout_joystick);
		setJoyStickParameter(globalActivity);
		layout_joystick.setOnTouchListener(joystickListener);

		/* Button declare */
		jsRunBtn = (Button) globalActivity.findViewById(R.id.runjs);
		jsRunBtn.setOnClickListener(onClickListener);

		/* Arc Menu */
		/* Set layout size & position */
		setARClayoutSize(width);
		LayoutParams params = new RelativeLayout.LayoutParams(arcLayoutsize,
				arcLayoutsize);
		Log.i("shinhua", "params width " + params.width + "params height"
				+ params.height);
		RelativeLayout layout = (RelativeLayout) globalActivity
				.findViewById(R.id.layout_robot);

		arcMenu = (ArcMenu) globalActivity.findViewById(R.id.arc_menu);
		arcMenu.setLayoutParams(params);
		initArcMenu(arcMenu, ITEM_DRAWABLES, globalActivity);

		/* Robot seekbar */
		seekbar = (ScreenUIVerticalSeekBar) globalActivity
				.findViewById(R.id.robotseekbar);
		seekbarlayout = (RelativeLayout) globalActivity
				.findViewById(R.id.layout_seekbar);
		setSeekbarParameter();

		/* DragDrop menu */
		dragMenu = (ViewGroup) globalActivity.findViewById(R.id.mainlayout);
		img = (ImageView) globalActivity.findViewById(R.id.screenmenu);

		dragMenu.setOnDragListener(dragListener);
		img.setOnTouchListener(imgListener);
		selected_item = (View) globalActivity.findViewById(R.id.screenmenu);
		
		
		/* Temporary - Wifi & bluetooth */
		wifistatus1 = (ImageView) globalActivity.findViewById(R.id.wifi_status1);
		wifistatus2 = (ImageView) globalActivity.findViewById(R.id.wifi_status2);
		wifistatus3 = (ImageView) globalActivity.findViewById(R.id.wifi_status3);
		wifistatus4 = (ImageView) globalActivity.findViewById(R.id.wifi_status4);
		bleConnect = (ImageView) globalActivity.findViewById(R.id.bluetooth_status);

		
		//lightingSwitch.setOnClickListener(onClickListener);
		
		// WiFi Monitor
		wifiService.scheduleAtFixedRate(new wifiMonitorThread(), 5000, 5000, TimeUnit.MILLISECONDS);

		/* Set listener for Beacon reset */
		beaconUtils = new BeaconUtils();
		BeaconReset = (Button) globalActivity.findViewById(R.id.BeaconReset);
		BeaconReset.setOnClickListener(onClickListener);

		/*--------------------------------------------------*/
		/* Temporary */
		BLEWrite = (Button) globalActivity.findViewById(R.id.BLEWriteBtn);
		BLEDataText = (EditText) globalActivity.findViewById(R.id.BLEDataText);

		BLEWrite.setOnClickListener(onClickListener);
		Button getAxisBtn = (Button) globalActivity
				.findViewById(R.id.getAxisBtn);
		getAxisBtn.setOnClickListener(onClickListener);
		
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
		js.setMinimumDistance(70); /* JoyStick Sensitivity */
		js.drawStickDefault(); /* Draw JoyStick function */
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
				Log.i("shinhua1", str[instructor]);
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
			case R.id.bleSwitch:
				Log.i("shinhua", "Blue tooth onclick");
				try {
					SendToBoard("BLE " + "00");
					BLEDevCon.CharacteristicWRN(2,1, 0, "00");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i(TAG, "BLE send data=" + " 00 " + " error");
					e.printStackTrace();
				}
				break;
			
			case R.id.getAxisBtn:
				SendCmdToBoardAlgorithm.SetCompass();
				handler.postDelayed(Axis_trigger_thread, Axis_GetPollTime);
				Log.d("jamesdebug", "touchBtn");

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

				jsRunBtn.setEnabled(false);

				break;

			case R.id.BLEWriteBtn:
				
				 if (BLEDevCon == null) // Add BLE control 
					 BLEDevCon =BLEDeviceControlActivity.getInstance(); // Parent , Childselected item mode 0 = write
				 BLEDevCon.CharacteristicWRN(2,1, 0, BLEDataText.getText().toString());
				 
				break;

			case R.id.BeaconReset:
				if (beaconUtils.getLibState() == true) {
					Log.i(TAG, "Beason reset by beaconUtils");
					beaconUtils.BeasonReset(ResetInterface);
				}else {
					Log.i(TAG, "Beason reset by uartCmd");
					uartCmd.BeasonReset(ResetInterface);
				}

				break;

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
			try {
				SendToBoard("pitchAngle bottom");
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 1:
			Log.i(TAG, "angleMiddle");
			try {
				SendToBoard("pitchAngle middle");
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 2:
			Log.i(TAG, "angleTop");
			try {
				SendToBoard("pitchAngle top");
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	/* Robot Seekbar Listener */
	SeekBar.OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener() {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

			switch (progress) {
			case 0:
				// vsProgress.setText(progress+"");
				try {
					SendToBoard("stretch bottom");
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 1:
				// vsProgress.setText(progress+"");
				try {
					SendToBoard("stretch top");
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;

			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {

		}

	};

	/* DragDrap Menu Listener */
	View.OnTouchListener imgListener = new OnTouchListener() {

		@SuppressLint("NewApi")
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Log.i("shinhua1","ACTION_DOWN");
				cleanThread(cleanService);
				clickCount++;
				if(clickCount == 2){
					executeColorPicker(v);
					clickCount = 0;
				}
				return true;
			}
			if(event.getAction() == MotionEvent.ACTION_MOVE){
				Log.i("shinhua1","ACTION_MOVE");
				offset_x = (int) event.getX();
				offset_y = (int) event.getY();
				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
				v.startDrag(null, shadowBuilder, v, 0);
				v.setVisibility(View.INVISIBLE);
				return true;
			}
			else {
				return false;
			}

		}

	};
	

	View.OnDragListener dragListener = new OnDragListener() {

		@Override
		public boolean onDrag(View v, DragEvent event) {
			// TODO Auto-generated method stub
			int action = event.getAction();
			switch (action) {
			case DragEvent.ACTION_DROP:

				int x = (int) event.getX() - offset_x;
				int y = (int) event.getY() - offset_y;

				int w = width - 100;
				int h = height - 100;
				if (x < 250)
					x = 250;
				if (y < 80)
					y = 80;

				if (x > w)
					x = w;
				if (y > h)
					y = h;

				/*
				 * LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				 * new ViewGroup.MarginLayoutParams(
				 * LinearLayout.LayoutParams.WRAP_CONTENT,
				 * LinearLayout.LayoutParams.WRAP_CONTENT));
				 */
				/*
				 * FrameLayout.LayoutParams fp = new FrameLayout.LayoutParams(
				 * new ViewGroup.MarginLayoutParams(
				 * FrameLayout.LayoutParams.WRAP_CONTENT,
				 * FrameLayout.LayoutParams.WRAP_CONTENT));
				 */
				
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						new ViewGroup.MarginLayoutParams(
								RelativeLayout.LayoutParams.WRAP_CONTENT,
								RelativeLayout.LayoutParams.WRAP_CONTENT));

				lp.setMargins(x, y, 0, 0);
				// lp.setMargins(left, top, right, bottom)

				selected_item.setLayoutParams(lp);
				v = (View)event.getLocalState();
				v.setVisibility(View.VISIBLE);
				break;
			case DragEvent.ACTION_DRAG_STARTED:
				Log.d("xyz", "Drag event started");
				break;
			case DragEvent.ACTION_DRAG_ENTERED:
				Log.d("xyz", "Drag event entered into ");
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				Log.d("xyz", "Drag event exited from ");

			case DragEvent.ACTION_DRAG_ENDED:
				RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
						new ViewGroup.MarginLayoutParams(
								RelativeLayout.LayoutParams.WRAP_CONTENT,
								RelativeLayout.LayoutParams.WRAP_CONTENT));

				lp1.setMargins(250, 80, 0, 0);
				
				v = (View)event.getLocalState();
				v.setVisibility(View.VISIBLE);
				break;

			}
			return true;
		}

	};

	/* Color Picker */
	private void executeColorPicker(View v) {

		// Context mContext = v.getContext();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Create color picker view

		View view = inflater.inflate(R.layout.color_picker_dialog, null);
		
		if (v == null) return;

		final ColorPicker picker = (ColorPicker) view.findViewById(R.id.picker);
		//SVBar svBar = (SVBar) view.findViewById(R.id.svbar);
		//OpacityBar opacityBar = (OpacityBar) view.findViewById(R.id.opacitybar);
		
		lightingSwitch = (Button) view.findViewById(R.id.bleSwitch);
		mConnectState = (TextView) view.findViewById(R.id.connectStatus);
		
		
		if (BLEDevCon == null) BLEDevCon =BLEDeviceControlActivity.getInstance();
		
		String bleConnectedStatusString = BLEDevCon.ismConnected();
		mConnectState.setText(bleConnectedStatusString);
		bluetoothIconStatus(bleConnectedStatusString);
		
		
		// shinhua add
		final TextView colorLevel = (TextView) view.findViewById(R.id.color_level);
		
		
		// picker.addSVBar(svBar);
		// picker.addOpacityBar(opacityBar);
		picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
			@Override
			public void onColorChanged(int intColor) {
				// String hexColor =
				// Integer.toHexString(intColor).toUpperCase();
				// hexCode.setText("#" + hexColor);
				int colorValue = Math.abs((int) (intColor / 18));
				String color = Integer.toString(colorValue).toUpperCase();
				colorLevel.setText("Current LED LEVEL" + color);


				// setText (bleConnectedStatusString)
				/*
				 *  Send BLE Command to Control Board.
				 *  data format is BLE + data
				 *  ex: "BLE e0"
				 * 
				 * */
				try {
					SendToBoard("BLE " + switchled(colorValue));
					BLEDevCon.CharacteristicWRN(2,1, 0, switchled(colorValue));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.i(TAG, "BLE send data=" + switchled(colorValue) + " error");
					e.printStackTrace();
				}
			}
		});

		
		lightingSwitch.setOnClickListener(onClickListener);
		
		
		// Config dialog
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setView(view);
		builder.setTitle("Choose Smart Lighting color");
		builder.setCancelable(true);
		builder.setNegativeButton("Cancel", null);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// Update color
				if( which == -1){
				String tag1;
				tag1 = Integer.toString(which);
				
				
//				try {
//					SendToBoard("BLE " + switchled(colorValue));
//					BLEDevCon.CharacteristicWRN(2,1, 0, switchled(colorValue));
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					Log.i(TAG, "BLE send data=" + switchled(colorValue) + " error");
//					e.printStackTrace();
//				}
//				
				
				}
			}
		});
		builder.create().show();
	}
	
	/* Bluetooth signal display icon */
	private void bluetoothIconStatus(String itor){
		if(itor == "BLE connected"){
			bleConnect.setVisibility(View.VISIBLE);
		}
		else if(itor == "BLE disconnected"){
			bleConnect.setVisibility(View.INVISIBLE);
		}
	}
	
	/* Send BlueTooth Command */
	private String switchled(int itor) {

		switch (itor) {
		case 0:
			BLEData = "0E";
			break;
		case 1:
			BLEData = "1D";
			break;
		case 2:
			BLEData = "2C";
			break;
		case 3:
			BLEData = "3B";
			break;
		case 4:
			BLEData = "4A";
			break;
		case 5:
			BLEData = "59";
			break;
		case 6:
			BLEData = "68";
			break;
		case 7:
			BLEData = "77";
			break;
		case 8:
			BLEData = "86";
			break;
		case 9:
			BLEData = "96";
			break;
		case 10:
			BLEData = "A4";
			break;
		case 11:
			BLEData = "B3";
			break;
		case 12:
			BLEData = "C2";
			break;
		case 13:
			BLEData = "D1";
			break;
		case 14:
			BLEData = "E0";
			break;
		}
		return BLEData;
	}

	/* XMPP Sendfunction */
	private void SendToBoard(String inStr) throws IOException {
		// Log.i(TAG," loggin status = " + loggin.GetLogStatus());

		if (loggin.GetLogStatus())
			XMPPSet.XMPPSendText("james1", inStr);
		else {
			String[] inM = inStr.split("\\s+");
			byte[] cmdByte = uartCmd.GetAllByte(inM);
			// String decoded = new String(cmdByte, "ISO-8859-1");
			UartCmd.SendMsgUart(1, cmdByte);
		}
	}

	private int Axis_BRSlessY(int Axis_InputVarY) throws IOException {

		while (Axis_BRSArraylow <= Axis_BRShigh) {

			Axis_BRStimes_Y ++;
			Axis_BRSArraymiddle = ((Axis_BRSArraylow + Axis_BRShigh) / 2);

			if (Axis_InputVarY <= MapList.Axis_GraduateY[Axis_BRSArraymiddle]) {

				Axis_BRShigh = Axis_BRSArraymiddle - 1;

			} else {
				Axis_BRSArraylow = Axis_BRSArraymiddle + 1;
			}
		}

		Log.d("jamesdebug", "The times is:" + Axis_BRStimes_Y);

		// if(Axis_low > 0)
		// {
		// Log.d("jamesdebug","The " + Axis_data[Axis_low - 1] + " is less " +
		// Axis_InputVar +
		// " The array index is : " + (Axis_low - 1));
		// }else{
		// Log.d("jamesdebug", "Can't find the element less " + Axis_InputVar);
		// }

		// if(Axis_low > 0)
		// {
		// Log.d("jamesdebug","The " + MapList.test[0][Axis_low - 1] +
		// " is less " + Axis_InputVar +
		// " The array index is : " + (Axis_low - 1));
		// }else{
		// Log.d("jamesdebug", "Can't find the element less " + Axis_InputVar);
		// }

		if (Axis_BRSArraylow > 0) {

			Log.d("jamesdebug", "The "
			        + MapList.Axis_GraduateY[Axis_BRSArraylow - 1]
					+ " is less " + Axis_InputVarY + " The array index is : "
					+ (Axis_BRSArraylow - 1));

			Axis_BRSretIndexY = Axis_BRSArraylow;

		} else {
			Log.d("jamesdebug", "Can't find the element less " + Axis_InputVarY);
		}
		return Axis_BRSretIndexY;
	}

	private int Axis_BRSlessX(int Axis_InputVarX) throws IOException {

		Axis_BRShigh_X = MapList.AxisX_Array[0][Axis_BRSserchArray_Index_Y].length - 1;

		while (Axis_BRSlow_X <= Axis_BRShigh_X) {

			Axis_BRStimes_X ++;
			Axis_BRSmiddle_X = ((Axis_BRSlow_X + Axis_BRShigh_X) / 2);

			if (Axis_InputVarX <= MapList.AxisX_Array[0][Axis_BRSserchArray_Index_Y][Axis_BRSmiddle_X]) {

				Axis_BRShigh_X = Axis_BRSmiddle_X - 1;

			} else {
				Axis_BRSlow_X = Axis_BRSmiddle_X + 1;
			}
		}

		if (Axis_BRSlow_X > 0) {

			Log.d("jamesdebug","The "
							+ MapList.AxisX_Array[0][Axis_BRSserchArray_Index_Y][Axis_BRSlow_X - 1]
							+ " is less " + Axis_InputVarX
							+ " The array index is : " + (Axis_BRSlow_X - 1));

			Axis_BRSretIndexX = Axis_BRSlow_X - 1;

		} else {
			Log.d("jamesdebug", "Can't find the element less " + Axis_InputVarX);
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
		public void run() {

			Axis_show_X.setText(" State X : "
					+ Float.toString(UartReceive.robotLocation[2] * 100));
			Axis_show_Y.setText(" State Y : "
					+ Float.toString(UartReceive.robotLocation[3] * 100));

			// Log.d("jamesdebug","Axis_showX: " + Float.toString(UartReceive.robotLocation[2] * 100));
			// Log.d("jamesdebug","Axis_showY: " + Float.toString(UartReceive.robotLocation[3] * 100));

			// Axis_InputVarY = Integer.parseInt(Axis_TestAxisInput.getText().toString());
			
			Axis_InputY_fromDW1000 = Integer.parseInt(Axis_show_Y.getText().toString());

			Log.d("jamesdebug", "The stream is: " + Axis_InputY_fromDW1000);

			try {

				Axis_BRSserchArray_Index_Y = Axis_BRSlessY(Axis_InputY_fromDW1000);
				Axis_BRSserchArray_Index_X = Axis_BRSlessX(Axis_InputX_fromDW1000);

				Log.d("jamesdebug", "***************Index[X][Y] is : [ "
						+ Axis_BRSserchArray_Index_X + " ][ " + Axis_BRSserchArray_Index_Y
						+ " ]");

			} catch (IOException e) {
				e.printStackTrace();
			}

			handler.postDelayed(Axis_trigger_thread, Axis_GetPollTime);

			Log.d("jamesdebug", "===================Info======================");

			game.source[0] = Axis_BRSserchArray_Index_X;
			game.source[1] = Axis_BRSserchArray_Index_Y;

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

	/* Clean drag & drop menu click count */
	private class cThread implements Runnable{

		@Override
		public void run() {
			try {
				Thread.sleep(450);
				clickCount = 0;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/* Create thread service.execute for clean button click count */ 
	private void cleanThread(ExecutorService service){
		service.execute(new cThread());
	}
	
	/* Monitor wifi signal */
	private class wifiMonitorThread implements Runnable{
		int rssi, level;
		String tag2;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			WifiManager wifi = (WifiManager) globalActivity.getSystemService(mContext.WIFI_SERVICE);
			rssi = wifi.getConnectionInfo().getRssi();
			level = wifi.calculateSignalLevel(rssi, 4);
			tag2 = Integer.toString(rssi);
			Log.i("shinhua1", "level " + tag2);
			
			Message meg1 = UIHandler.obtainMessage(1,tag2);
			UIHandler.sendMessage(meg1);
			
		}
		
	}
	
	/* Update UI Handler */
	private Handler UIHandler = new Handler(){
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			/* Change wifi UI display */ 
			wifiIconStatus( Integer.valueOf((String)msg.obj) );
		}
	};
	
	/* WiFi signal display icon */
	private void wifiIconStatus(int level){
		
		if(level < 0 && level >= -50){
			wifistatus1.setVisibility(View.VISIBLE);
			wifistatus2.setVisibility(View.VISIBLE);
			wifistatus3.setVisibility(View.VISIBLE);
			wifistatus4.setVisibility(View.VISIBLE);
		}else if(level < -50 && level >= -100){
			wifistatus1.setVisibility(View.VISIBLE);
			wifistatus2.setVisibility(View.VISIBLE);
			wifistatus3.setVisibility(View.VISIBLE);
			wifistatus4.setVisibility(View.INVISIBLE);
		}else if(level < -100 && level >= -150){
			wifistatus1.setVisibility(View.VISIBLE);
			wifistatus2.setVisibility(View.VISIBLE);
			wifistatus3.setVisibility(View.INVISIBLE);
			wifistatus4.setVisibility(View.INVISIBLE);
		}else if(level < -150 && level >= -200){
			wifistatus1.setVisibility(View.VISIBLE);
			wifistatus2.setVisibility(View.INVISIBLE);
			wifistatus3.setVisibility(View.INVISIBLE);
			wifistatus4.setVisibility(View.INVISIBLE);
		}
	}
	

}
