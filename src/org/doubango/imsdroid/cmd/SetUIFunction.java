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
import org.doubango.imsdroid.BLE.BLEDeviceControlActivity;
import org.doubango.imsdroid.Screens.ScreenDraw;
import org.doubango.imsdroid.Screens.ScreenUIJoyStick;
import org.doubango.imsdroid.Screens.ScreenUIVerticalSeekBar;
import org.doubango.imsdroid.Utils.NetworkStatus;
import org.doubango.imsdroid.map.Game;
import org.doubango.imsdroid.map.GameView;
import org.doubango.imsdroid.map.SendCmdToBoardAlgorithm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
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
	private boolean isNeedAdd = false;
	private XMPPSetting XMPPSet;
	private NetworkStatus loggin;
	private UartCmd uartCmd;
	private UartReceive uartRec;

	private BLEDeviceControlActivity BLEDevCon;

	// For map use
	private Button jsRunBtn;
	GameView gameView;
	Game game;
	// End for Map use

	private ExecutorService service = Executors.newFixedThreadPool(10);
	SendCmdToBoardAlgorithm SendAlgo;

	/* ThreadPool declare for JoyStick operate */
	int height, width;

	private ExecutorService newService = Executors.newFixedThreadPool(10);
	private ExecutorService cleanService = Executors.newFixedThreadPool(1);

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

	/* BlueTooth temporary declare */
	private Button BLEWrite;
	public static EditText BLEDataText;
	public String BLEData = null;

	/* Temporary declare */
	private Button tempMenu;
	
	int clickCount = 0;
	long startTime;
	int duration;
	static final int MAXDURATION = 150;

	public SetUIFunction(Activity activity) {
		globalActivity = activity;
		mContext = activity.getWindow().getDecorView().getContext();
	}

	public void StartUIFunction() {

		uartCmd = UartCmd.getInstance();
		loggin = NetworkStatus.getInstance();

		XMPPSet = new XMPPSetting();
		uartRec = new UartReceive();
		// uartRec.RunRecThread();

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

		// dragMenu.setOnTouchListener(dragListener);
		dragMenu.setOnDragListener(dragListener1);
		img.setOnTouchListener(imgListener);
		selected_item = (View) globalActivity.findViewById(R.id.screenmenu);

		/*--------------------------------------------------*/
		/* Temporary */
		BLEWrite = (Button) globalActivity.findViewById(R.id.BLEWriteBtn);
		BLEDataText = (EditText) globalActivity.findViewById(R.id.BLEDataText);

		BLEWrite.setOnClickListener(onClickListener);

		Button getAxisBtn = (Button) globalActivity
				.findViewById(R.id.getAxisBtn);
		getAxisBtn.setOnClickListener(onClickListener);

		tempMenu = (Button) globalActivity.findViewById(R.id.testMenu);
		tempMenu.setOnClickListener(onClickListener);
		
		
		//cleanThread.scheduleAtFixedRate(new cleanCount(), 0, 300, TimeUnit.MILLISECONDS);
	}

	private void getScreenSize(Activity v) {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
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
			// TODO Auto-generated method stub
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
			Log.isLoggable("shinhua1", indicator);
			switch (indicator) {

				
			case R.id.testMenu:
				executeColorPicker(v);
				break;

			case R.id.getAxisBtn:
				uartRec.RunRecThread();
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
					 BLEDevCon =BLEDeviceControlActivity.getBLEDevCon(); // Parent , Childselected item mode 0 = write
				 BLEDevCon.CharacteristicWRN(2,
				 1, 0, BLEDataText.getText().toString());
				 
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case 1:
			Log.i(TAG, "angleMiddle");
			try {
				SendToBoard("pitchAngle middle");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case 2:
			Log.i(TAG, "angleTop");
			try {
				SendToBoard("pitchAngle top");
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
			// TODO Auto-generated method stub

			switch (progress) {
			case 0:
				// vsProgress.setText(progress+"");
				try {
					SendToBoard("stretch bottom");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 1:
				// vsProgress.setText(progress+"");
				try {
					SendToBoard("stretch top");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

	};

	/* DragDrap Menu Listener */
	View.OnTouchListener dragListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			switch (event.getActionMasked()) {
			case MotionEvent.ACTION_MOVE:
				Log.i("shinhua1", "ACTION_MOVE");
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
				break;
			default:
				break;
			}
			return true;
		}

	};
	
	View.OnTouchListener imgListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			/*
			 * switch (event.getActionMasked()) { case MotionEvent.ACTION_DOWN:
			 * Log.i("shinhua1", "DragShadowBuilder"); offset_x = (int)
			 * event.getX(); offset_y = (int) event.getY();
			 * 
			 * DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
			 * v.startDrag(null, shadowBuilder, v, 0);
			 * v.setVisibility(View.INVISIBLE); break; default: break; }
			 * 
			 * return false; }
			 */

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				Log.i("shinhua1","ACTION_DOWN");
				cleanThread(cleanService);
				clickCount++;
				if(clickCount == 2){
					executeColorPicker(v);
					clickCount = 0;
					duration = 0;
				}
				return true;
			}
			if(event.getAction() == MotionEvent.ACTION_MOVE){
				Log.i("shinhua1","ACTION_DOWN");
				offset_x = (int) event.getX();
				offset_y = (int) event.getY();
				DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
				v.startDrag(null, shadowBuilder, v, 0);
				//v.setVisibility(View.INVISIBLE);
			}
			if(event.getAction() == MotionEvent.ACTION_UP){ 
				Log.i("shinhua1","ACTION_UP");
				if(clickCount == 2){
					executeColorPicker(v);
					clickCount = 0;
					duration = 0;
				}
				return true;
			}
			else {
				return false;
			}

		}

	};
	

	View.OnDragListener dragListener1 = new OnDragListener() {

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
				Log.i("xyz", x + " === " + y);
				if (x < 250)
					x = 250;
				if (y < 80)
					y = 80;

				if (x > w)
					x = w;
				if (y > h)
					y = h;

				Log.i("xyz", x + " =*= " + y);
				RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
						new ViewGroup.MarginLayoutParams(
								RelativeLayout.LayoutParams.WRAP_CONTENT,
								RelativeLayout.LayoutParams.WRAP_CONTENT));

				lp.setMargins(x, y, 0, 0);
				// lp.setMargins(left, top, right, bottom)
				selected_item.setLayoutParams(lp);
				v = (View)event.getLocalState();
				//v.setVisibility(View.VISIBLE);
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
				//v.setVisibility(View.VISIBLE);
				break;

			}
			return true;
		}

	};

	private void executeColorPicker(View v) {

		// Context mContext = v.getContext();
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Create color picker view

		View view = inflater.inflate(R.layout.color_picker_dialog, null);
		if (v == null)
			return;

		final ColorPicker picker = (ColorPicker) view.findViewById(R.id.picker);
		SVBar svBar = (SVBar) view.findViewById(R.id.svbar);
		OpacityBar opacityBar = (OpacityBar) view.findViewById(R.id.opacitybar);
		final TextView hexCode = (TextView) view.findViewById(R.id.hex_code);

		// shinhua add
		final TextView colorLevel = (TextView) view
				.findViewById(R.id.color_level);

		// picker.addSVBar(svBar);
		// picker.addOpacityBar(opacityBar);
		picker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
			@Override
			public void onColorChanged(int intColor) {
				// String hexColor =
				// Integer.toHexString(intColor).toUpperCase();
				// hexCode.setText("#" + hexColor);
				int colorValue = Math.abs((int) (intColor / 18));
				int level1 = colorValue * 15 + 14;
				// Log.i("shinhua1", colorValue +" ======= " +
				// switchled(colorValue));
				String color = Integer.toString(colorValue).toUpperCase();
				colorLevel.setText("Current LED LEVEL" + color);

			}
		});

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

			}
		});
		builder.create().show();
	}

	/* Send BlueTooth Command */
	private String switchled(int itor) {

		switch (itor) {
		case 0:
			BLEData = "0x0E";
			break;
		case 1:
			BLEData = "0x1D";
			break;
		case 2:
			BLEData = "0x2C";
			break;
		case 3:
			BLEData = "0x3B";
			break;
		case 4:
			BLEData = "0x4A";
			break;
		case 5:
			BLEData = "0x59";
			break;
		case 6:
			BLEData = "0x68";
			break;
		case 7:
			BLEData = "0x77";
			break;
		case 8:
			BLEData = "0x86";
			break;
		case 9:
			BLEData = "0x96";
			break;
		case 10:
			BLEData = "0xA4";
			break;
		case 11:
			BLEData = "0xB3";
			break;
		case 12:
			BLEData = "0xC2";
			break;
		case 13:
			BLEData = "0xD1";
			break;
		case 14:
			BLEData = "0xE0";
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
					// TODO Auto-generated catch block
					e.printStackTrace();
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
	

	private void cleanThread(ExecutorService service){
		service.execute(new cThread());
	}
	
	private class cThread implements Runnable{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
			
				Thread.sleep(450);
				Log.i("xyz", "Clean clickCount");
				clickCount = 0;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
		
	}
	
	
}
