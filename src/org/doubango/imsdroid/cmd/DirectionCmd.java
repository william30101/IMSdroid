package org.doubango.imsdroid.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android.util.Log;

public class DirectionCmd extends BaseCmd{

	private String TAG = "DirectionCmd";
	
	private byte funcByte = (byte) 0x01;
	private int dataSize = 3;
	private byte[] dataByte = new byte[dataSize];
	

	private String[] cmdStr= {"forward","backward","left","right","forLeft","bacLeft","forRig","bacRig"};
	private byte[] cmdByte = {0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08};

	public DirectionCmd()
	{
		super.SetByte(cmdStr,cmdByte,3);
		super.SetDataSize(dataSize);
		super.SetFuncByte(funcByte);
	}
	
	public void SetByte(String[] inStr)
	{
		Arrays.fill(dataByte, (byte)0x00);

		dataByte[0] = super.GetByteNum(inStr[1],3);
		
		if (inStr.length > 2)
			dataByte[1] = super.GetByteNum(inStr[2],3);
		
		if (inStr.length > 3)
			dataByte[2] = super.GetByteNum(inStr[3],3);
		
		super.SetDataByte(dataByte);
	}
	
	public ByteArrayOutputStream GetAllByte() throws IOException
	{
		return super.GetFullByte();
	}
}
