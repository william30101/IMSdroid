package org.doubango.imsdroid.cmd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android.util.Log;

public class HealthCmd extends BaseCmd{
	
	private String TAG = "HealthCmd";
	
	private byte funcByte = (byte) 0x08;
	private int dataSize = 4;
	private byte[] dataByte = new byte[dataSize];
	
	// We don't know now , need to fix it.
	private String[] byte3CmdStr0 = {"PIC32Error","4011Error","Ready","CommunicationError",
			"PeripheralError","UnknownCommand","TaskOvertime","NA"};
	private byte[] byte3CmdByte0 = {0x00,0x01,0x02,0x04,0x08,0x10,0x20,0x40};
	
	private String[] byte4CmdStr0 = {"Ultrasonic1" , "Ultrasonic2","Ultrasonic3"
			,"Ultrasonic4","Ultrasonic5","IMU" };
	private byte[] byte4CmdByte0 = {0x01,0x02,0x04,0x08,0x10,0x20};
	
	private String[] byte5CmdStr0 = {"LeftMotorOverCurrent","LeftMotorError","RightMotorOverCurrent","RightMotorError",
			"NeckMotor","NeckLaser","SideLaser","CliffLaser"};
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
	
	public void SetByte(String[] inStr)
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
