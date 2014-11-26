package org.doubango.imsdroid.cmd;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartCmd;
import org.doubango.imsdroid.UartReceive;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.Screens.ScreenDraw;
import org.doubango.imsdroid.Screens.ScreenUIJoyStick;
import org.doubango.imsdroid.Screens.ScreenUIVerticalSeekBar;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.capricorn.ArcMenu;

public class SetUIFunction {

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
	private int instructor; /* Robot Commands Direction Instructor */
	
	/* JoyStick object declare */
	RelativeLayout layout_joystick, layout_menu, layout_robot;
	ScreenUIJoyStick js;
	ScreenDraw myDraw;
	
	/* ARC Menu object declare */
	ArcMenu arcMenu;
	int arcLayoutsize;	
	private static final int[] ITEM_DRAWABLES = { R.drawable.robot_bowhead, R.drawable.robot_normal , R.drawable.robot_headup};
	private static final String[] message = { "Head up", "Normal", "Bow head"};

	/* Robot vertical seekbar object declare */
	ScreenUIVerticalSeekBar seekbar = null;
/*	TextView vsProgress;*/
	RelativeLayout seekbarlayout;
	LayoutParams seekbarparams, seekBarlayoutparams;
	AbsoluteLayout test;

	public void SetUIFunction(Activity v) {
		uartCmd = new UartCmd();
		loggin = NetworkStatus.getInstance();

		XMPPSet = new XMPPSetting();
		uartRec = new UartReceive();
		uartRec.RunRecThread();

		gameView = (GameView) v.findViewById(R.id.gameView1);
		game = new Game();

		SendAlgo = new SendCmdToBoardAlgorithm();

		getScreenSize(v);

		/* Joy Stick */
		layout_joystick = (RelativeLayout) v.findViewById(R.id.layout_joystick);
		setJoyStickParameter(v);
		layout_joystick.setOnTouchListener(joystickListener);

		
		/* Button declare */
		jsRunBtn = (Button) v.findViewById(R.id.runjs);
		jsRunBtn.setOnClickListener(onClickListener);
		
		
		/* Arc Menu */
		/* Set layout size & position */
		setARClayoutSize(width);
		LayoutParams params = new RelativeLayout.LayoutParams(arcLayoutsize, arcLayoutsize);
		Log.i("shinhua", "params width " + params.width + "params height" + params.height );
		RelativeLayout layout = (RelativeLayout) v.findViewById(R.id.layout_robot);

		arcMenu = (ArcMenu) v.findViewById(R.id.arc_menu);
		arcMenu.setLayoutParams(params);
		initArcMenu(arcMenu, ITEM_DRAWABLES, v);

		/* Robot seekbar */
		seekbar = (ScreenUIVerticalSeekBar) v.findViewById(R.id.robotseekbar);
	    //vsProgress=(TextView)v.findViewById(R.id.vertical_sb_progresstext);
	    
/*	    seekBarparams = seekbar.getLayoutParams();
	    seekBarparams.height = 500;*/

	    seekbarlayout = (RelativeLayout) v.findViewById(R.id.layout_seekbar);
	    seekBarlayoutparams = seekbarlayout.getLayoutParams();
	    seekBarlayoutparams.height = (int)((width/6)*1.5);
	    seekBarlayoutparams.width = width / 6;
	    
	    seekbarparams = seekbar.getLayoutParams();
	    seekbarparams.height = (int)((width/6)*2);

	    
	    
	    seekbar.setMax(1);
	    seekbar.setProgress(0);
		seekbar.setOnSeekBarChangeListener(seekbarListener);
        
		
		

	}
	

	private void getScreenSize(Activity v) {
		// TODO Auto-generated method stub
		Display display = v.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		width = size.x;
		height = size.y;	
	}
	
	
	private void setARClayoutSize(int width){
		this.arcLayoutsize = width / 6;
	}
	
	private void setJoyStickParameter(Activity v) {
		// TODO Auto-generated method stub
		js = new ScreenUIJoyStick(v.getApplicationContext(), layout_joystick,
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
	
	
	private void setSeekbarParameter(SeekBar seekbar){
		seekBarlayoutparams = seekbar.getLayoutParams();
		seekBarlayoutparams.height = 500;
		
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
	
	/* Set Navigation Button onClickListener */
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
                  	//Toast.makeText(getApplicationContext(), "position:" + message[position], Toast.LENGTH_SHORT).show();
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
	SeekBar.OnSeekBarChangeListener seekbarListener = new OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
	
			switch(progress){
			case 0:
				//vsProgress.setText(progress+"");
				try {
					SendToBoard("stretch bottom");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case 1:
				//vsProgress.setText(progress+"");
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
	
	
	
	
	
	
	/* XMPP Sendfunction */
	private void SendToBoard(String inStr) throws IOException {
		// Log.i(TAG," loggin status = " + loggin.GetLogStatus());

		if (loggin.GetLogStatus())
			XMPPSet.XMPPSendText("james1", inStr);
		else {
			String[] inM = inStr.split("\\s+");
			byte[] cmdByte = uartCmd.GetAllByte(inM);
			String decoded = new String(cmdByte, "ISO-8859-1");
			UartCmd.SendMsgUart(decoded, 1, cmdByte);
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
				}catch (InterruptedException e) {
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
