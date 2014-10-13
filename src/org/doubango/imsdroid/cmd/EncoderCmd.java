package org.doubango.imsdroid.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import android.util.Log;

public class EncoderCmd  extends BaseCmd{
private String TAG = "AxisCmd";
	
	private byte funcByte = (byte) 0x0d;
	private int dataSize = 8;
	private byte[] dataByte = new byte[dataSize];
	private byte[] allDataByte = new byte[11];
	
	
	
	public EncoderCmd()
	{
		
		super.SetDataSize(dataSize);
		super.SetFuncByte(funcByte);
	}
	
	public void SetByte(String inStr)
	{

		Arrays.fill(dataByte, (byte)0x00);
		// dataByte[0]  is xPolarity
		// dataByte[1] -> [2] is X axis
		// dataByte[3]  is yPolarity
		// dataByte[4] -> [5] is Y axis
		// dataByte[6] -> [7] CRC 16 , 0x00 = not used
		
		try {
			allDataByte = inStr.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dataByte = Arrays.copyOfRange(allDataByte, 2, 10);
		
		super.SetDataByte(dataByte);
	}
	
	public ByteArrayOutputStream GetAllByte() throws IOException
	{
		return super.GetFullByte();
	}	
}
