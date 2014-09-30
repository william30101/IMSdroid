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
	

	private String[] cmdStr= {"no","forward","backward","left","right","forRig","backRig","fordLeft","backLeft"};
	private byte[] cmdByte = {0x00,0x01,0x02,0x04,0x08,0x10,0x20,0x40,(byte) 0x80};

	public void SetByte(String[] inStr)
	{
		super.SetNum(cmdStr,cmdByte);
		
		Arrays.fill(dataByte, (byte)0x00);
		super.SetDataSize(dataSize);
		super.SetFuncByte(funcByte);
		
		dataByte[0] = super.GetNum(inStr[1]);
		dataByte[1] = super.GetNum(inStr[2]);
		dataByte[2] = super.GetNum(inStr[3]);
		
		super.SetDataByte(dataByte);
	}
	
	public ByteArrayOutputStream GetAllByte() throws IOException
	{
		return super.GetFullByte();
	}
}
