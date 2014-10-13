package org.doubango.imsdroid;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

import org.doubango.imsdroid.cmd.EncoderCmd;

import android.os.Handler;
import android.util.Log;



public class UartReceive {
	
	
	boolean debugNanoQueue = true , debugEncoderQueue = false;

	private int nanoCount = 0 , encoderCount = 0;  
	private String nanoTestData[] = {"#-001.27:017:001:015","#-001.21:017:002:015",
	"#-001.10:017:003:015"};
	private byte[] encoderTestData = {0x53,0x0d,(byte)0x02,0x30,0x03,0x15,0x01,(byte)0xff,0x00,0x00,0x45};
	
	private byte[] askEncoderData = {0x53,0x06,0x08,0x00,0x00,0x45};
	private String ReStrEnco,ReStrNano;
	
	// We could modify here , to chage how many data should we get from queue.
	private int getNanoDataSize = 3 , getEncoderDataSize = 2 , beSentMessage = 13;
	private int nanoInterval = 100 , encoderWriteInterval = 80 , encoderReadInterval = 100 , combineInterval = 200;
	public static int fd,nanoFd,encFd;
	private int minusNumber = 0 ;
	private boolean writerFirst = true; // Write First
	private static int writingWriters = 0;
	private static int waitingWriters = 0;
	private static int readingReaders = 0;
	
	byte [] ReByteEnco = new byte[11];
	
	float[] nanoFloat = new float[getNanoDataSize];
	
	private static ArrayList<float[]> nanoQueue = new ArrayList<float[]>();
	private static ArrayList<byte[]> encoderQueue = new ArrayList<byte[]>();
	
	byte [] encoderDataByteArr = new byte[11];
	
	private Handler handler = new Handler();
	
	Runnable rNano = new NanoThread();
	Runnable rWEncoder = new EncoderWriteThread();
	Runnable rREncoder = new EncoderReadThread();
	Runnable rCombine = new CombineThread();

	UartCmd uartCmd = new UartCmd();
	
	EncoderCmd encoderCmd = new EncoderCmd();
	
	private static String TAG = "App";
	
	public void RunRecThread() {
		
		//handler.postDelayed(rnano, nanoInterval);

		//handler.postDelayed(rWEncoder, encoderWriteInterval);
		
		handler.postDelayed(rREncoder, encoderReadInterval);

		//handler.postDelayed(rcombine, combineInterval);
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
				if (nanoCount  > 2)
					nanoCount = 0;
					
				String nanoStr = nanoTestData[nanoCount];
				String[] daf = nanoStr.split(":");
				float[] myflot = {Float.parseFloat(daf[0].substring(2, daf[0].length())),0};
				//Get data : #-001.27:017:001:015
				 
				
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
				
				
				if (uartCmd.GetNanoPanOpend()) {
					ReStrNano = uartCmd.ReceiveMsgUart(2);
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

							
								float[] myflot = {Float.parseFloat(daf[0].substring(2, daf[0].length())),0};
								//Add receive message from nanopan
								Log.i(TAG,"Nano my float distance = " + myflot[0]);
								nanoQueue.add(myflot);
								//view.append(ReStr);
								//scrollView.fullScroll(ScrollView.FOCUS_DOWN);
								ReStrNano = null;
							}
						}
					}
				}
				
				handler.postDelayed(rNano,nanoInterval);
			}
			
			
		}
 }

	public class EncoderWriteThread implements Runnable {

		public void run() {

			if (debugEncoderQueue) {
				writeLock();
				//MainActivity.SendMsgUart("test",2,askEncoderData);
				Log.i(TAG,"Write ask data");
				writeUnLock();
				handler.postDelayed(rWEncoder, encoderWriteInterval);
			} else {
				
				//Log.i(TAG,"opend fd = " + uartCmd.GetDrivingOpend());
				if (uartCmd.GetDrivingOpend()) {
					writeLock();
					Log.i(TAG,"Send Ask to Driving board");
					String ReStrEnco = null;
					try {
						ReStrEnco = new String(askEncoderData, "ISO-8859-1");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					uartCmd.SendMsgUart(ReStrEnco,1,askEncoderData);
					writeUnLock();
				}

				handler.postDelayed(rWEncoder, encoderWriteInterval);
			}

		}
	}
	
	
	public class EncoderReadThread implements Runnable {

		public void run() {

			if (debugEncoderQueue) {
				
				readLock();
				int dataSize = 8;
				byte[] dataByte = new byte[dataSize];
				
				Log.i(TAG, "EncoderThread running count = " + encoderCount);
				// ReStrEnco = "12345";
				//ReStrEnco = new String(endoerTestData, "ISO-8859-1");;
				// ReStr = "abcde";
				Arrays.fill(dataByte, (byte)0x00);
				// dataByte[0]  is xPolarity
				// dataByte[1] -> [2] is X axis
				// dataByte[3]  is yPolarity
				// dataByte[4] -> [5] is Y axis
				// dataByte[6] -> [7] CRC 16 , 0x00 = not used

				dataByte = Arrays.copyOfRange(encoderTestData, 2, 10);
				
				/*
				try {
					encoderDataByteArr = ReStrEnco.getBytes("ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				
				//encoderCmd.SetByte(ReStrEnco);
				//Log.i(TAG,"add encoder queue data = " + encoderCmd.GetDataByte());
				encoderQueue.add(dataByte);
				encoderCount++;
				
				readUnLock();
				
				handler.postDelayed(rREncoder, encoderReadInterval);

				// byte[] encoderBy = ReceiveMsgUart(1);

				// encoderQueue.add(encoderBy);
				// encoderCount++;


			} else {

				/*if (uartCmd.GetDrivingOpend() == false) {
					// Use UART1 for nanopan
					encFd = uartCmd.OpenSetUartPort("ttymxc0");
				}*/
				
				
				//Log.i(TAG,"encoder fd = " + encoderOpend);
				if (uartCmd.GetDrivingOpend()) {
					
					readLock();
					//ReStrEnco = ReceiveMsgUart(1);

					ReByteEnco = uartCmd.ReceiveByteMsgUart(1);

					//Log.i(TAG,"receive msg = " + ReByteEnco);
					
					
					if (ReByteEnco[0] != 0x01)
					{

						//Log.i(TAG,"Receive message = "+ ReStrEnco);
						//encoderCmd.SetByte(ReStrEnco);
						//byte [] test = encoderCmd.GetDataByte();
						Log.i(TAG,"Receive test[0] = "+ReByteEnco[0] + "test1 = "+ ReByteEnco[1] + "test2 = "+ ReByteEnco[2]+ "test3 = "+ ReByteEnco[3]+ 
								"test4 = "+ ReByteEnco[4] + "test5 = "+ ReByteEnco[5]);
						// Add receive message from Driving Board
						//encoderQueue.add(encoderCmd.GetDataByte());
						
						 
						 //Log.i(TAG,"receive Data byte = " + Arrays.copyOfRange(ReByteEnco, 2, 10));
						 //encoderQueue.add(Arrays.copyOfRange(ReByteEnco, 2, 10));
						
						//Log.i(TAG,"receive msg = " + ReStrEnco);

						// view.append(ReStr);
						// scrollView.fullScroll(ScrollView.FOCUS_DOWN);
						// Arrays.fill(ReByteEnco, (byte)0x00);
						// ReStrEnco = null;
					}
					
					readUnLock();
				}

				handler.postDelayed(rREncoder, encoderReadInterval);
			}

		}
	}

	public class CombineThread implements Runnable {

		public void run() {

			// Log.i(TAG,"encoderOpend = " + encoderOpend + "  nanoOpend = "
			// + nanoOpend );
			if ( (uartCmd.GetDrivingOpend() == true || debugNanoQueue == true) 
					&& ( uartCmd.GetNanoPanOpend() == true || debugEncoderQueue == true ) ) {
				// byte[] beSendMsg = new byte[beSentMessage];;

				Log.i(TAG, "nanoQueue.size() = " + nanoQueue.size()
						+ " encoderQueue.size() = " + encoderQueue.size());

				if (nanoQueue.size() >= getNanoDataSize
						&& encoderQueue.size() >= getEncoderDataSize) {

					// Arrays.fill(beSendMsg, (byte)0x00);

					minusNumber = nanoQueue.size() - getNanoDataSize;
					// Two input here.

					if (nanoQueue.size() % 3 != 0) {

						minusNumber = nanoQueue.size() - getNanoDataSize
								- (nanoQueue.size() % 3);
					}

					// Two input here.
					ArrayList<float[]> nanoData = getNanoRange(nanoQueue,
							minusNumber, nanoQueue.size()
									- (nanoQueue.size() % 3));
					ArrayList<byte[]> encoderData = getEncoderRange(encoderQueue,
							encoderQueue.size() - getEncoderDataSize,
							encoderQueue.size());

					// Calculate nanopan data and encoder data here (java
					// layer).
					// Output Data format 11 bytes 
					// [0x53] [0x09] [X Polarity] [X2] [X1] [Y polarity] [Y2] [Y1] [CRC2] [CRC1] [0x45]
					// Save to byte array beSendMsg[11]
					// ....................

					for (int i = 0; i < nanoData.size(); i++) {
						nanoFloat = nanoData.get(i);
						Log.i(TAG, "combine nanoFloat [" + i + " ] = "
								+ nanoFloat[0]);
					}

					byte[] encoByte = encoderData.get(0);

					uartCmd.Combine(nanoData, encoderData);
					// .................

					// End
					encoderCount = 0;
					nanoCount = 0;
					nanoQueue.clear();
					encoderQueue.clear();
					// One Output Here
					// SendMsgUart(beSendMsg.toString(),1);
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
	
	public static ArrayList<float[]> getNanoRange(ArrayList<float[]> list, int start, int last) {

		ArrayList<float[]> temp = new ArrayList<float[]>();
		Log.i(TAG,"nano start = " + start + " last = " + last);
		for (int x = start; x < last; x++) {
			temp.add(list.get(x));
			}

		return temp;
	}
	
	
	public synchronized void readLock() {
	    try {
	        while(writingWriters > 0 || (writerFirst && waitingWriters > 0)) {
	            wait();
	        }
	    }
	    catch(InterruptedException e) {
	        e.printStackTrace();
	    }

	    readingReaders++;
	 }
	 
	 public synchronized void readUnLock() {
	    readingReaders--;
	    writerFirst = true;
	    notifyAll();
	 }
	 
	 public synchronized void writeLock() {
	    waitingWriters++;
	    try {
	        while(readingReaders > 0 || writingWriters > 0) {
	            wait();
	        }
	    }
	    catch(InterruptedException e) {
	        e.printStackTrace();
	    }
	    finally {
	        waitingWriters--;
	    }

	    writingWriters++;
	 }
	 
	 public synchronized void writeUnLock() {
	    writingWriters--;
	    writerFirst = false;
	    notifyAll();
	 } 
}
