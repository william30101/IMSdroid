package org.doubango.imsdroid.Screens;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartCmd;
import org.doubango.imsdroid.UartReceive;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.Utils.NetworkStatus;

import android.app.Activity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;

public class SetBtnFun {

	private String TAG = "App";
	private boolean isNeedAdd = false;
	private XMPPSetting XMPPSet;
	private NetworkStatus loggin;
	private UartCmd uartCmd;
	private UartReceive uartRec;
	
	private ExecutorService service = Executors.newFixedThreadPool(10);
	
	
	public void SetBtn(Activity v)
	{
		uartCmd = new UartCmd();
		loggin = NetworkStatus.getInstance();
		
		XMPPSet = new XMPPSetting();
		uartRec = new UartReceive();
		uartRec.RunRecThread();
		
		ImageButton backward= (ImageButton)v.findViewById(R.id.backward);
		ImageButton forward= (ImageButton)v.findViewById(R.id.forward);
		ImageButton left = (ImageButton)v.findViewById(R.id.left);
		ImageButton right= (ImageButton)v.findViewById(R.id.right);
		ImageButton stop = (ImageButton)v.findViewById(R.id.stop);
		ImageButton forRig= (ImageButton)v.findViewById(R.id.forRig);
		ImageButton forLeft = (ImageButton)v.findViewById(R.id.forLeft);
		ImageButton bacRig = (ImageButton)v.findViewById(R.id.bacRig);
		ImageButton bacLeft= (ImageButton)v.findViewById(R.id.bacLeft);
		
		Button angleBottom = (Button)v.findViewById(R.id.angleBottom);
		Button angleMiddle = (Button)v.findViewById(R.id.angleMiddle);
		Button angleTop = (Button)v.findViewById(R.id.angleTop);
		Button stretchBottom = (Button)v.findViewById(R.id.stretchBottom);
		Button stretchTop = (Button)v.findViewById(R.id.stretchTop);
		
		
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
	}
	
	
	private Button.OnTouchListener ClickListener = new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			//return gestureDetector.onTouchEvent(event);

			int eventAction = event.getAction();
			switch(eventAction){

				case MotionEvent.ACTION_DOWN:
					isNeedAdd = true;
					service.execute(new MyThread(v));
                   	//Runnable r = new MyThread(v);
                   	//new Thread(r).start();
					
					break;
				case MotionEvent.ACTION_UP:

					isNeedAdd = false;
				try {
					SendToBoard("stop stop");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					//XMPPSet.XMPPSendText("james1","stop stop");
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
    
  private Button.OnClickListener onClickListener = new OnClickListener() {

		int btnMsg;

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			btnMsg = v.getId();

			switch (btnMsg) {
			case R.id.angleBottom:
				Log.i(TAG,"angleBottom");
				try {
					SendToBoard("pitchAngle bottom");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//XMPPSet.XMPPSendText("james1", "pitchAngle bottom");
				break;
			case R.id.angleMiddle:
				Log.i(TAG,"angleMiddle");
				try {
					SendToBoard("pitchAngle middle");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//XMPPSet.XMPPSendText("james1", "pitchAngle middle");
				break;
			case R.id.angleTop:
				Log.i(TAG,"angleTop");
				try {
					SendToBoard("pitchAngle top");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//XMPPSet.XMPPSendText("james1", "pitchAngle top");
				break;
			case R.id.stretchBottom:
				Log.i(TAG,"stretchBottom");
				try {
					SendToBoard("stretch bottom");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//XMPPSet.XMPPSendText("james1", "stretch bottom");
				break;
			case R.id.stretchTop:
				Log.i(TAG,"stretchTop");
				try {
					SendToBoard("stretch top");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//XMPPSet.XMPPSendText("james1", "stretch top");
				break;
			default:
				Log.i(TAG,"onClickListener not support");
				break;
			}
		}
	};
	
	private void SendToBoard(String inStr) throws IOException
	{
		//Log.i(TAG," loggin status = " + loggin.GetLogStatus());
		
		if (loggin.GetLogStatus())
			XMPPSet.XMPPSendText("james1", inStr);
		else
		{
			String[] inM = inStr.split("\\s+");
			byte[] cmdByte = uartCmd.GetAllByte(inM);
			String decoded = new String(cmdByte, "ISO-8859-1");
			UartCmd.SendMsgUart(decoded, 1, cmdByte);
		}
	}
	
	
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
							SendToBoard("stop stop");
							//XMPPSet.XMPPSendText("james1", "stop stop"); // Stop button be pressed.
						else
							SendToBoard("direction " + sub);
							//XMPPSet.XMPPSendText("james1", "direction " + sub);
						// XMPPSet.XMPPSendText("james1",sub+" test");
						// sctc.SctpSendData(sub);
						// comm.setMsg(this.view.getId(), 1);
						// start(service);
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
}
