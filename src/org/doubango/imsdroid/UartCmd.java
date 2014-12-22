package org.doubango.imsdroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.doubango.imsdroid.BLE.BLEDeviceControlActivity;
import org.doubango.imsdroid.cmd.AngleCmd;
import org.doubango.imsdroid.cmd.AskCmd;
import org.doubango.imsdroid.cmd.AxisCmd;
import org.doubango.imsdroid.cmd.BaseCmd;
import org.doubango.imsdroid.cmd.DirectionCmd;
import org.doubango.imsdroid.cmd.HealthCmd;
import org.doubango.imsdroid.cmd.RotateAngleCmd;
import org.doubango.imsdroid.cmd.StopCmd;
import org.doubango.imsdroid.cmd.StretchCmd;
import org.doubango.imsdroid.map.NetworkStatus;


import android.util.Log;

public class UartCmd extends BaseCmd{
	
	private static final String TAG = UartCmd.class.getSimpleName();
	ByteArrayOutputStream retStreamDatas;

	private String[] cmdStr= {"direction","stop","pitchAngle","stretch","stopBySensor","ask",
			"destination","health","axis","ret","startBySensor","mapFromPIC32","encoder","mapControl","mode",
			"RotateAngle", "BLE"};
	private byte[] cmdByte = {0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0x0d,
			0x0e,0x0f, 0x10, 0x40};
	
	private boolean drivingOpend = false , nanoPanOpend = false;

	public static int fd = 0,dw1000Fd = 0,driFd = 0;
	
	//init all cmd object here
	DirectionCmd directionCmd;
	StopCmd stopCmd;
	AngleCmd angleCmd;
	StretchCmd stretchCmd;
	HealthCmd healCmd;
	AxisCmd axisCmd;
	AskCmd askCmd;
	RotateAngleCmd rotateAngleCmd;
	
	private BLEDeviceControlActivity BLEDevCon;
	
	private static UartCmd uartInstance;
	
	public static UartCmd getInstance() {
		 if (uartInstance == null){
	            synchronized(NetworkStatus.class){
	                if(uartInstance == null) {
	                	uartInstance = new UartCmd();
	                }
	            }
	        }
	        return uartInstance;
	}
	
	
	private UartCmd()
	{
		super.SetByte(cmdStr,cmdByte,2);
		
		directionCmd = new DirectionCmd();
		stopCmd = new StopCmd();
		angleCmd = new AngleCmd();
		stretchCmd = new StretchCmd();
		healCmd = new HealthCmd();
		axisCmd = new AxisCmd();
		rotateAngleCmd = new RotateAngleCmd();
		askCmd = new AskCmd();
	}
	
	public byte[] GetAllByte(String[] inStr) throws IOException {

		retStreamDatas = new ByteArrayOutputStream();

		switch (super.GetByteNum(inStr[0], 2)) {
		case 0x01:

			directionCmd.SetByte(inStr);
			retStreamDatas = directionCmd.GetAllByte();
			break;

		case 0x02:

			stopCmd.SetByte(inStr);
			retStreamDatas = stopCmd.GetAllByte();
			break;

		case 0x03:
			angleCmd.SetByte(inStr);
			retStreamDatas = angleCmd.GetAllByte();
			break;

		case 0x04:
			stretchCmd.SetByte(inStr);
			retStreamDatas = stretchCmd.GetAllByte();
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
			healCmd.SetByte(inStr);
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
			
		case 0x10:
			rotateAngleCmd.SetByte(inStr);
			retStreamDatas = rotateAngleCmd.GetAllByte();
			break;
			
		case 0x40:
			//BleCmd.SetByte(inStr);
			if (BLEDevCon == null)
				// Add BLE control
				BLEDevCon = BLEDeviceControlActivity.getInstance();
			// Parent , Child selected item , mode 0 = write
			BLEDevCon.CharacteristicWRN(2, 1, 0, inStr[1]);
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
		
		// mxc3 for driving board , Baudrate 19200
		// mxc2 for DW1000 , 		Baudrate 115200
		if (portName.equals("ttymxc3")) {
			driFd = OpenUart(portName );
			if (driFd > 0) {
				Log.i(TAG,"Driving Board portname = "  + portName + " fd = " + driFd);
				return driFd;
			}
			
		} else if (portName.equals("ttymxc2")) {
			dw1000Fd = OpenUart(portName);
			if (dw1000Fd > 0) {
				Log.i(TAG,"DW1000 portname = "  + portName + " fd = " + dw1000Fd);
				return dw1000Fd;
			}
		}
		else
		{
			fd = 0;
		}
		
		return 0;
		
	}
	
	public boolean GetDW1000Opend()
	{
		return (dw1000Fd > 0 ? true : false);
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
	public static native int OpenUart(String str);
	public static native int CloseUart(int fdNum);
	public static native int SetUart(int fdNum , int baudrate);
	public static native int SendMsgUart(int fdNum,byte[] inByte);
	public static native String ReceiveDW1000Uart(int fdNum);
	public static native byte[] ReceiveByteMsgUart(int fdNum);
	public static native int StartCal();
	public static native byte[] Combine(ArrayList<float[]> nanoq , ArrayList<byte[]> encoq);
	public static native int WeightSet(float dwWeight , float encoderWeight);
	public static native float[] EKF(float a,float b,float c,int left,int right,int degree);
}
