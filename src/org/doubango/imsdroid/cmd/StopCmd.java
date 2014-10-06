package org.doubango.imsdroid.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import android.util.Log;

public class StopCmd extends BaseCmd{

private String TAG = "StopCmd";
	
	private byte funcByte = (byte) 0x02;
	private int dataSize = 3;
	private byte[] dataByte = new byte[dataSize];

	private String[] cmdStr= {"stop"};
	private byte[] cmdByte = {0x01};
	
	public StopCmd()
	{
		super.SetByte(cmdStr,cmdByte,3);
		super.SetDataSize(dataSize);
		super.SetFuncByte(funcByte);
	}
	
	public void SetByte(String[] inStr)
	{
		Arrays.fill(dataByte, (byte)0x00);

		
		
		dataByte[0] = super.GetByteNum(inStr[1],3);
		
		super.SetDataByte(dataByte);
	}
	
	public ByteArrayOutputStream GetAllByte() throws IOException
	{
		return super.GetFullByte();
	}
	
}
