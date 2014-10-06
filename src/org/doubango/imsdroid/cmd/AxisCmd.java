package org.doubango.imsdroid.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import android.util.Log;

public class AxisCmd extends BaseCmd{
	
	private String TAG = "AxisCmd";
	
	private byte funcByte = (byte) 0x09;
	private int dataSize = 8;
	private byte[] dataByte = new byte[dataSize];
	
	private byte xPolarity  = 0x00;
	private byte yPolarity  = 0x00;
	private byte[] xByte = new byte[2];
	private byte[] yByte = new byte[2];
	private byte[] crcByte = new byte[2];
	
	private long  x , y , iCrc;
	
	public AxisCmd()
	{
		
		super.SetDataSize(dataSize);
		super.SetFuncByte(funcByte);
	}
	
	public void SetByte(String[] inStr)
	{

		Arrays.fill(dataByte, (byte)0x00);
		// dataByte[0]  is xPolarity
		// dataByte[1] -> [2] is X axis
		// dataByte[3]  is yPolarity
		// dataByte[4] -> [5] is Y axis
		// dataByte[6] -> [7] CRC 16 , 0x00 = not used
		
		try {
			dataByte = inStr[1].getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*for (int i=0; i < dataSize ; i++)
		{
			dataByte[i] = inStr[i+1];
		}*/
		
		xPolarity = dataByte[0];
		xByte = Arrays.copyOfRange(dataByte, 1, 3);
		
		yPolarity = dataByte[3];
		yByte = Arrays.copyOfRange(dataByte, 4, 6);
		crcByte = Arrays.copyOfRange(dataByte, 6, 8);
		
		x = ((xByte[0] << 8) &0xff00) | ((xByte[1]) & 0xff);
		y = ((yByte[0] << 8) &0xff00) | ((yByte[1]) & 0xff);
		iCrc = ((crcByte[0] << 8) &0xff00) | ((crcByte[1]) & 0xff);
		
		Log.i("william","Axis X polarity = " + xPolarity + "X = " + x + 
				" Y polarity = " + yPolarity + " Y = " + y + " crc = " + iCrc);
		
		super.SetDataByte(dataByte);
	}
	
	public ByteArrayOutputStream GetAllByte() throws IOException
	{
		return super.GetFullByte();
	}	

}
