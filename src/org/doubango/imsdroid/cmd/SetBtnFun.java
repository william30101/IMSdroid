package org.doubango.imsdroid.cmd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.doubango.imsdroid.R;
import org.doubango.imsdroid.UartCmd;
import org.doubango.imsdroid.UartReceive;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.Utils.NetworkStatus;
import org.doubango.imsdroid.map.GameView;
import org.doubango.imsdroid.map.Game;
import org.doubango.imsdroid.map.MapList;
import org.doubango.imsdroid.map.SendCmdToBoardAlgorithm;


import android.app.Activity;
import android.os.Environment;
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
	
	//For map use
	private Button runBtn;
	GameView gameView;
	Game game;
	//End for Map use
	
	private ExecutorService service = Executors.newFixedThreadPool(10);
	
	SendCmdToBoardAlgorithm SendAlgo;
	
	
	public void SetBtn(Activity v)
	{
		uartCmd = UartCmd.getInstance();
		loggin = NetworkStatus.getInstance();
		
		XMPPSet = new XMPPSetting();
		uartRec = new UartReceive();
		
		
		gameView = (GameView) v.findViewById(R.id.gameView);
		
		game = new Game();
		
		SendAlgo = new SendCmdToBoardAlgorithm();
		
		
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
		
		//For Map use
		//Button saveBtn =  (Button)v.findViewById(R.id.saveBtn);
		Button resetBtn =  (Button)v.findViewById(R.id.resetBtn);
		Button stswatBtn =  (Button)v.findViewById(R.id.stswatBtn);
		Button map1Btn =  (Button)v.findViewById(R.id.map1Change);
		//Button map2Btn =  (Button)v.findViewById(R.id.map2Change);
		runBtn =  (Button)v.findViewById(R.id.runBtn);
		
		//saveBtn.setOnClickListener(onClickListener);
		resetBtn.setOnClickListener(onClickListener);
		stswatBtn.setOnClickListener(onClickListener);
		map1Btn.setOnClickListener(onClickListener);
		//map2Btn.setOnClickListener(onClickListener);
		runBtn.setOnClickListener(onClickListener);
		
		// End for Map use
		
		
		
		
		

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
			case R.id.map1Change:
				game.reloadMap(0,gameView);

				break;
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
				
			case R.id.stswatBtn:
				
				MapList.target[0][0] = 8;
				MapList.target[0][1] = 1;

				break;
			case R.id.runBtn:
				
				synchronized (SendAlgo) {
					try {
						SendAlgo.RobotStart(gameView,game,XMPPSet);

					} catch (Exception e) {
						e.printStackTrace();
					}

				}
				
				runBtn.setEnabled(false);
					

				break;
			case R.id.resetBtn:
       
				Log.i(TAG,"rest btn");
				
				MapList.target[0][0] = 8;
				MapList.target[0][1] = 8;
			
				game.source[0] = 8;
				game.source[1] = 1;
				break;
			
			case R.id.saveBtn:
				Log.i(TAG,"save btn");
				/*
				boolean sdCardExist = Environment.getExternalStorageState()   
	                    .equals(android.os.Environment.MEDIA_MOUNTED);
		    			
				
				if (sdCardExist)
				{
						Encoder enc = new Encoder();
		    			alllen = 0;
		    			
		    			sdcard = Environment.getExternalStorageDirectory();

		    			String dirc = sdcard.getParent();
		    			dirc = dirc + "/legacy";
		    			
		    			file = new File(dirc,"axisData.txt");
		    			Log.i(TAG," External storage path =" + dirc);
		    			
		    			 if (!file.exists())
		    			 {
							try {
								file.createNewFile();
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
		    			 }
		    			 else
		    			 {
		    				 file.delete();
		    				 
		    				 try {
								file.createNewFile();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		    			 }
		    			
		    			String  x , y;

		    			BufferedWriter writer;
						try {

							
							
							axisData = enc.GetAxisQueue();
							for (int i=0;i<axisData.size();i++)
							{
								double[] da = axisData.get(i);
								x = new Double(da[0]).toString();
								y = new Double(da[1]).toString();
								writer.write("index = " + i +" x = "+x +"  y = "+y + "\r\n");
							}
			    			writer.close();
			    			enc.CleanAxisQueue();
			    			
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    			   
				}
				*/
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
			//String decoded = new String(cmdByte, "ISO-8859-1");
			UartCmd.SendMsgUart( 1, cmdByte);
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
