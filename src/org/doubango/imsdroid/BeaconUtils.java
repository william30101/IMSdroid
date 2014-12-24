package org.doubango.imsdroid;

import android.util.Log;

public class BeaconUtils {
	private static final String TAG = "BeaconUtils";
	private static Boolean libExist = false;
	
	static
	{
		try
		{
			System.loadLibrary("beacon_control");
			libExist = true;
			Log.i("JNI", "Trying to load libbeacon_control.so");
		}
		catch(UnsatisfiedLinkError ule)
		{
			libExist = false;
			Log.i("JNI", "WARNING: could not to load libbeacon_control.so");
		}
	}
	
	public boolean getLibState() {
		return libExist;
	}
	
	public static native int BeasonReset(String str);

}
