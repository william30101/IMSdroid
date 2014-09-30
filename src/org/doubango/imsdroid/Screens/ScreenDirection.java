package org.doubango.imsdroid.Screens;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartCmd;
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
		setContentView(R.layout.screen_direction);
		
		ImageButton Backward= (ImageButton)findViewById(R.id.backward);
		ImageButton Forward= (ImageButton)findViewById(R.id.forward);
		ImageButton Left = (ImageButton)findViewById(R.id.left);
		ImageButton Right= (ImageButton)findViewById(R.id.right);
		ImageButton Stop = (ImageButton)findViewById(R.id.stop);
		
		Backward.setOnTouchListener(ClickListener);
		Forward.setOnTouchListener(ClickListener);
		Left.setOnTouchListener(ClickListener);
		Right.setOnTouchListener(ClickListener);
		Stop.setOnTouchListener(ClickListener);
		
		
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
					XMPPSet.XMPPSendText("james1","stop stop no no");
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
					if (sub.equals("stop"))
						XMPPSet.XMPPSendText("james1","stop stop no no");	//Stop button be pressed.
					else
						XMPPSet.XMPPSendText("james1","direction " + sub + " no no");
					//XMPPSet.XMPPSendText("james1",sub+" test");
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
