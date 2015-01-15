package org.doubango.imsdroid;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.doubango.imsdroid.cmd.EncoderCmd;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.util.Log;



public class UartReceive {
	
	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	boolean debugNanoQueue = false , debugEncoderQueue = false;

	private int nanoCount = 0 , encoderCount = 0;  
	private String nanoTestData[] = {
			"#-001.27:017:001:015","#-001.21:017:002:015","#-001.10:017:003:015",
			"#-002.27:017:001:015","#-003.21:017:002:015","#-004.10:017:003:015",
			"#-006.27:017:001:015","#-007.21:017:002:015"};
			
	private byte[] encoderTestData = {0x53,0x0d,(byte)0x02,0x30,0x03,0x15,0x01,(byte)0xff,0x00,0x00,0x45};
	private byte[] encoderTestData2 = {0x53,0x0d,(byte)0x01,0x50,0x03,0x1,0x01,(byte)0xff,0x00,0x00,0x45};

	private byte[] askEncoderData = {0x53,0x06,0x0d,0x00,0x00,0x45};
	private String ReStrEnco,ReStrNano;
	
	// We could modify here , to chage how many data should we get from queue.
	public static int getNanoDataSize = 3 , getEncoderDataSize = 1 , beSentMessage = 13;

	private int nanoInterval = 100 , encoderWriteWiatInterval = 50 , 
				encoderReadWaitInterval = 300 , encoderWaitInterval = 350, combineInterval = 400;
	public static int fd,nanoFd,encFd;
	private int minusNumber = 0 ;
	private boolean writerFirst = true , encoderDataChange = false; // Write First
	
	byte [] ReByteEnco = new byte[11];
	
	public static float robotLocation[]={0,0,0,0};
	
	public static float[] nanoFloat = new float[getNanoDataSize];
	public static float[] nanoFloat_1 = new float[getNanoDataSize];
	
	/*
	 * [0] = DW1000 anchor 1
	 * [1] = DW1000 anchor 2
	 * [2] = DW1000 anchor 3
	 * */ 
	public static float[] nanoFloat3Datas = new float[3];

	private static ArrayList<float[]> nanoQueue = new ArrayList<float[]>();
	private static ArrayList<byte[]> encoderQueue = new ArrayList<byte[]>();
	
	byte [] encoderDataByteArr = new byte[11];
	
	private Handler handler = new Handler();
	
	Runnable rNano = new NanoThread();
	Runnable rWEncoder = new EncoderWriteThread();
	Runnable rREncoder = new EncoderReadThread();
	Runnable rCombine = new CombineThread();
	Runnable rEncoder = new EncoderThreadPool();

	UartCmd uartCmd = UartCmd.getInstance();
	
	EncoderCmd encoderCmd = new EncoderCmd();
	
	public static int[] tempInt = new int[3]; // L Wheel , R Wheel , Compass
	
	// for DBG , save X Y data to file
	boolean nanoStart = false , 
			 encoderStart = false , 
			 combineStart = false;
	List<Point> AxisPointData = new ArrayList<Point>();
	
	long  encoderLSum = 0;
	long  encoderRSum = 0;
	
	private static String TAG = "App";
	
	public void RunRecThread() {
		
		nanoStart = true;
		encoderStart = true;
		combineStart = true;
	
		handler.postDelayed(rNano, nanoInterval);
	
		handler.postDelayed(rEncoder, encoderReadWaitInterval);

       	handler.postDelayed(rCombine, combineInterval);
		
	}
	
	public class NanoThread implements Runnable {
		   
		public void run() {
			
			if (debugNanoQueue)
			{
				Log.i(TAG,"NanoThread running count = " + nanoCount);
				/*ReStr = testdata1;
				//ReStr = "abcde";
				
				String[] daf = ReStr.split("\\s+");
				byte[] nanoBy = new byte[daf.length];
				for(int i=0;i<daf.length ; i++)
					nanoBy[i] = Byte.parseByte(daf[i]);
				*/
				
				//String nanoStr = ReceiveMsgUart(2);
				if (nanoCount  > 7)
					nanoCount = 0;
					
				String nanoStr = nanoTestData[nanoCount];
				String[] daf = nanoStr.split(":");
				float[] myflot = {Float.parseFloat(daf[2]),Float.parseFloat(daf[0].substring(2, daf[0].length()))};
				//Get data : #-001.27:017:001:015
				//nanoFloat3Datas[Integer.parseInt(daf[2]) - 1] = Float.parseFloat(daf[0].substring(2, daf[0].length()));

				nanoQueue.add(myflot);
				nanoCount++;
				
				
				handler.postDelayed(rNano,nanoInterval);
			
		    }
			else
			{
				/*if(uartCmd.GetNanoPanOpend() == false)
				{
					// Use UART1 for nanopan
					nanoFd = uartCmd.OpenSetUartPort("ttymxc2");
				}*/
				
				
				if (uartCmd.GetDW1000Opend()) {
					ReStrNano = uartCmd.ReceiveDW1000Uart(2);
					if ( ReStrNano != null) {
						//Log.i(TAG,"Nano Receive message = "+ ReStrNano + " leng= " + ReStrNano.length());
						String[] line20 =  ReStrNano.split("\r\n");
						//Log.i(TAG,"Nano line20 = " + line20[0]);
						for(int i=0 ;i< line20.length;i++)
						{
							//Log.i(TAG,"Nano line20[" + i + " ] = " + line20[i]);
							if (line20[i].contains("#") && line20[i].length() >= 5 && line20[i].contains(":"))
							{
								String[] daf = line20[i].split(":");

								if (daf.length == 4)
								{

									float[] myflot = {Float.parseFloat(daf[2]),
											Float.parseFloat(daf[0].substring(2, daf[0].length()))};
									
									// If data > 0 , we use it , else ignore it.
									if (myflot[1] > 0) 				
									{
										//Log.i(TAG, "Nano my float distance = "
										//		+ myflot[1]);

										nanoQueue.add(myflot);
									}
									// view.append(ReStr);
									// scrollView.fullScroll(ScrollView.FOCUS_DOWN);
									ReStrNano = null;
								}
							}
						}
					}
				}
				
				handler.postDelayed(rNano,nanoInterval);
			}
			
			
		}
 }

	public class EncoderThreadPool implements Runnable {

		public void run() {

			Log.i(TAG, "EncoderThreadPool");
			singleThreadExecutor.execute(rWEncoder);

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			singleThreadExecutor.execute(rREncoder);

			if (encoderStart)
				//handler.postDelayed(rEncoder, 350);
				handler.postDelayed(rEncoder, encoderWaitInterval);
		}

	}
	
	public class EncoderWriteThread implements Runnable {

		public void run() {

			if (debugEncoderQueue) {
				//writeLock();
				//MainActivity.SendMsgUart("test",2,askEncoderData);
				Log.i(TAG,"Write ask data");
				//writeUnLock();
				//handler.postDelayed(rWEncoder, encoderWriteWiatInterval);
			} else {
				
				//Log.i(TAG,"opend fd = " + uartCmd.GetDrivingOpend());

				if (uartCmd.GetDrivingOpend() == true) {
					//writeLock();
					Log.i(TAG,"Send Ask to Driving board");
					String ReStrEnco = null;
					try {
						ReStrEnco = new String(askEncoderData, "ISO-8859-1");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					UartCmd.SendMsgUart(1,askEncoderData);

				}
				//try {
				//	Thread.sleep(encoderWriteWiatInterval);
				//} catch (InterruptedException e) {
				//	e.printStackTrace();
				//}
				//handler.postDelayed(rWEncoder, encoderWriteInterval);
			}

		}
	}
	
	
	public class EncoderReadThread implements Runnable {

		@TargetApi(Build.VERSION_CODES.GINGERBREAD) public void run() {

			if (debugEncoderQueue) {
				
				//readLock();
				int dataSize = 8;
				byte[] dataByte = new byte[dataSize];
				
				Log.i(TAG, "EncoderThread running count = " + encoderCount);
				// ReStrEnco = "12345";
				//ReStrEnco = new String(endoerTestData, "ISO-8859-1");;
				// ReStr = "abcde";6
				Arrays.fill(dataByte, (byte)0x00);
				// dataByte[0]  is xPolarity
				// dataByte[1] -> [2] is X axis
				// dataByte[3]  is yPolarity
				// dataByte[4] -> [5] is Y axis
				// dataByte[6] -> [7] CRC 16 , 0x00 = not used
				if (encoderDataChange)
					dataByte = Arrays.copyOfRange(encoderTestData, 2, 10);
				else
					dataByte = Arrays.copyOfRange(encoderTestData2, 2, 10);
				
				encoderDataChange = !encoderDataChange;
				/*
				try {
					encoderDataByteArr = ReStrEnco.getBytes("ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}*/
				
				//encoderCmd.SetByte(ReStrEnco);
				//Log.i(TAG,"add encoder queue data = " + encoderCmd.GetDataByte());
				encoderQueue.add(dataByte);
				encoderCount++;
				
				//readUnLock();
				
				//handler.postDelayed(rREncoder, encoderReadWaitInterval);

				// byte[] encoderBy = ReceiveMsgUart(1);

				// encoderQueue.add(encoderBy);
				// encoderCount++;


			} else {

				/*if (uartCmd.GetDrivingOpend() == false) {
					// Use UART1 for nanopan
					encFd = uartCmd.OpenSetUartPort("ttymxc0");
				}*/
				
				
				//Log.i(TAG,"encoder fd = " + encoderOpend);

				if (uartCmd.GetDrivingOpend() == true) {
					
					Log.i(TAG,"Encoder ReadThread ");
					//readLock();
					//ReStrEnco = ReceiveMsgUart(1);
					//Log.i(TAG,"encoder read running");
					//while(true);
					//handler.postDelayed(rREncoder, encoderReadInterval);
					try {
						Thread.sleep(encoderReadWaitInterval);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ReByteEnco = UartCmd.ReceiveByteMsgUart(1);

					
					
					//Log.i(TAG,"Length="+ReByteEnco.length+",0="+ ReByteEnco[0]+",1="+ReByteEnco[1]);
						//Log.i(TAG,"encoder rec msg = " + ReByteEnco + " leng = " + ReByteEnco.length);
						//for(int i=0;i<ReByteEnco.length;i++)
						//	Log.i("wr","encoder data[ " + i + "] = " + ReByteEnco[i]);
						if (  ReByteEnco.length  ==  11 && ReByteEnco[0] == 0x53 &&  ReByteEnco[1] == 0x0d)
						{
							
							//Log.i(TAG,"Receive message = "+ ReStrEnco);
							//encoderCmd.SetByte(ReStrEnco);
							//byte [] test = encoderCmd.GetDataByte();
							Log.i(TAG,"Encoder Receive test[0] = "+ReByteEnco[0] + " test1 = "+ ReByteEnco[1] + " test2 = "+ ReByteEnco[2]+ " test3 = "+ ReByteEnco[3]+ 
									" test4 = "+ ReByteEnco[4] + " test5 = "+ ReByteEnco[5] +  " test6 = "+ ReByteEnco[6] + " test7 = "+ ReByteEnco[7]);
							//Log.i("123", "test6 = "+ ReByteEnco[6]);
							// Add receive message from Driving Board
							
							encoderCmd.SetDataByte(ReByteEnco);
							//byte [] test = encoderCmd.GetDataByte();
							
							encoderQueue.add(encoderCmd.GetDataByte());
							
							 
							 //Log.i(TAG,"receive Data byte = " + Arrays.copyOfRange(ReByteEnco, 2, 10));
							 //encoderQueue.add(Arrays.copyOfRange(ReByteEnco, 2, 10));
							
							//Log.i(TAG,"receive msg = " + ReStrEnco);
	
							// view.append(ReStr);
							// scrollView.fullScroll(ScrollView.FOCUS_DOWN);
							// Arrays.fill(ReByteEnco, (byte)0x00);
							// ReStrEnco = null;
							
							//try {
							//	Thread.sleep(encoderReadWaitInterval);
							//} catch (InterruptedException e) {
							//	e.printStackTrace();
							//}
						}
						
						//readUnLock();
					
				}

				//handler.postDelayed(rREncoder, encoderReadWaitInterval);
			}

		}
	}
	

	public class CombineThread implements 
	Runnable {

		public void run() {

			if (combineStart )
			{
			
				// Log.i(TAG,"encoderOpend = " + encoderOpend + "  nanoOpend = "
				// + nanoOpend );
				//Log.i(TAG, "nanoQueue.size() = " + nanoQueue.size()
				//		+ " encoderQueue.size() = " + encoderQueue.size());
				
				if ( ( uartCmd.GetDW1000Opend()== true || debugNanoQueue == true) 
						&& ( uartCmd.GetDrivingOpend() == true || debugEncoderQueue == true ) ) {
					// byte[] beSendMsg = new byte[beSentMessage];;
	
					Log.i(TAG, "nanoQueue.size() = " + nanoQueue.size()
							+ " encoderQueue.size() = " + encoderQueue.size());
					
					boolean getAllDW1000Data = false;
					float dw1000NewData[] = getDW1000NewData(nanoQueue);
					if (dw1000NewData[0] != 0 && dw1000NewData[1] != 0 && dw1000NewData[2] != 0)
						getAllDW1000Data = true;
					
					
					//if (nanoQueue.size() >= getNanoDataSize + 4
					//		&& encoderQueue.size() >= getEncoderDataSize) {
					if ( getAllDW1000Data == true
							&& encoderQueue.size() >= getEncoderDataSize) {
	
						// Arrays.fill(beSendMsg, (byte)0x00);
	
						//minusNumber = nanoQueue.size() - getNanoDataSize;
						// Two input here.
	
						//if (nanoQueue.size() % 3 != 0) {
	
						//	minusNumber = nanoQueue.size() - getNanoDataSize
						//			- (nanoQueue.size() % 3);
						//}
	
						// Two input here.
					//ArrayList<float[]> nanoData = new ArrayList<float[]>();
						//float nanoData[] = new float[3];
						//for (int i=0;i<3;i++)
						//	nanoData[i] = nanoFloat3Datas[i];
								
								//getNanoRange(nanoQueue,
								//minusNumber, nanoQueue.size()
										//- (nanoQueue.size() % 3));
						ArrayList<byte[]> encoderData = getEncoderRange(encoderQueue,
								encoderQueue.size() - getEncoderDataSize,
								encoderQueue.size());
	
	
						
						
						
						// Calculate nanopan data and encoder data here (java
						// layer).
						// Encoder data format
						// [L Polarity] [L2] [L1] [R polarity] [R2] [R1] [COM2] [COM1] [0x45]
						// Save to byte array beSendMsg[11]
						// ....................
						/*for (int i = 0; i < nanoData.size(); i++) {
							nanoFloat = nanoData.get(i);
							nanoFloat_1[i]=nanoFloat[1];
							Log.i(TAG, "combine nanoFloat [" + i + " ] = "
									+ nanoFloat_1[i]);
							
						}*/
	
						ArrayList<int[]> encoderDataQueue = new ArrayList<int[]>();
						byte[] encoByte = encoderData.get(0);
						//Log.i("toEKF","encoByte[0] = " + encoByte[0]+  "encoByte[1] = " + encoByte[1] + " encoByte[2] = "+ encoByte[2] + " encoByte[3] = " + encoByte[3]+" encoByte[4] =" + encoByte[4] 
							//			+ "encoByte[5] = " + encoByte[5]);
						//for (int i=0;i<encoderData.size();i++)
						//{
							tempInt[0] = 0;
							tempInt[1] = 0;
							
							tempInt[0]  = ( (encoByte[3] << 8) & 0xff00 | (encoByte[4] & 0xff));
							if (encoByte[2] == 2)
								tempInt[0] = -tempInt[0];
							 
							//encoderLSum = encoderLSum + tempInt[0];
							
							tempInt[1]  = ( (encoByte[6] << 8) & 0xff00 | (encoByte[7] & 0xff));
							if (encoByte[5] == 2)
								tempInt[1] = -tempInt[1];
							
							//encoderRSum = encoderRSum + tempInt[1];
							
							tempInt[2]  = ( (encoByte[8] << 8) & 0xff00 | (encoByte[9] & 0xff));
							
							
							//Log.i("toEKF","encoder size = " + encoderData.size() +"  encoder data L=" + tempInt[0] + " R=" + tempInt[1] + " com = " + tempInt[2]);
							
							//encoderDataQueue.add(tempInt);
						//}
						
						//thetaView.setText("theta : " + tempInt[2]);
						
						 
						//Log.i("toEKF","encoder size = " + encoderData.size() + " L = "+ tempInt[0] + " R = " + tempInt[1]+" L Sum =" + encoderLSum + " R Sum =" + encoderRSum+ " com =" + tempInt[2]);
						Log.i("toEKF","encoder size = " + encoderData.size() + " L = "+ tempInt[0] + " R = " + tempInt[1]+" Compass =" + tempInt[2] );
						//WeightSet(Float.parseFloat(dwWeight.getText( ).toString())
						//		,Float.parseFloat(encoderWeight.getText().toString()));
						
			///監看nanopan輸入值
						Log.i("toEKF","Nano1=" + dw1000NewData[0] + " Nano2=" + dw1000NewData[1] + " Nano3= " + dw1000NewData[2]);
			///------EKF-----------------------------------------------------------------------------------------
						//robotLocation = UartCmd.EKF((float)dw1000NewData[0],(float)dw1000NewData[1],(float)dw1000NewData[2],(int) tempInt[0] ,(int) tempInt[1],(int) tempInt[2]);
						robotLocation = UartCmd.EKF((float)dw1000NewData[0],(float)dw1000NewData[1],(float)dw1000NewData[2],(int) tempInt[0] ,(int) tempInt[1],(int) tempInt[2]);
			///--------------------------------------------------------------------------------------------------
						
						//robotLocation[2] = robotLocation[2] + (float)0.8;
						byte[] sendAxisToDriving = new byte[11];
						
						if (robotLocation[0] < 0  )
							sendAxisToDriving[2] = 2;
						else
							sendAxisToDriving[2] = 1;
						
						
						if (robotLocation[1] < 0  )
							sendAxisToDriving[5] = 2;
						else
							sendAxisToDriving[5] = 1;
						
						sendAxisToDriving[0] = 0x53;
						sendAxisToDriving[1] = 0x09;
						
						int LX = Math.abs(Math.round(robotLocation[0]));
						int LY = Math.abs(Math.round(robotLocation[1]));
						
						sendAxisToDriving[3] = (byte) (LX & 0xff00);
						sendAxisToDriving[4] = (byte) (LX & 0x00ff);
						
						
						sendAxisToDriving[6] = (byte) (LY & 0xff00);
						sendAxisToDriving[7] = (byte) (LY & 0x00ff);
						
						sendAxisToDriving[8] = 0x00;
						sendAxisToDriving[9] = 0x00;
						sendAxisToDriving[10] = 0x45;
						
						String ReStrLocation = null;
						try {
							ReStrLocation = new String(sendAxisToDriving, "ISO-8859-1");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
						/*for (int i=0;i<10;i++)
						{
							drivingaxisData[4] = (byte) (i *10);
							//MainActivity.SendMsgUart(1,sendAxisToDriving);
							MainActivity.SendMsgUart(1,drivingaxisData);
						}*/
						
						//MainActivity.SendMsgUart(1,sendAxisToDriving);
						
						Point point = new Point();
						Point pointOriginal = new Point();
						// Calibration to center
						//xCoordinateOri.setText(" State X  : " + Float.toString(robotLocation[2]));
						//yCoordinateOri.setText(" State Y : " + Float.toString(robotLocation[3]));
						
						pointOriginal.x = robotLocation[2];
						pointOriginal.y = robotLocation[3];
						
						pointOriginal.setCompass(tempInt[2]);
						
						AxisPointData.add(pointOriginal);
						
						point.x = (float) (robotLocation[0]*5 + 150);
						point.y = (float) (robotLocation[1]*5 + 150);
						
						//xCoordinate.setText(" Measure X : " + Float.toString(robotLocation[0]));
						//yCoordinate.setText(" Measure Y : " + Float.toString(robotLocation[1]));
						
						
						Log.i("toEKF","X = " + robotLocation[2] + "  Y = " + robotLocation[3]);
						//add for test
						//point.x = 200;
						//point.y = 300;				
						
				        /*if (count == 0){
				        	drawView.firsttouchX = pointOriginal.x;
				        	drawView.firsttouchY = pointOriginal.y;
	//			        	path.moveTo(firsttouchX, firsttouchY);
							count++;
				        }
				     
				        drawView.current_TouchX = point.x;
				        drawView.current_TouchY = point.y;
				        
				        
				        drawView.points.add(point);
				        */
	//			        
	//			    	paint.setStyle(Paint.Style.STROKE);
	//			        path.lineTo(current_TouchX, current_TouchY);					|| list.get(i*3)[0] == 1 && list.get(i*3 + 1)[0] == 3 && list.get(i*3 + 2)[0] == 2
	//					path.moveTo(current_TouchX, current_TouchY);
	//			        canvas.drawPath(path, paint);
				        
						//drawView.postInvalidate();
						/*
						try {
							Thread.sleep(50);
						} catch (Exception e) {
							e.printStackTrace();
						}*/
						// End
						
						encoderCount = 0;
						nanoCount = 0;
						nanoQueue.clear();
						encoderQueue.clear();
						
						encoderLSum = 0;
						encoderRSum = 0;
						// One Output Here
						// SendMsgUart(beSendMsg.toString(),1);
					}
	
					
				}
				handler.postDelayed(rCombine, combineInterval);
			}
		}
	}
	
	public static ArrayList<byte[]> getEncoderRange(ArrayList<byte[]> list, int start, int last) {

		ArrayList<byte[]> temp = new ArrayList<byte[]>();
		Log.i(TAG,"encoder start = " + start + " last = " + last);
		for (int x = start; x < last; x++) {
			temp.add(list.get(x));
			}

		return temp;
	}
	
	public static float[] getDW1000NewData(ArrayList<float[]> list) {
		boolean floatData0BeWrite = false;
		boolean floatData1BeWrite = false;
		boolean floatData2BeWrite = false;
		
		for (int i= list.size() - 1;i >= 0;i-- )
		{
			if (list.get(i)[0] == 1 && floatData0BeWrite == false)
			{
				nanoFloat3Datas[0] = list.get(i)[1];
				floatData0BeWrite = true;
			}
			else if (list.get(i)[0] == 2 && floatData1BeWrite == false)
			{
				nanoFloat3Datas[1] = list.get(i)[1];
				floatData1BeWrite = true;
			}
			else if (list.get(i)[0] == 3 && floatData2BeWrite == false)
			{
				nanoFloat3Datas[2] = list.get(i)[1];
				floatData2BeWrite = true;
			}
		}
		
		return nanoFloat3Datas;
	}
	
	public static ArrayList<float[]> getNanoRange(ArrayList<float[]> list, int start, int last) {

		ArrayList<float[]> temp = new ArrayList<float[]>();
		
		/*****************************************/
		// example : arr{1,2,3,1,2,3,1,2}        //
		// We need 2nd {1,2,3} , so we clear arraylist , and add newest
		/*****************************************/
		
		for (int i=0;i< (list.size() / 3 );i++)
		{
			if (list.get(i*3)[0] == 1 && list.get(i*3 + 1)[0] == 2 && list.get(i*3 + 2)[0] == 3 
					|| list.get(i*3)[0] == 1 && list.get(i*3 + 1)[0] == 3 && list.get(i*3 + 2)[0] == 2)
			{
				temp.clear();	// If we got newest data , clear old data .
				temp.add(list.get(i*3));
				temp.add(list.get(i*3 + 1));
				temp.add(list.get(i*3 + 2));
			}
		}
		
		/*Log.i(TAG,"start = " + start + " last = " + last);
		for (int x = start; x < last; x++) {
			temp.add(list.get(x));
			}
		 */
		return temp;
	}
}