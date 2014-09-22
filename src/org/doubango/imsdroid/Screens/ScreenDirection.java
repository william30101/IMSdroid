package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.ngn.services.INgnSipService;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;

public class ScreenDirection extends BaseScreen{

	
	private final INgnSipService mSipService;
	private static String TAG = ScreenTabDialer.class.getCanonicalName();
	
	private XMPPSetting XMPPSet;
	
	public Thread test = new Thread();
	private boolean isNeedAdd = false;
	
	public ScreenDirection() {
		super(SCREEN_TYPE.DIALER_T, TAG);
		
		mSipService = getEngine().getSipService();
		
	}
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.direction_btn);
		
		ImageButton BACKWARD= (ImageButton)findViewById(R.id.BACKWARD);
		ImageButton FORWARD= (ImageButton)findViewById(R.id.FORWARD);
		ImageButton LEFT= (ImageButton)findViewById(R.id.LEFT);
		ImageButton RIGHT= (ImageButton)findViewById(R.id.RIGHT);
		ImageButton STOP= (ImageButton)findViewById(R.id.STOP);
		
		BACKWARD.setOnTouchListener(ClickListener);
		FORWARD.setOnTouchListener(ClickListener);
		LEFT.setOnTouchListener(ClickListener);
		RIGHT.setOnTouchListener(ClickListener);
		STOP.setOnTouchListener(ClickListener);
		
		
		XMPPSet = new XMPPSetting();
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
					XMPPSet.XMPPSendText("james1","STOP");
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

					//SendMsg = this.view.getTag().toString();
					SendMsg = view.getResources().getResourceName(view.getId());
					String sub = SendMsg.substring(SendMsg.indexOf("/") + 1);
					Log.i(TAG,"Send message" +  sub);
					XMPPSet.XMPPSendText("james1",sub);
					//sctc.SctpSendData(sub);
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
	
	
}
