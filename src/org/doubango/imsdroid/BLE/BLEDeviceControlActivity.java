/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.doubango.imsdroid.BLE;

import org.doubango.imsdroid.R;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class BLEDeviceControlActivity {
    //private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public boolean write_count = false;
    public static byte write_byte[] = {0x01};
	private TextView mConnectionState;
    private TextView mDataField;
    private String mDeviceName;
    private static String mDeviceAddress;
    private ExpandableListView mGattServicesList;
    private static BluetoothLeService mBluetoothLeService;
    private static ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    public static boolean mConnected = false;

	private BluetoothGattCharacteristic mNotifyCharacteristic;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    
    public static EditText BLEDataText;
    
    public static final String TAG = "william";
    public static BLEDeviceControlActivity instance;
    
    private Activity BLEDeviceConActivity;
    private BLEDeviceScanActivity BLEDeviceControl;
    
    ExpandableListView expListView;
    //ArrayList<HashMap<String, String>> groupList;
    List<String> childList;
    //Map<String, List<String>> laptopCollection;
    
    //private AudioManager mAudioManager;
   // private ComponentName mRemoteControlResponder;
    public String ismConnected() {
    	Log.i("william"," BLE connect = " + mConnected);
		return mConnected ? "BLE connected": "BLE disconnected";
	}
    
    public static byte[] getWrite_byte() {
		return write_byte;
	}
    
    public static BLEDeviceControlActivity getInstance() {
		 if (instance == null){
	            synchronized(BLEDeviceControlActivity.class){
	                if(instance == null) {
	                     instance = new BLEDeviceControlActivity();
	                }
	            }
	        }
	        return instance;
	}
    
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                //finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }
        
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    
    

    public static BluetoothLeService getmBluetoothLeService() {
		return mBluetoothLeService;
	}

	public static void setmBluetoothLeService(BluetoothLeService mBluetoothLeService) {
		BLEDeviceControlActivity.mBluetoothLeService = mBluetoothLeService;
	}

	// Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	
            final String action = intent.getAction();
            
            Log.i(TAG,"onReceive here action = "+action);
            
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                Log.i(TAG,"BLE connected");
                //Message msg1 = ScreenDirection.BLEStatusHandler.obtainMessage(1, "BLE connected");
               // ScreenDirection.BLEStatusHandler.sendMessage(msg1);

                //invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                //invalidateOptionsMenu();
                //clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                                
                //dispatchKeyEvent(new KeyEvent (KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_VOLUME_UP));
            }
        }
    };

    /*
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        Log.i(TAG,"Dispatch key code KeyEvent="+keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                	super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_UP));
    	        	super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DPAD_UP));
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                    //TODO
                	super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN));
    	        	super.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DPAD_DOWN));
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }
    */
    public byte[] SendDataToBleDevice(String data)
    {
        if (!data.equals("") && data.length() == 2)
        {
        	byte pwm1 = (byte)data.toLowerCase().charAt(0);
        	byte pwm0 = (byte)data.toLowerCase().charAt(1);
        	if (pwm1 >=0x30 && pwm1 <= 0x39)
        		pwm1 = (byte) (pwm1 - 0x30);
        	else if (pwm1 >=0x61 && pwm1 <= 0x66)
        		pwm1 = (byte) (pwm1 - 87);
        	
        	if (pwm0 >=0x30 && pwm0 <= 0x39)
        		pwm0 = (byte) (pwm0 - 0x30);
        	else if (pwm0 >=0x61 && pwm0 <= 0x66)
        		pwm0 = (byte) (pwm0 - 87);
	       	 
        	write_byte[0] = (byte) ( (pwm1 << 4)+ pwm0);
	       	 
        	return write_byte;
        }
        else
        {
        	write_byte[0] = 0x00;
        	return write_byte;
        }
    }
    
    public boolean CharacteristicWRN(int groupPosition,int childPosition,int method, String data)
    {
    	
    	if (mGattCharacteristics != null) {
            final BluetoothGattCharacteristic characteristic =
                    mGattCharacteristics.get(groupPosition).get(childPosition);
            final int charaProp = characteristic.getProperties();
            
            switch (method)
            {
            	case 0:
            		Log.i(TAG,"Start to write BLE data");
            		 if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                         // If there is an active notification on a characteristic, clear
                         // it first so it doesn't update the data field on the user interface.
                         if (mNotifyCharacteristic != null) {
                             mBluetoothLeService.setCharacteristicNotification(
                                     mNotifyCharacteristic, false);
                             mNotifyCharacteristic = null;
                         }
                         
                         write_byte = SendDataToBleDevice(data);
                         
                       	 characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                       	 Log.i(TAG,"BLE write byte = "+write_byte[0]);
                       	 characteristic.setValue(write_byte);
                       	 mBluetoothLeService.writeCharacteristic(characteristic);
                     } 
            		
            		break;
            	case 1:
            		if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
                        if (mNotifyCharacteristic != null) {
                            mBluetoothLeService.setCharacteristicNotification(
                                    mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        mBluetoothLeService.readCharacteristic(characteristic);
                    }
            		
            		break;
            	case 2:
            		if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mNotifyCharacteristic = characteristic;
                        Log.i(TAG,"Enter notify");
                        mBluetoothLeService.setCharacteristicNotification(
                                characteristic, true);
                    }
            		break;
            	default:
            		break;
            	
            }
            
    	}
    	
    	return true;
    }

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
        //mDataField.setText(R.string.no_data);
    }

    public void BLEDeviceControlStart(Activity v, String deviceName , String deviceAddress)
    {
    	 instance = new BLEDeviceControlActivity();

    	 BLEDeviceConActivity = v;
    	 Context context = v.getApplicationContext();
         //final Intent intent = v.getIntent();
        //mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
         //mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
    	 mDeviceName = deviceName;
    	 mDeviceAddress = deviceAddress;
         v.registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());


         // Sets up UI references.
       //  ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        // mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services_list);
         //mGattServicesList.setOnChildClickListener(servicesListClickListner);
         //mConnectionState = (TextView) v.findViewById(R.id.BLEconnectStatus);
        // mDataField = (TextView) findViewById(R.id.data_value);

         //getActionBar().setTitle(mDeviceName);
         //getActionBar().setDisplayHomeAsUpEnabled(true);
         Intent gattServiceIntent = new Intent(v, BluetoothLeService.class);
         context.bindService(gattServiceIntent, mServiceConnection, context.BIND_AUTO_CREATE);

    }
    
    public ServiceConnection getmServiceConnection() {
		return mServiceConnection;
	}

    public void connectBLEDevice()
    {
    	mBluetoothLeService.connect(mDeviceAddress);
    }
	
    /*
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {t
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
        
        mAudioManager.registerMediaButtonEventReceiver(
                mRemoteControlResponder);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
*/
	/*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        
        mAudioManager.unregisterMediaButtonEventReceiver(
                mRemoteControlResponder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
*/

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = BLEDeviceConActivity.getResources().getString(R.string.unknown_service);
        String unknownCharaString = BLEDeviceConActivity.getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<HashMap<String, String>> groupList = new ArrayList<HashMap<String, String>>();
        

        ArrayList<ArrayList<HashMap<String, String>>>   laptopCollection = new ArrayList<ArrayList<HashMap<String, String>>>();

        
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);
            
            groupList.add(currentServiceData);
            //groupList.add(SampleGattAttributes.lookup(uuid, unknownServiceString));
            //groupList.add(uuid);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
            
            ArrayList<HashMap<String, String>> childList = 
            		new ArrayList<HashMap<String, String>>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
                
                childList.add(currentCharaData);
                
                
                
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
            
            laptopCollection.add(childList);
        }

        /*
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                //R.layout.child_item,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                //android.R.layout.simple_expandable_list    private void loadChild(String[] laptopModels) {
        childList = new ArrayList<String>();
        for (String model : laptopModels)
            childList.add(model);
    }_item_2,
                R.layout.child_item,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
        */
        
        
        
        //Base expandable
        //expListView = (ExpandableListView) findViewById(R.id.gatt_services_list);
       // final ExpandableListAdapter expListAdapter = new ExpandableListAdapter(
       //        this, DevCon, groupList, laptopCollection);
       // mGattServicesList.setAdapter(expListAdapter);
        
       // mGattServicesList.setOnChildClickListener(servicesListClickListner);

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
    
    public static void test()
    {
    	Log.i(TAG,"Test here");
    }

}
