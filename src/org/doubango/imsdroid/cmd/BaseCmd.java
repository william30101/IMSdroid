package org.doubango.imsdroid.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseCmd {
	protected byte funcByte;
	protected int datasize = 0;
	protected byte[] dataByte = new byte[20];
	//protected byte[] rdataByte = new byte[20];
	
	protected static ByteArrayOutputStream retDataByte = new ByteArrayOutputStream();
	
	
	private Map byte2Map = new HashMap();
	private Map byte3Map = new HashMap();
	private Map byte4Map = new HashMap();
	private Map byte5Map = new HashMap();

	public void SetByte(String[] inStr, byte[] bValue, int byteNum) {
		for (int i = 0; i < inStr.length; i++) {
			switch (byteNum) {
			case 2:
				this.byte3Map.put(inStr[i], bValue[i]);
				break;
			case 3:
				this.byte3Map.put(inStr[i], bValue[i]);
				break;
			case 4:
				this.byte4Map.put(inStr[i], bValue[i]);
				break;
			case 5:
				this.byte5Map.put(inStr[i], bValue[i]);
				break;
			default:
				break;
			}
		}
	}
	
	
	
	public byte GetByteNum(String inStr , int byteNum)
	{
		byte retByte = 0x00;
		switch (byteNum) {
		case 2:
			if (byte3Map != null)
				retByte = (Byte) this.byte3Map.get(inStr);
			break;
		case 3:
			if (byte3Map != null)
				retByte = (Byte) this.byte3Map.get(inStr);
			break;
		case 4:
			retByte = (Byte) this.byte4Map.get(inStr);
			break;
		case 5:
			retByte = (Byte) this.byte5Map.get(inStr);
			break;
		default:
			break;
		}
		
		return retByte;

	}
	
	public ByteArrayOutputStream GetFullByte() throws IOException
	{
		
		retDataByte.reset();
		byte start = 0x53;
		byte end = 0x45;
		//byte[] ret = new byte[12];
		//Arrays.fill(ret, (byte) 0x00);
		
		//retDataByte.write(buffer)
		//retDataByte.add(start);
		//retDataByte.add(this.funcByte);
		
		byte[] rdataByte = new byte[2 + datasize + 1];
		
		rdataByte[0] = start;
		rdataByte[1] = this.funcByte;
		for (int i=2; i < datasize + 2 ; i++ )
		{
			rdataByte[i] = (byte) (this.dataByte[i -2] & 0xff);
		}
		
		rdataByte[datasize + 2] = end; 
		
		retDataByte.write(rdataByte);
		
		return retDataByte;
	}
	
	public void SetDataSize(int isize)
	{
		this.datasize = isize;
	}
	
	public int GetDataSize()
	{
		return this.datasize;
	}
	
	public void SetFuncByte(byte fun)
	{
		this.funcByte = fun;
	}
	
	public byte GetFuncByte()
	{
		return this.funcByte;
	}
	
	public void SetDataByte(byte[] data)
	{
		if (datasize != 0)
		{
			this.dataByte = data;
		}
	}

	public byte[] GetDataByte()
	{
		return this.dataByte;
	}
	
	
}
