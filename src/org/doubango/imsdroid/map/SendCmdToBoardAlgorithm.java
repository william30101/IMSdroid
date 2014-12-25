package org.doubango.imsdroid.map;

import java.io.IOException;
import java.util.ArrayList;

import org.doubango.imsdroid.UartCmd;
import org.doubango.imsdroid.UartReceive;
import org.doubango.imsdroid.XMPPSetting;
import org.doubango.imsdroid.UartReceive.NanoThread;
import org.doubango.imsdroid.cmd.SetUIFunction;

import android.util.Log;

public class SendCmdToBoardAlgorithm {

	private String TAG = "william";
	private int testtime = 2000;
	
	//private static XMPPSetting XMPPSet;
	public UartCmd uartCmd = UartCmd.getInstance();
	boolean arduinoDebug = false;
	int nextX = 0 , nextY = 0;
	int originalX = 0, originalY = 0;
	MapList mapList;
	
	// Setting direction for e-Compass
		//Index of Axis_eComAngle_F  = 0
		//Index of Axis_eComAngle_RF = 1
		//Index of Axis_eComAngle_R  = 2
		//Index of Axis_eComAngle_RB = 3
		//Index of Axis_eComAngle_B  = 4
		//Index of Axis_eComAngle_LB = 5
		//Index of Axis_eComAngle_L  = 6
		//Index of Axis_eComAngle_LF = 7

	SetUIFunction setUIfunction = SetUIFunction.getInstance();
	
	private String Axis_SendeComAngle_to32_F = "forward";
	private String Axis_SendeComAngle_to32_B = "forward";
	private String Axis_SendeComAngle_to32_L = "forward";
	private String Axis_SendeComAngle_to32_R = "forward";
	
	public static int Axis_InitialCompass = 0;
	public static int Axis_eComAngle_ret = 0;
	public static int Axis_eComAngle_tmp = 0;
//	public static String Axis_SendeComAngle_to32 = "forward";

	public int Axis_simulator_com = 0;
	public static int [] Axis_eComAngle_Array = new int []
			{0, 0, 0, 0, 0, 0, 0, 0};

	public static boolean Axis_RunDrawCircle_StopUpdate = false;
	
	private static ArrayList<int[][]> pathQ = new ArrayList<int[][]>();
	
	public static GameView _gameView ;
	public static Game _game ;
	private static XMPPSetting _inXMPPSet;
	
	GameView gameView;
	
	public void DirectionCorrect(XMPPSetting inXMPPSet,String inString, int times) {
	
		for (int i = 0; i < times; i++) {
			
			if (!arduinoDebug)
			{
				synchronized (inXMPPSet) {
					try {
						inXMPPSet.XMPPSendText("james1", "direction " + inString);
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else
			{
				//UartMsg.SendMsgUartNano(inString + "\n");
				inXMPPSet.XMPPSendText("james1", inString + "\n");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			/*try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			}
		}
		
	}
	
	
	
	public void SendCommand(XMPPSetting inXMPPSet, String inString) {
		// TODO Auto-generated method stub
		int loopcount = 0;
		String correctStr = null;
		Log.i(TAG, " Send command = " + inString);

		if (inString.equals("left")) {
			loopcount = 90;
			correctStr = inString;
		}
		else if (inString.equals("right")) {
			loopcount = 90;
			correctStr = inString;
		} else if (inString.equals("backward")) {
			loopcount = 13;
			correctStr = inString;
		} else if (inString.equals("direction forward")) {
			loopcount = 32;
			correctStr = inString;
		}
		// for 45 , 135 , 225 , 315 angle 
		else {
			loopcount = 45;
			if (inString.equals("bacRig") || inString.equals("bacLeft"))
				correctStr = "backward";

			/*
			for (int i = 0; i < 45; i++) {
				synchronized (inXMPPSet) {
					try {
						inXMPPSet.XMPPSendText("james1", "direction "
								+ inString);

					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}

			if (inString.equals("bacRig") || inString.equals("bacLeft"))
				inString = "backward";

			DirectionCorrect(inXMPPSet, inString, 10);*/
		}
		
		
		
		
		
		
		
		
		////////////////////////////////////
		// Send command here .            //
		///////////////////////////////////
		for (int i=0;i< loopcount ; i++)
		{
			if (!arduinoDebug) {
	
				synchronized (inXMPPSet) {
					try {
						inXMPPSet.XMPPSendText("james1", "direction "
								+ inString);
	
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	
				}
			}
			else
			{
				//UartMsg.SendMsgUartNano(inString + "\n");
				inXMPPSet.XMPPSendText("james1", inString + "\n");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		// Correct direction
		DirectionCorrect(inXMPPSet, inString, 10);
		
	}
	
	public static void SetCompass()
	{
		Log.d("jamescompass", "=========================compass_in======================================");
		Axis_eComAngle_Array[0] = UartReceive.tempInt[2];
		Axis_eComAngle_tmp = Axis_eComAngle_Array[0];

		for (int i = 1 ; i < 8 ; i++)
		{
			if ( Axis_eComAngle_tmp > 360 ) {
				Axis_eComAngle_tmp = Axis_eComAngle_tmp - 360;
			}

			Axis_eComAngle_tmp = Axis_eComAngle_tmp + 45;
			Axis_eComAngle_Array[i] = Axis_eComAngle_tmp;
		};
		
		Axis_InitialCompass = Axis_eComAngle_Array[0];
		Log.d("jamescompass", "=========================compass_in======================================");
		Log.d("jamescompass", "=========================compass_in======================================");
		Log.d("jamescompass", "=========================compass_in======================================");
	}
	
	/*
	 * 			getCompassTable Describe
	 * 		NowCompass : current compass value
	 * 		InitialCompass : initial compass value
	 * return value is
		 * compassTableSelected (NowCompass >=  InitialCompass)
		 * 			0 		: 315-44  degree ,  FindCompass_F
		 * 			1 		: 45-134  degree ,  FindCompass_R
		 * 			2 		: 135-224 degree ,  FindCompass_B
		 * 			3 		: 225-314 degree ,  FindCompass_L
		 *
		 *
		 * compassTableSelected (NowCompass <  InitialCompass)
		 * 			0 		: 315-44  degree ,  FindCompass_F
		 * 			3 		: 45-134  degree ,  FindCompass_L
		 * 			2 		: 135-224 degree ,  FindCompass_B
		 * 			1 		: 225-314 degree ,  FindCompass_R
		 *
	 * */
	public String getCompassTable(int NowCompass , int InitialCompass , int dx , int dy) {

		int compassTableSelected = 0 , compassDelta = 0;
		boolean positiveValue = true;
		String retval = "forwoard";
		if (NowCompass >=  InitialCompass)
		{
			compassDelta = NowCompass - InitialCompass;
			positiveValue = true;
		}
		else {
			compassDelta = InitialCompass - NowCompass;
			positiveValue = false;
		}

		int compassBaseNumber = compassDelta / 90;
		int compassModNumber = compassDelta % 90;

		if (compassModNumber >= 45)
			compassTableSelected = compassBaseNumber + 1;
		else
			compassTableSelected = compassBaseNumber;

		// If degree between 315 <-> 360 , compassTableSelected =
		// compassBaseNumber(3) + 1 = 4
		// use the forward table .

		Log.d("jamesdebug","original compass: " + InitialCompass);
		Log.d("jamesdebug","now compass: " + NowCompass);
		Log.d("jamesdebug","Select compassID: " + compassTableSelected);
		Log.d("jamesdebug","=============dx=============: " + dx);
		Log.d("jamesdebug","=============dy=============: " + dy);
		
		switch (compassTableSelected) {
		
				
			case 0:
				retval = FindCompass_F( dx, dy );
				break;
				
			case 1:
				if (positiveValue)
				{
					retval = FindCompass_R( dx, dy );
				}
				else {
					retval = FindCompass_L( dx, dy );
				}
				break;
				
			case 2:
				retval = FindCompass_B( dx, dy );
				break;
				
			case 3:
				if (positiveValue)
				{
					retval = FindCompass_L( dx, dy );
				}
				else {
					retval = FindCompass_R( dx, dy );
				}
				break;
				
			case 4:
				retval = FindCompass_F( dx, dy );
				break;
				
			default:
				break;
				
			}

		return retval;
	}
	
	public String FindCompass_F(int dx , int dy)
	{

		if (dx == 0 && dy == 1) {
			
			Axis_SendeComAngle_to32_F = "direction forward";
			Axis_simulator_com = 0;
			
		} else if (dx == -1 && dy == 0)  {
			
			Axis_SendeComAngle_to32_F = "RotateAngle P 90";
			Axis_simulator_com = 90;
			
		} else if (dx == 0 && dy == -1)  {
			
			Axis_SendeComAngle_to32_F = "RotateAngle P 180";
			Axis_simulator_com = 180;
			
		} else if (dx == 1 && dy == 0)   {
			
			Axis_SendeComAngle_to32_F = "RotateAngle N 90";
			Axis_simulator_com = 270;
		}

		return Axis_SendeComAngle_to32_F;
	}

	public String FindCompass_B(int dx , int dy)
	{

		if (dx == 0 && dy == 1) {
			
			Axis_SendeComAngle_to32_B = "RotateAngle P 180";
			Axis_simulator_com = 0;
		} else if (dx == -1 && dy == 0)  {
			
			Axis_SendeComAngle_to32_B = "RotateAngle N 90";
			Axis_simulator_com = 90;
		} else if (dx == 0 && dy == -1)  {
			
			Axis_SendeComAngle_to32_B = "direction forward";
			Axis_simulator_com = 180;
			
		} else if (dx == 1 && dy == 0)   {
			
			Axis_SendeComAngle_to32_B = "RotateAngle P 90";
			Axis_simulator_com = 270;
		}

		return Axis_SendeComAngle_to32_B;
	}
	
	public String FindCompass_L(int dx , int dy)
	{

		if (dx == 0 && dy == 1) {
			
			Axis_SendeComAngle_to32_L = "RotateAngle P 90";
			Axis_simulator_com = 0;
			
		} else if (dx == -1 && dy == 0)  {
			
			Axis_SendeComAngle_to32_L = "RotateAngle P 180";
			Axis_simulator_com = 90;
		} else if (dx == 0 && dy == -1)  {
			
			Axis_SendeComAngle_to32_L = "RotateAngle N 90";
			Axis_simulator_com = 180;
		} else if (dx == 1 && dy == 0)   {
			
			Axis_SendeComAngle_to32_L = "direction forward";
			Axis_simulator_com = 270;
		}

		return Axis_SendeComAngle_to32_L;
	}
	
	public String FindCompass_R(int dx , int dy)
	{
		
		if (dx == 0 && dy == 1) {
			
			Axis_SendeComAngle_to32_R = "RotateAngle N 90";
			Axis_simulator_com = 0;
			
		} else if (dx == -1 && dy == 0)  {
			
			Axis_SendeComAngle_to32_R = "direction forward";
			Axis_simulator_com = 90;
		} else if (dx == 0 && dy == -1)  {
			
			Axis_SendeComAngle_to32_R = "RotateAngle P 90";
			Axis_simulator_com = 180;
		} else if (dx == 1 && dy == 0)   {
			
			Axis_SendeComAngle_to32_R = "RotateAngle P 180";
			Axis_simulator_com = 270;
		}

		return Axis_SendeComAngle_to32_R;
	}
	
//	
//	public String FindDirection(final XMPPSetting inXMPPSet, int inTheta)
//	{
//		if (inTheta == 0)
//		{
//			//Direction_forward_times(10);
//			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if (inTheta == 45)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle P 45";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if ( inTheta == -45)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle N 45";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if (inTheta == 90)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle P 90";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if ( inTheta == -90)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle N 90";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if (inTheta == 135)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle P 135";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if (inTheta == -135)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle N 135";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if (inTheta == 180)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle P 180";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if (inTheta == -180)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle P 180";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if (inTheta == 225)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle P 225";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if (inTheta == -225)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle N 255";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if (inTheta == 270)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle P 270";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if (inTheta == -270)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle N 270";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if (inTheta == 315)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle P 315";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
//		else if (inTheta == -315)
//		{
//			Axis_SendeComAngle_to32 = "RotateAngle N 315";
////			Axis_SendeComAngle_to32 = "direction forward";
//		}
////
////		try {
////			
////			//setUIfunction.SendToBoard(Axis_SendeComAngle_to32);
////			String[] inM = Axis_SendeComAngle_to32.split("\\s+");
////			byte[] cmdByte = uartCmd.GetAllByte(inM);
//////			 String decoded = new String(cmdByte, "ISO-8859-1");
////			UartCmd.SendMsgUart(1, cmdByte);
////			
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
////		
////		try {
////			
////			Thread.sleep(100);
////			
////		} catch (InterruptedException e) {
////			e.printStackTrace();
////		}
//		
//		return Axis_SendeComAngle_to32;
//	}

	public void Direction_times(int times, String xxx, XMPPSetting inXMPPSet)
	{
		Log.d("jamesdebug", "xxx = " + xxx);
		
		if (xxx != "direction forward")
		{
			
			
				
			
			inXMPPSet.XMPPSendText("james1", xxx);
			
			try {
				Thread.sleep(300);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
//				try {
//				
//				Log.d("jamesdebug", "Direction_" + xxx);
////				String forward_string = "direction forward";
//				//setUIfunction.SendToBoard("direction forward");
//				String[] inM = xxx.split("\\s+");
//				byte[] cmdByte = uartCmd.GetAllByte(inM);
////				 String decoded = new String(cmdByte, "ISO-8859-1");
//				UartCmd.SendMsgUart(1, cmdByte);
//				try {
//					
//					Thread.sleep(300);
//					
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//				Axis_InitialCompass = UartReceive.tempInt[2];
//				
//			} catch (IOException e) {
//				e.printStackTrace();
//			}


			try {
				
				Thread.sleep(5000);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		
			String forward_string = "direction forward";
			String[] inM = forward_string.split("\\s+");
			byte[] cmdByte;
			
			for (int i=0;i<10;i++)
			{
				inXMPPSet.XMPPSendText("james1", "direction forward");
				
//				try {
//					cmdByte = uartCmd.GetAllByte(inM);
//					UartCmd.SendMsgUart(1, cmdByte);
					Log.d("jamesdebug", "correct forward times= " + i);
					
					try {
						
						Thread.sleep(100);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
//					
//					
//					
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
			}
//			 String decoded = new String(cmdByte, "ISO-8859-1");
		} else if (xxx == "direction forward"){
			for( int i = 0; i < times; i++ )
			{
				
				inXMPPSet.XMPPSendText("james1", xxx);
				Log.d("jamesdebug", "Direction_" + xxx +" _times: " + i);
//				try {
//					
//					
////					String forward_string = "direction forward";
//					//setUIfunction.SendToBoard("direction forward");
//					
//					
//					
//					String[] inM = xxx.split("\\s+");
//					byte[] cmdByte = uartCmd.GetAllByte(inM);
////					 String decoded = new String(cmdByte, "ISO-8859-1");
//					UartCmd.SendMsgUart(1, cmdByte);
					try {
						
						Thread.sleep(300);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
//					
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
			}
		}
	}
	
	public void RobotStart(final GameView gameView , final Game game , final XMPPSetting inXMPPSet) 
	{
		Log.i("william", "Robot start running");
		
		new Thread() {
			public void run() {
				
				Axis_RunDrawCircle_StopUpdate = true;
				
				if (gameView.algorithmDone == true) { // When user press Start , path cal done
					int old_dx , old_dy;
					gameView.drawCircleFlag = true;
					gameView.refreshFlag = false; // Stop onDraw
					
					//PathQueue[size - 1] == Start  . . .  PathQueue[0] == Target
					pathQ = gameView.getPathQueue();
					
					for (int i = pathQ.size() - 1; i >= 0; i--) {
						gameView.drawCount = i;
						gameView.postInvalidate();
						try {
							Thread.sleep(50);
						} catch (Exception e) {
							e.printStackTrace();
						}

						int[][] axis = pathQ.get(i);
						int[][] old_axis = new int[2][2];

						//if ((old_axis = gameView.getPathQueue().get(
						//		gameView.getPathQueue().size() - 2)) != null)
						//	gameView.over2Grid = true;
						originalX = axis[1][0];
						originalY = axis[1][1];
						nextX = axis[0][0];
						nextY = axis[0][1];

						if (  i <  (pathQ.size() - 1) )
							old_axis = pathQ.get( i  + 1);
						else
						{
							// If move only 2 times , we don't have pre move data.
							old_axis[0][0] = nextX;
							old_axis[0][1] = nextY;
							old_axis[1][0] = originalX;
							old_axis[1][1] = originalY;
						}
						// Move on horizontal direction
						if (i <  (pathQ.size() - 1))
						{
							old_dx = originalX - old_axis[1][0];
							old_dy = originalY - old_axis[1][1];
						}
						else
						{
							// If first move , default forward direction.
							old_dx = 0;
							old_dy = 1;
						}

						Log.i(TAG, " ( oldX , oldY ) = (" + old_axis[1][0] + " , " + old_axis[1][1]+
								")  (oX , oY) = (" + originalX + " , " + originalY + 
								")  (nX , nY) = (" + nextX+ " , " + nextY + ")");
						
						int dx = nextX - originalX;
						int dy = nextY - originalY;
												
						String dir = getCompassTable(Axis_simulator_com, 0, dx, dy);
						
						//String dir = getCompassTable(UartReceive.tempInt[2], Axis_eComAngle_Array[0], dx, dy);
						Direction_times(10, dir, inXMPPSet);
					}

					// Clear Target bitmap 
					game.target[0] = -1;
					game.target[1] = -1;
					
					// Set Source location to Target
					// new start position
					game.source[0] = nextX;
					game.source[1] = nextY;

					// Redrawing source and target bitmap position
					game.setPathFlag(false);
					
					// Clear path line  , when path running done.
					game.getSearchProcess().clear();
					
					// We don't need to draw circle when arrived target.
					gameView.drawCircleFlag = false;

					gameView.postInvalidate();
					
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Axis_RunDrawCircle_StopUpdate = false;
				
			}
		}.start();
		
	}

}
