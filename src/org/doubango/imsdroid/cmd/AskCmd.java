package org.doubango.imsdroid.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class AskCmd extends BaseCmd{

	private String TAG = "AskCmd";
	
	private byte funcByte = (byte) 0x06;
	private int dataSize = 3;
	private byte[] dataByte = new byte[dataSize];
	
	private String[] cmdStr= {"direction","stop","angle","stretch","stopBySensor","ask",
			"destination","health","axis","ret","startBySensor","map"};
	private byte[] cmdByte = {0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b};
	
	public void SetByte(String[] inStr)
	{
		super.SetNum(cmdStr,cmdByte);
		
		Arrays.fill(dataByte, (byte)0x00);
		super.SetDataSize(dataSize);
		super.SetFuncByte(funcByte);
		
		dataByte[0] = super.GetNum(inStr[1]);
		
		super.SetDataByte(dataByte);
	}
	
	public ByteArrayOutputStream GetAllByte() throws IOException
	{
		return super.GetFullByte();
	}	
	
}
