package org.doubango.imsdroid.UnitTest;

import org.doubango.imsdroid.UartCmd;
import org.doubango.imsdroid.BLE.BLEDeviceControlActivity;
import org.doubango.imsdroid.cmd.RotateAngleCmd;

import android.test.AndroidTestCase;
import android.util.Log;

public class TestService extends AndroidTestCase {

	private final static String TAG = TestService.class.getSimpleName();
	
	private UartCmd UCmd = UartCmd.getInstance();
	private BLEDeviceControlActivity BLEDevice = BLEDeviceControlActivity.getInstance();
	
	/*
	 * RotateAngle cmd test;
	 * from xmpp string to byte[] command.
	 * 
	 * */
	public void testRotageAngleCmd() throws Exception 
	{
		String message = "RotateAngle P 250";
		String[] inM = message.split("\\s+");

		byte[] cmdByte = UCmd.GetAllByte(inM);
		byte[] resultByte = {0x53,0x10,0x01,0x00,(byte)0xfa,0x45};
		for (int i=0;i<cmdByte.length;i++)
		{
			assertEquals("Assert error index = " + i,resultByte[i],cmdByte[i]);
		}
	}
	
	
	
	/*
	 * Direction cmd test;
	 * from xmpp string to byte[] command.
	 * 
	 * */
	public void testDirectionCmd() throws Exception 
	{
		String message = "direction forward";
		String[] inM = message.split("\\s+");

		byte[] cmdByte = UCmd.GetAllByte(inM);
		byte[] resultByte = {0x53,0x01,0x01,0x00,0x00,0x45};
		for (int i=0;i<cmdByte.length;i++)
		{
			assertEquals("Assert error index = " + i , resultByte[i],cmdByte[i]);
		}
	}
	
	
	/*
	 * PitchAngle cmd test;
	 * from xmpp string to byte[] command.
	 * 
	 * */
	public void testPitchAngleCmd() throws Exception 
	{
		String message = "pitchAngle top";
		String[] inM = message.split("\\s+");

		byte[] cmdByte = UCmd.GetAllByte(inM);
		byte[] resultByte = {0x53,0x03,0x01,0x00,0x00,0x45};
		for (int i=0;i<cmdByte.length;i++)
		{
			assertEquals("Assert error index = " + i,resultByte[i],cmdByte[i]);
		}
	}
	
	/*
	 * Stretch cmd test;
	 * from xmpp string to byte[] command.
	 * 
	 * */
	public void testStretchCmd() throws Exception 
	{
		String message = "stretch top";
		String[] inM = message.split("\\s+");

		byte[] cmdByte = UCmd.GetAllByte(inM);
		byte[] resultByte = {0x53,0x04,0x01,0x00,0x00,0x45};
		for (int i=0;i<cmdByte.length;i++)
		{
			assertEquals("Assert error index = " + i, resultByte[i],cmdByte[i]);
		}
	}
	
	/*
	 * Ask cmd test;
	 * from xmpp string to byte[] command.
	 * 
	 * */
	public void testAskCmd() throws Exception 
	{
		String message = "ask encoder";
		String[] inM = message.split("\\s+");

		byte[] cmdByte = UCmd.GetAllByte(inM);
		byte[] resultByte = {0x53,0x06,0x0d,0x00,0x00,0x45};
		for (int i=0;i<cmdByte.length;i++)
		{
			assertEquals("Assert error index = " + i,resultByte[i],cmdByte[i]);
		}
	}
	
	/*
	 * Stop cmd test;
	 * from xmpp string to byte[] command.
	 * 
	 * */
	public void testStopCmd() throws Exception 
	{
		String message = "stop stop";
		String[] inM = message.split("\\s+");

		byte[] cmdByte = UCmd.GetAllByte(inM);
		byte[] resultByte = {0x53,0x02,0x01,0x00,0x00,0x45};
		for (int i=0;i<cmdByte.length;i++)
		{
			assertEquals("Assert error index = " + i, resultByte[i],cmdByte[i]);
		}
	}

	/*
	 * Health cmd test;
	 * from xmpp string to byte[] command.
	 * 
	 * */
	public void testHealthCmd() throws Exception 
	{
		String message = "health PIC32Error Ultrasonic4 LeftMotorOverCurrent";
		String[] inM = message.split("\\s+");

		byte[] cmdByte = UCmd.GetAllByte(inM);
		byte[] resultByte = {0x53,0x08,0x00,0x08,0x01,0x00,0x45};
		for (int i=0;i<cmdByte.length;i++)
		{
			assertEquals("Assert error index = " + i,resultByte[i],cmdByte[i]);
		}
	}
	
	/*
	 * BLE cmd test;
	 * from xmpp string to byte[] command.
	 * 
	 * */
	public void testBLECmd() throws Exception 
	{

		//String message = "BLE e0";
		//String[] inM = message.split("\\s+");

		
		//byte[] cmdByte = UCmd.GetAllByte(inM);

		BLEDevice.SendDataToBleDevice("e0");
		assertEquals("BLE data not equal ",(byte)0xe0,BLEDevice.getWrite_byte()[0]);
		
	}
}
