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
	
	protected ByteArrayOutputStream retDataByte = new ByteArrayOutputStream();
	
	

	private Map nameMap = new HashMap();

	public void SetNum(String[] inStr, byte[] bValue)
	{
		for (int i=0; i< inStr.length ; i++)
		{
			this.nameMap.put(inStr[i], bValue[i]);
		}
	}
	
	public byte GetNum(String inStr)
	{
		return (Byte) this.nameMap.get(inStr);
	}
	
	public ByteArrayOutputStream GetFullByte() throws IOException
	{
		
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
		for (int i=2; i < datasize ; i++ )
		{
			rdataByte[i] = this.dataByte[i -2];
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
