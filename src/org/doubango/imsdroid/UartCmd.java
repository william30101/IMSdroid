package org.doubango.imsdroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.doubango.imsdroid.cmd.BaseCmd;
import org.doubango.imsdroid.cmd.DirectionCmd;
import org.doubango.imsdroid.cmd.StopCmd;

import android.util.Log;

public class UartCmd extends BaseCmd{
	
	private String TAG = "william";
	ByteArrayOutputStream retStreamDatas;

	private String[] cmdStr= {"direction","stop","angle","stretch","stopBySensor","ask",
			"destination","health","axis","ret","startBySensor","map"};
	private byte[] cmdByte = {0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b};
	
	public byte[] GetAllByte(String[] inStr) throws IOException
	{
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
	
	public static native int OpenUart(String str);
	public static native int ReadService(String str);
	public static native void CloseUart(int i);
	public static native int SetUart(int i);
	public static native int SendMsgUart(byte[] msg);
	public static native String ReceiveMsgUart();
	
}
