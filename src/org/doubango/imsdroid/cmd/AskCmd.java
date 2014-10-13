package org.doubango.imsdroid.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class AskCmd extends BaseCmd{

	private String TAG = "AskCmd";
	
	private byte funcByte = (byte) 0x06;
	private int dataSize = 3;
	private byte[] dataByte = new byte[dataSize];
	
	private String[] cmdStr= {"direction","stop","pitchAngle","stretch","stopBySensor","ask",
			"destination","health","axis","ret","startBySensor","mapFromPIC32","encoder","mapControl","mode"};
	private byte[] cmdByte = {0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0x0d,0x0e,0x0f};
	
	public AskCmd()
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
