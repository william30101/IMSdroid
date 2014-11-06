package org.doubango.imsdroid.map;

import java.util.ArrayList;

import org.doubango.imsdroid.XMPPSetting;

import android.util.Log;

public class SendCmdToBoardAlgorithm {

	private String TAG = "william";
	
	//private static XMPPSetting XMPPSet;
	
	boolean arduinoDebug = false;
	int nextX = 0 , nextY = 0;
	int originalX = 0, originalY = 0;
	MapList mapList;
	
	ArrayList<int[][]> pathQ = new ArrayList<int[][]>();
	//GameView gameView;
	
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
		} else if (inString.equals("forward")) {
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
	
	
	public String FindDirection(int inTheta)
	{
		String direction = "forward";
		
		if (inTheta == 0)
		{
			direction = "forward";
		}
		if (inTheta == 45)
		{
			direction = "forRig";
		}
		if (inTheta == -45)
		{
			direction = "forLeft";
		}
		else if (inTheta == 90)
		{
			direction = "right";
		}
		else if (inTheta == -90)
		{
			direction = "left";
		}
		else if (inTheta == 135)
		{
			direction = "bacRig";
		}
		else if (inTheta == -135)
		{
			direction = "bacLeft";
		}
		else if (inTheta == 180)
		{
			direction = "backward";
		}
		// Is this okay??
		// need to check this condition
		else if (inTheta == -180)
		{
			direction = "backward";
		}
		else if (inTheta == 225)
		{
			direction = "bacLeft";
		}
		else if (inTheta == -225)
		{
			direction = "bacRig";
		}
		else if (inTheta == 270)
		{
			direction = "left";
		}
		else if (inTheta == -270)
		{
			direction = "right";
		}
		else if (inTheta == 315)
		{
			direction = "forLeft";
		}
		else if (inTheta == -315)
		{
			direction = "forRig";
		}
		
		return direction;
	}
	
	public int FindCompass(final XMPPSetting inXMPPSet, int dx , int dy)
	{
		int compass = 0;
		if (dx == 0 && dy == 1) {
			compass = 0;
			//SendCommand("forward");
			//SendCommand(inXMPPSet,"forward");
		} else if (dx == -1 && dy == 1) {
			compass = 45;
			//SendCommand("forRig");
		} else if (dx == -1 && dy == 0) {
			compass = 90;
			//SendCommand("right");
		} else if (dx == -1 && dy == -1) {
			compass = 135;
			//SendCommand("bacRig");
		} else if (dx == 0 && dy == -1) {
			compass = 180;
			SendCommand(inXMPPSet,"backward");
			//SendCommand("backward");
		} else if (dx == 1 && dy == -1) {
			compass = 225;
			//SendCommand("bacLeft");
		} else if (dx == 1 && dy == 0) {
			compass = 270;
			//SendCommand("left");
		} else if (dx == 1 && dy == 1) {
			compass = 315;
			//SendCommand("forLeft");
		}
		
		return compass;
	}
	
	public void RobotStart(final GameView gameView , final Game game , final XMPPSetting inXMPPSet)
	{
		Log.i("william", "Robot thread running");
		
		new Thread() {
			public void run() {
				
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
						
						int OriginalCompass = FindCompass(inXMPPSet, old_dx , old_dy);
						int nextCompass = FindCompass(inXMPPSet, dx , dy);
						
						int theta = nextCompass - OriginalCompass;
						
						Log.i(TAG, " OriginalCompass = " + OriginalCompass+
								" nextCompass = " + nextCompass + " \n theta = " + theta);
						//Avoid backward , but robort  turn to forward 
						if (OriginalCompass == 180 && nextCompass == 180)
							theta = 180;
						//else if (OriginalCompass == 225 && nextCompass == 225)
						////	theta = 180;
						//else if (OriginalCompass == 135 && nextCompass == 135)
						
						//	theta = 180;
						//else if (OriginalCompass == 135 || OriginalCompass == 180 
						//		|| OriginalCompass == 225)
						//	theta = theta + 180 ;
						
						String dir = FindDirection(theta);
						SendCommand(inXMPPSet,dir);
						
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
			}
		}.start();
	}

}
