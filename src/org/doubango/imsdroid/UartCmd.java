package org.doubango.imsdroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.doubango.imsdroid.cmd.BaseCmd;
import org.doubango.imsdroid.cmd.DirectionCmd;
import org.doubango.imsdroid.cmd.StopCmd;


import android.util.Log;

public class UartCmd extends BaseCmd{
	
	private String TAG = "william";
	ByteArrayOutputStream retStreamDatas;

	private String[] cmdStr= {"direction","stop","angle","stretch","stopBySensor","ask",
			"destination","health","axis","ret","startBySensor","map"};
	private byte[] cmdByte = {0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c};
	
	private boolean drivingOpend = false , nanoPanOpend = false;
	private int Baud_rate = 0; // { B19200, B115200};
	private static final String functionName = IMSDroid.class.getSimpleName();
	
	public static int fd = 0,nanoFd = 0,driFd = 0;
	
	public byte[] GetAllByte(String[] inStr) throws IOException
	{
		
		
		super.SetNum(cmdStr,cmdByte);

		retStreamDatas = new ByteArrayOutputStream();
		byte[] retBytes ;
		switch(super.GetNum(inStr[0]))
		{
			case 0x01:
				DirectionCmd direc = new DirectionCmd();
				direc.SetByte(inStr);
				retStreamDatas = direc.GetAllByte();
				break;
				
			case 0x02:
				StopCmd scmd = new StopCmd();
				scmd.SetByte(inStr);
				retStreamDatas = scmd.GetAllByte();
				break;
				
			case 0x03:
				break;	
				
			case 0x04:
				break;
				
			case 0x05:
				break;
				
			case 0x06:
				break;	
				
			case 0x07:
				break;
				
			case 0x08:
				break;	
				
			case 0x09:
				break;
				
			case 0x0a:
				break;
				
			default:
				break;
		}
		
		retBytes = retStreamDatas.toByteArray();
		
		
		return retBytes;
	}

	
	public int OpenSetUartPort(String portName)
	{
		
		// mxc0 for driving board , 19200
		// mxc2 for nanoPan , Baudrate 115200
		if (portName.equals("ttymxc0")) {
			nanoFd = OpenUart(portName, 1 );

			
			if (nanoFd > 0) {
				Baud_rate = 0; // 19200
				SetUart(Baud_rate, 1);
				fd = nanoFd;
			}

		} else if (portName.equals("ttymxc2")) {

			driFd = OpenUart(portName, 2 );
			if (driFd > 0) {
				Baud_rate = 1; // 115200
				SetUart(Baud_rate, 2);
				fd = driFd;
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
	public static native int SendMsgUart(String msg,int fdNum);
	public static native String ReceiveMsgUart(int fdNum);
	public static native int StartCal();
	public static native byte[] Combine(ArrayList<float[]> nanoq , ArrayList<byte[]> encoq);
	
}
