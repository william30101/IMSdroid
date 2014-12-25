package org.doubango.imsdroid.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;


import android.util.Log;

public class RotateAngleCmd extends BaseCmd{

	private final static String TAG = RotateAngleCmd.class.getSimpleName();
	
	private byte funcByte = (byte) 0x10;
	private int dataSize = 3;
	private byte[] dataByte = new byte[dataSize];
	
	private String[] cmdStr= {"P","N"};
	private byte[] cmdByte = {0x01,0x02};
	
	public RotateAngleCmd()
	{
		super.SetByte(cmdStr,cmdByte,3);
		super.SetDataSize(dataSize);
		super.SetFuncByte(funcByte);
	}
	
	public void SetByte(String[] inStr)
	{
		Arrays.fill(dataByte, (byte)0x00);
		dataByte[0] = super.GetByteNum(inStr[1] , 3);
		
		// Trasfer to Hex data
		int compassData = Integer.parseInt(inStr[2],10);
		
		
		//High Byte dataByte[1] 
		dataByte[1] = (byte)( ( (compassData & 0xff00) >> 8));
				
		dataByte[1]  = (byte) (dataByte[1]  & 0xff);
		
		//Low Byte dataByte[2] 
		dataByte[2] = (byte)((compassData & 0xff));
		
		dataByte[2]  = (byte) (dataByte[2]  & 0xff);
		
		super.SetDataByte(dataByte);
	}
	
	public ByteArrayOutputStream GetAllByte() throws IOException
	{
		return super.GetFullByte();
	}
}
