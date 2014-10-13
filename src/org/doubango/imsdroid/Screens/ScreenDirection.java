package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartCmd;
import org.doubango.imsdroid.UartReceive;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.ngn.services.INgnSipService;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;

public class ScreenDirection extends BaseScreen{

	
	private final INgnSipService mSipService;
	private static String TAG = ScreenTabDialer.class.getCanonicalName();
	
	private XMPPSetting XMPPSet;
	private UartReceive uartRec;
	
	public Thread test = new Thread();
	private boolean isNeedAdd = false;
	
	public ScreenDirection() {
		super(SCREEN_TYPE.DIALER_T, TAG);
		
		mSipService = getEngine().getSipService();
		
	}
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.screen_direction);
		
		ImageButton backward= (ImageButton)findViewById(R.id.backward);
		ImageButton forward= (ImageButton)findViewById(R.id.forward);
		ImageButton left = (ImageButton)findViewById(R.id.left);
		ImageButton right= (ImageButton)findViewById(R.id.right);
		ImageButton stop = (ImageButton)findViewById(R.id.stop);
		ImageButton forRig= (ImageButton)findViewById(R.id.forRig);
		ImageButton forLeft = (ImageButton)findViewById(R.id.forLeft);
		ImageButton bacRig = (ImageButton)findViewById(R.id.bacRig);
		ImageButton bacLeft= (ImageButton)findViewById(R.id.bacLeft);
		
		Button angleBottom = (Button)findViewById(R.id.angleBottom);
		Button angleMiddle = (Button)findViewById(R.id.angleMiddle);
		Button angleTop = (Button)findViewById(R.id.angleTop);
		Button stretchBottom = (Button)findViewById(R.id.stretchBottom);
		Button stretchTop = (Button)findViewById(R.id.stretchTop);
		
		Button axisBtn = (Button)findViewById(R.id.axisBtn);
		
		Button askBtn = (Button)findViewById(R.id.askBtn);
		
		backward.setOnTouchListener(ClickListener);
		forward.setOnTouchListener(ClickListener);
		left.setOnTouchListener(ClickListener);
		right.setOnTouchListener(ClickListener);
		stop.setOnTouchListener(ClickListener);
		forRig.setOnTouchListener(ClickListener);
		forLeft.setOnTouchListener(ClickListener);
		bacRig.setOnTouchListener(ClickListener);
		bacLeft.setOnTouchListener(ClickListener);
		
		angleBottom.setOnClickListener(onClickListener);
		angleMiddle.setOnClickListener(onClickListener);
		angleTop.setOnClickListener(onClickListener);
		stretchBottom.setOnClickListener(onClickListener);
		stretchTop.setOnClickListener(onClickListener);
		axisBtn.setOnClickListener(onClickListener);
		
		askBtn.setOnClickListener(onClickListener);
		
		XMPPSet = new XMPPSetting();
		uartRec = new UartReceive();
		uartRec.RunRecThread();
		
	}
	
	private Button.OnTouchListener ClickListener = new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			//return gestureDetector.onTouchEvent(event);

			int eventAction = event.getAction();
			switch(eventAction){

				case MotionEvent.ACTION_DOWN:
					isNeedAdd = true;
                   	Runnable r = new MyThread(v);
                   	new Thread(r).start();
					
					break;
				case MotionEvent.ACTION_UP:

					isNeedAdd = false;
					XMPPSet.XMPPSendText("james1","stop stop");
					break;
			
				case MotionEvent.ACTION_MOVE:
				//	System.out.println("action move");
					break;
			default:

					break;
		}
			
			return false;
		}


  };
  
  
  public class MyThread implements Runnable {

	   private View view;
	   String SendMsg;

		public MyThread(View v) {
			// store parameter for later user
			this.view = v;
		}

		public void run() {
			while (isNeedAdd) {
				// uiHandler.sendEmptyMessage(0);
				try {
					// Using SCTP transmit message

					// SendMsg = this.view.getTag().toString();
					SendMsg = view.getResources().getResourceName(view.getId());
					String sub = SendMsg.substring(SendMsg.indexOf("/") + 1);
					Log.i(TAG, "Send message" + sub);
					if (sub.equals("stop"))
						XMPPSet.XMPPSendText("james1", "stop stop"); // Stop
																			// button
																			// be
																			// pressed.
					else
						XMPPSet.XMPPSendText("james1", "direction " + sub);
					// XMPPSet.XMPPSendText("james1",sub+" test");
					// sctc.SctpSendData(sub);
					// comm.setMsg(this.view.getId(), 1);
					// start(service);
					Thread.sleep(100l);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private Button.OnClickListener onClickListener = new OnClickListener() {

		int btnMsg;

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			btnMsg = v.getId();

			switch (btnMsg) {
			case R.id.angleBottom:
				Log.i(TAG,"angleBottom");
				XMPPSet.XMPPSendText("james1", "pitchAngle bottom");
				break;
			case R.id.angleMiddle:
				Log.i(TAG,"angleMiddle");
				XMPPSet.XMPPSendText("james1", "pitchAngle middle");
				break;
			case R.id.angleTop:
				Log.i(TAG,"angleTop");
				XMPPSet.XMPPSendText("james1", "pitchAngle top");
				break;
			case R.id.stretchBottom:
				Log.i(TAG,"stretchBottom");
				XMPPSet.XMPPSendText("james1", "stretch bottom");
				break;
			case R.id.stretchTop:
				Log.i(TAG,"stretchTop");
				XMPPSet.XMPPSendText("james1", "stretch top");
				break;
			case R.id.axisBtn:
				Log.i(TAG,"axisBtn");
				XMPPSet.XMPPSendText("james1", "axis set");
				break;
			case R.id.askBtn:
				Log.i(TAG,"askBtn");
				XMPPSet.XMPPSendText("james1", "ask encoder");
				break;
			default:
				Log.i(TAG,"onClickListener not support");
				break;
			}
		}
	};
	
	
}
