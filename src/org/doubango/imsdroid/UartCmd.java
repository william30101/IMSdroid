package org.doubango.imsdroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.doubango.imsdroid.cmd.AngleCmd;
import org.doubango.imsdroid.cmd.AskCmd;
import org.doubango.imsdroid.cmd.AxisCmd;
import org.doubango.imsdroid.cmd.BaseCmd;
import org.doubango.imsdroid.cmd.DirectionCmd;
import org.doubango.imsdroid.cmd.HealthCmd;
import org.doubango.imsdroid.cmd.StopCmd;
import org.doubango.imsdroid.cmd.StretchCmd;


import android.util.Log;

public class UartCmd extends BaseCmd{
	
	private String TAG = "william";
	ByteArrayOutputStream retStreamDatas;

	private String[] cmdStr= {"direction","stop","pitchAngle","stretch","stopBySensor","ask",
			"destination","health","axis","ret","startBySensor","mapFromPIC32","encoder","mapControl","mode"};
	private byte[] cmdByte = {0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0x0d,0x0e,0x0f};
	
	private boolean drivingOpend = false , nanoPanOpend = false;
	private int Baud_rate = 0; // { B19200, B115200};
	private static final String functionName = IMSDroid.class.getSimpleName();
	
	public static int fd = 0,nanoFd = 0,driFd = 0;
	
	//init all cmd object here
	DirectionCmd direc = new DirectionCmd();
	StopCmd scmd = new StopCmd();
	AngleCmd angleCmd = new AngleCmd();
	StretchCmd stretCmd = new StretchCmd();
	
	HealthCmd healCmd = new HealthCmd();
	
	AxisCmd axisCmd = new AxisCmd();
	AskCmd askCmd = new AskCmd();
	
	public UartCmd()
	{
		super.SetByte(cmdStr,cmdByte,2);
	}
	
	public byte[] GetAllByte(String[] inStr) throws IOException {

		retStreamDatas = new ByteArrayOutputStream();

		switch (super.GetByteNum(inStr[0], 2)) {
		case 0x01:

			direc.SetByte(inStr);
			retStreamDatas = direc.GetAllByte();
			break;

		case 0x02:

			scmd.SetByte(inStr);
			retStreamDatas = scmd.GetAllByte();
			break;

		case 0x03:
			angleCmd.SetByte(inStr);
			retStreamDatas = angleCmd.GetAllByte();
			break;

		case 0x04:
			stretCmd.SetByte(inStr);
			retStreamDatas = stretCmd.GetAllByte();
			break;

		case 0x05:
			break;

		case 0x06:
			askCmd.SetByte(inStr);
			retStreamDatas = askCmd.GetAllByte();
			break;

		case 0x07:
			break;

		// Need to modify it.
		case 0x08:
			healCmd.GetByte(inStr);
			break;

		case 0x09:
			Integer R = new Integer(255);
			byte r = R.byteValue();
			Integer R2 = new Integer(200);
			byte r2 = R2.byteValue();
			byte[] test = { 0x01, 0x01, r, 0x01, 0x02, r2, 0x00, 0x00 };
			inStr[1] = new String(test, "ISO-8859-1");
			axisCmd.SetByte(inStr);
			retStreamDatas = axisCmd.GetAllByte();
			break;

		case 0x0a:
			break;

		default:
			break;
		}

		byte[] retBytes = retStreamDatas.toByteArray();
		retStreamDatas.reset();
		
		for (int i =0;i<retBytes.length;i++)
		{
			retBytes[i] = (byte) (retBytes[i] & 0xFF);
		}

		return retBytes;
	}

	public static int unsignedToBytes(byte b) {
	    return b & 0xFF;
	  }
	
	public int OpenSetUartPort(String portName)
	{
		
		// mxc0 for driving board , 19200
		// mxc2 for nanoPan , Baudrate 115200
		if (portName.equals("ttymxc3")) {
			
			driFd = OpenUart(portName, 1 );
			if (driFd > 0) {
				Baud_rate = 0; // 19200
				SetUart(Baud_rate, 1);
				fd = driFd;
				
				Log.i(TAG,"Driving Board fd = " + driFd);
			}
			
		} else if (portName.equals("ttymxc2")) {

			//portName = "ttymxc2";
			nanoFd = OpenUart(portName, 2 );
			//driFd = OpenUart(portName, 1 );
			
			if (nanoFd > 0) {
			//if (driFd > 0) {
				Baud_rate = 1; // 115200
				SetUart(Baud_rate, 2);
				fd = nanoFd;
				//fd = driFd;
				Log.i(TAG,"Nano Board fd = " + nanoFd);
			}
		}
		else
		{
			fd = 0;
		}
		
		
		Log.i(TAG, functionName + " portname = "  + portName +" fd = " + fd);
		

		return fd;
		
	}
	
	public boolean GetNanoPanOpend()
	{
		return (nanoFd > 0 ? true : false);
	}
	
	public boolean GetDrivingOpend()
	{
		return (driFd > 0 ? true : false);
	}
	
	static
	{
		try
		{
			System.loadLibrary("hello-uart");
			Log.i("JNI", "Trying to load libhello-uart.so");
		}
		catch(UnsatisfiedLinkError ule)
		{
			Log.i("JNI", "WARNING: could not to load libhello-uart.so");
		}
	}
	
	public static native int WriteDemoData(int[] data, int size);
	public static native int OpenUart(String str, int fdNum);
	public static native int CloseUart(int fdNum);
	public static native int SetUart(int i , int fdNum);
	public static native int SendMsgUart(String msg,int fdNum,byte[] inByte);
	public static native String ReceiveMsgUart(int fdNum);
	public static native byte[] ReceiveByteMsgUart(int fdNum);
	public static native int StartCal();
	public static native byte[] Combine(ArrayList<float[]> nanoq , ArrayList<byte[]> encoq);
	
}
