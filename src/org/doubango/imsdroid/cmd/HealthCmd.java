package org.doubango.imsdroid.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android.util.Log;

public class HealthCmd extends BaseCmd{
	
	private String TAG = "HealthCmd";
	
	private byte funcByte = (byte) 0x08;
	private int dataSize = 3;
	private byte[] dataByte = new byte[dataSize];
	
	// We don't know now , need to fix it.
	private String[] byte3CmdStr0 = {"PICError","4011Error","Ready","Left motor over current","Left motor error",
			"Right motor over current","Right motor error","n/a"};
	private byte[] byte3CmdByte0 = {0x00,0x01,0x02,0x04,0x08,0x10,0x20,0x40};
	
	private String[] byte4CmdStr0 = {"Ultrasonic3","Ultrasonic4","Ultrasonic5","Neck laser","Side laser","Cliff laser"};
	private byte[] byte4CmdByte0 = {0x01,0x02,0x04,0x08,0x10,0x20};
	
	private String[] byte5CmdStr0 = {"Communication","Communication with control","Task impossible"," Task overtime",
			"Neck/Head motor","IMU" , "Ultrasonic1" , "Ultrasonic2"};
	private byte[] byte5CmdByte0 = {0x01,0x02,0x04,0x08,0x10,0x20 , 0x40,(byte)0x80};
	
	public HealthCmd()
	{
		super.SetByte(byte3CmdStr0,byte3CmdByte0,3);
		super.SetByte(byte4CmdStr0,byte4CmdByte0,4);
		super.SetByte(byte5CmdStr0,byte5CmdByte0,5);
		super.SetDataSize(dataSize);
		super.SetFuncByte(funcByte);
		
	}
	/*
	public void SetByte(String[] inStr)
	{

		Arrays.fill(dataByte, (byte)0x00);
		
		dataByte[0] = super.GetNum(inStr[1]);
		
		super.SetDataByte(dataByte);
	}*/
	
	public void GetByte(String[] inStr)
	{

		Arrays.fill(dataByte, (byte)0x00);
		
		dataByte[0] = super.GetByteNum(inStr[1],3);
		dataByte[1] = super.GetByteNum(inStr[2],4);
		dataByte[2] = super.GetByteNum(inStr[3],5);
		
		super.SetDataByte(dataByte);
	}
	
	public ByteArrayOutputStream GetAllByte() throws IOException
	{
		return super.GetFullByte();
	}	

}
