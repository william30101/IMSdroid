package org.doubango.imsdroid.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class RetCmd extends BaseCmd{

private String TAG = "RetCmd";
	
	private byte funcByte = (byte) 0x0a;
	private int dataSize = 3;
	private byte[] dataByte = new byte[dataSize];
	

	private String[] cmdStr= {"success","fail"};
	private byte[] cmdByte = {0x00,0x01};

	public void SetByte(String[] inStr)
	{
		super.SetByte(cmdStr,cmdByte,3);
		
		Arrays.fill(dataByte, (byte)0x00);
		super.SetDataSize(dataSize);
		super.SetFuncByte(funcByte);
		
		dataByte[0] = super.GetByteNum(inStr[1],3);
		
		super.SetDataByte(dataByte);
	}
	
	public ByteArrayOutputStream GetAllByte() throws IOException
	{
		return super.GetFullByte();
	}
	
}
