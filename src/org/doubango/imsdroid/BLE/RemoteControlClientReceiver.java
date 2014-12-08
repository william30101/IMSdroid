package org.doubango.imsdroid.BLE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class RemoteControlClientReceiver extends BroadcastReceiver
{
	private static final String TAG = "william";
    private static long mHeadsetDownTime = 0;
    private static long mHeadsetUpTime = 0;
     @Override
     public void onReceive(Context context, Intent intent)
     {
          /// Code
    	 Log.i(TAG,"Enter MediaRec class ");
    	 if(Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())){
             KeyEvent Xevent = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
             int keyCode = Xevent.getKeyCode();


             Log.i(TAG,"keyType= "+keyCode);
             Intent i = new Intent();
             i.setAction("com.MainActivity.Shakey.MEDIA_BUTTON");
             i.putExtra("keyType", keyCode);
             //context.sendBroadcast(i);
             //Toast.makeText(context, String.valueOf(MainActivity.mult), Toast.LENGTH_SHORT).show();
             //MainActivity.mult++;
             abortBroadcast();

         }
     }
 }
    
