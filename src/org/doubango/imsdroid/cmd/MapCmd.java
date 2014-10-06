package org.doubango.imsdroid.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android.util.Log;

public class MapCmd extends BaseCmd{

private String TAG = "AxisCmd";
	
	private byte funcByte = (byte) 0x0c;
	private int dataSize = 9;
	private byte[] dataByte = new byte[dataSize];
	
	private byte[] xByte = new byte[4];
	private byte[] yByte = new byte[4];
	private byte[] crcByte = new byte[4];
	
	private long  x , y, iCrc;
	
	public void SetByte(String[] inStr)
	{

		Arrays.fill(dataByte, (byte)0x00);
		super.SetDataSize(dataSize);
		super.SetFuncByte(funcByte);
		
		// dataByte[0] -> [3] is X axis
		// dataByte[4] -> [7] is Y axis
		// dataByte[8] -> CRC
		for (int i=0; i < dataSize ; i++)
		{
			dataByte[i] = super.GetByteNum(inStr[i+1],3);
		}
		
		xByte = Arrays.copyOfRange(dataByte, 0, 3);
		yByte = Arrays.copyOfRange(dataByte, 4, 7);
		crcByte = Arrays.copyOfRange(dataByte, 8, 11);
		
		x = (xByte[0] << 24) | (xByte[1] << 16) | (xByte[2] << 8) | (xByte[0]);
		y = (yByte[0] << 24) | (yByte[1] << 16) | (yByte[2] << 8) | (yByte[0]);
		iCrc = (crcByte[0] << 24) | (crcByte[1] << 16) | (crcByte[2] << 8) | (crcByte[0]);
		
		Log.i(TAG,"Axis X = " + x + " Y = " + y + " crc = " + iCrc);
		
		super.SetDataByte(dataByte);
	}
	
	public ByteArrayOutputStream GetAllByte() throws IOException
	{
		return super.GetFullByte();
	}	
}
